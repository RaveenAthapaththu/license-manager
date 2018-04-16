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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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

//    public static String sendEmail(String fromAddress, ArrayList<String> toList, ArrayList<String> ccList,
//                                   String subject, String body, String logMessage) throws IOException {
//
//        prop.load( SyncService.class.getClassLoader().getResourceAsStream("application.properties"));
//
//
//        prop.put("mail.smtp.port", "587");
//        prop.put("mail.smtp.auth", "true");
//        prop.put("mail.smtp.starttls.enable", "true");
//        prop.put("mail.smtp.host", "smtp.gmail.com");
//
//        javax.mail.Session session = javax.mail.Session.getDefaultInstance(prop, new Authenticator() {
//            protected PasswordAuthentication getPasswordAuthentication() {
//                return new PasswordAuthentication(prop.getProperty("user"), prop.getProperty("emailPassword"));
//            }
//        });
//
//        try {
//            MimeMessage message = new MimeMessage(session);
//            message.setFrom(new InternetAddress(fromAddress));
//            for (String aToList : toList) {
//                message.addRecipient(Message.RecipientType.TO,
//                        new InternetAddress(aToList));
//            }
//            for (String aCcList : ccList) {
//                message.addRecipient(Message.RecipientType.CC,
//                        new InternetAddress(aCcList));
//            }
//            message.setSubject(subject);
//            message.setContent(body, "text/html");
//            Transport transport = session.getTransport(prop.getProperty("protocol"));
//            transport.connect(prop.getProperty("host"), prop.getProperty("user"), prop.getProperty("emailPassword"));
//            Transport.send(message);
//            LOG.info("Email sent successfully");
//
//        } catch (MessagingException mex) {
//            LOG.error("Email sending failed", mex);
//        }
//        return null;
//    }
}

