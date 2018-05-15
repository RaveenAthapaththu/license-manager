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
import org.wso2.internal.apps.license.manager.exception.LicenseManagerConfigurationException;
import org.wso2.internal.apps.license.manager.exception.LicenseManagerDataException;
import org.wso2.internal.apps.license.manager.models.JarFile;
import org.wso2.internal.apps.license.manager.models.LicenseMissingJar;
import org.wso2.internal.apps.license.manager.models.NewLicenseEntry;
import org.wso2.internal.apps.license.manager.util.Constants;
import org.wso2.internal.apps.license.manager.util.DBHandler;
import org.wso2.internal.apps.license.manager.util.EmailUtils;
import org.wso2.internal.apps.license.manager.util.FtpConnectionHandler;
import org.wso2.internal.apps.license.manager.util.JsonUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.mail.MessagingException;

/**
 * Implementation of the executions of the services exposed by the micro service.
 */
public class ServiceExecutor {

    private static final Logger log = LoggerFactory.getLogger(ServiceExecutor.class);

    /**
     * Get the all the licenses available as a list of json array.
     *
     * @return list of licenses
     * @throws LicenseManagerDataException if the SFTP connection fails
     */
    public JsonArray getListOfAllLicenses() throws LicenseManagerDataException {

        JsonArray listOfLicensesAsJson;
        DBHandler dbHandler = null;
        try {
            dbHandler = new DBHandler();
            listOfLicensesAsJson = dbHandler.selectAllLicense();
        } catch (SQLException | ClassNotFoundException e) {
            throw new LicenseManagerDataException("Failed to retrieve data from the database.", e);
        } finally {
            if (dbHandler != null) {
                try {
                    dbHandler.closeConnection();
                } catch (SQLException e) {
                    log.error("Failed to close the database connection. " + e.getMessage(), e);
                }
            }
        }
        return listOfLicensesAsJson;
    }

    /**
     * Get the list of zip packs uploaded to the FTP server.
     *
     * @return list of names of the uploaded packs
     * @throws LicenseManagerConfigurationException if the SFTP connection fails
     */
    public JsonArray getListOfPacksName() throws LicenseManagerConfigurationException {

        FtpConnectionHandler ftpConnectionHandler = new FtpConnectionHandler();
        ArrayList<String> listOfPacks;
        JsonArray listOfPacksAsJson;
        try {
            ftpConnectionHandler.initiateSftpConnection();
            listOfPacks = ftpConnectionHandler.listFilesInFtpServer();
            listOfPacksAsJson = JsonUtils.getListOfPacksUploadedAsJson(listOfPacks);
            return listOfPacksAsJson;
        } finally {
            // Close the connections.
            ftpConnectionHandler.closeSftpConnection();
        }
    }

    /**
     * Update the name and version of the list of jars which name and version is undefined with user input.
     *
     * @param jarFileInformationHolder java object which contains jar object details
     * @param jarsWithNames            json array which hols the user inputs
     */
    public void updateNameMissingListOfJars(JarFileInformationHolder jarFileInformationHolder,
                                            JsonArray jarsWithNames) {

        // Define the name and the version from the user input.
        for (int i = 0; i < jarsWithNames.size(); i++) {
            JsonObject jar = jarsWithNames.get(i).getAsJsonObject();
            int index = jar.get("index").getAsInt();
            jarFileInformationHolder.getErrorJarFileList().get(index).setProjectName(jar.get("name").getAsString());
            jarFileInformationHolder.getErrorJarFileList().get(index).setVersion(jar.get("version").getAsString());
        }

        // Add name defined jars into the jar list of the jar holder.
        for (JarFile jarFile : jarFileInformationHolder.getErrorJarFileList()) {
            jarFileInformationHolder.getJarFileList().add(jarFile);
        }
    }

    /**
     * Insert the information of all jars extracted from the pack into the database.
     *
     * @param jarFileInformationHolder java object which contains jar object details
     * @return java object which holds the license missing jars
     * @throws LicenseManagerDataException if the data insertion fails
     */
    public JarFileInfoDataHandler insertJarInfoToDb(JarFileInformationHolder jarFileInformationHolder)
            throws LicenseManagerDataException {

        JarFileInfoDataHandler jarFileInfoDataHandler;
        try {
            jarFileInfoDataHandler = new JarFileInfoDataHandler(jarFileInformationHolder);
            jarFileInfoDataHandler.enterJarsIntoDB();
        } catch (ClassNotFoundException | SQLException e) {
            throw new LicenseManagerDataException("Failed to connect with database.", e);
        }
        return jarFileInfoDataHandler;
    }

    /**
     * Update the licenses of the license missing jars with the user input.
     *
     * @param licenseMissingJarList list of license missing jars
     * @param licenseDefinedJars    user inputs for the licenses
     */
    public void updateLicensesOfLicenseMissingJars(List<LicenseMissingJar> licenseMissingJarList,
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
     * @param componentList list of component jars with new licenses
     * @param libraryList   list of library jars with new licenses
     * @param productId     product id which the jars belong to
     * @param username      user who added the licenses
     * @throws LicenseManagerDataException if the data insertion fails
     * @throws MessagingException          if sending mail fails
     */
    public void insertNewLicensesToDb(List<LicenseMissingJar> componentList, List<LicenseMissingJar> libraryList,
                                      int productId, String username)
            throws LicenseManagerDataException, MessagingException {

        DBHandler dbHandler = null;
        Boolean isInsertionSuccess = false;
        List<NewLicenseEntry> newLicenseEntryComponentList = new ArrayList<>();
        List<NewLicenseEntry> newLicenseEntryLibraryList = new ArrayList<>();

        try {
            dbHandler = new DBHandler();

            // Insert new licenses for the components.
            for (LicenseMissingJar licenseMissingJar : componentList) {
                String name = licenseMissingJar.getJarFile().getProjectName();
                String componentName = licenseMissingJar.getJarFile().getJarFile().getName();
                String licenseKey = licenseMissingJar.getLicenseKey();
                String version = licenseMissingJar.getJarFile().getVersion();
                dbHandler.insertComponent(name, componentName, version);
                dbHandler.insertProductComponent(componentName, productId);
                dbHandler.insertComponentLicense(componentName, licenseKey);
                NewLicenseEntry newEntry = new NewLicenseEntry(componentName, licenseKey);
                newLicenseEntryComponentList.add(newEntry);
            }

            // Insert new licenses for the libraries.
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

                int libId = dbHandler.getLibraryId(name, libraryFileName, version, type);
                dbHandler.insertLibraryLicense(licenseKey, libId);

                // If the parent is wso2 insert it as a component-library relationship
                if (parent != null && parent.getType().equals(Constants.JAR_TYPE_WSO2)) {
                    dbHandler.insertComponentLibrary(componentKey, libId);
                } else {
                    dbHandler.insertProductLibrary(libId, productId);
                }

                NewLicenseEntry newEntry = new NewLicenseEntry(libraryFileName, licenseKey);
                newLicenseEntryLibraryList.add(newEntry);
            }
            isInsertionSuccess = true;
        } catch (ClassNotFoundException | SQLException e) {
            throw new LicenseManagerDataException("Failed to add licenses.", e);
        } finally {
            // Send an email to the admin if there are any new licenses added.
            if (newLicenseEntryComponentList.size() > 0 || newLicenseEntryLibraryList.size() > 0) {
                EmailUtils.sendEmail(username, newLicenseEntryComponentList, newLicenseEntryLibraryList, isInsertionSuccess);
            }
            try {
                if (dbHandler != null) {
                    dbHandler.closeConnection();
                }
            } catch (SQLException e) {
                log.error("Could not close the database connection. " + e.getMessage(), e);
            }
        }
    }
}
