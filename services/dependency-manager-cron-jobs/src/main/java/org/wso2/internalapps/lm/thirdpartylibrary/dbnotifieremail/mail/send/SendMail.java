package org.wso2.internalapps.lm.thirdpartylibrary.dbnotifieremail.mail.send;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;
import java.sql.ResultSet;

import org.wso2.internalapps.lm.thirdpartylibrary.common.filereader.ConfigurationReader;
import org.wso2.internalapps.lm.thirdpartylibrary.common.filereader.POJO_Config_File;
import org.wso2.internalapps.lm.thirdpartylibrary.dbnotifieremail.checktables.CheckLM_LIBRARY;
import org.wso2.internalapps.lm.thirdpartylibrary.dbnotifieremail.mail.create.ComposeMail;
import org.wso2.internalapps.lm.thirdpartylibrary.dbnotifieremail.mail.create.SendEmail;
import org.wso2.internalapps.lm.thirdpartylibrary.dbnotifieremail.mail.create.MailBody;
import org.wso2.internalapps.lm.thirdpartylibrary.dbnotifieremail.mainclass.SendEmailNotification;

import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;


public class SendMail {
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    /** Application name. */
    private static final String APPLICATION_NAME =
            "Gmail API Java Licence Manager Notifier";

    /** Directory to store user credentials for this application. */
    private static final java.io.File DATA_STORE_DIR = new java.io.File(
            System.getProperty("user.home"), ".credentials/gmail-java-lm-notifier");

    /** Global instance of the {@link FileDataStoreFactory}. */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /** Global instance of the JSON factory. */
    private static final JsonFactory JSON_FACTORY =
            JacksonFactory.getDefaultInstance();

    /** Global instance of the HTTP transport. */
    private static HttpTransport HTTP_TRANSPORT;

    /** Global instance of the scopes required by this quickstart.
     *
     * If modifying these scopes, delete your previously saved credentials
     * at ~/.credentials/gmail-java-quickstart
     */
    private static final List<String> SCOPES =
            Arrays.asList(GmailScopes.GMAIL_LABELS);

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Creates an authorized Credential object.
     * @return an authorized Credential object.
     * @throws IOException
     */
    public static Credential authorize() throws IOException {
        // Load client secrets.
        InputStream in =
                SendEmailNotification.class.getResourceAsStream("/client_secret.json");
        System.out.println(in);
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        // Build flow and trigger user authorization request.

        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                        .setDataStoreFactory(DATA_STORE_FACTORY)
                        .setAccessType("offline")
                        .build();
        Credential credential = new AuthorizationCodeInstalledApp(
                flow, new LocalServerReceiver()).authorize("user");
        System.out.println(
                "Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());

        //have to generate these
        String refreshToken = "1/SqutZIGHMdy4CM1eAEgE9kfImUzoP9pSIv12paQckaA";
        String accessToken = "ya29.GlsGBSfFHOIe8sQJFSUBMkqE_NBx6sHvR4-afUuTSOW4pU1JfDm08PtZaivIklKtHmLojJ4_Rj0RAlKT8Rw4JyZRRwxJQMPkTgXf-j0HN7nqyLSzXb44vzprrlc4";

        credential.setAccessToken(accessToken);
        credential.setRefreshToken(refreshToken);


        return credential;
    }

    /**
     * Build and return an authorized Gmail client service.
     * @return an authorized Gmail client service
     * @throws IOException
     */
    public static Gmail getGmailService() throws IOException {
        Credential credential = authorize();
        return new Gmail.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public static void SendMailLM() {

        String user = "me";
        try{
            // Build a new authorized API client service.
            Gmail service = getGmailService();
            CheckLM_LIBRARY obj = new CheckLM_LIBRARY();

            ResultSet results = obj.readLMLibraryTable();
            String MsgTable = MailBody.createEmailBody(results);

            ConfigurationReader configs = new ConfigurationReader();
            POJO_Config_File configurations = configs.getConfigurations();

            MimeMessage msg = ComposeMail.createEmail(configurations.getRECIEVER_EMAIL(),"me","LM_DB-Library Details",MsgTable);
            Message sendmsg = SendEmail.sendMessage(service,"me",msg);

            LOGGER.info("Email is sent successfully");
        }catch (Exception e){
            LOGGER.info("Error occured: "+ e.getMessage());
        }
    }
}
