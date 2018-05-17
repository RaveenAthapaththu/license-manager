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

import org.wso2.internal.apps.license.manager.connector.DatabaseConnectionPool;
import org.wso2.internal.apps.license.manager.util.SqlRelatedConstants;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Handle the data transfer operations when retrieving all the components in a pack while generating the license text.
 */
public class LicenseTextDataHandler {

    private Connection connection;

    public LicenseTextDataHandler() throws SQLException {

        DatabaseConnectionPool databaseConnectionPool = DatabaseConnectionPool.getDbConnectionPool();
        connection = databaseConnectionPool.getDataSource().getConnection();
    }

    public ResultSet getLicenseForAllJars(String productName, String version) throws SQLException {

        String query = "SELECT * FROM " +
                "(SELECT " +
                "LM_PRODUCT.PRODUCT_NAME, " +
                "LM_PRODUCT.PRODUCT_VERSION, " +
                "LM_COMPONENT.COMP_ID, " +
                "LM_COMPONENT.COMP_KEY," +
                "LM_COMPONENT.COMP_TYPE, " +
                "LM_COMPONENT_LICENSE.LICENSE_KEY FROM " +
                "(((LM_PRODUCT INNER JOIN LM_COMPONENT_PRODUCT ON LM_PRODUCT.PRODUCT_ID=LM_COMPONENT_PRODUCT" +
                ".PRODUCT_ID) " +
                "INNER JOIN LM_COMPONENT ON LM_COMPONENT.COMP_KEY=LM_COMPONENT_PRODUCT.COMP_KEY) " +
                "INNER JOIN LM_COMPONENT_LICENSE ON LM_COMPONENT_LICENSE.COMP_KEY=LM_COMPONENT.COMP_KEY) " +
                "UNION " +
                "SELECT " +
                "LM_PRODUCT.PRODUCT_NAME," +
                "LM_PRODUCT.PRODUCT_VERSION," +
                "LM_LIBRARY.LIB_ID," +
                "LM_LIBRARY.LIB_FILE_NAME," +
                "LM_LIBRARY.LIB_TYPE," +
                "LM_LIBRARY_LICENSE.LICENSE_KEY " +
                "FROM ((((LM_PRODUCT INNER JOIN LM_COMPONENT_PRODUCT " +
                "ON LM_PRODUCT.PRODUCT_ID=LM_COMPONENT_PRODUCT.PRODUCT_ID) " +
                "INNER JOIN LM_COMPONENT_LIBRARY ON LM_COMPONENT_PRODUCT.COMP_KEY=LM_COMPONENT_LIBRARY.COMP_KEY)" +
                "INNER JOIN LM_LIBRARY ON LM_COMPONENT_LIBRARY.LIB_ID=LM_LIBRARY.LIB_ID)" +
                "INNER JOIN LM_LIBRARY_LICENSE ON LM_LIBRARY_LICENSE.LIB_ID=LM_LIBRARY.LIB_ID)" +
                "UNION " +
                "SELECT " +
                "LM_PRODUCT.PRODUCT_NAME, " +
                "LM_PRODUCT.PRODUCT_VERSION, " +
                "LM_LIBRARY.LIB_ID, " +
                "LM_LIBRARY.LIB_FILE_NAME, " +
                "LM_LIBRARY.LIB_TYPE, " +
                "LM_LIBRARY_LICENSE.LICENSE_KEY FROM " +
                "(((LM_PRODUCT INNER JOIN LM_LIBRARY_PRODUCT ON LM_PRODUCT.PRODUCT_ID=LM_LIBRARY_PRODUCT.PRODUCT_ID)" +
                "INNER JOIN LM_LIBRARY ON LM_LIBRARY.LIB_ID=LM_LIBRARY_PRODUCT.LIB_ID) " +
                "INNER JOIN LM_LIBRARY_LICENSE ON LM_LIBRARY_LICENSE.LIB_ID=LM_LIBRARY.LIB_ID) " +
                "UNION " +
                "SELECT " +
                "LM_PRODUCT.PRODUCT_NAME, " +
                "LM_PRODUCT.PRODUCT_VERSION, " +
                "LM_LIBRARY.LIB_ID, " +
                "LM_LIBRARY2.LIB_FILE_NAME," +
                "LM_LIBRARY2.LIB_TYPE, " +
                "LM_LIBRARY_LICENSE.LICENSE_KEY FROM " +
                "(((((LM_PRODUCT INNER JOIN LM_LIBRARY_PRODUCT ON LM_PRODUCT.PRODUCT_ID=LM_LIBRARY_PRODUCT" +
                ".PRODUCT_ID)" +
                "INNER JOIN LM_LIBRARY ON LM_LIBRARY.LIB_ID=LM_LIBRARY_PRODUCT.LIB_ID) " +
                "INNER JOIN LM_COMPONENT_LIBRARY ON LM_LIBRARY.LIB_FILE_NAME=LM_COMPONENT_LIBRARY.COMP_KEY) " +
                "INNER JOIN LM_LIBRARY AS LM_LIBRARY2 ON LM_COMPONENT_LIBRARY.LIB_ID=LM_LIBRARY2.LIB_ID) " +
                "INNER JOIN LM_LIBRARY_LICENSE ON LM_LIBRARY_LICENSE.LIB_ID=LM_COMPONENT_LIBRARY.LIB_ID))AS BS " +
                "WHERE PRODUCT_NAME=? AND PRODUCT_VERSION=? ORDER BY COMP_KEY";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, productName);
        preparedStatement.setString(2, version);
        ResultSet resultSet = preparedStatement.executeQuery();
        return resultSet;
    }

    public ResultSet getLicenseDescriptions(String key) throws SQLException {

        String query = SqlRelatedConstants.SELECT_LICENSE_FOR_KEY;
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, key);
        ResultSet resultSet = preparedStatement.executeQuery();
        return resultSet;
    }
}
