package edu.unc.ceccr.chembench.workflows.descriptors;


import edu.unc.ceccr.chembench.global.Constants;
import edu.unc.ceccr.chembench.persistence.Descriptors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DescriptorUploaded implements DescriptorSet{
    private static final Logger logger = LoggerFactory.getLogger(DescriptorUploaded.class);

    @Override
    public String getDescriptorSetName() {
        return Constants.UPLOADED;
    }

    @Override
    public String getFileEnding() {
        return "";
    }

    @Override
    public String getFileErrorOut() {
        //to be implemented
        return null;
    }

    @Override
    public void generateDescriptors(String sdfile, String outfile) throws Exception {
        //does nothing, users upload descriptors so no need to generate
    }

    @Override
    public String checkDescriptors(String outputFile) throws Exception {
        //to be implemented
        return null;
    }

    @Override
    public void readDescriptors(String xFile, List<String> descriptorNames,
                                List<Descriptors> descriptorValueMatrix) throws Exception {
        logger.debug("Trying to read uploaded descriptors");
        File file = new File(xFile);
        if (!file.exists() || file.length() == 0) {
            logger.error(xFile + ": xFile not found");
            throw new Exception("Could not read X file descriptors: " + xFile + "\n");
        }

        try {
            FileReader fin = new FileReader(file);
            BufferedReader br = new BufferedReader(fin);
            String line = br.readLine(); // header. ignored.
            line = br.readLine(); // contains descriptor names
            Scanner tok = new Scanner(line);
            tok.useDelimiter("\\s+");
            while (tok.hasNext()) {
                descriptorNames.add(tok.next());
            }
            tok.close();

            while ((line = br.readLine()) != null) {
                tok = new Scanner(line);
                tok.useDelimiter("\\s+");
                Descriptors di = new Descriptors();
                if (tok.hasNext()) {
                    di.setCompoundIndex(Integer.parseInt(tok.next())); // first value is the index of the compound
                }
                if (tok.hasNext()) {
                    di.setCompoundName(tok.next()); // second value is the name of the compound
                }
                List<Double> descriptorValues = new ArrayList<>();
                while (tok.hasNextDouble()) {
                    descriptorValues.add(tok.nextDouble());
                }
                if (!descriptorValues.isEmpty()) {
                    di.setDescriptorValues(descriptorValues);
                    descriptorValueMatrix.add(di);
                }
                tok.close();
            }
            br.close();
        } catch (FileNotFoundException e) {
            logger.error(file + ": File not found");
        }
    }

    @Override
    public void readDescriptorsChunks(String outputFile, List<String> descriptorNames,
                                      List<Descriptors> descriptorValueMatrix) throws Exception {
        //no file ending changes so no changes to readDescriptors necessary
        readDescriptors(outputFile, descriptorNames, descriptorValueMatrix);
    }

    @Override
    public String splitFile(String workingDir, String descriptorsFile) throws Exception {
        File file = new File(workingDir + descriptorsFile);
        if (!file.exists() || file.length() == 0) {
            throw new Exception("Could not read UPLOADED descriptors.\n");
        }
        FileReader fin = new FileReader(file);
        BufferedReader br = new BufferedReader(fin);

        int currentFile = 0;
        int moleculesInCurrentFile = 0;
        BufferedWriter outFilePart =
                new BufferedWriter(new FileWriter(workingDir + descriptorsFile + "_" + currentFile));

        // don't bother changing the numbers to reflect #compounds in file
        // part, it doesn't matter
        String header = br.readLine() + "\n";
        String descriptorNames = br.readLine() + "\n";
        outFilePart.write(header);
        outFilePart.write(descriptorNames);

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
                outFilePart.write(descriptorNames);
            }
        }
        br.close();
        outFilePart.write("\n");
        outFilePart.close();

        return descriptorsFile;
    }
}
