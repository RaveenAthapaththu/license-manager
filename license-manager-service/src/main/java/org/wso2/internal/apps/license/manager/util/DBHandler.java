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
package org.wso2.internal.apps.license.manager.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private Connection connection;

    public DBHandler() throws ClassNotFoundException, SQLException {

        String databaseUrl = SystemVariableUtil.getValue(Constants.DATABASE_URL, null);
        String databaseDriver = SystemVariableUtil.getValue(Constants.DATABASE_DRIVER, null);
        String databaseUsername = SystemVariableUtil.getValue(Constants.DATABASE_USERNAME, null);
        String databasePassword = SystemVariableUtil.getValue(Constants.DATABASE_PASSWORD, null);
        Class.forName(databaseDriver);
        connection = DriverManager.getConnection(databaseUrl, databaseUsername, databasePassword);

    }

    private static int getLastInsertId(Connection con) throws SQLException {

        Statement stmt;
        stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT LAST_INSERT_ID();");
        rs.next();
        return rs.getInt("LAST_INSERT_ID()");
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

        connection.close();
    }

    /**
     * Select all the licenses available from LM_LICENSE table.
     *
     * @return json array of licenses
     * @throws SQLException if the sql execution fails
     */
    public JsonArray selectAllLicense() throws SQLException {

        JsonArray resultArray = new JsonArray();
        String query = "SELECT * FROM LM_LICENSE";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            JsonObject licenseJson = new JsonObject();
            licenseJson.addProperty("LICENSE_ID", rs.getInt("LICENSE_ID"));
            licenseJson.addProperty("LICENSE_KEY", rs.getString("LICENSE_KEY"));
            licenseJson.addProperty("LICENSE_NAME", rs.getString("LICENSE_NAME"));
            resultArray.add(licenseJson);
        }
        if (log.isDebugEnabled()) {
            log.debug("Licenses are retrieved from the LM_LICENSE table.");
        }
        return resultArray;
    }

    /**
     * Select license of a given id from LM_LICENSE table.
     *
     * @param id id of the license
     * @return json array of licenses
     * @throws SQLException if the sql execution fails
     */
    public String selectLicenseFromId(int id) throws SQLException {

        String licenseKey = null;

        String query = "SELECT LICENSE_KEY FROM LM_LICENSE WHERE LICENSE_ID=?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, id);
        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            licenseKey = rs.getString("LICENSE_KEY");
        }
        if (log.isDebugEnabled()) {
            log.debug("License for the id " + id + "  is retrieved from the LM_LICENSE table.");
        }
        return licenseKey;
    }

    /**
     * Check for the existence and insert component into LM_COMPONENT table.
     *
     * @param name     name for the component
     * @param fileName name of the jar file
     * @param version  version of the component
     * @throws SQLException if the sql execution fails
     */
    public void insertComponent(String name, String fileName, String version) throws SQLException {

        if (!isComponentExists(fileName)) {
            String insertComponent = "INSERT INTO LM_COMPONENT"
                    + "(COMP_NAME, COMP_FILE_NAME, COMP_KEY, COMP_TYPE,COMP_VERSION) VALUES"
                    + "(?,?,?,?,?)";
            PreparedStatement preparedStatement;
            preparedStatement = connection.prepareStatement(insertComponent);
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, fileName);
            preparedStatement.setString(3, fileName);
            preparedStatement.setString(4, "bundle");
            preparedStatement.setString(5, version);
            preparedStatement.executeUpdate();

            if (log.isDebugEnabled()) {
                log.debug("Successfully inserted the component with the key " + name + " into LM_COMPONENT table.");
            }
        }
    }

    /**
     * Insert a new library.
     *
     * @param name     name for the library
     * @param fileName name of the jar file
     * @param version  version of the library
     * @param type     type of the library
     * @return id for the inserted library
     * @throws SQLException if the sql execution fails
     */
    private int insertLibrary(String name, String fileName, String version, String type) throws SQLException {

        String insertLibrary = "INSERT INTO  LM_LIBRARY"
                + "(LIB_NAME, LIB_FILE_NAME, LIB_TYPE, LIB_VERSION) VALUES"
                + "(?,?,?,?)";
        PreparedStatement preparedStatement;
        preparedStatement = connection.prepareStatement(insertLibrary);
        preparedStatement.setString(1, name);
        preparedStatement.setString(2, fileName);
        preparedStatement.setString(3, type);
        preparedStatement.setString(4, version);
        preparedStatement.executeUpdate();

        if (log.isDebugEnabled()) {
            log.debug("Successfully inserted the component with the key " + name + " into LM_LIBRARY table.");
        }

        return getLastInsertId(connection);
    }

    /**
     * Insert a Product - Component relationship into LM_COMPONENT_PRODUCT table unless already exists.
     *
     * @param compKey   primary key for the component
     * @param productId primary key for the product
     * @throws SQLException if the sql execution fails
     */
    public void insertProductComponent(String compKey, int productId) throws SQLException {

        if (selectProductComponent(compKey, productId) == -1) {

            String insertLibrary = "INSERT INTO  LM_COMPONENT_PRODUCT (COMP_KEY, PRODUCT_ID) VALUES (?,?)";
            PreparedStatement preparedStatement;
            preparedStatement = connection.prepareStatement(insertLibrary);
            preparedStatement.setString(1, compKey);
            preparedStatement.setInt(2, productId);
            preparedStatement.executeUpdate();

            if (log.isDebugEnabled()) {
                log.debug("Successfully inserted the product - component relationship for the product id " + productId +
                        " and component key " + compKey + " into LM_COMPONENT_PRODUCT table.");
            }
        }
    }

    /**
     * Get the id of a product-component relationship from the LM_COMPONENT_TABLE.
     *
     * @param compKey   primary key for the component
     * @param productId primary key for the product
     * @return id of the entry if exists, -1 if not
     * @throws SQLException if the sql execution fails
     */
    private int selectProductComponent(String compKey, int productId) throws SQLException {

        int id = -1;
        String query;

        query = "SELECT PRODUCT_ID FROM LM_COMPONENT_PRODUCT WHERE COMP_KEY=? AND PRODUCT_ID=?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, compKey);
        preparedStatement.setInt(2, productId);
        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            id = rs.getInt("PRODUCT_ID");
        }
        return id;
    }

    /**
     * Insert a Product - Library relationship into LM_LIBRARY_PRODUCT table unless already exists.
     *
     * @param libId     primary key for the library
     * @param productId primary key for the product
     * @throws SQLException if the sql execution fails
     */
    public void insertProductLibrary(int libId, int productId) throws SQLException {

        if (selectProductLibrary(libId, productId) == -1) {
            String insertLibrary = "INSERT INTO  LM_LIBRARY_PRODUCT (LIB_ID, PRODUCT_ID) VALUES (?,?)";
            PreparedStatement preparedStatement;
            preparedStatement = connection.prepareStatement(insertLibrary);
            preparedStatement.setInt(1, libId);
            preparedStatement.setInt(2, productId);
            preparedStatement.executeUpdate();

            if (log.isDebugEnabled()) {
                log.debug("Successfully inserted the product - library relationship for the product id " + productId +
                        " and library id " + libId + " into LM_LIBRARY_PRODUCT table.");
            }
        }
    }

    /**
     * Get the id of a product-library relationship from the LM_LIBRARY_PRODUCT table.
     *
     * @param libId     primary key for the library
     * @param productId primary key for the product
     * @return id of the entry if exists, -1 if not
     * @throws SQLException if the sql execution fails
     */
    private int selectProductLibrary(int libId, int productId) throws SQLException {

        int id = -1;

        String query = "SELECT LIB_ID FROM LM_LIBRARY_PRODUCT WHERE LIB_ID=? AND PRODUCT_ID=?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, libId);
        preparedStatement.setInt(2, productId);
        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            id = rs.getInt("LIB_ID");
        }
        return id;
    }

    /**
     * Insert a Component - Library relationship into LM_COMPONENT_LIBRARY table unless already exists.
     *
     * @param compKey   primary key for the component
     * @param libraryId primary key for the library
     * @throws SQLException if the sql execution fails
     */
    public void insertComponentLibrary(String compKey, int libraryId) throws SQLException {

        if (selectComponentLibrary(compKey, libraryId) == -1) {
            String insertLibrary = "INSERT INTO  LM_COMPONENT_LIBRARY (LIB_ID, COMP_KEY) VALUES (?,?)";
            PreparedStatement preparedStatement;
            preparedStatement = connection.prepareStatement(insertLibrary);
            preparedStatement.setInt(1, libraryId);
            preparedStatement.setString(2, compKey);
            preparedStatement.executeUpdate();

            if (log.isDebugEnabled()) {
                log.debug("Successfully inserted the component - library relationship for the component key " +
                        compKey + " and library id " + libraryId + " into LM_COMPONENT_LIBRARY table.");
            }
        }
    }

    /**
     * Get the id of a component-library relationship from LM_COMPONENT_LIBRARY table.
     *
     * @param compKey   primary key for the component
     * @param libraryId primary key for the library
     * @return id of the entry if exists, -1 if not
     * @throws SQLException if the sql execution fails
     */
    private int selectComponentLibrary(String compKey, int libraryId) throws SQLException {

        int id = -1;
        String query = "SELECT LIB_ID FROM LM_COMPONENT_LIBRARY WHERE LIB_ID=? AND COMP_KEY=?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, libraryId);
        preparedStatement.setString(2, compKey);
        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            id = rs.getInt("LIB_ID");
        }
        return id;
    }

    /**
     * Insert new component- license relationship into the LM_COMPONENT_LICENSE table.
     *
     * @param compKey    primary key to the component
     * @param licenseKey primary key to license
     * @throws SQLException if sql query execution fails
     */
    public void insertComponentLicense(String compKey, String licenseKey) throws SQLException {

        String insertQuery = "INSERT INTO  LM_COMPONENT_LICENSE (COMP_KEY, LICENSE_KEY) VALUES (?,?)";
        PreparedStatement preparedStatement;
        preparedStatement = connection.prepareStatement(insertQuery);
        preparedStatement.setString(1, compKey);
        preparedStatement.setString(2, licenseKey);
        preparedStatement.executeUpdate();

        if (log.isDebugEnabled()) {
            log.debug("Successfully inserted the licenses for the component " + compKey +
                    " into LM_COMPONENT_LICENSE table.");
        }
    }

    /**
     * Insert new library- license relationship into the LM_COMPONENT_LICENSE table.
     *
     * @param licenseKey primary key to the license
     * @param libId      primary key to library
     * @throws SQLException if the sql execution fails
     */
    public void insertLibraryLicense(String licenseKey, int libId) throws SQLException {

        String insertQuery = "INSERT INTO  LM_LIBRARY_LICENSE (LIB_ID, LICENSE_KEY) VALUES (?,?)";
        PreparedStatement preparedStatement;
        preparedStatement = connection.prepareStatement(insertQuery);
        preparedStatement.setInt(1, libId);
        preparedStatement.setString(2, licenseKey);
        preparedStatement.executeUpdate();

        if (log.isDebugEnabled()) {
            log.debug("Successfully inserted the licenses for the library of id " + libId +
                    " into LM_LIBRARY_LICENSE table.");
        }
    }

    /**
     * Gets the id of an existing library.
     *
     * @param name    name of the library
     * @param version version of the library
     * @param type    type of the library
     * @return id
     * @throws SQLException if the sql execution fails
     */
    public int selectLibraryId(String name, String version, String type) throws SQLException {

        int libraryId = -1;
        String query;

        query = "SELECT LIB_ID FROM LM_LIBRARY WHERE LIB_NAME=? AND LIB_VERSION=? AND LIB_TYPE=?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, name);
        preparedStatement.setString(2, version);
        preparedStatement.setString(3, type);
        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            libraryId = rs.getInt("LIB_ID");
        }
        return libraryId;
    }

    /**
     * Insert a product entry to the LM_PRODUCT table.
     *
     * @param product name of the product.
     * @param version version of the product.
     * @return ID of the inserted product.
     * @throws SQLException if the sql query execution fails
     */
    public int insertProduct(String product, String version) throws SQLException {

        String insertProduct = "INSERT INTO LM_PRODUCT (PRODUCT_NAME, PRODUCT_VERSION) VALUES (?,?)";
        PreparedStatement preparedStatement;
        preparedStatement = connection.prepareStatement(insertProduct);
        preparedStatement.setString(1, product);
        preparedStatement.setString(2, version);
        preparedStatement.executeUpdate();
        if (log.isDebugEnabled()) {
            log.debug("Successfully inserted the product " + product + " into LM_PRODUCT table.");
        }
        return getLastInsertId(connection);

    }

    /**
     * Get the id for a given library. If exists, returns the id otherwise insert and returns id.
     *
     * @param name     name for the library jar
     * @param fileName file name of the jar
     * @param version  version of the library
     * @param type     type of the library
     * @return id of the library
     * @throws SQLException if the sql execution fails
     */
    public int getLibraryId(String name, String fileName, String version, String type) throws SQLException {

        int libraryId;
        libraryId = selectLibraryId(name, version, type);
        if (libraryId == -1) {
            libraryId = insertLibrary(name, fileName, version, type);
        }
        return libraryId;
    }

    /**
     * Get the product id when the name and the version is given.
     *
     * @param product name of the product
     * @param version version of the product
     * @return id the product
     * @throws SQLException if the sql execution fails
     */
    private int selectProductId(String product, String version) throws SQLException {

        int productId = -1;

        String query = "SELECT PRODUCT_ID FROM LM_PRODUCT WHERE PRODUCT_NAME=? AND PRODUCT_VERSION=? ";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, product);
        preparedStatement.setString(2, version);
        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            productId = rs.getInt("PRODUCT_ID");
        }
        return productId;
    }

    /**
     * Get the id for a given product. If exists, returns the id otherwise insert and returns id.
     *
     * @param product name for the product
     * @param version version of the product
     * @return id of the product
     * @throws SQLException if the sql execution fails
     */
    public int getProductId(String product, String version) throws SQLException {

        int productId;
        productId = selectProductId(product, version);
        if (productId == -1) {
            productId = insertProduct(product, version);
        }
        return productId;

    }

    /**
     * Check the existence of a given component.
     *
     * @param compKey primary key for the component
     * @return true/false based on the existence
     * @throws SQLException if the sql execution fails
     */
    public boolean isComponentExists(String compKey) throws SQLException {

        int id = -1;
        String query = "SELECT COMP_ID FROM LM_COMPONENT WHERE COMP_KEY=?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, compKey);
        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            id = rs.getInt("COMP_ID");
        }
        return id != -1;
    }

    /**
     * Check whether license exists for a given library from LM_LIBRARY_LICENSE table.
     *
     * @param libraryId primary key of the library
     * @return true of license exists. false otherwise
     * @throws SQLException if sql execution fails
     */
    public boolean isLibraryLicenseExists(int libraryId) throws SQLException {

        boolean isExist = false;
        String query = "SELECT LIB_ID FROM LM_LIBRARY_LICENSE WHERE LIB_ID=?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, libraryId);
        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            isExist = true;
        }
        return isExist;
    }

    /**
     * Check whether license exists for a given component from LM_COMPONENT_LICENSE table.
     *
     * @param compKey primary key of the component
     * @return true of license exists. false otherwise
     * @throws SQLException if sql execution fails
     */
    public boolean isComponentLicenseExists(String compKey) throws SQLException {

        boolean isExist = false;
        String query = "SELECT COMP_KEY FROM LM_COMPONENT_LICENSE WHERE COMP_KEY=?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, compKey);
        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            isExist = true;
        }
        return isExist;
    }

    /**
     * Select licenses for component despite of its version.
     *
     * @param compName name of the component
     * @return license key of any version of the given component
     * @throws SQLException if sql execution fails
     */
    public String getComponentLicenseForAnyVersion(String compName) throws SQLException {

        String licenseKey = "NEW";
        String query = "SELECT LICENSE_KEY FROM LM_COMPONENT_LICENSE WHERE COMP_KEY = (SELECT COMP_KEY FROM " +
                "LM_COMPONENT WHERE COMP_NAME=? LIMIT 1)";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, compName);
        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            licenseKey = rs.getString("LICENSE_KEY");
        }
        return licenseKey;
    }

    /**
     * Select licenses for library despite of its version.
     *
     * @param libraryName name of the library
     * @return license key of any version of the given component
     * @throws SQLException if sql execution fails
     */
    public String getLibraryLicenseForAnyVersion(String libraryName) throws SQLException {

        String licenseKey = "NEW";
        String query = "SELECT LICENSE_KEY FROM LM_LIBRARY_LICENSE WHERE LIB_ID = (SELECT LIB_ID FROM LM_LIBRARY " +
                "WHERE LIB_NAME=? LIMIT 1)";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, libraryName);
        ResultSet rs = preparedStatement.executeQuery();
        while (rs.next()) {
            licenseKey = rs.getString("LICENSE_KEY");
        }
        return licenseKey;
    }
}
