package org.wso2.internalapps.lm.thirdpartylibrary.dbnotifieremail.mainclass;

import org.wso2.internalapps.lm.thirdpartylibrary.dbnotifieremail.mail.send.SendMail;

import java.io.IOException;

public class SendEmailNotification {
    public static void main(String[] args) throws IOException {
        SendMail.SendMailLM();
    }
}
