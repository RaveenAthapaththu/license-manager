package org.wso2.internalapps.lm.thirdpartylibrary.dbnotifieremail.mail.create;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;

import static org.wso2.internalapps.lm.thirdpartylibrary.dbnotifieremail.mail.create.EncodeMimeMessage.createMessageWithEmail;


public class SendEmail {
    public static Message sendMessage(Gmail service,
                                      String userId,
                                      MimeMessage emailContent
    )
            throws MessagingException, IOException {
        Message message = createMessageWithEmail(emailContent);

        message = service.users().messages().send(userId, message).execute();
        System.out.println("Message id: " + message.getId());
        System.out.println(message.toPrettyString());
        return message;
    }
}

