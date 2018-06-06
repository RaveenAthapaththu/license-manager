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

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.internal.apps.license.manager.datahandler.LicenseExistingJarFileDataHandler;
import org.wso2.internal.apps.license.manager.exception.LicenseManagerDataException;
import org.wso2.internal.apps.license.manager.model.JarFile;
import org.wso2.internal.apps.license.manager.model.JarFilesHolder;
import org.wso2.internal.apps.license.manager.model.LicenseMissingJar;
import org.wso2.internal.apps.license.manager.model.TaskProgress;
import org.wso2.internal.apps.license.manager.util.Constants;
import org.wso2.internal.apps.license.manager.util.ProgressTracker;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the API service which updates the name and version of the faulty named jars.
 */
public class UpdateJarInfoApiServiceImpl {

    private static final Logger log = LoggerFactory.getLogger(UpdateJarInfoApiServiceImpl.class);

    public TaskProgress startUpdatingDatabase(String username, JsonArray jarsWithDefinedNames) {

        TaskProgress taskProgress = ProgressTracker.getTaskProgress(username);
        taskProgress.setStatus(Constants.RUNNING);
        taskProgress.setStepNumber(Constants.UPDATE_DB_STEP_ID);
        taskProgress.setMessage("Start updating the database");
        new Thread(() -> {
            try {
                taskProgress.setExecutingThreadId(Thread.currentThread().getId());
                updateJarInfo(jarsWithDefinedNames, taskProgress);
                taskProgress.setStatus(Constants.COMPLETE);
            } catch (LicenseManagerDataException e) {
                taskProgress.setStatus(Constants.FAILED);
                taskProgress.setMessage("Failed to add jar information into the database.");
            }
        }).start();
        return taskProgress;

    }

    private void updateJarInfo(JsonArray jarsWithDefinedNames, TaskProgress taskProgress) throws
            LicenseManagerDataException {

        updateFaultyNamedListOfJars(taskProgress.getData(), jarsWithDefinedNames);
        enterJarsIntoDB(taskProgress);

    }

    /**
     * Update the name and version of the list of jars which name and version is undefined with user input.
     *
     * @param jarFilesHolder       java object which contains jar object details
     * @param jarsWithDefinedNames json array which hols the user inputs
     */
    private void updateFaultyNamedListOfJars(JarFilesHolder jarFilesHolder,
                                             JsonArray jarsWithDefinedNames) {

        // Define the name and the version from the user input.
        for (int i = 0; i < jarsWithDefinedNames.size(); i++) {
            JsonObject jar = jarsWithDefinedNames.get(i).getAsJsonObject();
            int index = jar.get("index").getAsInt();
            jarFilesHolder.getFaultyNamedJars().get(index).setProjectName(jar.get("name").getAsString());
            jarFilesHolder.getFaultyNamedJars().get(index).setVersion(jar.get("version").getAsString());
        }

        // Add name defined jars into the jar list of the jar holder.
        for (JarFile jarFile : jarFilesHolder.getFaultyNamedJars()) {
            jarFilesHolder.getJarFilesInPack().add(jarFile);
        }
    }

    /**
     * Recursively insert the information of all jars extracted from the pack into the database.
     *
     * @throws LicenseManagerDataException if the data insertion fails
     */
    private void enterJarsIntoDB(TaskProgress taskProgress) throws LicenseManagerDataException {

        JarFilesHolder jarFilesHolder = taskProgress.getData();
        List<LicenseMissingJar> licenseMissingLibraries = new ArrayList<>();
        List<LicenseMissingJar> licenseMissingComponents = new ArrayList<>();
        int productId = 0;
        try (LicenseExistingJarFileDataHandler licenseExistingJarFileDAL = new LicenseExistingJarFileDataHandler()) {
            productId = licenseExistingJarFileDAL.getProductId(jarFilesHolder.getProductName(),
                    jarFilesHolder.getProductVersion());

            double progress;
            double count = 0;
            int totalNumberOfJars = jarFilesHolder.getJarFilesInPack().size();
            for (JarFile jarFile : jarFilesHolder.getJarFilesInPack()) {
                String version = jarFile.getVersion();
                String name = jarFile.getProjectName();
                String fileName = jarFile.getJarFile().getName();
                String type = jarFile.getType();

                // If the jarFile type is WSO2 add it as a component.
                if (type.equals(Constants.JAR_TYPE_WSO2)) {
                    if (!licenseExistingJarFileDAL.isComponentExists(fileName)) {
                        String licenseForAnyVersion = licenseExistingJarFileDAL.getComponentLicenseForAnyVersion(name);
                        licenseMissingComponents.add(new LicenseMissingJar(jarFile, licenseForAnyVersion));
                    } else if (licenseExistingJarFileDAL.isComponentExists(fileName) && !licenseExistingJarFileDAL
                            .isComponentLicenseExists(fileName)) {
                        String licenseForAnyVersion = licenseExistingJarFileDAL.getComponentLicenseForAnyVersion(name);
                        licenseMissingComponents.add(new LicenseMissingJar(jarFile, licenseForAnyVersion));
                    } else {
                        licenseExistingJarFileDAL.insertProductComponent(fileName, productId);
                    }
                } else {  // If jarFile is a third party library.
                       int libraryId = licenseExistingJarFileDAL.selectLibraryId(name, version, type);
                    if (libraryId != -1) {
                        boolean isLicenseExists = licenseExistingJarFileDAL.isLibraryLicenseExists(libraryId);
                        // If a jarFile has a parent and if the parent is "wso2", add parent and library to the
                        // LM_COMPONENT_LIBRARY table.
                        if (jarFile.getParent() != null && jarFile.getParent().getType().equals(Constants
                                .JAR_TYPE_WSO2)) {
                            if (licenseExistingJarFileDAL.isComponentExists(jarFile.getParent().getJarFile().getName
                                    ())) {
                                licenseExistingJarFileDAL.insertComponentLibrary(jarFile.getParent().getJarFile()
                                        .getName(), libraryId);
                            } else {
                                String licenseForAnyVersion = licenseExistingJarFileDAL.getLibraryLicenseForAnyVersion
                                        (name);
                                licenseMissingLibraries.add(new LicenseMissingJar(jarFile, licenseForAnyVersion));
                            }
                        } else {
                            licenseExistingJarFileDAL.insertProductLibrary(libraryId, productId);
                        }
                        if (!isLicenseExists) {
                            String licenseForAnyVersion = licenseExistingJarFileDAL.getLibraryLicenseForAnyVersion
                                    (name);
                            licenseMissingLibraries.add(new LicenseMissingJar(jarFile, licenseForAnyVersion));
                        }
                    } else {
                        String licenseForAnyVersion = licenseExistingJarFileDAL.getLibraryLicenseForAnyVersion(name);
                        licenseMissingLibraries.add(new LicenseMissingJar(jarFile, licenseForAnyVersion));
                    }
                }
                count += 1;
                progress = count / totalNumberOfJars * 100;
                taskProgress.setMessage("Updating the database \n Progress : " + Math.round(progress) + "% ");
            }

        } catch (SQLException e) {
            throw new LicenseManagerDataException("Failed to add data to the database.", e);
        } catch (IOException e) {
            log.error("Failed to close the database connection while retrieving license details. " +
                    e.getMessage(), e);
        }

        jarFilesHolder.setProductId(productId);
        jarFilesHolder.setLicenseMissingComponents(licenseMissingComponents);
        jarFilesHolder.setLicenseMissingLibraries(licenseMissingLibraries);

    }

}
