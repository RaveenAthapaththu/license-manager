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
import org.wso2.internal.apps.license.manager.util.SqlRelatedConstants;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Child class which contains data transferring operations specifically related to defining licenses for the jars.
 */
public class NewLicenseOfJarDataHandler extends JarFileInfoDataHandler {

    private static final Logger log = LoggerFactory.getLogger(NewLicenseOfJarDataHandler.class);

    public NewLicenseOfJarDataHandler() throws SQLException {

        super();
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

        String insertLibrary = SqlRelatedConstants.INSERT_LIBRARY;
        PreparedStatement preparedStatement;
        preparedStatement = connection.prepareStatement(insertLibrary, Statement.RETURN_GENERATED_KEYS);
        preparedStatement.setString(1, name);
        preparedStatement.setString(2, fileName);
        preparedStatement.setString(3, type);
        preparedStatement.setString(4, version);
        preparedStatement.executeUpdate();
        ResultSet resultSet = preparedStatement.getGeneratedKeys();
        int id = -1;
        while (resultSet.next()) {
            id = resultSet.getInt("GENERATED_KEY");
        }
        if (log.isDebugEnabled()) {
            log.debug("Successfully inserted the component with the key " + name + " into LM_LIBRARY table.");
        }

        return id;
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
            String insertComponent = SqlRelatedConstants.INSERT_COMPONENT;
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
     * Insert new component- license relationship into the LM_COMPONENT_LICENSE table.
     *
     * @param compKey    primary key to the component
     * @param licenseKey primary key to license
     * @throws SQLException if sql query execution fails
     */
    public void insertComponentLicense(String compKey, String licenseKey) throws SQLException {

        String insertQuery = SqlRelatedConstants.INSERT_COMPONENT_LICENSE;
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

        String insertQuery = SqlRelatedConstants.INSERT_LIBRARY_LICENSE;
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
}
