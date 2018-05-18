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

package org.wso2.internal.apps.license.manager.connector;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.wso2.internal.apps.license.manager.util.Constants;
import org.wso2.msf4j.util.SystemVariableUtil;

import javax.sql.DataSource;

/**
 * A singleton class to create a database connection pool of 5 connections.
 */
public class DatabaseConnectionPool {

    private static DataSource dataSource = null;
    private static DatabaseConnectionPool databaseConnectionPool = null;

    private DatabaseConnectionPool() {

        String databaseUrl = SystemVariableUtil.getValue(Constants.DATABASE_URL, null);
        String databaseUsername = SystemVariableUtil.getValue(Constants.DATABASE_USERNAME, null);
        String databasePassword = SystemVariableUtil.getValue(Constants.DATABASE_PASSWORD, null);
        int maximumNumberOfConnections = Integer.valueOf(SystemVariableUtil.getValue(Constants
                .DATABASE_CONNECTIONS_MAX_NUMBER, "3"));
        GenericObjectPool connectionPool = new GenericObjectPool();
        connectionPool.setMaxActive(maximumNumberOfConnections);
        ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(databaseUrl, databaseUsername,
                databasePassword);
        PoolableConnectionFactory pcf = new PoolableConnectionFactory(connectionFactory, connectionPool, null, null,
                false, true);
        dataSource = new PoolingDataSource(connectionPool);

    }

    public static synchronized DatabaseConnectionPool getDbConnectionPool() {

        if (databaseConnectionPool == null) {
            databaseConnectionPool = new DatabaseConnectionPool();
        }
        return databaseConnectionPool;

    }

    public DataSource getDataSource() {

        return dataSource;
    }

}






