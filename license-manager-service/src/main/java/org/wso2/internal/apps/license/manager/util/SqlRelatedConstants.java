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

/**
 * SQL queries and the constants used when executing the queries.
 */
public class SqlRelatedConstants {

    public static final String INSERT_PRODUCT = "INSERT INTO LM_PRODUCT (PRODUCT_NAME, PRODUCT_VERSION) VALUES (?,?)";
    public static final String INSERT_COMPONENT = "INSERT IGNORE INTO LM_COMPONENT (COMP_NAME, COMP_FILE_NAME, " +
            "COMP_KEY, COMP_TYPE,COMP_VERSION) VALUES (?,?,?,?,?)";
    public static final String INSERT_LIBRARY = "INSERT INTO  LM_LIBRARY (LIB_NAME, LIB_FILE_NAME, LIB_TYPE, " +
            "LIB_VERSION) VALUES (?,?,?,?)";
    public static final String INSERT_INTO_COMPONENT_PRODUCT = "INSERT IGNORE INTO  LM_COMPONENT_PRODUCT (COMP_KEY, " +
            "PRODUCT_ID) VALUES (?,?)";
    public static final String INSERT_INTO_LIBRARY_PRODUCT = "INSERT IGNORE INTO LM_LIBRARY_PRODUCT (LIB_ID, " +
            "PRODUCT_ID) VALUES (?,?)";
    public static final String INSERT_INTO_COMPONENT_LIBRARY = "INSERT IGNORE INTO  LM_COMPONENT_LIBRARY (LIB_ID, " +
            "COMP_KEY) VALUES (?,?)";
    public static final String INSERT_COMPONENT_LICENSE = "INSERT IGNORE INTO  LM_COMPONENT_LICENSE (COMP_KEY, " +
            "LICENSE_KEY) VALUES (?,?)";
    public static final String INSERT_LIBRARY_LICENSE = "INSERT IGNORE INTO  LM_LIBRARY_LICENSE (LIB_ID, LICENSE_KEY)" +
            " VALUES (?,?)";
    public static final String SELECT_ALL_LICENSES = "SELECT * FROM LM_LICENSE";
    public static final String SELECT_PRODUCT = "SELECT PRODUCT_ID FROM LM_PRODUCT WHERE PRODUCT_NAME=? AND " +
            "PRODUCT_VERSION=?";
    public static final String SELECT_LIBRARY = "SELECT LIB_ID FROM LM_LIBRARY WHERE LIB_NAME=? AND LIB_VERSION=? AND" +
            " LIB_TYPE=?";
    public static final String SELECT_COMPONENT = "SELECT COMP_KEY FROM LM_COMPONENT_LICENSE WHERE COMP_KEY=?";
    public static final String SELECT_FROM_PRODUCT_COMPONENT = "SELECT PRODUCT_ID FROM LM_COMPONENT_PRODUCT WHERE " +
            "COMP_KEY=? AND PRODUCT_ID=?";
    public static final String SELECT_LICENSE_FOR_COMP = "SELECT COMP_KEY FROM LM_COMPONENT_LICENSE WHERE COMP_KEY=?";

    public static final String SELECT_LICENSE_FOR_LIB = "SELECT LIB_ID FROM LM_LIBRARY_LICENSE WHERE LIB_ID=?";
    public static final String SELECT_LICENSE_FOR_ANY_COMP = "SELECT LICENSE_KEY FROM LM_COMPONENT_LICENSE WHERE " +
            "COMP_KEY = (SELECT COMP_KEY FROM LM_COMPONENT WHERE COMP_NAME=? LIMIT 1)";
    public static final String SELECT_LICENSE_FOR_ANY_LIB = "SELECT LICENSE_KEY FROM LM_LIBRARY_LICENSE WHERE LIB_ID " +
            "= (SELECT LIB_ID FROM LM_LIBRARY WHERE LIB_NAME=? LIMIT 1)";
    public static final String SELECT_LICENSE_FOR_KEY = "SELECT * FROM LM_LICENSE WHERE LICENSE_KEY=?";

    public static final String PRIMARY_KEY_LIBRARY = "LIB_ID";
    public static final String PRIMARY_KEY_PRODUCT = "PRODUCT_ID";
    public static final String PRIMARY_KEY_LICENSE = "LICENSE_KEY";
    public static final String LICENSE_ID = "LICENSE_ID";
    public static final String LICENSE_NAME = "LICENSE_NAME";
    public static final String LICENSE_URL = "LICENSE_URL";
    public static final String COMPONENT_KEY = "COMP_KEY";
    public static final String COMPONENT_TYPE = "COMP_TYPE";

}
