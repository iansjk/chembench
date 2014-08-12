package edu.unc.ceccr.workflows.modelingPrediction;

import edu.unc.ceccr.global.Constants;
import edu.unc.ceccr.utilities.RunExternalProgram;
import org.apache.log4j.Logger;

public class PredictionUtilities {
    private static Logger logger = Logger.getLogger(PredictionUtilities.class.getName());
    //Execute external programs to generate a prediction for a given molecule set.
    // Used for legacy models that were created using Sasha's kNN code.

    public static void MoveToPredictionsDir(String userName, String jobName) throws Exception {
        //When the prediction job is finished, move all the files over to the predictions dir.
        logger.debug("User: " + userName + " Job: " + jobName + " Moving to PREDICTIONS dir.");
        String moveFrom = Constants.CECCR_USER_BASE_PATH + userName + "/" + jobName + "/";
        String moveTo = Constants.CECCR_USER_BASE_PATH + userName + "/PREDICTIONS/" + jobName + "/";
        String execstr = "mv " + moveFrom + " " + moveTo;
        RunExternalProgram.runCommand(execstr, "");
    }

}
