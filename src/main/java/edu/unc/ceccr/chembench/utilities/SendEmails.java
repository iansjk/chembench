package edu.unc.ceccr.chembench.utilities;

import edu.unc.ceccr.chembench.global.Constants;
import edu.unc.ceccr.chembench.persistence.Job;
import edu.unc.ceccr.chembench.persistence.User;
import edu.unc.ceccr.chembench.persistence.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.util.Date;
import java.util.Iterator;

@Component
public class SendEmails {

    private static final Logger logger = LoggerFactory.getLogger(SendEmails.class);
    private static UserRepository userRepository;

    public static boolean isValidEmail(String email) {
        // FIXME don't roll your own email address validator
        // TODO switch to Apache commons
        return (email.length() > 0 && email.indexOf("@") > 0) && (email.indexOf(".") > 2);
    }

    public static void sendJobCompletedEmail(Job j) throws Exception {
        User user = userRepository.findByUserName(j.getUserName());
        String subject = "Chembench Job Completed: " + j.getJobName();
        String message =
                user.getFirstName() + "," + "<br /> Your " + j.getJobType().toLowerCase() + " job, '" + j.getJobName()
                        + "', is finished." + "<br /> Please log in to check the results!";

        sendEmail(user.getEmail(), "", "", subject, message);
    }

    public static void sendEmail(String address, String cc, String bcc, String subject, String message) {
        try {
            logger.debug("Sending an email...");

            // gonna exec sendmail, gonna pack it up nice

            String workingDir = Constants.CECCR_USER_BASE_PATH + "EMAILS/";
            File wdFile = new File(workingDir);
            wdFile.mkdirs();

            // create email file
            Date t = new Date();
            String fileName = t.toString().replace(" ", "_");
            fileName += ".txt";

            FileWriter fw = new FileWriter(new File(workingDir + fileName));
            fw.write("Subject: " + subject + "\n");
            fw.write("To: " + address + "\n");
            fw.write("From: " + Constants.WEBSITEEMAIL + "\n");
            fw.write("MIME-Version: 1.0\n");
            fw.write("Content-Type: " + "text/html" + "\n\n");

            fw.write(message);

            fw.close();

            String execstr = "sendmail.sh " + workingDir + fileName;
            RunExternalProgram.runCommand(execstr, workingDir);

            logger.debug("Email sent!");
        } catch (Exception ex) {
            logger.error("", ex);
        }
        /*
         * //The old way
         * Properties props=System.getProperties();
         * props.put(Constants.MAILHOST,Constants.MAILSERVER);
         * javax.mail.Session
         * session=javax.mail.Session.getInstance(props,null);
         * Message message=new MimeMessage(session);
         * message.setFrom(new InternetAddress(Constants.WEBSITEEMAIL));
         * message.addRecipient(Message.RecipientType.TO,new
         * InternetAddress(userInfo.getEmail()));
         * message.setSubject("Sorry,"+userInfo.getFirstName());
         * String HtmlBody="message goes here";
         * message.setContent(HtmlBody, "text/html");
         * Transport.send(message);
         */

    }

    public static void sendEmailToAdmins(String subject, String message) {
        Iterator<?> it = Constants.ADMINEMAIL_LIST.iterator();
        while (it.hasNext()) {
            String adminAddress = (String) it.next();
            sendEmail(adminAddress, "", "", subject, message);
        }
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        SendEmails.userRepository = userRepository;
    }
}
