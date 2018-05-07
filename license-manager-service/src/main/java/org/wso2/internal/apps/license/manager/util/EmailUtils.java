/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.internal.apps.license.manager.util;

import org.wso2.internal.apps.license.manager.models.NewLicenseEntry;
import org.wso2.msf4j.util.SystemVariableUtil;

import java.util.List;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Email sending related utilities.
 */
public class EmailUtils {
    /**
     * send the email to the admin with the newly added licenses and libraries.
     * @param addedBy       person who added the new licenses.
     * @param components    the newly added components.
     * @param libraries     the newly added libraries.
     * @throws MessagingException if the email couldn't be sent throws the exception.
     */
    public static void sendEmail(String addedBy, List<NewLicenseEntry> components, List<NewLicenseEntry> libraries)
            throws MessagingException {

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class","javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        String username = SystemVariableUtil.getValue(Constants.EMAIL_USERNAME, null);
        String password = SystemVariableUtil.getValue(Constants.EMAIL_PASSWORD, null);
        String adminEmailsAsString = SystemVariableUtil.getValue(Constants.LICENSE_MANAGER_ADMINS, null);

        Session session = Session.getDefaultInstance(props,
                new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });

        String body = createHtmlBody(addedBy,components,libraries);
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(username));
        InternetAddress[] adminEmails = InternetAddress.parse(adminEmailsAsString, true);
        message.setRecipients(Message.RecipientType.TO, adminEmails);
        message.setSubject("New licenses added");
        message.setContent(body, "text/html");
        Transport.send(message);

    }

    /**
     * Create the html body of the email.
     * @param addedBy person who added the new licenses.
     * @param components    the newly added components.
     * @param libraries     the newly added libraries.
     * @return html body as a string
     */
    private static String createHtmlBody(String addedBy, List<NewLicenseEntry> components, List<NewLicenseEntry> libraries){
        String finalHtml;
        String htmlComponents="";
        String htmlLibraries="";
        if(components.size()>0){
            htmlComponents +=
                    "<h3>Components</h3>\n" +
                            "<table>\n" +
                            "  <tr>\n" +
                            "    <th>File Name</th>\n" +
                            "    <th>Name</th>\n" +
                            "    <th>Version</th>\n" +
                            "    <th>License</th>\n" +
                            "  </tr>\n";
            for (int i = 0; i < components.size(); i++) {
                String name = components.get(i).getName();
                String fileName = components.get(i).getFileName();
                String version = components.get(i).getVersion();
                String license = components.get(i).getLicenseKey();
                Boolean isEven = i % 2 == 0;

                if (isEven || i==0) {
                    htmlComponents = htmlComponents + "<tr style=\"background-color: #dddddd;\"> \n";
                } else {
                    htmlComponents = htmlComponents + "<tr style=\"background-color: #ffffff;\"> \n";
                }
                htmlComponents = htmlComponents +
                        "<td>" + fileName + "</td>\n" +
                        "<td>" + name + "</td>\n" +
                        "<td>" + version + "</td>\n" +
                        "<td>" + license + "</td>\n" +
                        "</tr>";
            }
            htmlComponents +="</table>\n";
        }
        if(libraries.size()>0){
            htmlLibraries +=
                    "<h3>Libraries</h3>\n" +
                            "<table>\n" +
                            "  <tr>\n" +
                            "    <th>File Name</th>\n" +
                            "    <th>Name</th>\n" +
                            "    <th>Version</th>\n" +
                            "    <th>License</th>\n" +
                            "  </tr>\n";
            for (int i = 0; i < libraries.size(); i++) {
                String name = libraries.get(i).getName();
                String fileName = libraries.get(i).getFileName();
                String version = libraries.get(i).getVersion();
                String license = libraries.get(i).getLicenseKey();
                Boolean isEven = i % 2 == 0;

                if (isEven || i==0) {
                    htmlLibraries = htmlLibraries + "<tr style=\"background-color: #dddddd;\"> \n";
                } else {
                    htmlLibraries = htmlLibraries + "<tr style=\"background-color: #ffffff;\"> \n";
                }
                htmlLibraries = htmlLibraries +
                        "<td>" + fileName + "</td>\n" +
                        "<td>" + name + "</td>\n" +
                        "<td>" + version + "</td>\n" +
                        "<td>" + license + "</td>\n" +
                        "</tr>";
            }
            htmlLibraries +="</table>\n";

        }
        finalHtml = "Hi," +
                "\n \n Following new licenses were added by " + addedBy + "." +
                "<html>\n" +
                "<head>\n" +
                "<style>\n" +
                "table {\n" +
                "    font-family: arial, sans-serif;\n" +
                "    border-collapse: collapse;\n" +
                "    width: 100%;\n" +
                "}\n" +
                "\n" +
                "td, th {\n" +
                "    border: 1px solid #c6c6c6;\n" +
                "    text-align: left;\n" +
                "    padding: 8px;\n" +
                "}\n" +
                "\n" +
                "</style>\n" +
                "</head>\n" +
                "<body>\n" +
                "\n" +
                htmlComponents +
                "\n" +
                htmlLibraries +
                "</table>\n" +
                "</body>\n" +
                "</html>\n";
        return finalHtml;
    }
}
