package edu.unc.ceccr.action;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

//struts2
import com.opensymphony.xwork2.ActionSupport; 
import com.opensymphony.xwork2.ActionContext; 

import org.apache.struts.upload.FormFile;
import org.apache.struts2.interceptor.SessionAware;
import org.hibernate.Session;

import edu.unc.ceccr.formbean.QsarFormBean;
import edu.unc.ceccr.global.Constants;
import edu.unc.ceccr.global.ErrorMessages;
import edu.unc.ceccr.persistence.DataSet;
import edu.unc.ceccr.persistence.HibernateUtil;
import edu.unc.ceccr.persistence.Predictor;
import edu.unc.ceccr.persistence.Queue;
import edu.unc.ceccr.persistence.User;
import edu.unc.ceccr.taskObjects.CreateDatasetTask;
import edu.unc.ceccr.taskObjects.QsarModelingTask;
import edu.unc.ceccr.utilities.DatasetFileOperations;
import edu.unc.ceccr.utilities.FileAndDirOperations;
import edu.unc.ceccr.utilities.PopulateDataObjects;
import edu.unc.ceccr.utilities.Utility;

public class DatasetFormActions extends ActionSupport{
	public String ajaxLoadModeling() throws Exception {
		return SUCCESS;
	}
	public String ajaxLoadPrediction() throws Exception {
		return SUCCESS;
	}
	public String ajaxLoadModelingWithDescriptors() throws Exception {
		return SUCCESS;
	}
	public String ajaxLoadPredictionWithDescriptors() throws Exception {
		return SUCCESS;
	}
	public String ajaxLoadAutoSplit() throws Exception {
		return SUCCESS;
	}
	public String ajaxLoadManualSplit() throws Exception {
		return SUCCESS;
	}

	public String loadPage() throws Exception {

		String result = SUCCESS;
		
		//check that the user is logged in
		ActionContext context = ActionContext.getContext();
		
		if(context == null){
			Utility.writeToStrutsDebug("No ActionContext available");
		}
		else{
			user = (User) context.getSession().get("user");
			
			if(user == null){
				Utility.writeToStrutsDebug("No user is logged in.");
				result = LOGIN;
			}
		}
		
		//set up any values that need to be populated onto the page (dropdowns, lists, display stuff)

		Session session = HibernateUtil.getSession();
		
		userDatasetNames = PopulateDataObjects.populateDatasetNames(user.getUserName(), true, session);
		userPredictorNames = PopulateDataObjects.populatePredictorNames(user.getUserName(), true, session);
		userPredictionNames = PopulateDataObjects.populatePredictionNames(user.getUserName(), true, session);
		userTaskNames = PopulateDataObjects.populateTaskNames(user.getUserName(), false, session);
		
		userPredictorList = PopulateDataObjects.populatePredictors(user.getUserName(), true, true, session);
		userContinuousDatasets = PopulateDataObjects.populateDataset(user.getUserName(), Constants.CONTINUOUS,true, session);
		userCategoryDatasets = PopulateDataObjects.populateDataset(user.getUserName(), Constants.CATEGORY,true, session);

		session.close();
		//log the results
		if(result.equals(SUCCESS)){
			Utility.writeToStrutsDebug("Forwarding user " + user.getUserName() + " to dataset page.");
		}
		else{
			Utility.writeToStrutsDebug("Cannot load page.");
		}
		
		dataTypeModeling = Constants.CONTINUOUS;
		//go to the page
		return result;
	}
	public String execute() throws Exception {
		
		String result = SUCCESS;

		ActionContext context = ActionContext.getContext();
		user = (User) context.getSession().get("user");
		String userName = user.getUserName();
		
		try{
			Utility.writeToDebug("Starting dataset task");
			Utility.writeToDebug("datasetName: " + datasetName);
		}
		catch(Exception ex){
			Utility.writeToDebug(ex);
		}
		
		String msg = "";
		
		if(datasetType.equalsIgnoreCase(Constants.MODELING)){
			Utility.writeToDebug("type: " + datasetType);
			//do file check
			if(sdfFileModeling == null || actFileModeling == null){
				errorString += "File upload failed or no files supplied.";
				result = ERROR;
			}
			
			if(result.equalsIgnoreCase(SUCCESS)){
				//verify uploaded files and copy them to the dataset dir
				try{
					msg = DatasetFileOperations.uploadDataset(userName, sdfFileModeling, sdfFileModelingFileName, 
							actFileModeling, actFileModelingFileName, null, "", datasetName, 
							dataTypeModeling, datasetType);
				}
				catch(Exception ex){
					Utility.writeToDebug(ex);
					result = ERROR;
					msg += "An exception occurred while uploading this dataset: " + ex.getMessage();
				}
				if(!msg.equals("")){
					errorString += msg;
					result = ERROR;
				}
			}
			
			if(result.equalsIgnoreCase(SUCCESS)){
				CreateDatasetTask datasetTask = new CreateDatasetTask(userName, 
						datasetType, //MODELING, PREDICTION, MODELINGWITHDESCRIPTORS, or PREDICTIONWITHDESCRIPTORS
						sdfFileModelingFileName, //sdfFileName
						actFileModelingFileName, //actFileName
						"", //xFileName
						"", //descriptor type, if datasetType is MODELINGWITHDESCRIPTORS or PREDICTIONWITHDESCRIPTORS
						dataTypeModeling, //act file type, Continuous or Category, if datasetType is MODELING or MODELINGWITHDESCRIPTORS
						standardizeModeling, //used in MODELING and PREDICTION
						splitType, //RANDOM or USERDEFINED
						numExternalCompounds, //if splitType is RANDOM
						useActivityBinning, //if splitType is RANDOM
						externalCompoundList, //if splitType is USERDEFINED
						datasetName,
						paperReference,
						dataSetDescription);
				try{
					Utility.writeToDebug("getting ACT compound count from " + Constants.CECCR_USER_BASE_PATH + userName + "/DATASETS/" + datasetName + "/" + actFileModelingFileName);
					int numCompounds = DatasetFileOperations.getACTCompoundList(
							Constants.CECCR_USER_BASE_PATH + userName + "/DATASETS/" + datasetName + "/" + actFileModelingFileName).size();
					int numModels = 0;
					Queue.getInstance().addJob(datasetTask, userName, datasetName, numCompounds, numModels);
				}
				catch(Exception ex){
					Utility.writeToDebug(ex);
				}
			}
		}
		else if(datasetType.equalsIgnoreCase(Constants.PREDICTION)){
			Utility.writeToDebug("type: " + datasetType);
			//do file check
			if(sdfFilePrediction == null){
				errorString += "File upload failed or no files supplied.";
				result = ERROR;
			}
			
			if(result.equalsIgnoreCase(SUCCESS)){
				//verify uploaded files and copy them to the dataset dir
				try{
					msg = DatasetFileOperations.uploadDataset(userName, sdfFilePrediction, sdfFilePredictionFileName, null, 
							"", null, "", datasetName, dataTypeModeling, datasetType);
				}
				catch(Exception ex){
					Utility.writeToDebug(ex);
					result = ERROR;
					msg += "An exception occurred while uploading this dataset: " + ex.getMessage();
				}
			
				if(!msg.equals("")){
					errorString += msg;
					result = ERROR;
				}
			}
			if(result.equalsIgnoreCase(SUCCESS)){
				try{
					CreateDatasetTask datasetTask = new CreateDatasetTask(userName, 
							datasetType, //MODELING, PREDICTION, MODELINGWITHDESCRIPTORS, or PREDICTIONWITHDESCRIPTORS
							sdfFilePredictionFileName, //sdfFileName
							"", //actFileName
							"", //xFileName
							"", //descriptor type, if datasetType is MODELINGWITHDESCRIPTORS or PREDICTIONWITHDESCRIPTORS
							"", //act file type, Continuous or Category, if datasetType is MODELING or MODELINGWITHDESCRIPTORS
							standardizePrediction, //used in MODELING and PREDICTION
							splitType, //RANDOM or USERDEFINED
							numExternalCompounds, //if splitType is RANDOM
							useActivityBinning, //if splitType is RANDOM
							externalCompoundList, //if splitType is USERDEFINED
							datasetName,
							paperReference,
							dataSetDescription);
					
					int numCompounds = DatasetFileOperations.getSDFCompoundList(
							Constants.CECCR_USER_BASE_PATH + userName + "/DATASETS/" + datasetName + "/" + sdfFilePredictionFileName).size();
					int numModels = 0;
					Queue.getInstance().addJob(datasetTask, userName, datasetName, numCompounds, numModels);
				}
				catch(Exception ex){
					Utility.writeToDebug(ex);
					result = ERROR;
					msg += "An exception occurred while creating this dataset: " + ex.getMessage();
				}
			}
		}
		else if(datasetType.equalsIgnoreCase(Constants.MODELINGWITHDESCRIPTORS)){
			Utility.writeToDebug("type: " + datasetType);
			
			if(xFileModDesc == null || actFileModDesc == null){
				errorString += "File upload failed or no files supplied.";
				result = ERROR;
			}
			
			if(result.equalsIgnoreCase(SUCCESS)){
				//verify uploaded files and copy them to the dataset dir
				try{
					msg = DatasetFileOperations.uploadDataset(userName, sdfFileModDesc, sdfFileModDescFileName, actFileModDesc, 
							actFileModDescFileName, xFileModDesc, xFileModDescFileName, datasetName, 
							dataTypeModeling, datasetType);
				}
				catch(Exception ex){
					Utility.writeToDebug(ex);
					result = ERROR;
					msg += "An exception occurred while uploading this dataset: " + ex.getMessage();
				}
				
				if(!msg.equals("")){
					errorString += msg;
					result = ERROR;
				}
			}
			if(result.equalsIgnoreCase(SUCCESS)){
				try{
					CreateDatasetTask datasetTask = new CreateDatasetTask(userName, 
						datasetType, //MODELING, PREDICTION, MODELINGWITHDESCRIPTORS, or PREDICTIONWITHDESCRIPTORS
						sdfFileModDescFileName, //sdfFileName
						actFileModDescFileName, //actFileName
						xFileModDescFileName, //xFileName
						descriptorTypeModDesc, //descriptor type, if datasetType is MODELINGWITHDESCRIPTORS or PREDICTIONWITHDESCRIPTORS
						dataTypeModDesc, //act file type, Continuous or Category, if datasetType is MODELING or MODELINGWITHDESCRIPTORS
						"", //used in MODELING and PREDICTION
						splitType, //RANDOM or USERDEFINED
						numExternalCompounds, //if splitType is RANDOM
						useActivityBinning, //if splitType is RANDOM
						externalCompoundList, //if splitType is USERDEFINED
						datasetName,
						paperReference,
						dataSetDescription);

					int numCompounds = DatasetFileOperations.getACTCompoundList(
							Constants.CECCR_USER_BASE_PATH + userName + "/DATASETS/" + datasetName + "/" + actFileModDescFileName).size();
					int numModels = 0;
					Queue.getInstance().addJob(datasetTask, userName, datasetName, numCompounds, numModels);
				}
				catch(Exception ex){
					Utility.writeToDebug(ex);
					result = ERROR;
					msg += "An exception occurred while creating this dataset: " + ex.getMessage();
				}
			}
		}
		else if(datasetType.equalsIgnoreCase(Constants.PREDICTIONWITHDESCRIPTORS)){
			Utility.writeToDebug("type: " + datasetType);
			if(xFilePredDesc == null){
				errorString += "File upload failed or no files supplied.";
				result = ERROR;
			}
			
			if(result.equalsIgnoreCase(SUCCESS)){
				//verify uploaded files and copy them to the dataset dir
				try{
					msg = DatasetFileOperations.uploadDataset(userName, sdfFilePredDesc, sdfFilePredDescFileName, null, "", 
							xFilePredDesc, xFilePredDescFileName, datasetName, dataTypeModeling, datasetType);
				}
				catch(Exception ex){
					Utility.writeToDebug(ex);
					result = ERROR;
					msg += "An exception occurred while uploading this dataset: " + ex.getMessage();
				}
				
				if(!msg.equals("")){
					errorString += msg;
					result = ERROR;
				}
			}
			
			if(result.equalsIgnoreCase(SUCCESS)){
				try{
					CreateDatasetTask datasetTask = new CreateDatasetTask(userName, 
						datasetType, //MODELING, PREDICTION, MODELINGWITHDESCRIPTORS, or PREDICTIONWITHDESCRIPTORS
						sdfFilePredDescFileName, //sdfFileName
						"", //actFileName
						xFilePredDescFileName, //xFileName
						descriptorTypePredDesc, //descriptor type, if datasetType is MODELINGWITHDESCRIPTORS or PREDICTIONWITHDESCRIPTORS
						"", //act file type, Continuous or Category, if datasetType is MODELING or MODELINGWITHDESCRIPTORS
						"", //used in MODELING and PREDICTION
						splitType, //RANDOM or USERDEFINED
						numExternalCompounds, //if splitType is RANDOM
						useActivityBinning, //if splitType is RANDOM
						externalCompoundList, //if splitType is USERDEFINED
						datasetName,
						paperReference,
						dataSetDescription);
				
					int numCompounds = DatasetFileOperations.getXCompoundList(
							Constants.CECCR_USER_BASE_PATH + userName + "/DATASETS/" + datasetName + "/" + xFilePredDescFileName).size();
					int numModels = 0;
					Queue.getInstance().addJob(datasetTask, userName, datasetName, numCompounds, numModels);
				}
				catch(Exception ex){
					Utility.writeToDebug(ex);
					result = ERROR;
					msg += "An exception occurred while creating this dataset: " + ex.getMessage();
				}
			}
		}
		
		return result;
	}
	
	private String errorString = "";
	private String datasetName = "";
	private String datasetType = Constants.MODELING;
	private String splitType = Constants.RANDOM;
	private String dataTypeModeling = Constants.CONTINUOUS;
	private String dataTypeModDesc = Constants.CONTINUOUS;
	private String dataSetDescription = "";
	private String externalCompoundList = "";
	private String useActivityBinning = "true";
	private String numExternalCompounds = "5";
	private String standardizeModeling = "true";
	private String standardizePrediction = "true";
	private String paperReference = "";
	private String descriptorTypeModDesc = "";
	private String descriptorTypePredDesc = "";

	public String getErrorString() {
		return errorString;
	}
	public void setErrorString(String errorString) {
		this.errorString = errorString;
	}
	public String getDatasetName() {
		return datasetName;
	}
	public void setDatasetName(String datasetName) {
		this.datasetName = datasetName;
	}
	public String getDatasetType() {
		return datasetType;
	}
	public void setDatasetType(String datasetType) {
		this.datasetType = datasetType;
	}
	public String getDataTypeModeling() {
		return dataTypeModeling;
	}
	public void setDataTypeModeling(String dataTypeModeling) {
		this.dataTypeModeling = dataTypeModeling;
	}
	public String getDataTypeModDesc() {
		return dataTypeModDesc;
	}
	public void setDataTypeModDesc(String dataTypeModDesc) {
		this.dataTypeModDesc = dataTypeModDesc;
	}
	public String getDataSetDescription() {
		return dataSetDescription;
	}
	public void setDataSetDescription(String dataSetDescription) {
		this.dataSetDescription = dataSetDescription;
	}
	public String getExternalCompoundList() {
		return externalCompoundList;
	}
	public void setExternalCompoundList(String externalCompoundList) {
		this.externalCompoundList = externalCompoundList;
	}
	public String getUseActivityBinning() {
		return useActivityBinning;
	}
	public void setUseActivityBinning(String useActivityBinning) {
		this.useActivityBinning = useActivityBinning;
	}
	public String getNumExternalCompounds() {
		return numExternalCompounds;
	}
	public void setNumExternalCompounds(String numExternalCompounds) {
		this.numExternalCompounds = numExternalCompounds;
	}
	public String getStandardizeModeling() {
		return standardizeModeling;
	}
	public void setStandardizeModeling(String standardizeModeling) {
		this.standardizeModeling = standardizeModeling;
	}
	public String getStandardizePrediction() {
		return standardizePrediction;
	}
	public void setStandardizePrediction(String standardizePrediction) {
		this.standardizePrediction = standardizePrediction;
	}
	public String getPaperReference() {
		return paperReference;
	}
	public void setPaperReference(String paperReference) {
		this.paperReference = paperReference;
	}

	public String getDescriptorTypeModDesc() {
		return descriptorTypeModDesc;
	}
	public void setDescriptorTypeModDesc(String descriptorTypeModDesc) {
		this.descriptorTypeModDesc = descriptorTypeModDesc;
	}
	
	public String getDescriptorTypePredDesc() {
		return descriptorTypePredDesc;
	}
	public void setDescriptorTypePredDesc(String descriptorTypePredDesc) {
		this.descriptorTypePredDesc = descriptorTypePredDesc;
	}
	
	//file upload stuff
	//modeling
	private File sdfFileModeling = null;
	private String sdfFileModelingContentType = "";
	private String sdfFileModelingFileName = ""; 

	private File actFileModeling = null;
	private String actFileModelingContentType = "";
	private String actFileModelingFileName = "";
	
	//prediction
	private File sdfFilePrediction = null;
	private String sdfFilePredictionContentType = "";
	private String sdfFilePredictionFileName = ""; 

	//modeling with descriptors
	private File actFileModDesc = null;
	private String actFileModDescContentType = "";
	private String actFileModDescFileName = "";
	
	private File xFileModDesc = null;
	private String xFileModDescContentType= "";
	private String xFileModDescFileName = "";

	private File sdfFileModDesc = null;
	private String sdfFileModDescContentType = "";
	private String sdfFileModDescFileName = ""; 

	//prediction with descriptors
	private File xFilePredDesc = null;
	private String xFilePredDescContentType= "";
	private String xFilePredDescFileName = "";

	private File sdfFilePredDesc = null;
	private String sdfFilePredDescContentType = "";
	private String sdfFilePredDescFileName = ""; 
	
	
	public File getSdfFileModeling() {
		return sdfFileModeling;
	}
	public void setSdfFileModeling(File sdfFileModeling) {
		this.sdfFileModeling = sdfFileModeling;
	}
	public String getSdfFileModelingContentType() {
		return sdfFileModelingContentType;
	}
	public void setSdfFileModelingContentType(String sdfFileModelingContentType) {
		this.sdfFileModelingContentType = sdfFileModelingContentType;
	}
	public String getSdfFileModelingFileName() {
		return sdfFileModelingFileName;
	}
	public void setSdfFileModelingFileName(String sdfFileModelingFileName) {
		this.sdfFileModelingFileName = sdfFileModelingFileName;
	}
	public File getSdfFileModDesc() {
		return sdfFileModDesc;
	}
	public void setSdfFileModDesc(File sdfFileModDesc) {
		this.sdfFileModDesc = sdfFileModDesc;
	}
	public String getSdfFileModDescContentType() {
		return sdfFileModDescContentType;
	}
	public void setSdfFileModDescContentType(String sdfFileModDescContentType) {
		this.sdfFileModDescContentType = sdfFileModDescContentType;
	}
	public String getSdfFileModDescFileName() {
		return sdfFileModDescFileName;
	}
	public void setSdfFileModDescFileName(String sdfFileModDescFileName) {
		this.sdfFileModDescFileName = sdfFileModDescFileName;
	}
	public File getSdfFilePrediction() {
		return sdfFilePrediction;
	}
	public void setSdfFilePrediction(File sdfFilePrediction) {
		this.sdfFilePrediction = sdfFilePrediction;
	}
	public String getSdfFilePredictionContentType() {
		return sdfFilePredictionContentType;
	}
	public void setSdfFilePredictionContentType(String sdfFilePredictionContentType) {
		this.sdfFilePredictionContentType = sdfFilePredictionContentType;
	}
	public String getSdfFilePredictionFileName() {
		return sdfFilePredictionFileName;
	}
	public void setSdfFilePredictionFileName(String sdfFilePredictionFileName) {
		this.sdfFilePredictionFileName = sdfFilePredictionFileName;
	}
	public File getActFileModeling() {
		return actFileModeling;
	}
	public void setActFileModeling(File actFileModeling) {
		this.actFileModeling = actFileModeling;
	}
	public String getActFileModelingContentType() {
		return actFileModelingContentType;
	}
	public void setActFileModelingContentType(String actFileModelingContentType) {
		this.actFileModelingContentType = actFileModelingContentType;
	}
	public String getActFileModelingFileName() {
		return actFileModelingFileName;
	}
	public void setActFileModelingFileName(String actFileModelingFileName) {
		this.actFileModelingFileName = actFileModelingFileName;
	}
	public File getActFileModDesc() {
		return actFileModDesc;
	}
	public void setActFileModDesc(File actFileModDesc) {
		this.actFileModDesc = actFileModDesc;
	}
	public String getActFileModDescContentType() {
		return actFileModDescContentType;
	}
	public void setActFileModDescContentType(String actFileModDescContentType) {
		this.actFileModDescContentType = actFileModDescContentType;
	}
	public String getActFileModDescFileName() {
		return actFileModDescFileName;
	}
	public void setActFileModDescFileName(String actFileModDescFileName) {
		this.actFileModDescFileName = actFileModDescFileName;
	}
	public File getXFileModDesc() {
		return xFileModDesc;
	}
	public void setXFileModDesc(File xFileModDesc) {
		this.xFileModDesc = xFileModDesc;
	}
	public String getXFileModDescContentType() {
		return xFileModDescContentType;
	}
	public void setXFileModDescContentType(String fileContentType) {
		xFileModDescContentType = fileContentType;
	}
	public String getXFileModDescFileName() {
		return xFileModDescFileName;
	}
	public void setXFileModDescFileName(String fileName) {
		xFileModDescFileName = fileName;
	}public File getXFilePredDesc() {
		return xFilePredDesc;
	}
	public void setXFilePredDesc(File filePredDesc) {
		xFilePredDesc = filePredDesc;
	}
	public String getXFilePredDescContentType() {
		return xFilePredDescContentType;
	}
	public void setXFilePredDescContentType(String filePredDescContentType) {
		xFilePredDescContentType = filePredDescContentType;
	}
	public String getXFilePredDescFileName() {
		return xFilePredDescFileName;
	}
	public void setXFilePredDescFileName(String filePredDescFileName) {
		xFilePredDescFileName = filePredDescFileName;
	}
	public File getSdfFilePredDesc() {
		return sdfFilePredDesc;
	}
	public void setSdfFilePredDesc(File sdfFilePredDesc) {
		this.sdfFilePredDesc = sdfFilePredDesc;
	}
	public String getSdfFilePredDescContentType() {
		return sdfFilePredDescContentType;
	}
	public void setSdfFilePredDescContentType(String sdfFilePredDescContentType) {
		this.sdfFilePredDescContentType = sdfFilePredDescContentType;
	}
	public String getSdfFilePredDescFileName() {
		return sdfFilePredDescFileName;
	}
	public void setSdfFilePredDescFileName(String sdfFilePredDescFileName) {
		this.sdfFilePredDescFileName = sdfFilePredDescFileName;
	}

	//end file upload stuff
	
	
	//====== variables used for display on the JSP =====//
	private User user;
	
	private List<String> userDatasetNames;
	private List<String> userPredictorNames;
	private List<String> userPredictionNames;
	private List<String> userTaskNames;
	
	private List<Predictor> userPredictorList;
	private List<DataSet> userContinuousDatasets;
	private List<DataSet> userCategoryDatasets;

	public User getUser(){
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	
	public List<String> getUserDatasetNames(){
		return userDatasetNames;
	}
	public void setUserDatasetNames(List<String> userDatasetNames) {
		this.userDatasetNames = userDatasetNames;
	}
	
	public List<String> getUserPredictorNames(){
		return userPredictorNames;
	}
	public void setUserPredictorNames(List<String> userPredictorNames) {
		this.userPredictorNames = userPredictorNames;
	}
	
	public List<String> getUserPredictionNames(){
		return userPredictionNames;
	}
	public void setUserPredictionNames(List<String> userPredictionNames) {
		this.userPredictionNames = userPredictionNames;
	}
	
	public List<String> getUserTaskNames(){
		return userTaskNames;
	}
	public void setUserTaskNames(List<String> userTaskNames) {
		this.userTaskNames = userTaskNames;
	}
	
	
	public List<Predictor> getUserPredictorList(){
		return userPredictorList;
	}
	public void setUserPredictorList(List<Predictor> userPredictorList) {
		this.userPredictorList = userPredictorList;
	}
	
	public List<DataSet> getUserContinuousDatasets(){
		return userContinuousDatasets;
	}
	public void setUserContinuousDatasets(List<DataSet> userContinuousDatasets) {
		this.userContinuousDatasets = userContinuousDatasets;
	}
	
	public List<DataSet> getUserCategoryDatasets(){
		return userCategoryDatasets;
	}
	public void setUserCategoryDatasets(List<DataSet> userCategoryDatasets) {
		this.userCategoryDatasets = userCategoryDatasets;
	}
	
}