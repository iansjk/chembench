package edu.unc.ceccr.chembench.taskObjects;

import edu.unc.ceccr.chembench.global.Constants;

public abstract class WorkflowTask {

    public String jobList = Constants.INCOMING;
    public Long lookupId;
    public String jobType;
    public String step;

    public abstract Long setUp() throws Exception;
    //creates any needed dirs
    //creates Job object and adds it to Incoming queue
    //returns id of created object (datasetId, predictorId, or predictionId).

    public abstract void preProcess() throws Exception;
    //Does any work that must be done locally to prepare for main calculation
    //Things that go in here: Data splitting, normalizing of descriptors

    public abstract void executeLocal() throws Exception;
    //does the main calculation work using the Chembench server's processing power
    //Things that go in here: Calls to kNN Modeling, kNN prediction.

    public abstract String executeLSF() throws Exception;
    //an alternative to executeLocal; should perform the same functions as executeLocal.
    //does the main calculation work by submitting it to LSF
    //Things that go in here: Calls to kNN Modeling, kNN prediction.
    //Copying files to / from LSF is done in the preProcess / postProcess step.
    //returns a string containing the LSF-assigned job ID so the job can be tracked

    public abstract void postProcess() throws Exception;
    //Does any work that must be done locally, after the main calculation work is done
    //Things that go in here: Reading in all models from a modeling run and calculating stats for display,
    //averaging results from the different predictors in a prediction run
    //Also, saves all results to the database

    public abstract void delete() throws Exception;
    //Removes files, directories, and database entries associated with a workflowTask

    public abstract String getStatus() throws Exception;
    //returns a basic (one-word) status

    public abstract String getProgress(String userName);
    //returns a detailed status message (% progress, etc)
    //returns more info if userName is an admin or userName owns the task

    public abstract void setStep(String step) throws Exception;
    //allows changing of the "step" variable. Used during job recovery
    //to allow jobs to resume at an arbitrary place in their execution.
    //At present, changing the step only affects the message displayed on the
    //Jobs Queue page.

}
