///*
// * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
// *
// * WSO2 Inc. licenses this file to you under the Apache License,
// * Version 2.0 (the "License"); you may not use this file except
// * in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied. See the License for the
// * specific language governing permissions and limitations
// * under the License.
// */
//
///*
// * To change this template, choose Tools | Templates
// * and open the template in the editor.
// */
//package org.wso2.internal.apps.license.manager.impl.enterData;
//
//import au.com.bytecode.opencsv.CSVReader;
//import com.workingdogs.village.DataSetException;
//import com.workingdogs.village.Record;
//import com.workingdogs.village.TableDataSet;
//import org.wso2.internal.apps.license.manager.impl.main.JarHolder;
//import org.wso2.internal.apps.license.manager.impl.tables.LM_COMPONENT;
//import org.wso2.internal.apps.license.manager.impl.tables.LM_COMPONENT_LICENSE;
//import org.wso2.internal.apps.license.manager.impl.tables.LM_LIBRARY;
//import org.wso2.internal.apps.license.manager.impl.tables.LM_LIBRARY_LICENSE;
//import org.wso2.internal.apps.license.manager.impl.tables.LM_LICENSE;
//
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.Reader;
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.SQLException;
//import java.util.Scanner;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
///**
// *
// * @author pubudu
// */
//public class EnterOldData {
//
//    Connection con;
//    Logger logger;
//    Scanner scan = new Scanner(System.in);
//
//    public EnterOldData(String driver, String url, String uname,
//            String password, Logger log) throws ClassNotFoundException,
//            SQLException {
//        Class.forName(driver);
//        this.con = DriverManager.getConnection(url, uname, password);
//        this.logger = log;
//    }
//
//    public void enterJars(Reader read) throws FileNotFoundException,
//            IOException, SQLException, ClassNotFoundException, DataSetException {
//        // "/home/pubudu/Desktop/license_generation/milinda_license/LicenseDB.csv"
//        CSVReader r = new CSVReader(read);
//        String[] next;
//        while ((next = r.readNext()) != null) {
//            String name = JarHolder.getName(next[0]);
//            String version = JarHolder.getVersion(next[0]);
//            String type = next[1];
//            String license = next[2];
//            String fileName = next[0];
//            insert(name, version, fileName, type, license);
//        }
//    }
//
//    private void insert(String name, String version, String fileName,
//            String type, String license) throws DataSetException {
//        LM_COMPONENT compTab = new LM_COMPONENT();
//        LM_LIBRARY libTab = new LM_LIBRARY();
//        int res;
//        TableDataSet tds;
//        Record record;
//        if (type.equals("bundle")) {
//            try {
//                logger.info("Inserting component " + fileName
//                        + " in component table.");
//                tds = new TableDataSet(con, compTab.table);
//                record = tds.addRecord(tds);
//                res = record.setValue(compTab.COMP_NAME, name)
//                        .setValue(compTab.COMP_FILE_NAME, fileName)
//                        .setValue(compTab.COMP_KEY, fileName)
//                        .setValue(compTab.COMP_TYPE,type)
//                        .setValue(compTab.COMP_VERSION, version)
//                        .save(con);
//                insertComponentLicsnse(fileName, license);
//            } catch (SQLException ex) {
//                logger.log(Level.SEVERE, null, ex);
//            }
//        } else {
//            try {
//                logger.info("Inserting library " + fileName
//                        + " in library table.");
//                tds = new TableDataSet(con, libTab.table);
//                record = tds.addRecord(tds);
//                res = record.setValue(libTab.LIB_NAME, name)
//                        .setValue(libTab.LIB_FILE_NAME, fileName)
//                        .setValue(libTab.LIB_TYPE,type)
//                        .setValue(libTab.LIB_VERSION, version)
//                        .save(con);
//                insertLibraryLicense(name+"-"+version,license,EnterData.getLastInsertId(con));
//            } catch (SQLException ex) {
//                logger.log(Level.SEVERE, null, ex);
//            }
//        }
//    }
//
//    public void enterLicenses(Reader reader)
//            throws DataSetException, SQLException, FileNotFoundException, IOException {
//
//        LM_LICENSE licTab = new LM_LICENSE();
//        TableDataSet tds = new TableDataSet(con, licTab.table);
//
//        CSVReader r = new CSVReader(reader);
//        String[] next;
//        while ((next = r.readNext()) != null) {
//            logger.info("Inserting license "+ next[1]+ " in to licese table.");
//            Record record = tds.addRecord();
//            record.setValue(licTab.LICENSE_KEY, next[0]).setValue(
//                    licTab.LICENSE_NAME, next[1])
//                    .setValue(licTab.LICENSE_URL, next[2])
//                    .save(con);
//        }
//    }
//
//    private void insertComponentLicsnse(String compKey, String licenseKey)
//            throws DataSetException {
//        LM_COMPONENT_LICENSE complicTab = new LM_COMPONENT_LICENSE();
//        TableDataSet tds;
//        Record record;
//        try {
//            logger.info("Inserting component " + compKey + " in "
//                    + complicTab.table + " table.");
//            tds = new TableDataSet(con, complicTab.table);
//            record = tds.addRecord(tds);
//            record.setValue(complicTab.COMP_KEY, compKey).setValue(
//                    complicTab.LICENSE_KEY,
//                    getLibraryLicense(licenseKey, con, compKey)).save();
//        } catch (SQLException ex) {
//            logger.log(Level.SEVERE, null, ex);
//        }
//    }
//
//    private void insertLibraryLicense(String libKey, String licenseKey,
//            int libId) throws DataSetException {
//        LM_LIBRARY_LICENSE liblicTab = new LM_LIBRARY_LICENSE();
//        TableDataSet tds;
//        Record record;
//        try {
//            logger.info("Inserting component " + libKey + " in "
//                    + liblicTab.table + " table.");
//            tds = new TableDataSet(con, liblicTab.table);
//            record = tds.addRecord(tds);
//            record.setValue(liblicTab.LIB_ID, libId)
//                    .setValue(liblicTab.LICENSE_KEY,getLibraryLicense(licenseKey, con, libKey))
//                    .save();
//        } catch (SQLException ex) {
//            logger.log(Level.SEVERE, null, ex);
//        }
//    }
//
//
//    private String getLibraryLicense(String license, Connection con, String jarName)
//            throws DataSetException, SQLException {
//        LM_LICENSE licTab = new LM_LICENSE();
//        TableDataSet tds = new TableDataSet(con, licTab.table);
//        tds.where(licTab.LICENSE_KEY +"='"+ license+"'");
//
//        tds.fetchRecords();
//        if (tds.size() == 0) {
//            System.out.println("There is no license key entry " + license
//                    + " which is required for " + jarName + ".\n"
//                    + "Please enter licence key : ");
//            String key = scan.nextLine();
//            System.out.println("Please enter license name : ");
//            String name = scan.nextLine();
//            System.out.println("Please enter license version : ");
//            String version = scan.nextLine();
//            System.out.println("Please enter license year : ");
//            String year = scan.nextLine();
//            System.out.println("Please enter license source : ");
//            String source = scan.nextLine();
//            System.out.println("Please enter license url : ");
//            String url = scan.nextLine();
//
//            Record record = tds.addRecord();
//            record.setValue(licTab.LICENSE_KEY, key).setValue(licTab.LICENSE_NAME, name)
//                    .setValue(licTab.LICENSE_VERSION,version)
//                    .setValue(licTab.LICENSE_SOURCE, source)
//                    .setValue(licTab.LICENSE_YEAR, year)
//                    .setValue(licTab.LICENSE_URL, url)
//                    .save(con);
//            return key;
//        } else {
//            return tds.getRecord(0).getValue("LICENSE_KEY").asString();
//        }
//    }
//}
