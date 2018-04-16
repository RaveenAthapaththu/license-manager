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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wso2.internal.apps.license.manager.impl.enterData;

import com.workingdogs.village.DataSetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.internal.apps.license.manager.impl.main.Jar;
import org.wso2.internal.apps.license.manager.impl.main.JarHolder;
import org.wso2.internal.apps.license.manager.impl.models.DBHandler;
import org.wso2.internal.apps.license.manager.util.Constants;
import org.wso2.msf4j.MicroservicesRunner;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author pubudu
 */
public class EnterData {

    private static final Logger log = LoggerFactory.getLogger(MicroservicesRunner.class);
    private DBHandler dbHandler;
    private JarHolder jarHolder;
    private List<Jar> licenseMissingComponents = new ArrayList<>();
    private List<Jar> licenseMissingLibraries = new ArrayList<>();
    private int productId;

    public EnterData(JarHolder jarHolder) throws
            ClassNotFoundException, SQLException {

        this.dbHandler = new DBHandler();
        this.jarHolder = jarHolder;
    }

    public List<Jar> getLicenseMissingComponents() {

        return licenseMissingComponents;
    }

    public List<Jar> getLicenseMissingLibraries() {

        return licenseMissingLibraries;
    }

    public int getProductId() {

        return productId;
    }

    public void enter() throws DataSetException, SQLException {

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
                licenseMissingComponents.add(mj);
            } else if (dbHandler.isComponentExists(fileName) && !dbHandler.isComponentLicenseExists(fileName)) {
                licenseMissingComponents.add(mj);
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
                // If a jar has a parent and i the parent is "wso2", add parent and library to the
                // LM_COMPONENT_LIBRARY table.
                if (mj.getParent() != null && mj.getParent().getType().equals(Constants.JAR_TYPE_WSO2)) {
                    if (dbHandler.isComponentExists(mj.getParent().getJarFile().getName())) {
                        dbHandler.insertComponentLibrary(mj.getParent().getJarFile().getName(), libraryId);
                    } else
                        licenseMissingLibraries.add(mj);
                } else {
                    dbHandler.insertProductLibrary(libraryId, productId);
                }
                if (!isLicenseExists) {
                    licenseMissingLibraries.add(mj);
                }
            } else {
                licenseMissingLibraries.add(mj);
            }
        }
    }
}
