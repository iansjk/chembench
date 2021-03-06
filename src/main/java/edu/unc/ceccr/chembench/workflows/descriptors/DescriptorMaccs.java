package edu.unc.ceccr.chembench.workflows.descriptors;

import edu.unc.ceccr.chembench.global.Constants;
import edu.unc.ceccr.chembench.persistence.Descriptors;
import edu.unc.ceccr.chembench.utilities.RunExternalProgram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DescriptorMaccs implements DescriptorSet{
    private static final Logger logger = LoggerFactory.getLogger(DescriptorMaccs.class);

    @Override
    public String getDescriptorSetName() {
        return Constants.MACCS;
    }

    @Override
    public String getFileEnding() {
        return ".maccs";
    }

    @Override
    public String getFileErrorOut() {
        return "maccs.out";
    }

    @Override
    public void generateDescriptors(String sdfile, String outfile) throws Exception {
        //command: "maccs.sh infile.sdf outfile.maccs"
        String execstr = "maccs.sh " + sdfile + " " + outfile + getFileEnding() + " " + Constants.CECCR_BASE_PATH +
                "mmlsoft/SVL_DIR/batch_sd_MACCSFP.svl";
        String workingDir = sdfile.replaceAll("/[^/]+$", "");

        RunExternalProgram.runCommandAndLogOutput(execstr, workingDir + "/Descriptors/", "maccs.sh");
    }

    @Override
    public void readDescriptors(String maccsOutputFile, List<String> descriptorNames,
                   List<Descriptors> descriptorValueMatrix) throws Exception {
        // generate with "maccs.sh infile.sdf outfile.maccs"
        maccsOutputFile +=getFileEnding();
        readDescriptorFile (maccsOutputFile, descriptorNames, descriptorValueMatrix);
    }

    @Override
    public void readDescriptorsChunks(String outputFile, List<String> descriptorNames,
                                      List<Descriptors> descriptorValueMatrix) throws Exception {
        readDescriptorFile (outputFile, descriptorNames, descriptorValueMatrix);
    }

    @Override
    public String splitFile(String workingDir, String descriptorsFile) throws Exception {
        descriptorsFile += getFileEnding();

        File file = new File(workingDir + descriptorsFile);
        if (!file.exists() || file.length() == 0) {
            throw new Exception("Could not read Maccs descriptors.\n");
        }
        FileReader fin = new FileReader(file);
        BufferedReader br = new BufferedReader(fin);

        String header = ""; // stores everything up to where descriptors
        // begin.
        int currentFile = 0;
        int moleculesInCurrentFile = 0;
        BufferedWriter outFilePart =
                new BufferedWriter(new FileWriter(workingDir + descriptorsFile + "_" + currentFile));

        header = br.readLine() + "\n";
        outFilePart.write(header);

        String line;
        while ((line = br.readLine()) != null) {
            outFilePart.write(line + "\n");

            moleculesInCurrentFile++;
            if (moleculesInCurrentFile == compoundsPerChunk) {
                outFilePart.close();
                moleculesInCurrentFile = 0;
                currentFile++;
                outFilePart = new BufferedWriter(new FileWriter(workingDir + descriptorsFile + "_" + currentFile));
                outFilePart.write(header);
            }
        }
        br.close();
        outFilePart.close();

        return descriptorsFile;
    }

    @Override
    public String checkDescriptors(String maccsOutputFile) throws Exception {
        // right now this doesn't check anything. The MACCS keys never seem to
        // cause issues.
        String errors = "";


        File file = new File(maccsOutputFile + getFileEnding());
        if (!file.exists() || file.length() == 0) {
            errors = "Could not read descriptor file.\n";
        }
        return errors;
    }

    private void readDescriptorFile (String outputFile, List<String> descriptorNames, List<Descriptors>
            descriptorValueMatrix) throws Exception{
        logger.debug("reading Maccs Descriptors");
        File file = new File(outputFile);
        if (!file.exists() || file.length() == 0) {
            throw new Exception("Could not read MACCS keys.\n");
        }
        FileReader fin = new FileReader(file);
        BufferedReader br = new BufferedReader(fin);
        /* first line is junk, it says "name,FP:MACCS." */
        String line = br.readLine();

        while ((line = br.readLine()) != null) {
            List<Double> descriptorValues = new ArrayList<>();
            Scanner tok = new Scanner(line);
            tok.useDelimiter(",");
            tok.next(); // skip compound identifier
            String tmp = tok.next();
            tok.close();
            tok = new Scanner(tmp);
            tok.useDelimiter(" ");
            int last = 0;
            int descriptor = 0;
            while (tok.hasNext()) {
                descriptor = Integer.parseInt(tok.next());
                for (int i = last; i < descriptor; i++) {
                    descriptorValues.add(0d);
                }
                descriptorValues.add(1d);
                last = descriptor + 1;
            }
            tok.close();
            for (int i = last; i < Constants.NUM_MACCS_KEYS; i++) {
                descriptorValues.add(0d);
            }
            Descriptors di = new Descriptors();
            di.setDescriptorValues(descriptorValues);
            descriptorValueMatrix.add(di);

        }
        br.close();
        for (int i = 0; i < Constants.NUM_MACCS_KEYS; i++) {
            descriptorNames.add((new Integer(i)).toString());
        }
    }
}
