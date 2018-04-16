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
import org.wso2.msf4j.MicroservicesRunner;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
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

    public void enter() throws ClassNotFoundException, DataSetException, SQLException {

        try {
            this.productId = dbHandler.getProductId(jarHolder.getProductName(), jarHolder.getProductVersion());
            Iterator<Jar> i = jarHolder.getJarList().iterator();
            while (i.hasNext()) {
                Jar j = i.next();
                insert(j);

            }
        } catch (DataSetException | SQLException | ClassNotFoundException e) {
            throw e;
        } finally {
            if (dbHandler != null) {
                dbHandler.closeConnection();
            }
        }

    }

    private void insert(Jar mj) throws DataSetException, SQLException, ClassNotFoundException {

        String version = mj.getVersion();
        String name = mj.getProjectName();
        String fileName = mj.getJarFile().getName();
        String type = mj.getType();
        if (type.equals("wso2")) {

            if (!dbHandler.isComponentExists(fileName)) {
                licenseMissingComponents.add(mj);
            } else if (dbHandler.isComponentExists(fileName) && !dbHandler.isComponentLicenseExists(fileName)) {
                licenseMissingComponents.add(mj);
            } else {
                dbHandler.insertProductComponent(fileName, productId);
            }

        } else {
            String libraryType = (mj.getParent() == null) ? ((mj.isBundle()) ? "bundle" : "jar") :
                    "jarinbundle";
            int libraryId = dbHandler.selectLibraryId(name, version, libraryType);
            if (libraryId != -1) {
                try {
                    boolean isLicenseExists = dbHandler.isLibraryLicenseExists(libraryId);
                    if (mj.getParent() != null && mj.getParent().getType().equals("wso2")) {
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
                } catch (DataSetException e) {
                    throw e;
                }
            } else {
                licenseMissingLibraries.add(mj);
            }

        }
    }

//    private void insertProductComponent(String compKey) throws DataSetException, SQLException {
//
//        LM_COMPONENT_PRODUCT compprodtab = new LM_COMPONENT_PRODUCT();
//        TableDataSet tds;
//        Record record;
//        try {
//            tds = new TableDataSet(con, compprodtab.table);
//            record = tds.addRecord();
//            record.setValue(compprodtab.COMP_KEY, compKey)
//                    .setValue(compprodtab.PRODUCT_ID, productId)
//                    .save();
//        } catch (SQLException ex) {
//            log.error(ex.getMessage());
//        }
//
//    }
//
//    private void insertLibraryLicense(String licenseKey, String libId) throws DataSetException {
//
//        LM_LIBRARY_LICENSE liblicTab = new LM_LIBRARY_LICENSE();
//        TableDataSet tds;
//        Record record;
//        try {
//
//            tds = new TableDataSet(con, liblicTab.table);
//            record = tds.addRecord();
//            record.setValue(liblicTab.LIB_ID, libId)
//                    .setValue(liblicTab.LICENSE_KEY, licenseKey)
//                    .save();
//        } catch (SQLException ex) {
//            log.error(ex.getMessage());
//        }
//    }
//
//    private void insertProductLibrary(int libId) throws DataSetException, SQLException {
//
//        LM_LIBRARY_PRODUCT libprodtab = new LM_LIBRARY_PRODUCT();
//        TableDataSet tds;
//        Record record;
//        try {
//            tds = new TableDataSet(con, libprodtab.table);
//            record = tds.addRecord();
//            record.setValue(libprodtab.LIB_ID, libId)
//                    .setValue(libprodtab.PRODUCT_ID, productId)
//                    .save();
//        } catch (SQLException ex) {
//            log.error(ex.getMessage());
//        }
//    }
//
//    private void insertComponentLibrary(String component, int libraryId) throws DataSetException, SQLException {
//
//        LM_COMPONENT_LIBRARY complibtab = new LM_COMPONENT_LIBRARY();
//        TableDataSet tds;
//        Record record;
//        try {
//            tds = new TableDataSet(con, complibtab.table);
//            record = tds.addRecord();
//            record.setValue(complibtab.LIB_ID, libraryId)
//                    .setValue(complibtab.COMP_KEY, component)
//                    .save();
//        } catch (SQLException ex) {
//            log.error(ex.getMessage());
//        }
//    }
}
