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
package org.wso2.internal.apps.license.manager.impl.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.workingdogs.village.DataSetException;
import com.workingdogs.village.Record;
import com.workingdogs.village.TableDataSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.internal.apps.license.manager.impl.tables.LM_COMPONENT_LIBRARY;
import org.wso2.internal.apps.license.manager.impl.tables.LM_COMPONENT_LICENSE;
import org.wso2.internal.apps.license.manager.impl.tables.LM_COMPONENT_PRODUCT;
import org.wso2.internal.apps.license.manager.impl.tables.LM_LIBRARY;
import org.wso2.internal.apps.license.manager.impl.tables.LM_LIBRARY_LICENSE;
import org.wso2.internal.apps.license.manager.impl.tables.LM_LIBRARY_PRODUCT;
import org.wso2.internal.apps.license.manager.impl.tables.LM_LICENSE;
import org.wso2.internal.apps.license.manager.impl.tables.LM_PRODUCT;
import org.wso2.internal.apps.license.manager.util.Constants;
import org.wso2.msf4j.MicroservicesRunner;
import org.wso2.msf4j.util.SystemVariableUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Class to handle the interactions with the database.
 */
public class DBHandler {

    private static final Logger log = LoggerFactory.getLogger(MicroservicesRunner.class);
    private Connection con;

    public DBHandler() throws ClassNotFoundException, SQLException {

        String databaseUrl = SystemVariableUtil.getValue(Constants.DATABASE_URL, null);
        String databaseDriver = SystemVariableUtil.getValue(Constants.DATABASE_DRIVER, null);
        String databaseUsername = SystemVariableUtil.getValue(Constants.DATABASE_USERNAME, null);
        String databasePassword = SystemVariableUtil.getValue(Constants.DATABASE_PASSWORD, null);
        Class.forName(databaseDriver);
        con = DriverManager.getConnection(databaseUrl, databaseUsername, databasePassword);

    }

    private Connection initiateConnection() throws ClassNotFoundException, SQLException {

        String databaseUrl = SystemVariableUtil.getValue(Constants.DATABASE_URL, null);
        String databaseDriver = SystemVariableUtil.getValue(Constants.DATABASE_DRIVER, null);
        String databaseUsername = SystemVariableUtil.getValue(Constants.DATABASE_USERNAME, null);
        String databasePassword = SystemVariableUtil.getValue(Constants.DATABASE_PASSWORD, null);
        Class.forName(databaseDriver);
        return DriverManager.getConnection(databaseUrl, databaseUsername, databasePassword);

    }

    public void closeConnection() throws SQLException {

        con.close();
    }

    public JsonArray selectAllLicense() throws SQLException, DataSetException, ClassNotFoundException {

        LM_LICENSE licenseTable = new LM_LICENSE();
        TableDataSet tds;

        JsonArray resultArray = new JsonArray();
        try {
            tds = new TableDataSet(con, licenseTable.table);
            tds.fetchRecords();
            for (int i = 0; i < tds.size(); i++) {
                Record record = tds.getRecord(i);
                JsonObject licenseJson = new JsonObject();
                licenseJson.addProperty("LICENSE_ID", record.getValue("LICENSE_ID").asInt());
                licenseJson.addProperty("LICENSE_KEY", record.getValue("LICENSE_KEY").toString());
                licenseJson.addProperty("LICENSE_NAME", record.getValue("LICENSE_NAME").toString());
                resultArray.add(licenseJson);
            }
        } catch (SQLException | DataSetException e) {
            throw e;
        }
        return resultArray;
    }

    public String selectLicenseFromId(int id) throws SQLException, DataSetException {

//        LM_LICENSE licenseTable = new LM_LICENSE();
        TableDataSet tds;
        Record rec;
        String licenseKey = null;

        String query = "SELECT * FROM LM_LICENSE WHERE LICENSE_ID=?";
        PreparedStatement preparedStatement = con.prepareStatement(query);
        preparedStatement.setInt(1, id);
        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            licenseKey = rs.getString("LICENSE_KEY");
        }
        return licenseKey;
    }

    public void insertComponent(String name, String fileName, String version) throws SQLException {

        String insertComponent = "INSERT IGNORE INTO LM_COMPONENT"
                + "(COMP_NAME, COMP_FILE_NAME, COMP_KEY, COMP_TYPE,COMP_VERSION) VALUES"
                + "(?,?,?,?,?)";
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = con.prepareStatement(insertComponent);
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, fileName);
            preparedStatement.setString(3, fileName);
            preparedStatement.setString(4, "bundle");
            preparedStatement.setString(5, version);

            preparedStatement.executeUpdate();
        } catch (SQLException ex) {
            throw ex;
        }

    }

    private int insertLibrary(String name, String fileName, String version, String type) throws DataSetException,
            SQLException {

        LM_LIBRARY libTab = new LM_LIBRARY();
        TableDataSet tds;
        Record record;
        try {
            tds = new TableDataSet(con, libTab.table);
            record = tds.addRecord();
            record.setValue(libTab.LIB_NAME, name)
                    .setValue(libTab.LIB_FILE_NAME, fileName)
                    .setValue(libTab.LIB_TYPE, type)
                    .setValue(libTab.LIB_VERSION, version)
                    .save(con);
            Statement stmt;
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT LAST_INSERT_ID();");
            rs.next();
            return rs.getInt("LAST_INSERT_ID()");
        } catch (SQLException ex) {
            throw ex;

        }
    }

    public void insertProductComponent(String compKey, int productId) throws DataSetException, SQLException {

        LM_COMPONENT_PRODUCT compprodtab = new LM_COMPONENT_PRODUCT();
        TableDataSet tds;
        Record record;
        if (selectProductComponent(compKey, productId) == -1) {
            try {
                tds = new TableDataSet(con, compprodtab.table);
                record = tds.addRecord();
                record.setValue(compprodtab.COMP_KEY, compKey)
                        .setValue(compprodtab.PRODUCT_ID, productId)
                        .save();
            } catch (SQLException ex) {
                throw ex;

            }
        }
    }

    private int selectProductComponent(String compKey, int productId) throws SQLException {

        int id = -1;
        String query;

        query = "SELECT * FROM LM_COMPONENT_PRODUCT WHERE COMP_KEY=? AND PRODUCT_ID=?";
        PreparedStatement preparedStatement = con.prepareStatement(query);
        preparedStatement.setString(1, compKey);
        preparedStatement.setInt(2, productId);
        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            id = rs.getInt("PRODUCT_ID");
        }
        return id;
    }

    public void insertProductLibrary(int libId, int productId) throws DataSetException, SQLException {

        LM_LIBRARY_PRODUCT libprodtab = new LM_LIBRARY_PRODUCT();
        TableDataSet tds;
        if (selectProductLibrary(libId, productId) == -1) {
            Record record;
            try {
                tds = new TableDataSet(con, libprodtab.table);
                record = tds.addRecord();
                record.setValue(libprodtab.LIB_ID, libId)
                        .setValue(libprodtab.PRODUCT_ID, productId)
                        .save();
            } catch (SQLException ex) {
                throw ex;
            }
        }

    }

    private int selectProductLibrary(int libId, int productId) throws SQLException {

        int id = -1;
        String query;

        query = "SELECT * FROM LM_LIBRARY_PRODUCT WHERE LIB_ID=? AND PRODUCT_ID=?";
        PreparedStatement preparedStatement = con.prepareStatement(query);
        preparedStatement.setInt(1, libId);
        preparedStatement.setInt(2, productId);
        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            id = rs.getInt("LIB_ID");
        }
        return id;
    }

    public void insertComponentLibrary(String component, int libraryId) throws SQLException, DataSetException {

        LM_COMPONENT_LIBRARY complibtab = new LM_COMPONENT_LIBRARY();
        TableDataSet tds;
        if (selectComponentLibrary(component, libraryId) == -1) {
            Record record;
            try {
                tds = new TableDataSet(con, complibtab.table);
                record = tds.addRecord();
                record.setValue(complibtab.LIB_ID, libraryId)
                        .setValue(complibtab.COMP_KEY, component)
                        .save();
            } catch (SQLException | DataSetException ex) {
                throw ex;
            }
        }

    }

    private int selectComponentLibrary(String component, int libraryId) throws SQLException {

        int id = -1;
        String query;
        query = "SELECT * FROM LM_COMPONENT_LIBRARY WHERE LIB_ID=? AND COMP_KEY=?";
        PreparedStatement preparedStatement = con.prepareStatement(query);
        preparedStatement.setInt(1, libraryId);
        preparedStatement.setString(2, component);
        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            id = rs.getInt("LIB_ID");
        }
        return id;
    }

    public void insertComponentLicense(String compKey, String licenseKey) throws DataSetException, SQLException {

        LM_COMPONENT_LICENSE complicTab = new LM_COMPONENT_LICENSE();
        TableDataSet tds;
        Record record;
        try {

            tds = new TableDataSet(con, complicTab.table);
            record = tds.addRecord();
            record.setValue(complicTab.COMP_KEY, compKey).setValue(
                    complicTab.LICENSE_KEY, licenseKey).save();
        } catch (SQLException ex) {
            throw ex;

        }
    }

    public void insertLibraryLicense(String licenseKey, String libId) throws DataSetException, SQLException {

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
            throw ex;
        }
    }

    public int selectLibraryId(String name, String version, String type) throws SQLException, ClassNotFoundException {

        int libraryId = -1;
        String query;

        query = "SELECT * FROM LM_LIBRARY WHERE LIB_NAME=? AND LIB_VERSION=? AND LIB_TYPE=?";
        PreparedStatement preparedStatement = con.prepareStatement(query);
        preparedStatement.setString(1, name);
        preparedStatement.setString(2, version);
        preparedStatement.setString(3, type);
        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            libraryId = rs.getInt("LIB_ID");
        }
        return libraryId;
    }

    public int getLibraryId(String name, String fileName, String version, String type) throws SQLException,
            ClassNotFoundException, DataSetException {

        int libraryId = -1;
        libraryId = selectLibraryId(name, version, type);
        if (libraryId == -1) {
            try {
                libraryId = insertLibrary(name, fileName, version, type);
            } catch (DataSetException e) {
                throw e;
            }
        }
        return libraryId;
    }

    private int insertProduct(String product, String version) throws DataSetException, SQLException {

        LM_PRODUCT prodTab = new LM_PRODUCT();
        TableDataSet tds;
        Record record;
        int productId = -1;
        try {
            tds = new TableDataSet(con, prodTab.table);
            record = tds.addRecord();
            record.setValue(prodTab.PRODUCT_NAME, product)
                    .setValue(prodTab.PRODUCT_VERSION, version)
                    .save(con);
            productId = getLastInsertId(con);
            return productId;
        } catch (SQLException ex) {
            throw ex;
        }

    }

    private int selectProductId(String product, String version) throws SQLException {

        int productId = -1;
        String query;

        query = "SELECT * FROM LM_PRODUCT WHERE PRODUCT_NAME=? AND PRODUCT_VERSION=? ";
        PreparedStatement preparedStatement = con.prepareStatement(query);
        preparedStatement.setString(1, product);
        preparedStatement.setString(2, version);
        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            productId = rs.getInt("PRODUCT_ID");
        }
        return productId;
    }

    public int getProductId(String product, String version) throws SQLException, DataSetException {

        int productId = -1;
        productId = selectProductId(product, version);
        if (productId == -1) {
            try {
                productId = insertProduct(product, version);
            } catch (DataSetException e) {
                throw e;
            }
        }
        return productId;

    }

    public boolean isComponentExists(String fileName) throws SQLException {

        String query;
        int id= -1;
        query = "SELECT * FROM LM_COMPONENT WHERE COMP_KEY=?";
        PreparedStatement preparedStatement = con.prepareStatement(query);
        preparedStatement.setString(1, fileName);
        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            id = rs.getInt("COMP_ID");
        }
        return id != -1;
    }

    public boolean isLibraryLicenseExists(int libraryId) throws DataSetException, SQLException {

        LM_LIBRARY_LICENSE libTable = new LM_LIBRARY_LICENSE();
        TableDataSet tds;
        boolean isExist = false;
        try {
            tds = new TableDataSet(con, libTable.table);
            tds.where(libTable.LIB_ID + "=" + Integer.toString(libraryId));
            tds.fetchRecords();
            if (tds.size() == 0) {

                isExist = false;
            } else {

                isExist = true;
            }
        } catch (SQLException ex) {
            throw ex;
        }
        return isExist;
    }

    public boolean isComponentLicenseExists(String fileName) throws DataSetException, SQLException {

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
            throw ex;
        }

    }
    public static int getLastInsertId(Connection con) throws SQLException {

        Statement stmt;
        stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT LAST_INSERT_ID();");
        rs.next();
        return rs.getInt("LAST_INSERT_ID()");
    }
    //
    //        LM_LIBRARY libTab = new LM_LIBRARY();
    //        TableDataSet tds;
    //            throws DataSetException, SQLException, ClassNotFoundException {
    //        Record record;
    //        try {
    //            tds = new TableDataSet(con, libTab.table);
    //            record = tds.addRecord();
    //            record.setValue(libTab.LIB_NAME, name).setValue(
    //                    libTab.LIB_FILE_NAME, fileName).setValue(
    //                    libTab.LIB_TYPE, (parent == null) ? ((isBundle) ? "bundle" : "jar") : "jarinbundle")
    //                    .setValue(libTab.LIB_VERSION, version).save(con);
    //        } catch (SQLException ex) {
    //            log.error(ex.getMessage());
    //            return false;
    //        }
    //        return true;
    //    }
    //
    //    public boolean isLibraryExists(String name, String version) throws DataSetException, SQLException,
    //            ClassNotFoundException {
    //
    //        LM_LIBRARY libTable = new LM_LIBRARY();
    //        TableDataSet tds;
    //        try {
    //            tds = new TableDataSet(con, libTable.table);
    //            tds.where(libTable.LIB_NAME + "='" + name + "' AND " + libTable.LIB_VERSION + "='" + version + "'");
    //            tds.fetchRecords();
    //            if (tds.size() == 0) {
    //                return false;
    //            } else {
    //                return true;
    //            }
    //        } catch (SQLException ex) {
    //            log.error(ex.getMessage());
    //            return false;
    //
    //        }

//    public boolean insertLibrary(String name, String fileName, String version, boolean isBundle, Jar parent)
    //            ClassNotFoundException {
    //
    //        int libraryId = selectLibraryId(name, version,type);
    //        boolean isExist = false;
    //        if(libraryId != -1){
    //            isExist =true;
    //        }
    //        return isExist;
    //    }
//    }

//    public boolean isLibraryExists(String name, String version, String type) throws DataSetException, SQLException,
    //
    //        }
    //            return id;
    //            log.error(ex.getMessage());
    //        } catch (SQLException ex) {
    //            return id;
    //            id = record.getValue(libTable.LIB_ID).asInt();
    //            Record record = tds.getRecord(0);
    //            tds.fetchRecords();
    //            tds.where(libTable.LIB_NAME + "='" + name + "' AND " + libTable.LIB_VERSION + "='" + version + "'");
    //            tds = new TableDataSet(con, libTable.table);
    //        try {
    //        int id = 0;
    //        TableDataSet tds;
    //        LM_LIBRARY libTable = new LM_LIBRARY();
    //
    //    public int getLibraryId(String name, String version) throws DataSetException {
    //
    //    }
    //
    //        return licenseKey;
    //        }
    //
    //            log.error(ex.getMessage());
    //        } catch (SQLException ex) {
    //
    //            return licenseKey;
    //            licenseKey = record.getValue(libTable.LICENSE_KEY).toString();
    //            record = tds.getRecord(0);
    //            tds.fetchRecords();
    //            tds.where(libTable.LIB_ID + "=" + Integer.toString(libraryId));
    //            tds = new TableDataSet(con, libTable.table);
    //
    //        try {
    //
    //        String licenseKey = "";
    //        Record record;
    //        TableDataSet tds;
    //        LM_LIBRARY_LICENSE libTable = new LM_LIBRARY_LICENSE();
    //
    //    public String getLicenseKeyFromLibraryLicense(int libraryId) throws DataSetException {
    //
    //    }
    //
    //        return id;
    //        }
    //            log.error(ex.getMessage());
    //        } catch (SQLException ex) {
    //
    //            id = record.getValue(libTable.LIB_ID).asInt();
    //            record = tds.getRecord(0);
    //            tds.fetchRecords();
    //                    libTable.LIB_TYPE + "='" + type + "'");
    //            tds.where(libTable.LIB_NAME + "='" + name + "' AND " + libTable.LIB_VERSION + "='" + version + "'
    // AND " +
    //            tds = new TableDataSet(con, libTable.table);
    //        try {
    //
    //        int id = -1;
    //        Record record;
    //        TableDataSet tds;
    //        LM_LIBRARY libTable = new LM_LIBRARY();
    //
//    public int getActualLibraryId(String name, String version, String type) throws DataSetException {

//    }

//    public int getLibId(String fileName, Jar parent, boolean isBundle) throws DataSetException {
//
//        LM_LIBRARY libtab = new LM_LIBRARY();
//        TableDataSet tds;
//        try {
//
//            tds = new TableDataSet(con, libtab.table);
//            String type;
//            if (parent == null) {
//                type = ((isBundle) ? "bundle" : "jar");
//            } else {
//                type = "jarinbundle";
//            }
//
//            tds.where(libtab.LIB_FILE_NAME + "='" + fileName + "' AND " + libtab.LIB_TYPE + "='" + type + "'");
//            tds.fetchRecords(1);
//            return tds.getRecord(0).getValue(libtab.LIB_ID).asInt();
//
//        } catch (SQLException ex) {
//            return -1;
//        }
//
//    }

}
