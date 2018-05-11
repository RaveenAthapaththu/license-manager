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

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.internal.apps.license.manager.exception.LicenseManagerRuntimeException;
import org.wso2.internal.apps.license.manager.impl.JarHolder;
import org.wso2.internal.apps.license.manager.models.Jar;
import org.wso2.internal.apps.license.manager.models.TaskProgress;
import org.wso2.msf4j.util.SystemVariableUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Contains the functions required for the License Generation process.
 */
public class LicenseManagerUtils {

    private static final Logger log = LoggerFactory.getLogger(LicenseManagerUtils.class);

    /**
     * Static function to unzip a file to a given location.
     *
     * @param infile    the location of the zipped file.
     * @param outFolder location where the file should be unzipped.
     * @throws LicenseManagerRuntimeException if file extraction fails.
     */
    public static void unzip(String infile, String outFolder) throws LicenseManagerRuntimeException {

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
            throw new LicenseManagerRuntimeException("Failed to unzip the file. ", e);
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

    /**
     * Delete folders.
     *
     * @param filePath path to the folder.
     */
    public static void deleteFolder(String filePath) {

        File file = new File(filePath);
        if (file.isDirectory()) {
            try {
                FileUtils.deleteDirectory(file);
            } catch (IOException e) {
                log.error("Error while deleting the folder. " + e.getMessage(), e);
            }
        } else if (file.isFile()) {
            file.delete();
        }
    }

    /**
     * Recursively check the jars of a pack and creates a new JarHolder object.
     *
     * @param file path to the pack
     * @return JarHolder contains the details of jars.
     * @throws LicenseManagerRuntimeException if the jar extraction fails.
     */
    private static JarHolder checkJars(String file) throws LicenseManagerRuntimeException {

        if (StringUtils.isEmpty(file) || !new File(file).exists() || !new File(file).isDirectory()) {
            throw new LicenseManagerRuntimeException("Folder is not found in the location");
        }
        JarHolder jh = new JarHolder();
        jh.extractJarsRecursively(file);
        return jh;
    }

    /**
     * Start the jar extraction process of a pack in a new thread.
     *
     * @param username username of the task executor.
     * @param packName pack to be extracted.
     * @return TaskProgress
     */
    public static TaskProgress startPackExtractionProcess(String username, String packName) {

        final TaskProgress taskProgress = ProgressTracker.createNewTaskProgress(username);
        taskProgress.setMessage("Pack extraction process started");

        // Starting a new thread to extract the pack
        new Thread(new Runnable() {

            @Override
            public void run() {

                String ftpHost = SystemVariableUtil.getValue(Constants.FTP_HOST, null);
                String ftpFilePath = SystemVariableUtil.getValue(Constants.FTP_FILE_LOCATION, null);

                Session session = null;
                ChannelSftp sftpChannel = null;

                try {
                    if (log.isDebugEnabled()) {
                        log.debug("Start downloading the" + packName + " from the FTP server " + ftpHost);
                    }
                    taskProgress.setMessage("Downloading the pack");

                    // Initiate SFTP connection.
                    session = createSftpSession();
                    session.connect();
                    sftpChannel = (ChannelSftp) session.openChannel("sftp");
                    sftpChannel.connect();

                    // Download file from FTP server.
                    String pathToStorage = SystemVariableUtil.getValue(Constants.FILE_DOWNLOAD_PATH, null);
                    sftpChannel.get(ftpFilePath + packName, pathToStorage);
                    if (log.isDebugEnabled()) {
                        log.debug("The file " + packName + " is successfully downloaded to location " + pathToStorage);
                    }

                    // Unzip the downloaded file.
                    String zipFilePath = pathToStorage + packName;
                    String filePath = zipFilePath.substring(0, zipFilePath.lastIndexOf('.'));
                    File zipFile = new File(zipFilePath);
                    File dir = new File(filePath);
                    if (log.isDebugEnabled()) {
                        log.debug("Start unzipping the file " + packName + "in the location " + pathToStorage);
                    }
                    taskProgress.setMessage("Unzipping the pack");
                    LicenseManagerUtils.unzip(zipFile.getAbsolutePath(), dir.getAbsolutePath());
                    if (log.isDebugEnabled()) {
                        log.debug("The file " + packName + " is successfully unzipped to location " + pathToStorage);
                        log.debug("Start extracting jars from " + packName.substring(0, packName.lastIndexOf('.')));
                    }

                    // Extract jars from the pack.
                    taskProgress.setMessage("Extracting jars");
                    JarHolder jarHolder = LicenseManagerUtils.checkJars(filePath);
                    if (log.isDebugEnabled()) {
                        log.debug("Jars are successfully extracted from " + packName.substring(0, packName
                                .lastIndexOf('.')));
                    }
                    taskProgress.setMessage("Jar extraction complete");
                    taskProgress.setStatus(Constants.COMPLETE);
                    taskProgress.setData(jarHolder);

                } catch (JSchException | SftpException e) {
                    taskProgress.setStatus(Constants.FAILED);
                    taskProgress.setMessage("Failed to connect with FTP server");
                    log.error("Failed to connect with FTP server. " + e.getMessage(), e);

                } catch (LicenseManagerRuntimeException e) {
                    taskProgress.setStatus(Constants.FAILED);
                    taskProgress.setMessage("Failed to extract jars from the pack");
                    log.error("Error while extracting jars. " + e.getMessage(), e);

                } finally {
                    // Close the connections.
                    if (session != null) {
                        session.disconnect();
                    }
                    if (sftpChannel != null) {
                        sftpChannel.exit();
                    }
                }
            }
        }).start();
        return taskProgress;
    }

    /**
     * Remove the duplicates in the name missing jars.
     *
     * @param nameMissingJars list of jars in which the names and version are missing
     * @return unique list of jars with name missing.
     */
    public static List<Jar> removeDuplicates(List<Jar> nameMissingJars) {

        List<Jar> nameMissingUniqueJars = new ArrayList<>();
        for (Jar jar : nameMissingJars) {
            boolean newJar = true;
            for (Jar uniqueJar : nameMissingUniqueJars) {
                if (jar.getProjectName().equals(uniqueJar.getProjectName())) {
                    newJar = false;
                }
            }
            if (newJar) {
                nameMissingUniqueJars.add(jar);
            }
        }
        return nameMissingUniqueJars;
    }

    /**
     * Remove files from local file storage and FTP server after generating the license text.
     *
     * @param fileName product name for which the licenses were generated
     */
    public static void cleanFileStorage(String fileName) {

        LicenseManagerUtils.deleteFolder(fileName + ".zip");
        LicenseManagerUtils.deleteFolder(fileName);
        String ftpFilePath = SystemVariableUtil.getValue(Constants.FTP_FILE_LOCATION, null);

        Session session = null;
        ChannelSftp sftpChannel = null;
        try {
            session = createSftpSession();
            session.connect();
            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();

            // Remove the pack from
            sftpChannel.rm(ftpFilePath + fileName + ".zip");
            if (log.isDebugEnabled()) {
                log.debug("The file " + fileName + ".zip" + " is removed from the FTP server");
            }
        } catch (JSchException | SftpException e) {
            log.error("Failed to remove the zip file from the FTP server. " + e.getMessage(), e);
        } finally {
            // Close the connections.
            if (session != null) {
                session.disconnect();
            }
            if (sftpChannel != null) {
                sftpChannel.exit();
            }
        }
    }

    /**
     * Initiates a session to communicate with FTP server.
     *
     * @return SFTP session
     * @throws JSchException if the initiation fails.
     */
    private static Session createSftpSession() throws JSchException {

        String ftpHost = SystemVariableUtil.getValue(Constants.FTP_HOST, null);
        int ftpPort = Integer.valueOf(SystemVariableUtil.getValue(Constants.FTP_PORT, null));
        String ftpUsername = SystemVariableUtil.getValue(Constants.FTP_USERNAME, null);
        String ftpPassword = SystemVariableUtil.getValue(Constants.FTP_PASSWORD, null);
        JSch jsch = new JSch();

        // Initiate SFTP connection.
        Session session = jsch.getSession(ftpUsername, ftpHost, ftpPort);
        Hashtable<String, String> config = new Hashtable<>();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
        session.setPassword(ftpPassword);
        session.connect();
        return session;
    }

}
