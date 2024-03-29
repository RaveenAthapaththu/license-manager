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

import com.google.gson.JsonArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.internal.apps.license.manager.connector.DatabaseConnectionPool;
import org.wso2.internal.apps.license.manager.util.JsonUtils;
import org.wso2.internal.apps.license.manager.util.SqlRelatedConstants;

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Handles the data transferring operations related to licenses.
 */
public class LicensesDataHandler implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(LicensesDataHandler.class);
    private Connection connection;

    public LicensesDataHandler() throws SQLException {

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
     * Select all the licenses available from LM_LICENSE table.
     *
     * @return the result set of licenses
     * @throws SQLException if the sql execution fails
     */
    public JsonArray selectAllLicense() throws SQLException {

        String query = SqlRelatedConstants.SELECT_ALL_LICENSES;
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                JsonArray licenseArray = JsonUtils.createJsonArrayFromLicenseResultSet(resultSet);
                if (log.isDebugEnabled()) {
                    log.debug("Licenses are retrieved from the LM_LICENSE table.");
                }
                return licenseArray;
            }
        }
    }
}
