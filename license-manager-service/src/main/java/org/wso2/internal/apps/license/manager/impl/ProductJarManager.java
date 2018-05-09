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

import com.workingdogs.village.DataSetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.internal.apps.license.manager.impl.JarHolder;
import org.wso2.internal.apps.license.manager.models.Jar;
import org.wso2.internal.apps.license.manager.models.LicenseMissingJar;
import org.wso2.internal.apps.license.manager.util.Constants;
import org.wso2.internal.apps.license.manager.util.DBHandler;
import org.wso2.msf4j.MicroservicesRunner;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author pubudu
 */
public class ProductJarManager {

    private static final Logger log = LoggerFactory.getLogger(MicroservicesRunner.class);
    private DBHandler dbHandler;
    private JarHolder jarHolder;
    private List<LicenseMissingJar> licenseMissingComponents = new ArrayList<>();
    private List<LicenseMissingJar> licenseMissingLibraries = new ArrayList<>();
    private int productId;

    public ProductJarManager(JarHolder jarHolder) throws ClassNotFoundException, SQLException {

        this.dbHandler = new DBHandler();
        this.jarHolder = jarHolder;
    }

    public List<LicenseMissingJar> getLicenseMissingComponents() {

        return licenseMissingComponents;
    }

    public List<LicenseMissingJar> getLicenseMissingLibraries() {

        return licenseMissingLibraries;
    }

    public int getProductId() {

        return productId;
    }

    public void enterJarsIntoDB() throws DataSetException, SQLException {

        try {
            this.productId = dbHandler.getProductId(jarHolder.getProductName(), jarHolder.getProductVersion());
            for (Jar j : jarHolder.getJarList()) {
                insert(j);
            }
        } finally {
            if (dbHandler != null) {
                dbHandler.closeConnection();
            }
        }
    }

    private void insert(Jar mj) throws DataSetException, SQLException {

        String version = mj.getVersion();
        String name = mj.getProjectName();
        String fileName = mj.getJarFile().getName();
        String type = mj.getType();
        if (type.equals(Constants.JAR_TYPE_WSO2)) {
            if (!dbHandler.isComponentExists(fileName)) {
                String licenseForAnyVersion = dbHandler.getComponetLicenseForAnyVersion(name);
                licenseMissingComponents.add(new LicenseMissingJar(mj, licenseForAnyVersion));
            } else if (dbHandler.isComponentExists(fileName) && !dbHandler.isComponentLicenseExists(fileName)) {
                String licenseForAnyVersion = dbHandler.getComponetLicenseForAnyVersion(name);
                licenseMissingComponents.add(new LicenseMissingJar(mj, licenseForAnyVersion));
            } else {
                dbHandler.insertProductComponent(fileName, productId);
            }
        } else {
            String libraryType = (mj.getParent() == null) ?
                    ((mj.isBundle()) ? Constants.JAR_TYPE_BUNDLE : Constants.JAR_TYPE_JAR) :
                    Constants.JAR_TYPE_JAR_IN_BUNDLE;
            int libraryId = dbHandler.selectLibraryId(name, version, libraryType);
            if (libraryId != -1) {
                boolean isLicenseExists = dbHandler.isLibraryLicenseExists(libraryId);
                // If a jar has a parent and if the parent is "wso2", add parent and library to the
                // LM_COMPONENT_LIBRARY table.
                if (mj.getParent() != null && mj.getParent().getType().equals(Constants.JAR_TYPE_WSO2)) {
                    if (dbHandler.isComponentExists(mj.getParent().getJarFile().getName())) {
                        dbHandler.insertComponentLibrary(mj.getParent().getJarFile().getName(), libraryId);
                    } else {
                        String licenseForAnyVersion = dbHandler.getLibraryLicenseForAnyVersion(name);
                        licenseMissingLibraries.add(new LicenseMissingJar(mj, licenseForAnyVersion));
                    }
                } else {
                    dbHandler.insertProductLibrary(libraryId, productId);
                }
                if (!isLicenseExists) {
                    String licenseForAnyVersion = dbHandler.getLibraryLicenseForAnyVersion(name);
                    licenseMissingLibraries.add(new LicenseMissingJar(mj, licenseForAnyVersion));
                }
            } else {
                String licenseForAnyVersion = dbHandler.getLibraryLicenseForAnyVersion(name);
                licenseMissingLibraries.add(new LicenseMissingJar(mj, licenseForAnyVersion));
            }
        }
    }
}
