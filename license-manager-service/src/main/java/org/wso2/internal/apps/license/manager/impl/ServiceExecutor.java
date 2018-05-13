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
import org.wso2.internal.apps.license.manager.exception.LicenseManagerConfigurationException;
import org.wso2.internal.apps.license.manager.exception.LicenseManagerDataException;
import org.wso2.internal.apps.license.manager.models.Jar;
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

public class ServiceExecutor {

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

    public void updateNameMissingListOfJars(JarHolder jarHolder, JsonArray jarsWithNames) {
        // Define the name and the version from the user input.
        for (int i = 0; i < jarsWithNames.size(); i++) {
            JsonObject jar = jarsWithNames.get(i).getAsJsonObject();
            int index = jar.get("index").getAsInt();
            jarHolder.getErrorJarList().get(index).setProjectName(jar.get("name").getAsString());
            jarHolder.getErrorJarList().get(index).setVersion(jar.get("version").getAsString());
        }

        // Add name defined jars into the jar list of the jar holder.
        for (Jar jar : jarHolder.getErrorJarList()) {
            jarHolder.getJarList().add(jar);
        }
    }

    public ProductJarManager insertJarInfoToDb(JarHolder jarHolder) throws LicenseManagerDataException {

        ProductJarManager productJarManager = null;
        try {
            productJarManager = new ProductJarManager(jarHolder);
            productJarManager.enterJarsIntoDB();
        } catch (ClassNotFoundException | SQLException e) {
            throw new LicenseManagerDataException("Failed to connect with database.", e);
        }
        return productJarManager;
    }

    public void updateLicensesOfLicenseMissingJars(List<LicenseMissingJar> licenseMissingJarList,
                                                   JsonArray licenseDefinedJars) {

        for (int i = 0; i < licenseDefinedJars.size(); i++) {
            int index = licenseDefinedJars.get(i).getAsJsonObject().get("index").getAsInt();
            String licenseKey = licenseDefinedJars.get(i).getAsJsonObject().get("licenseKey").getAsString();
            licenseMissingJarList.get(index).setLicenseKey(licenseKey);
        }
    }

    public void insertNewLicensesToDb(List<LicenseMissingJar> componentList,
                                      List<LicenseMissingJar> libraryList,
                                      int productId, String username) throws LicenseManagerDataException, MessagingException {

        DBHandler dbHandler = null;
        List<NewLicenseEntry> newLicenseEntryComponentList = new ArrayList<>();
        List<NewLicenseEntry> newLicenseEntryLibraryList = new ArrayList<>();

        try {
            dbHandler = new DBHandler();
            // Insert new licenses for the components.
            for (LicenseMissingJar licenseMissingJar : componentList) {
                String name = licenseMissingJar.getJar().getProjectName();
                String componentName = licenseMissingJar.getJar().getJarFile().getName();
                String licenseKey = licenseMissingJar.getLicenseKey();
                String version = licenseMissingJar.getJar().getVersion();
                dbHandler.insertComponent(name, componentName, version);
                dbHandler.insertProductComponent(componentName, productId);
                dbHandler.insertComponentLicense(componentName, licenseKey);
                NewLicenseEntry newEntry = new NewLicenseEntry(componentName, licenseKey);
                newLicenseEntryComponentList.add(newEntry);
            }

            // Insert new licenses for the libraries.
            for (LicenseMissingJar licenseMissingJar : libraryList) {
                String name = licenseMissingJar.getJar().getProjectName();
                String libraryFileName = licenseMissingJar.getJar().getJarFile().getName();
                String licenseKey = licenseMissingJar.getLicenseKey();
                String version = licenseMissingJar.getJar().getVersion();
                String type = licenseMissingJar.getJar().getType();
                String componentKey = null;
                Jar parent = null;

                if (licenseMissingJar.getJar().getParent() != null) {
                    parent = licenseMissingJar.getJar().getParent();
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
            // If there are new licenses added successfully, send a mail to the admin.
            if (newLicenseEntryComponentList.size() > 0 || newLicenseEntryLibraryList.size() > 0) {
                EmailUtils.sendEmail(username, newLicenseEntryComponentList, newLicenseEntryLibraryList, true);
            }
        } catch (ClassNotFoundException | SQLException e) {
            if (newLicenseEntryComponentList.size() > 0 || newLicenseEntryLibraryList.size() > 0) {
                EmailUtils.sendEmail(username, newLicenseEntryComponentList, newLicenseEntryLibraryList, false);
            }
            throw new LicenseManagerDataException("Failed to add licenses.", e);
        } finally {
            try {
                dbHandler.closeConnection();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }
}
