package edu.unc.ceccr.chembench.actions;

import com.opensymphony.xwork2.ActionSupport;
import edu.unc.ceccr.chembench.global.Constants;
import edu.unc.ceccr.chembench.jobs.CentralDogma;
import edu.unc.ceccr.chembench.persistence.*;
import edu.unc.ceccr.chembench.utilities.FileAndDirOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowire;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Configurable(autowire = Autowire.BY_NAME)
public class DeleteAction extends ActionSupport {
    private static final Logger logger = LoggerFactory.getLogger(DeleteAction.class);
    private static final long serialVersionUID = 8940848615449675885L;
    private Long id;
    private String userToDelete;

    private DatasetRepository datasetRepository;
    private PredictorRepository predictorRepository;
    private PredictionRepository predictionRepository;
    private JobRepository jobRepository;
    private ExternalValidationRepository externalValidationRepository;
    private PredictionValueRepository predictionValueRepository;
    private UserRepository userRepository;
    private RandomForestTreeRepository randomForestTreeRepository;
    private RandomForestGroveRepository randomForestGroveRepository;
    private RandomForestParametersRepository randomForestParametersRepository;
    private SvmModelRepository svmModelRepository;
    private SvmParametersRepository svmParametersRepository;
    private KnnPlusModelRepository knnPlusModelRepository;
    private KnnPlusParametersRepository knnPlusParametersRepository;

    private void checkDatasetDependencies(Dataset ds) {
        // make sure there are no predictors, predictions, or jobs that depend
        // on this dataset
        logger.debug("checking dataset dependencies");

        String userName = ds.getUserName();
        List<Predictor> userPredictors = predictorRepository.findByUserName(userName);
        userPredictors.addAll(predictorRepository.findPublicPredictors());
        List<Prediction> userPredictions = predictionRepository.findByUserName(userName);

        // check each predictor
        for (Predictor predictor : userPredictors) {
            logger.debug("predictor id: " + predictor.getDatasetId() + " dataset id: " + ds.getId());
            if (predictor.getDatasetId() != null && predictor.getDatasetId().equals(ds.getId())) {
                addActionError("The predictor '" + predictor.getName() + "' depends on this dataset. Please" +
                        " delete it first.\n");
            }
        }

        // check each prediction
        for (Prediction prediction : userPredictions) {
            logger.debug("Prediction id: " + prediction.getDatasetId() + " dataset id: " + ds.getId());
            if (prediction.getDatasetId() != null && prediction.getDatasetId().equals(ds.getId())) {
                addActionError("The prediction '" + prediction.getName() + "' depends on this dataset. Please " +
                        "delete it first.\n");
            }
        }

        // check each job
        // Actually, we don't need to check the jobs.
        // When a modeling or prediction job runs, it creates a Predictor or
        // Prediction entry in the database
        // and that's enough to catch the dependency.

    }

    private void checkPredictorDependencies(Predictor p) throws ClassNotFoundException, SQLException {
        // make sure there are no predictions or prediction jobs that depend
        // on this predictor

        String userName = p.getUserName();
        List<Prediction> userPredictions = predictionRepository.findByUserName(userName);

        // check each prediction
        for (Prediction prediction : userPredictions) {
            String[] predictorIds = prediction.getPredictorIds().split("\\s+");
            for (String predictorId : predictorIds) {
                if (Long.parseLong(predictorId) == p.getId()) {
                    addActionError("The prediction '" + prediction.getName() + "' depends on this predictor." +
                            " Please delete it first.\n");
                }
            }
        }
        // We don't need to check the jobs. When a prediction job runs, it creates a Prediction entry in the database
        // and that's enough to catch the dependency.
    }

    private boolean checkPermissions(String objectUser) {
        // make sure the user has permissions to delete this object
        User user = User.getCurrentUser();
        return user.getUserName().equalsIgnoreCase(objectUser) || user.getIsAdmin().equals(Constants.YES);
    }

    private void deleteDataset(Dataset ds) {
        // delete the files associated with this dataset
        String dir = Constants.CECCR_USER_BASE_PATH + ds.getUserName() + "/DATASETS/" + ds.getName();
        if ((new File(dir)).exists()) {
            if (!FileAndDirOperations.deleteDir(new File(dir))) {
                logger.warn("error deleting dir: " + dir);
            }
        }

        // delete the database entry for the dataset
        datasetRepository.delete(ds);
    }

    public String deleteDataset() throws Exception {
        if (id == null) {
            addActionError("No dataset ID supplied.");
            return ERROR;
        }
        logger.debug("Deleting dataset with id: " + id);

        Dataset ds = datasetRepository.findOne(id);
        if (ds == null) {
            addActionError("Invalid dataset ID supplied.");
            return ERROR;
        }

        if (!checkPermissions(ds.getUserName())) {
            addActionError("Error: You do not have the permissions " + "needed to delete this dataset.");
            return ERROR;
        }

        // make sure nothing else depends on this dataset existing
        checkDatasetDependencies(ds);
        if (!getActionErrors().isEmpty()) {
            return ERROR;
        }

        deleteDataset(ds);
        return SUCCESS;
    }

    public String deletePredictor() throws Exception {
        logger.debug("Deleting predictor with id: " + id);
        if (id == null) {
            logger.debug("No predictor ID supplied.");
            return ERROR;
        }

        Predictor p = predictorRepository.findOne(id);
        if (p == null) {
            addActionError("Invalid predictor ID supplied.");
            return ERROR;
        }

        if (!checkPermissions(p.getUserName())) {
            addActionError("You do not have the permissions " + "needed to delete this predictor.");
            return ERROR;
        }

        // make sure nothing else depends on this predictor existing
        checkPredictorDependencies(p);
        if (!getActionErrors().isEmpty()) {
            return ERROR;
        }

        deletePredictor(p);
        return SUCCESS;
    }

    private void deletePredictor(Predictor predictor) {
        List<ExternalValidation> extVals = new ArrayList<>();
        // delete the files associated with this predictor
        String dir = Constants.CECCR_USER_BASE_PATH + predictor.getUserName() + "/PREDICTORS/" + predictor.getName() + "/";
        if (!FileAndDirOperations.deleteDir(new File(dir))) {
            logger.warn("error deleting dir: " + dir);
        }

        // delete the database entry for the predictor
        // delete any child predictors too. (Their files will already be gone since deleteDir recurses into subdirs.)
        List<Predictor> allPredictors = new ArrayList<>();
        allPredictors.add(predictor);
        if (predictor.getChildIds() != null && !predictor.getChildIds().trim().equals("")) {
            String[] childIdArray = predictor.getChildIds().split("\\s+");
            for (String childId : childIdArray) {
                if (childId.equals("null")) {
                    logger.warn("Attempted to delete a nonexistant child " +
                            "predictor belonging to predictor id " + predictor.getId());
                } else {
                    Predictor childPredictor = predictorRepository.findOne(Long.parseLong(childId));
                    if (childPredictor == null) {
                        logger.warn(String.format("Child predictor with id %s not found", childId));
                    } else {
                        allPredictors.add(childPredictor);
                        extVals.addAll(externalValidationRepository.findByPredictorId(childPredictor.getId()));
                    }
                }
            }
        }
        extVals.addAll(externalValidationRepository.findByPredictorId(predictor.getId()));
        for (ExternalValidation ev : extVals) {
            externalValidationRepository.delete(ev);
        }

        for (Predictor p : allPredictors) {
            switch (p.getModelMethod()) {
                case Constants.RANDOMFOREST:
                case Constants.RANDOMFOREST_R:
                    RandomForestParameters rfParams = randomForestParametersRepository.findOne(p.getModelingParametersId());
                    if (rfParams != null) {
                        randomForestParametersRepository.delete(rfParams);
                    }
                    List<RandomForestGrove> groves = randomForestGroveRepository.findByPredictorId(p.getId());
                    for (RandomForestGrove grove : groves) {
                        for (RandomForestTree tree : randomForestTreeRepository.findByRandomForestGroveId(grove.getId())) {
                            randomForestTreeRepository.delete(tree);
                        }
                        randomForestGroveRepository.delete(grove);
                    }
                    break;
                case Constants.SVM:
                    SvmParameters svmParams = svmParametersRepository.findOne(p.getModelingParametersId());
                    if (svmParams != null) {
                        svmParametersRepository.delete(svmParams);
                    }
                    for (SvmModel svmModel : svmModelRepository.findByPredictorId(p.getId())) {
                        svmModelRepository.delete(svmModel);
                    }
                    break;
                case Constants.KNNSA:
                case Constants.KNNGA:
                    KnnPlusParameters knnPlusParams = knnPlusParametersRepository.findOne(p.getModelingParametersId());
                    if (knnPlusParams != null) {
                        knnPlusParametersRepository.delete(knnPlusParams);
                    }
                    for (KnnPlusModel knnPlusModel : knnPlusModelRepository.findByPredictorId(p.getId())) {
                        knnPlusModelRepository.delete(knnPlusModel);
                    }
                    break;
            }
            predictorRepository.delete(p);
        }
    }

    public String deletePrediction() throws Exception {
        logger.debug("Deleting prediction with id: " + id);

        if (id == null) {
            addActionError("No prediction ID supplied.");
            return ERROR;
        }

        Prediction p = predictionRepository.findOne(id);
        if (p == null) {
            addActionError("Invalid prediction ID.");
            return ERROR;
        }

        if (!checkPermissions(p.getUserName())) {
            addActionError("You do not have the permissions " + "needed to delete this prediction.");
            return ERROR;
        }

        deletePrediction(p);
        return SUCCESS;
    }

    private void deletePrediction(Prediction p) {
        // delete the files associated with this prediction
        String dir = Constants.CECCR_USER_BASE_PATH + p.getUserName() + "/PREDICTIONS/" + p.getName();
        if (!FileAndDirOperations.deleteDir(new File(dir))) {
            logger.warn("error deleting dir: " + dir);
        }

        // delete the prediction values associated with the prediction
        List<PredictionValue> pvs = predictionValueRepository.findByPredictionId(p.getId());
        for (PredictionValue pv : pvs) {
            predictionValueRepository.delete(pv);
        }

        // delete the database entry for the prediction
        predictionRepository.delete(p);
    }

    private void deleteJob(Job j) throws Exception {
        if (j.getJobType().equals(Constants.MODELING)) {
            if (j.getLookupId() != null) {
                logger.debug("getting predictor with id: " + j.getLookupId());
                Predictor p = predictorRepository.findOne(j.getLookupId());

                String parentPredictorName = "";
                if (p.getName().matches(".*_fold_(\\d+)_of_(\\d+)")) {
                    // this is a child predictor in an nfold run
                    int pos = p.getName().lastIndexOf("_fold");
                    parentPredictorName = p.getName().substring(0, pos);
                }
                Predictor parentPredictor =
                        predictorRepository.findByNameAndUserName(parentPredictorName, p.getUserName());
                if (!parentPredictorName.isEmpty() && parentPredictor != null) {
                    logger.debug("Parent predictor is not null, deleting sibling jobs.");
                    String[] childPredictorIds = parentPredictor.getChildIds().split("\\s+");

                    // get siblings
                    ArrayList<Predictor> siblingPredictors = new ArrayList<>();
                    for (String childPredictorId : childPredictorIds) {
                        if (!childPredictorId.equals("" + p.getId())) {
                            Predictor sibling = predictorRepository.findOne(Long.parseLong(childPredictorId));
                            siblingPredictors.add(sibling);
                        }
                    }

                    // find sibling jobs and cancel those
                    for (Predictor sp : siblingPredictors) {
                        Job sibJob = jobRepository.findByJobNameAndUserName(sp.getName(), sp.getUserName());
                        try {
                            CentralDogma.getInstance().cancelJob(sibJob.getId());
                        } catch (Exception ex) {
                            // if some siblings are missing, don't
                            // crash, just keep deleting things
                            logger.error("", ex);
                        }
                    }
                    // cancel this job
                    CentralDogma.getInstance().cancelJob(id);

                    // delete the parent predictor
                    deletePredictor(parentPredictor);
                } else {
                    CentralDogma.getInstance().cancelJob(id);
                }
            }
        } else {
            CentralDogma.getInstance().cancelJob(id);
        }
    }

    public String deleteJob() throws Exception {
        if (id == null) {
            addActionError("No id supplied.");
            return ERROR;
        }
        Job j = jobRepository.findOne(id);
        if (j == null) {
            addActionError("Invalid job ID.");
            return ERROR;
        }
        logger.debug("Deleting job with id: " + j.getId());
        deleteJob(j);
        return SUCCESS;
    }

    public void deleteUser(String userName) throws Exception {
        List<Dataset> datasets = datasetRepository.findByUserName(userName);
        List<Predictor> predictors = predictorRepository.findByUserName(userName);
        List<Prediction> predictions = predictionRepository.findByUserName(userName);
        List<Job> jobs = jobRepository.findByUserName(userName);
        for (Prediction p : predictions) {
            deletePrediction(p);
        }
        for (Predictor p : predictors) {
            deletePredictor(p);
        }
        for (Dataset d : datasets) {
            deleteDataset(d);
        }
        for (Job j : jobs) {
            deleteJob(j);
        }

        User u = userRepository.findByUserName(userName);
        userRepository.delete(u);

        // last, delete all the files that user has recurses
        File dir = new File(Constants.CECCR_USER_BASE_PATH + userName);
        FileAndDirOperations.deleteDir(dir);
    }

    public String deleteUser() throws Exception {
        if (userToDelete.isEmpty() || userToDelete.contains("..") || userToDelete.contains("~") || userToDelete
                .contains("/")) {
            // just being a little safer, since there's a recursive delete in this function
            return ERROR;
        }
        deleteUser(userToDelete);
        return SUCCESS;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUserToDelete(String userToDelete) {
        this.userToDelete = userToDelete;
    }

    @Autowired
    public void setDatasetRepository(DatasetRepository datasetRepository) {
        this.datasetRepository = datasetRepository;
    }

    @Autowired
    public void setPredictorRepository(PredictorRepository predictorRepository) {
        this.predictorRepository = predictorRepository;
    }

    @Autowired
    public void setPredictionRepository(PredictionRepository predictionRepository) {
        this.predictionRepository = predictionRepository;
    }

    @Autowired
    public void setJobRepository(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    @Autowired
    public void setExternalValidationRepository(ExternalValidationRepository externalValidationRepository) {
        this.externalValidationRepository = externalValidationRepository;
    }

    @Autowired
    public void setPredictionValueRepository(PredictionValueRepository predictionValueRepository) {
        this.predictionValueRepository = predictionValueRepository;
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setRandomForestTreeRepository(RandomForestTreeRepository randomForestTreeRepository) {
        this.randomForestTreeRepository = randomForestTreeRepository;
    }

    @Autowired
    public void setRandomForestGroveRepository(RandomForestGroveRepository randomForestGroveRepository) {
        this.randomForestGroveRepository = randomForestGroveRepository;
    }

    @Autowired
    public void setRandomForestParametersRepository(RandomForestParametersRepository randomForestParametersRepository) {
        this.randomForestParametersRepository = randomForestParametersRepository;
    }

    @Autowired
    public void setSvmModelRepository(SvmModelRepository svmModelRepository) {
        this.svmModelRepository = svmModelRepository;
    }

    @Autowired
    public void setSvmParametersRepository(SvmParametersRepository svmParametersRepository) {
        this.svmParametersRepository = svmParametersRepository;
    }

    @Autowired
    public void setKnnPlusModelRepository(KnnPlusModelRepository knnPlusModelRepository) {
        this.knnPlusModelRepository = knnPlusModelRepository;
    }

    @Autowired
    public void setKnnPlusParametersRepository(KnnPlusParametersRepository knnPlusParametersRepository) {
        this.knnPlusParametersRepository = knnPlusParametersRepository;
    }
}
