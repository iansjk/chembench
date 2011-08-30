package edu.unc.ceccr.workflows.utilities;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.Session;

import edu.unc.ceccr.persistence.DataSet;
import edu.unc.ceccr.persistence.HibernateUtil;
import edu.unc.ceccr.persistence.Predictor;
import edu.unc.ceccr.utilities.PopulateDataObjects;
import edu.unc.ceccr.utilities.Utility;
import edu.unc.ceccr.utilities.FileAndDirOperations;
import edu.unc.ceccr.workflows.datasets.DatasetFileOperations;
import edu.unc.ceccr.global.Constants;

public class CopyJobFiles{
	
	public static void getDatasetFiles(String userName, DataSet dataset, String jobType, String toDir) throws Exception{
		//gathers the dataset files needed for a modeling or prediction run
		
		String allUserDir = Constants.CECCR_USER_BASE_PATH + "all-users" + "/DATASETS/" + dataset.getName() + "/";
		String userFilesDir = Constants.CECCR_USER_BASE_PATH + userName + "/DATASETS/" + dataset.getName() + "/";
		
		String fromDir = "";
		if(dataset.getUserName().equals(userName)){
			//get it from the user's DATASET directory
			fromDir = userFilesDir;
		}
		else{
			fromDir = allUserDir;
		}
		
		String sdFile = dataset.getSdfFile();
		String actFile = dataset.getActFile();
		String xFile = dataset.getXdataFile();
		String externalSplitXFile = "";

		//make sure toDir exists
		new File(toDir).mkdirs();
		
		if(dataset.getDatasetType().equals(Constants.MODELING) || dataset.getDatasetType().equals(Constants.MODELINGWITHDESCRIPTORS)){
			if(dataset.getSplitType().equals(Constants.NFOLD) && jobType.equals(Constants.MODELING)){
				//use the right external set for this fold
				Pattern p = Pattern.compile("fold_(\\d+)_of_(\\d+)");
				Matcher matcher = p.matcher(toDir);
				int foldNum = 0;
				if(matcher.find()){
					foldNum = Integer.parseInt(matcher.group(1));
				}
				else{
					throw new Exception("Could not find fold number in path: " + toDir);
				}

				String datasetDir = Constants.CECCR_USER_BASE_PATH + dataset.getUserName() + "/DATASETS/" + dataset.getName() + "/";
				String foldPath = datasetDir + dataset.getActFile() + ".fold" + (foldNum);
				String extPath = toDir + "ext_0.a";
				FileAndDirOperations.copyFile(foldPath, extPath);
				DatasetFileOperations.makeXFromACT(toDir, "ext_0.a");
			}
			else{
				//for nfold, external split file is produced from fold info. All other cases will copy the dataset's ext_0.x.
				externalSplitXFile = Constants.EXTERNAL_SET_X_FILE;
			}
		}
		
		Utility.writeToDebug("Fetching dataset files from " + userFilesDir, userName, "");
		if(!sdFile.equals("")){
			FileAndDirOperations.copyFile(fromDir + sdFile, toDir + sdFile);
		}
		if(!actFile.equals("")){
			FileAndDirOperations.copyFile(fromDir + actFile, toDir + actFile);
		}
		if(!xFile.equals("")){
			FileAndDirOperations.copyFile(fromDir + xFile, toDir + xFile);
		}
		if(!externalSplitXFile.equals("") && new File(fromDir + externalSplitXFile).exists()){
			FileAndDirOperations.copyFile(fromDir + externalSplitXFile, toDir + externalSplitXFile);
		}
		String descriptorDir = "Descriptors/";
		if(new File(fromDir + descriptorDir).exists()){
			FileAndDirOperations.copyDirContents(fromDir + descriptorDir, toDir, false);
		}
	}
	
	public static void getPredictorFiles(String userName, Predictor predictor, String toDir) throws Exception{
		//gathers the predictor files needed for a prediction run
		String fromDir;
		
		if(predictor.getUserName().equals(userName)){
			fromDir = Constants.CECCR_USER_BASE_PATH + userName + "/PREDICTORS/";
		}
		else{
			fromDir = Constants.CECCR_USER_BASE_PATH + "all-users/PREDICTORS/";
		}
		
		if(predictor.getParentId()!= null){
			Session s = HibernateUtil.getSession();
			Predictor parentPredictor = PopulateDataObjects.getPredictorById(predictor.getParentId(), s);
			fromDir += parentPredictor.getName() + "/" + predictor.getName() + "/";
		}
		else{
			fromDir += predictor.getName() + "/";
		}
		

		Utility.writeToDebug("Copying predictor from " + fromDir + " to " + toDir, userName, "");
		
		File knnOutputFile = new File(fromDir + "knn-output.list");
		if(knnOutputFile.exists()){
			//copy only the models listed in knn-output
			ArrayList<String> fileList = new ArrayList<String>();
			
			BufferedReader br = new BufferedReader(new FileReader(knnOutputFile));
			String line;
			while((line = br.readLine()) != null){
				String[] tokens = line.split("\\s+");
				for(int i = 0; i < tokens.length; i++){
					fileList.add(tokens[i]);
				}
			}
			
			//copy the X file (needed for scaling)
			fileList.add("train_0.x");
			//copy the models list (needed for kNN prediction to run)
			fileList.add("knn-output.list");
			
			for(String s: fileList){
				FileAndDirOperations.copyFile(fromDir + s, toDir);
			}
		}
		else{
			//copy all the files
			//(This shouldn't happen with any kNN models, but it's there just in case)
			FileAndDirOperations.copyDirContents(fromDir, toDir, false);
		}
	}
}