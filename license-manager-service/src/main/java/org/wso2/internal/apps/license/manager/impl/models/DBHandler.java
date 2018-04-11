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
 * TODO: Class level comments
 */
public class DBHandler {

    private static final Logger log = LoggerFactory.getLogger(MicroservicesRunner.class);

    private static Connection initiateConnection() throws ClassNotFoundException, SQLException {

        String databaseUrl = SystemVariableUtil.getValue(Constants.DATABASE_URL, null);
        String databaseDriver = SystemVariableUtil.getValue(Constants.DATABASE_DRIVER, null);
        String databaseUsername = SystemVariableUtil.getValue(Constants.DATABASE_USERNAME, null);
        String databasePassword = SystemVariableUtil.getValue(Constants.DATABASE_PASSWORD, null);
        Class.forName(databaseDriver);
        return DriverManager.getConnection(databaseUrl, databaseUsername, databasePassword);

    }

    private static void closeConection(Connection connection) throws SQLException {

        connection.close();
    }

    public static JsonArray selectAllLicense() throws SQLException, DataSetException, ClassNotFoundException {

        Connection con = initiateConnection();
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
        } finally {
            closeConection(con);
        }
        return resultArray;
    }

    public static String selectLicenseFromId(int id) throws SQLException, ClassNotFoundException {

        Connection con = initiateConnection();
        LM_LICENSE licenseTable = new LM_LICENSE();
        TableDataSet tds;
        Record rec;

        try {
            tds = new TableDataSet(con, licenseTable.table);
            tds.where(licenseTable.LICENSE_ID + "=" + Integer.toString(id));
            tds.fetchRecords(1);
            rec = tds.getRecord(0);
            String licenseKey = rec.getValue(licenseTable.LICENSE_KEY).toString();
            return licenseKey;

        } catch (SQLException ex) {
            log.error("selectLicenseFromId(SQLException) " + ex.getMessage());

        } catch (DataSetException ex) {
            log.error("selectLicenseFromId(DataSetException) " + ex.getMessage());
        } finally {
            closeConection(con);
        }
        return "";
    }

    public static void insertComponent(String name, String fileName, String version) throws SQLException,
            ClassNotFoundException {

        Connection con = initiateConnection();

        String insertComponent = "INSERT INTO LM_COMPONENT"
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
//            if (ex.getErrorCode() != Constants.DUPLICATE_ENTRY_ERROR_CODE) {
//                throw ex;
//            }
        } finally {
            closeConection(con);
        }

    }

    private static int insertLibrary(String name, String fileName, String version, String type) throws DataSetException,
            SQLException, ClassNotFoundException {

        Connection con = initiateConnection();

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
//            if (ex.getErrorCode() != Constants.DUPLICATE_ENTRY_ERROR_CODE) {
//                throw ex;
//            }
        } finally {
            closeConection(con);
        }
        return -1;
    }

    public static void insertProductComponent(String compKey, int productId) throws DataSetException, SQLException,
            ClassNotFoundException {

        Connection con = initiateConnection();

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
//            if (ex.getErrorCode() != Constants.DUPLICATE_ENTRY_ERROR_CODE) {
//                throw ex;
//            }
        } finally {
            closeConection(con);
        }
    }

    public static void insertProductLibrary(int libId, int productId) throws DataSetException, SQLException,
            ClassNotFoundException {

        Connection con = initiateConnection();

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
//            if (ex.getErrorCode() != Constants.DUPLICATE_ENTRY_ERROR_CODE) {
//                throw ex;
//            }
        } finally {
            closeConection(con);
        }
    }

    public static void insertComponentLibrary(String component, int libraryId) throws DataSetException, SQLException,
            ClassNotFoundException {

        Connection con = initiateConnection();

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
//            if (ex.getErrorCode() != Constants.DUPLICATE_ENTRY_ERROR_CODE) {
//                throw ex;
//            }
        } finally {
            closeConection(con);
        }
    }

    public static void insertComponentLicense(String compKey, String licenseKey) throws DataSetException, SQLException,
            ClassNotFoundException {

        Connection con = initiateConnection();

        LM_COMPONENT_LICENSE complicTab = new LM_COMPONENT_LICENSE();
        TableDataSet tds;
        Record record;
        try {

            tds = new TableDataSet(con, complicTab.table);
            record = tds.addRecord();
            record.setValue(complicTab.COMP_KEY, compKey).setValue(
                    complicTab.LICENSE_KEY, licenseKey).save();
        } catch (SQLException ex) {
            log.error(ex.getMessage());
        }
        finally {
            closeConection(con);
        }
    }

    public static void insertLibraryLicense(String licenseKey, String libId) throws DataSetException, SQLException,
            ClassNotFoundException {

        Connection con = initiateConnection();

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
            log.error("insertLibraryLicense(SQLException) " + ex.getMessage());
        }
        finally {
            closeConection(con);
        }
    }

    private static int selectLibraryId(String name, String version, String type) throws SQLException, ClassNotFoundException {

        Connection con = initiateConnection();

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
        closeConection(con);
        return libraryId;
    }

    public static int getLibraryId(String name, String fileName, String version, String type) throws SQLException,
            ClassNotFoundException {

        int libraryId = -1;
        libraryId = selectLibraryId(name, version, type);
        if (libraryId == -1) {
            try {
                libraryId = insertLibrary(name, fileName, version, type);
            } catch (DataSetException e) {
                e.printStackTrace();
            }
        }
        return libraryId;
    }

}
