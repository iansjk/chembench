package edu.unc.ceccr.chembench.workflows.modelingPrediction;


import edu.unc.ceccr.chembench.global.Constants;
import edu.unc.ceccr.chembench.utilities.FileAndDirOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Scanner;

public class LsfUtilities {
    private static final Logger logger = LoggerFactory.getLogger(LsfUtilities.class);

    public static void retrieveCompletedPredictor(String filePath, String lsfPath) throws Exception {
        FileAndDirOperations.copyDirContents(lsfPath, filePath, true);
        logger.warn("About to recursively delete " + lsfPath);
        FileAndDirOperations.deleteDir(new File(lsfPath));
    }

    public static void makeLsfModelingDirectory(String filePath, String lsfPath) throws Exception {
        // create a dir out in /largefs/ceccr/ to run the calculation of the
        // job
        File dir = new File(lsfPath);
        dir.mkdirs();
        FileAndDirOperations.deleteDirContents(lsfPath);
        logger.debug("Created fresh directories @ " + lsfPath);

        if (new File(lsfPath + "yRandom/").exists()) {
            FileAndDirOperations.deleteDirContents(lsfPath + "yRandom/");
        }

        // copy all files from current modeling dir out there
        FileAndDirOperations.copyDirContents(filePath, lsfPath, true);
        logger.debug("Copied all files from " + filePath + " to " + lsfPath);
        // copy kNN executables to the temp directory and to the yRandom
        // subdirectory also, make them executable
        FileAndDirOperations.copyDirContents(Constants.CECCR_BASE_PATH + "mmlsoft/bin/", lsfPath, false);
        FileAndDirOperations.makeDirContentsExecutable(lsfPath);
        FileAndDirOperations.copyDirContents(Constants.CECCR_BASE_PATH + "mmlsoft/bin/", lsfPath + "yRandom/", false);
        FileAndDirOperations.makeDirContentsExecutable(lsfPath + "yRandom/");
        logger.debug("Copied mmlsoft/bin to lsfPath");

    }

    public static String getLsfJobId(String logFilePath) throws Exception {
        Thread.sleep(200); // give the file time to close properly? I guess?
        BufferedReader in = new BufferedReader(new FileReader(logFilePath));

        // we're looking at the stdout of the 'bsub' command, and expect
        // something that looks like this:
        //     Job <443904> is submitted to queue <patrons>.
        // remove the jobId from the line and return it.
        String line = in.readLine();

        if (line == null) {
            // stdout is empty, which should not happen and indicates that
            // an error occurred in job submission.
            throw new RuntimeException("LSF job submission failed");
        }

        Scanner sc = new Scanner(line);
        String jobId = "";
        if (sc.hasNext()) {
            sc.next();
        }
        if (sc.hasNext()) {
            jobId = sc.next();
        }
        logger.debug(jobId.substring(1, jobId.length() - 1));
        in.close();
        sc.close();
        return jobId.substring(1, jobId.length() - 1);
    }

}
