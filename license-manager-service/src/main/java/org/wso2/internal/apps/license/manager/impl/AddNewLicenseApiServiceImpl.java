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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.internal.apps.license.manager.datahandler.NewLicenseOfJarDataHandler;
import org.wso2.internal.apps.license.manager.exception.LicenseManagerDataException;
import org.wso2.internal.apps.license.manager.model.JarFile;
import org.wso2.internal.apps.license.manager.model.JarFilesHolder;
import org.wso2.internal.apps.license.manager.model.LicenseMissingJar;
import org.wso2.internal.apps.license.manager.model.NewLicenseEntry;
import org.wso2.internal.apps.license.manager.util.Constants;
import org.wso2.internal.apps.license.manager.util.EmailUtils;
import org.wso2.internal.apps.license.manager.util.JsonUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.mail.MessagingException;

/**
 * Implementation of the API service to add new licenses to the jars which did not have licenses defined.
 */
public class AddNewLicenseApiServiceImpl {

    private static final Logger log = LoggerFactory.getLogger(AddNewLicenseApiServiceImpl.class);

    public void updateLicenses(JarFilesHolder jarFilesHolder, String payload, String username) throws
            LicenseManagerDataException, MessagingException {

        JsonArray componentsJson = JsonUtils.getAttributesFromRequestBody(payload, "components");
        JsonArray librariesJson = JsonUtils.getAttributesFromRequestBody(payload, "libraries");
        updateLicensesOfLicenseMissingJars(jarFilesHolder.getLicenseMissingComponents(), componentsJson);
        updateLicensesOfLicenseMissingJars(jarFilesHolder.getLicenseMissingComponents(), librariesJson);
        insertNewLicensesToDb(jarFilesHolder, username);
    }

    /**
     * Update the licenses of the license missing jars with the user input.
     *
     * @param licenseMissingJarList list of license missing jars
     * @param licenseDefinedJars    user inputs for the licenses
     */
    private void updateLicensesOfLicenseMissingJars(List<LicenseMissingJar> licenseMissingJarList,
                                                    JsonArray licenseDefinedJars) {

        for (int i = 0; i < licenseDefinedJars.size(); i++) {
            int index = licenseDefinedJars.get(i).getAsJsonObject().get("index").getAsInt();
            String licenseKey = licenseDefinedJars.get(i).getAsJsonObject().get("licenseKey").getAsString();
            licenseMissingJarList.get(index).setLicenseKey(licenseKey);
        }
    }

    /**
     * Insert new licenses for the jars into the database and send mail to the admin.
     *
     * @param username  user who added the licenses
     * @throws LicenseManagerDataException if the data insertion fails
     * @throws MessagingException          if sending mail fails
     */
    private void insertNewLicensesToDb(JarFilesHolder jarFilesHolder, String username)
            throws LicenseManagerDataException, MessagingException {

        Boolean isInsertionSuccess = false;

        List<NewLicenseEntry> newLicenseEntryComponentList = null;
        List<NewLicenseEntry> newLicenseEntryLibraryList = null;
        try (NewLicenseOfJarDataHandler newLicenseDAL = new NewLicenseOfJarDataHandler()) {
            newLicenseEntryComponentList = insertComponentLicenses(jarFilesHolder.getLicenseMissingComponents(),
                    jarFilesHolder.getProductId(), newLicenseDAL);
            newLicenseEntryLibraryList = insertLibraryLicenses(jarFilesHolder.getLicenseMissingLibraries(),
                    jarFilesHolder.getProductId(), newLicenseDAL);
            isInsertionSuccess = true;
        } catch (SQLException e) {
            throw new LicenseManagerDataException("Failed to add licenses.", e);
        } catch (IOException e) {
            log.error("Failed to close the database connection while adding new licenses for the jars. " +
                    e.getMessage(), e);
        } finally {
            // Send an email to the admin if there are any new licenses added.
            if (newLicenseEntryComponentList.size() > 0 || newLicenseEntryLibraryList.size() > 0) {
                EmailUtils.sendEmail(username, newLicenseEntryComponentList, newLicenseEntryLibraryList,
                        isInsertionSuccess);
            }
        }
    }

    private List<NewLicenseEntry> insertComponentLicenses(List<LicenseMissingJar> componentList, int productId,
                                                          NewLicenseOfJarDataHandler newLicenseDAL) throws
            SQLException {

        List<NewLicenseEntry> newLicenseEntryComponentList = new ArrayList<>();

        for (LicenseMissingJar licenseMissingJar : componentList) {
            String name = licenseMissingJar.getJarFile().getProjectName();
            String componentName = licenseMissingJar.getJarFile().getJarFile().getName();
            String licenseKey = licenseMissingJar.getLicenseKey();
            String version = licenseMissingJar.getJarFile().getVersion();
            newLicenseDAL.insertComponent(name, componentName, version);
            newLicenseDAL.insertProductComponent(componentName, productId);
            newLicenseDAL.insertComponentLicense(componentName, licenseKey);
            NewLicenseEntry newEntry = new NewLicenseEntry(componentName, licenseKey);
            newLicenseEntryComponentList.add(newEntry);
        }
        return newLicenseEntryComponentList;
    }

    private List<NewLicenseEntry> insertLibraryLicenses(List<LicenseMissingJar> libraryList, int productId,
                                                        NewLicenseOfJarDataHandler newLicenseDAL) throws SQLException {

        List<NewLicenseEntry> newLicenseEntryLibraryList = new ArrayList<>();

        for (LicenseMissingJar licenseMissingJar : libraryList) {
            String name = licenseMissingJar.getJarFile().getProjectName();
            String libraryFileName = licenseMissingJar.getJarFile().getJarFile().getName();
            String licenseKey = licenseMissingJar.getLicenseKey();
            String version = licenseMissingJar.getJarFile().getVersion();
            String type = licenseMissingJar.getJarFile().getType();
            String componentKey = null;
            JarFile parent = null;

            if (licenseMissingJar.getJarFile().getParent() != null) {
                parent = licenseMissingJar.getJarFile().getParent();
                componentKey = parent.getJarFile().getName();
            }

            int libId = newLicenseDAL.getLibraryId(name, libraryFileName, version, type);
            newLicenseDAL.insertLibraryLicense(licenseKey, libId);

            // If the parent is wso2 insert it as a component-library relationship
            if (parent != null && parent.getType().equals(Constants.JAR_TYPE_WSO2)) {
                newLicenseDAL.insertComponentLibrary(componentKey, libId);
            } else {
                newLicenseDAL.insertProductLibrary(libId, productId);
            }

            NewLicenseEntry newEntry = new NewLicenseEntry(libraryFileName, licenseKey);
            newLicenseEntryLibraryList.add(newEntry);
        }
        return newLicenseEntryLibraryList;
    }

}
