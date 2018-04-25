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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.wso2.internal.apps.license.manager.impl.exception.LicenseManagerRuntimeException;
import org.wso2.internal.apps.license.manager.impl.main.JarHolder;
import org.wso2.internal.apps.license.manager.impl.models.NewLicenseEntry;
import org.wso2.msf4j.util.SystemVariableUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Contains the functions required for the License Generation process.
 */
public class LicenseManagerUtils {

    public static void unzip(String infile, String outFolder) throws IOException {

        Enumeration entries;
        ZipFile zipFile;

        try {
            zipFile = new ZipFile(infile);
            entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                File f = new File(outFolder + File.separator + entry.getName());
                if (!entry.isDirectory()) {
                    f.getParentFile().mkdirs();
                    copyInputStream(zipFile.getInputStream(entry), new BufferedOutputStream(new FileOutputStream(f
                            .getAbsolutePath())));
                }
            }
            zipFile.close();
        } catch (IOException e) {
            throw e;
        }
    }

    private static void copyInputStream(InputStream in, OutputStream out) throws IOException {

        byte[] buffer = new byte[1024];
        int len;

        while ((len = in.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
        }

        in.close();
        out.close();
    }

    public static void deleteFolder(String filePath) {

        File file = new File(filePath);
        if (file.isDirectory()) {
            try {
                FileUtils.deleteDirectory(file);
                System.out.println("License Generated pack is deleted");
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Problem occurs when deleting the pack : " + filePath);

            }
        } else if (file.isFile()) {
            file.delete();

        }

    }

    public static JarHolder checkJars(String file) throws LicenseManagerRuntimeException {

        if (StringUtils.isEmpty(file) || !new File(file).exists() || !new File(file).isDirectory()) {
            throw new LicenseManagerRuntimeException("Folder is not found in the location");
        }
        JarHolder jh = new JarHolder();
        try {
            jh.generateMap(file);
        } catch (IOException e) {
            throw new LicenseManagerRuntimeException("Folder is not found in the location", e);
        }
        return jh;
    }

    public static boolean checkInnerJars(String filePath) throws IOException {
        boolean containsJars = false;
        ZipInputStream zip = new ZipInputStream(new FileInputStream(filePath));
        for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
            if (entry.getName().endsWith(".jar") || entry.getName().endsWith(".mar")) {
                containsJars = true;
            }
        }
        return containsJars;
    }

    public static void sendEmail(String addedBy, List<NewLicenseEntry> components,List<NewLicenseEntry> libraries)
            throws MessagingException {

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");

        String username = SystemVariableUtil.getValue(Constants.EMAIL_USERNAME, null);
        String password = SystemVariableUtil.getValue(Constants.EMAIL_PASSWORD, null);
        String adminEmailsAsString = SystemVariableUtil.getValue(Constants.LICENSE_MANAGER_ADMINS, null);

        Session session = Session.getDefaultInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {

                        return new PasswordAuthentication(username, password);
                    }
                });

        String body = createHtmlBody(addedBy,components,libraries);
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress("pwanjulie@gmail.com"));
        InternetAddress[] adminEmails = InternetAddress.parse(adminEmailsAsString, true);
        message.setRecipients(Message.RecipientType.TO, adminEmails);
        message.setSubject("New licenses added");
        message.setContent(body, "text/html");
        Transport.send(message);

    }

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

