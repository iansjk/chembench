package edu.unc.ceccr.chembench.servlet;

import edu.unc.ceccr.chembench.global.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;


public class DescriptorMatrixServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(DescriptorMatrixServlet.class);

    //serves up files for use with the dataset visualization Flash app

    protected void doGet(HttpServletRequest request, HttpServletResponse response) {

        String project = request.getParameter("project");
        String name = request.getParameter("name");
        String userName = request.getParameter("user");

        File matFile = new File(Constants.CECCR_USER_BASE_PATH + userName + "/DATASETS/" + project + "/" + name);

        BufferedInputStream input = null;
        BufferedOutputStream output = null;
        if (matFile.exists() && matFile.isFile()) {
            logger.debug("MAT FILE EXISTS? " + matFile.exists());
            try {
                input = new BufferedInputStream(new FileInputStream(matFile));
                int contentLength = input.available();

                response.reset();
                response.setContentLength(contentLength);

                output = new BufferedOutputStream(response.getOutputStream());

                // Write file contents to response.
                while (contentLength-- > 0) {
                    output.write(input.read());
                }

                output.flush();
            } catch (IOException e) {
                logger.error("", e);
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        logger.error("", e);
                    }
                }
                if (output != null) {
                    try {
                        output.close();
                    } catch (IOException e) {
                        logger.error("", e);
                    }
                }
            }
        }
    }
}
