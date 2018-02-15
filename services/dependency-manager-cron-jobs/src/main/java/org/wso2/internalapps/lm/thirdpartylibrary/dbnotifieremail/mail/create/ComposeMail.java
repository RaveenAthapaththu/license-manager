package org.wso2.internalapps.lm.thirdpartylibrary.dbnotifieremail.mail.create;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class ComposeMail {
    public static MimeMessage createEmail(String to,
                                          String from,
                                          String subject,
                                          String MsgTable)
            throws MessagingException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);

        MimeMessage email = new MimeMessage(session);

        email.setFrom(new InternetAddress(from));
        email.addRecipient(javax.mail.Message.RecipientType.TO,
                new InternetAddress(to));
        email.setSubject(subject);
        email.setContent(MsgTable,"text/html");

        return email;
    }
}
