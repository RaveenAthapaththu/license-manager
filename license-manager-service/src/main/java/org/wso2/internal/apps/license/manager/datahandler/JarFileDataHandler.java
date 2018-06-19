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

package org.wso2.internal.apps.license.manager.datahandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.internal.apps.license.manager.connector.DatabaseConnectionPool;
import org.wso2.internal.apps.license.manager.util.SqlRelatedConstants;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Super class which handles the data transfers related to jar files with the database.
 */
public class JarFileDataHandler implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(LicenseExistingJarFileDataHandler.class);
    Connection connection;

    JarFileDataHandler() throws SQLException {

        DatabaseConnectionPool databaseConnectionPool = DatabaseConnectionPool.getDbConnectionPool();
        connection = databaseConnectionPool.getConnection();
    }

    @Override
    public void close() throws IOException {

        try {
            connection.close();
        } catch (SQLException e) {
            throw new IOException();
        }
    }

    /**
     * Insert a Product - Component relationship into LM_COMPONENT_PRODUCT table unless already exists.
     *
     * @param compKey   primary key for the component
     * @param productId primary key for the product
     * @throws SQLException if the sql execution fails
     */
    public void insertProductComponent(String compKey, int productId) throws SQLException {

        String insertProductComponent = SqlRelatedConstants.INSERT_INTO_COMPONENT_PRODUCT;
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertProductComponent)) {

            preparedStatement.setString(1, compKey);
            preparedStatement.setInt(2, productId);
            preparedStatement.executeUpdate();

            if (log.isDebugEnabled()) {
                log.debug("Successfully inserted the product - component relationship for the product id " +
                        productId +
                        " and component key " + compKey + " into LM_COMPONENT_PRODUCT table.");
            }
        }
    }

    /**
     * Insert a Product - Library relationship into LM_LIBRARY_PRODUCT table unless already exists.
     *
     * @param libId     primary key for the library
     * @param productId primary key for the product
     * @throws SQLException if the sql execution fails
     */
    public void insertProductLibrary(int libId, int productId) throws SQLException {

        String insertProductLibrary = SqlRelatedConstants.INSERT_INTO_LIBRARY_PRODUCT;
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertProductLibrary)) {
            preparedStatement.setInt(1, libId);
            preparedStatement.setInt(2, productId);
            preparedStatement.executeUpdate();
            if (log.isDebugEnabled()) {
                log.debug("Successfully inserted the product - library relationship for the product id " +
                        productId +
                        " and library id " + libId + " into LM_LIBRARY_PRODUCT table.");
            }
        }
    }

    /**
     * Insert a Component - Library relationship into LM_COMPONENT_LIBRARY table unless already exists.
     *
     * @param compKey   primary key for the component
     * @param libraryId primary key for the library
     * @throws SQLException if the sql execution fails
     */
    public void insertComponentLibrary(String compKey, int libraryId) throws SQLException {

        String insertComponentLibrary = SqlRelatedConstants.INSERT_INTO_COMPONENT_LIBRARY;
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertComponentLibrary)) {
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
        String query = SqlRelatedConstants.SELECT_LIBRARY;
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, version);
            preparedStatement.setString(3, type);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    libraryId = rs.getInt(SqlRelatedConstants.PRIMARY_KEY_LIBRARY);
                }
                return libraryId;
            }
        }
    }

    /**
     * Check the existence of a given component.
     *
     * @param compKey primary key for the component
     * @return true/false based on the existence
     * @throws SQLException if the sql execution fails
     */
    public boolean isComponentExists(String compKey) throws SQLException {

        boolean isExist = false;
        String query = SqlRelatedConstants.SELECT_COMPONENT;
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, compKey);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    isExist = true;
                }
                return isExist;
            }
        }
    }
}
