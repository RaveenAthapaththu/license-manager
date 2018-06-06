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

package org.wso2.internal.apps.license.manager.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.internal.apps.license.manager.connector.FtpConnectionManager;
import org.wso2.internal.apps.license.manager.exception.LicenseManagerConfigurationException;
import org.wso2.internal.apps.license.manager.exception.LicenseManagerRuntimeException;
import org.wso2.internal.apps.license.manager.model.JarFilesHolder;
import org.wso2.internal.apps.license.manager.model.TaskProgress;
import org.wso2.internal.apps.license.manager.util.Constants;
import org.wso2.internal.apps.license.manager.util.LicenseManagerUtils;
import org.wso2.internal.apps.license.manager.util.ProgressTracker;
import org.wso2.msf4j.util.SystemVariableUtil;

import java.io.File;
import java.util.Set;

/**
 * Implementation of the API service to get the list of jars with faulty names from which the name and version can
 * not be extracted.
 */
public class GetFaultyNamedJarsApiServiceImpl {

    private static final Logger log = LoggerFactory.getLogger(GetFaultyNamedJarsApiServiceImpl.class);

    /**
     * Start the jar extraction process of a pack in a new thread.
     *
     * @param username username of the task executor.
     * @param packName pack to be extracted.
     * @return TaskProgress
     */
    public TaskProgress startPackExtractionProcess(String username, final String packName) {

        // Stop if there are threads running for the same user.
        endAnyExistingTasks(username);
        TaskProgress taskProgress = ProgressTracker.createNewTaskProgress(username);
        taskProgress.setStepNumber(Constants.PACK_EXTRACTION_STEP_ID);
        taskProgress.setMessage("Pack extraction process started");

        // Starting a new thread to extract the pack
        new Thread(() -> {
            taskProgress.setExecutingThreadId(Thread.currentThread().getId());
            String pathToStorage = SystemVariableUtil.getValue(Constants.FILE_DOWNLOAD_PATH, null);

            try {
                taskProgress.setMessage("Downloading the pack");

                // Initiate SFTP connection and download file
                FtpConnectionManager ftpConnectionManager = FtpConnectionManager.getFtpConnectionManager();
                ftpConnectionManager.downloadFileFromFtpServer(packName);

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
                JarFilesHolder jarFilesHolder = LicenseManagerUtils.checkJars(filePath);
                if (log.isDebugEnabled()) {
                    log.debug("Jars are successfully extracted from " + packName.substring(0, packName
                            .lastIndexOf('.')));
                }
                taskProgress.setMessage("JarFile.java extraction complete");
                taskProgress.setStatus(Constants.COMPLETE);
                taskProgress.setData(jarFilesHolder);
            } catch (LicenseManagerRuntimeException e) {
                taskProgress.setStatus(Constants.FAILED);
                taskProgress.setMessage("Failed to extract jars from the pack");
                log.error("Error while extracting jars. " + e.getMessage(), e);
            } catch (LicenseManagerConfigurationException e) {
                taskProgress.setStatus(Constants.FAILED);
                taskProgress.setMessage("Failed to connect with FTP server");
                log.error("Failed to connect with FTP server. " + e.getMessage(), e);
            }
        }).start();
        return taskProgress;
    }

    private void endAnyExistingTasks(String username) {
        // Create a new object to track the progress.
        TaskProgress taskProgress = ProgressTracker.getTaskProgress(username);

        if (taskProgress != null) {
            long previousThreadId = taskProgress.getExecutingThreadId();
            //Take the set of running threads
            Set<Thread> setOfThread = Thread.getAllStackTraces().keySet();

            //Iterate over set to the relevant thread
            for (Thread thread : setOfThread) {
                if (thread.getId() == previousThreadId) {
                    thread.interrupt();
                }
            }
            ProgressTracker.deleteTaskProgress(username);
        }
    }
}
