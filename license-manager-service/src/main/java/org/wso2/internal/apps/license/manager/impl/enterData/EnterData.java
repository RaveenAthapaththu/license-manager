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
import com.workingdogs.village.Record;
import com.workingdogs.village.TableDataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.internal.apps.license.manager.impl.main.JarHolder;
import org.wso2.internal.apps.license.manager.impl.main.MyJar;
import org.wso2.internal.apps.license.manager.impl.tables.LM_COMPONENT;
import org.wso2.internal.apps.license.manager.impl.tables.LM_COMPONENT_LIBRARY;
import org.wso2.internal.apps.license.manager.impl.tables.LM_COMPONENT_LICENSE;
import org.wso2.internal.apps.license.manager.impl.tables.LM_COMPONENT_PRODUCT;
import org.wso2.internal.apps.license.manager.impl.tables.LM_LIBRARY;
import org.wso2.internal.apps.license.manager.impl.tables.LM_LIBRARY_LICENSE;
import org.wso2.internal.apps.license.manager.impl.tables.LM_LIBRARY_PRODUCT;
import org.wso2.internal.apps.license.manager.impl.tables.LM_LICENSE;
import org.wso2.internal.apps.license.manager.impl.tables.LM_PRODUCT;
import org.wso2.msf4j.MicroservicesRunner;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

/**
 * @author pubudu
 */
public class EnterData {

    private static final Logger log = LoggerFactory.getLogger(MicroservicesRunner.class);
    private Connection con;
    private JarHolder jarHolder;
    private Scanner scan = JarHolder.scan;
    private List<MyJar> licenseMissingComponents = new ArrayList<>();
    private List<MyJar> licenseMissingLibraries = new ArrayList<>();
    private int productId;

    public EnterData(String driver, String url, String uname, String password, JarHolder jarHolder) throws
            ClassNotFoundException, SQLException {

        Class.forName(driver);
        this.con = DriverManager.getConnection(url, uname, password);
        this.jarHolder = jarHolder;
    }

    public static int getLastInsertId(Connection con) throws SQLException {

        Statement stmt;
        stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT LAST_INSERT_ID();");
        rs.next();
        return rs.getInt("LAST_INSERT_ID()");
    }

    public List<MyJar> getLicenseMissingComponents() {

        return licenseMissingComponents;
    }

    public List<MyJar> getLicenseMissingLibraries() {

        return licenseMissingLibraries;
    }

    public int getProductId() {

        return productId;
    }

    public void enter() throws DataSetException {

        insertProduct(jarHolder.getProductName(), jarHolder.getProductVersion());
        Iterator<MyJar> i = jarHolder.getJarList().iterator();
        while (i.hasNext()) {
            MyJar j = i.next();
            insert(j);
        }
    }

    private void insert(MyJar mj) throws DataSetException {

        String name = mj.getProjectName(), version = mj.getVersion(), fileName = mj.getJarFile().getName(), type = mj
                .getType();
        if (type.equals("wso2")) {

            if (!isComponentExists(fileName)) {
                licenseMissingComponents.add(mj);
            } else if (isComponentExists(fileName) && !isComponentLicenseExists(fileName)) {
                licenseMissingComponents.add(mj);
            } else {
                insertProductComponent(fileName);
            }

        } else {
            boolean libraryExists = isLibraryExists(name, version);
            if (libraryExists) {
                try {
                    int libraryId = getLibraryId(name, version);
                    int lastInsert;
                    boolean inserted = insertLibrary(name, fileName, version, mj.isBundle(), mj.getParent());
                    boolean isLicenseExists = isLibraryLicenseExists(libraryId);
                    String libraryType = (mj.getParent() == null) ? ((mj.isBundle()) ? "bundle" : "jar") :
                            "jarinbundle";

                    if (inserted && isLicenseExists) {
                        lastInsert = getActualLibraryId(name, version, libraryType);
                        String licenseKey = getLicenseKeyFromLibraryLicense(libraryId);
                        insertLibraryLicense(licenseKey, Integer.toString(lastInsert));
                    } else if (inserted && !isLicenseExists) {
                        licenseMissingLibraries.add(mj);
                        return;
                    } else if (!inserted && !isLicenseExists) {
                        licenseMissingLibraries.add(mj);
                        return;
                    }
                } catch (Exception ex) {
                    log.error(ex.getMessage());
                }
                try {
                    int libId = getLibId(fileName, mj.getParent(), mj.isBundle());
                    if (libId != -1) {
                        if (mj.getParent() == null) {
                            insertProductLibrary(libId);
                        } else {
                            insertComponentLibrary(mj.getParent().getJarFile().getName(), libId);
                        }
                    }
                } catch (DataSetException e) {
                    log.error(e.getMessage());
                }
            } else {
                licenseMissingLibraries.add(mj);
            }

        }
    }

    private void insertProduct(String product, String version) throws DataSetException {

        LM_PRODUCT prodTab = new LM_PRODUCT();
        TableDataSet tds;
        Record record;
        try {

            tds = new TableDataSet(con, prodTab.table);
            record = tds.addRecord();
            record.setValue(prodTab.PRODUCT_NAME, product)
                    .setValue(prodTab.PRODUCT_VERSION, version)
                    .save(con);
            productId = getLastInsertId(con);
        } catch (SQLException ex) {
            try {
                tds = new TableDataSet(con, prodTab.table);
                tds.where(prodTab.PRODUCT_NAME + "='" + product + "'" + " AND " + prodTab.PRODUCT_VERSION + "='" +
                        version + "'").fetchRecords();
                productId = tds.getRecord(0).getValue("PRODUCT_ID").asInt();
            } catch (SQLException ex1) {
                log.error(ex1.getMessage());
            }
        }

    }

    private boolean insertComponent(String name, String fileName, String version) throws DataSetException {

        LM_COMPONENT compTab = new LM_COMPONENT();
        TableDataSet tds;
        Record record;
        try {
            tds = new TableDataSet(con, compTab.table);
            record = tds.addRecord();
            record.setValue(compTab.COMP_NAME, name).setValue(
                    compTab.COMP_FILE_NAME, fileName).setValue(
                    compTab.COMP_KEY, fileName).setValue(
                    compTab.COMP_TYPE, "bundle")
                    .setValue(compTab.COMP_VERSION, version).save(con);
        } catch (SQLException ex) {
            log.error(ex.getMessage());
            return false;
        }
        return true;
    }

    private boolean insertLibrary(String name, String fileName, String version, boolean isBundle, MyJar parent)
            throws DataSetException {

        LM_LIBRARY libTab = new LM_LIBRARY();
        TableDataSet tds;
        Record record;
        try {
            tds = new TableDataSet(con, libTab.table);
            record = tds.addRecord();
            record.setValue(libTab.LIB_NAME, name).setValue(
                    libTab.LIB_FILE_NAME, fileName).setValue(
                    libTab.LIB_TYPE, (parent == null) ? ((isBundle) ? "bundle" : "jar") : "jarinbundle")
                    .setValue(libTab.LIB_VERSION, version).save(con);
        } catch (SQLException ex) {
            log.error(ex.getMessage());
            return false;
        }
        return true;
    }

    private void insertComponentLicsnse(String compKey, String licenseKey) throws DataSetException {

        LM_COMPONENT_LICENSE complicTab = new LM_COMPONENT_LICENSE();
        TableDataSet tds;
        Record record;
        try {
            tds = new TableDataSet(con, complicTab.table);
            record = tds.addRecord();
            record.setValue(complicTab.COMP_KEY, compKey).setValue(
                    complicTab.LICENSE_KEY,
                    getLibraryLicense(licenseKey, con, compKey)).save();
        } catch (SQLException ex) {
            log.error(ex.getMessage());
        }
    }

    private void insertLibraryLicense(String licenseKey, String libId) throws DataSetException {

        LM_LIBRARY_LICENSE liblicTab = new LM_LIBRARY_LICENSE();
        TableDataSet tds;
        Record record;
        try {

            tds = new TableDataSet(con, liblicTab.table);
            record = tds.addRecord();
            record.setValue(liblicTab.LIB_ID, libId)
                    .setValue(liblicTab.LICENSE_KEY, licenseKey)
                    .save();
        } catch (SQLException ex) {
            log.error(ex.getMessage());
        }
    }

    private String getLibraryLicense(String license, Connection con, String jarName) throws DataSetException,
            SQLException {

        LM_LICENSE licTab = new LM_LICENSE();
        TableDataSet tds = new TableDataSet(con, licTab.table);
        tds.where(licTab.LICENSE_KEY + "='" + license + "'");
        tds.fetchRecords();
        if (tds.size() == 0) {
            String key = scan.nextLine();
            return getLibraryLicense(key, con, jarName);
        } else {
            return tds.getRecord(0).getValue("LICENSE_KEY").asString();
        }
    }

    private boolean isLibraryExists(String name, String version) throws DataSetException {

        LM_LIBRARY libTable = new LM_LIBRARY();
        TableDataSet tds;
        try {
            tds = new TableDataSet(con, libTable.table);
            tds.where(libTable.LIB_NAME + "='" + name + "' AND " + libTable.LIB_VERSION + "='" + version + "'");
            tds.fetchRecords();
            if (tds.size() == 0) {
                return false;
            } else {
                return true;
            }
        } catch (SQLException ex) {
            log.error(ex.getMessage());
            return false;
        }

    }

    private boolean isComponentExists(String fileName) throws DataSetException {

        LM_COMPONENT compTable = new LM_COMPONENT();
        TableDataSet tds;
        try {
            tds = new TableDataSet(con, compTable.table);
            tds.where(compTable.COMP_FILE_NAME + "='" + fileName + "' ");
            tds.fetchRecords();
            if (tds.size() == 0) {
                return false;
            } else {
                return true;
            }
        } catch (SQLException ex) {
            log.error(ex.getMessage());
            return false;
        }

    }

    private int getActualLibraryId(String name, String version, String type) throws DataSetException {

        LM_LIBRARY libTable = new LM_LIBRARY();
        TableDataSet tds;
        Record record;
        int id = -1;

        try {
            tds = new TableDataSet(con, libTable.table);
            tds.where(libTable.LIB_NAME + "='" + name + "' AND " + libTable.LIB_VERSION + "='" + version + "' AND " +
                    libTable.LIB_TYPE + "='" + type + "'");
            tds.fetchRecords();
            record = tds.getRecord(0);
            id = record.getValue(libTable.LIB_ID).asInt();

        } catch (SQLException ex) {
            log.error(ex.getMessage());
        }
        return id;

    }

    private String getLicenseKeyFromLibraryLicense(int libraryId) throws DataSetException {

        LM_LIBRARY_LICENSE libTable = new LM_LIBRARY_LICENSE();
        TableDataSet tds;
        Record record;
        String licenseKey = "";

        try {

            tds = new TableDataSet(con, libTable.table);
            tds.where(libTable.LIB_ID + "=" + Integer.toString(libraryId));
            tds.fetchRecords();
            record = tds.getRecord(0);
            licenseKey = record.getValue(libTable.LICENSE_KEY).toString();
            return licenseKey;

        } catch (SQLException ex) {
            log.error(ex.getMessage());

        }
        return licenseKey;

    }

    private int getLibraryId(String name, String version) throws DataSetException {

        LM_LIBRARY libTable = new LM_LIBRARY();
        TableDataSet tds;
        int id = 0;
        try {
            tds = new TableDataSet(con, libTable.table);
            tds.where(libTable.LIB_NAME + "='" + name + "' AND " + libTable.LIB_VERSION + "='" + version + "'");
            tds.fetchRecords();
            Record record = tds.getRecord(0);
            id = record.getValue(libTable.LIB_ID).asInt();
            return id;
        } catch (SQLException ex) {
            log.error(ex.getMessage());
            return id;
        }

    }

    private boolean isLibraryLicenseExists(int libraryId) throws DataSetException {

        LM_LIBRARY_LICENSE libTable = new LM_LIBRARY_LICENSE();
        TableDataSet tds;
        try {
            tds = new TableDataSet(con, libTable.table);
            tds.where(libTable.LIB_ID + "=" + Integer.toString(libraryId));
            tds.fetchRecords();
            if (tds.size() == 0) {

                return false;
            } else {

                return true;
            }
        } catch (SQLException ex) {
            log.error(ex.getMessage());
            return false;
        }

    }

    private boolean isComponentLicenseExists(String fileName) throws DataSetException {

        LM_COMPONENT_LICENSE compLicenseTable = new LM_COMPONENT_LICENSE();
        TableDataSet tds;
        Record record;
        try {
            tds = new TableDataSet(con, compLicenseTable.table);
            tds.where(compLicenseTable.COMP_KEY + "='" + fileName + "'");
            tds.fetchRecords();
            if (tds.size() == 0) {
                return false;
            } else {
                return true;
            }
        } catch (SQLException ex) {
            log.error(ex.getMessage());
            return false;
        }

    }

    private void insertProductComponent(String compKey) throws DataSetException {

        LM_COMPONENT_PRODUCT compprodtab = new LM_COMPONENT_PRODUCT();
        TableDataSet tds;
        Record record;
        try {
            tds = new TableDataSet(con, compprodtab.table);
            record = tds.addRecord();
            record.setValue(compprodtab.COMP_KEY, compKey)
                    .setValue(compprodtab.PRODUCT_ID, productId)
                    .save();
        } catch (SQLException ex) {
            log.error(ex.getMessage());
        }

    }

    private void insertProductLibrary(int libId) throws DataSetException {

        LM_LIBRARY_PRODUCT libprodtab = new LM_LIBRARY_PRODUCT();
        TableDataSet tds;
        Record record;
        try {
            tds = new TableDataSet(con, libprodtab.table);
            record = tds.addRecord();
            record.setValue(libprodtab.LIB_ID, libId)
                    .setValue(libprodtab.PRODUCT_ID, productId)
                    .save();
        } catch (SQLException ex) {
            log.error(ex.getMessage());
        }
    }

    private void insertComponentLibrary(String component, int libraryId) throws DataSetException {

        LM_COMPONENT_LIBRARY complibtab = new LM_COMPONENT_LIBRARY();
        TableDataSet tds;
        Record record;
        try {
            tds = new TableDataSet(con, complibtab.table);
            record = tds.addRecord();
            record.setValue(complibtab.LIB_ID, libraryId)
                    .setValue(complibtab.COMP_KEY, component)
                    .save();
        } catch (SQLException ex) {
            log.error(ex.getMessage());
        }
    }

    private int getLibId(String fileName, MyJar parent, boolean isBundle) throws DataSetException {

        LM_LIBRARY libtab = new LM_LIBRARY();
        TableDataSet tds;
        try {

            tds = new TableDataSet(con, libtab.table);
            String type;
            if (parent == null) {
                type = ((isBundle) ? "bundle" : "jar");
            } else {
                type = "jarinbundle";
            }

            tds.where(libtab.LIB_FILE_NAME + "='" + fileName + "' AND " + libtab.LIB_TYPE + "='" + type + "'");
            tds.fetchRecords(1);
            return tds.getRecord(0).getValue(libtab.LIB_ID).asInt();

        } catch (SQLException ex) {
            return -1;
        }

    }
}
