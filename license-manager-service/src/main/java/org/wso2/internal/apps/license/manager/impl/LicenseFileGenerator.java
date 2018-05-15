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

package org.wso2.internal.apps.license.manager.impl;

import org.wso2.internal.apps.license.manager.exception.LicenseManagerDataException;
import org.wso2.internal.apps.license.manager.util.Constants;
import org.wso2.msf4j.util.SystemVariableUtil;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generates the license text for a given product and version.
 */
public class LicenseFileGenerator {

    private Connection con;
    private String file = "\n" +
            "This product is licensed by WSO2 Inc. under Apache License 2.0. The license\n" +
            "can be downloaded from the following locations:\n" +
            "\thttp://www.apache.org/licenses/LICENSE-2.0.html\n" +
            "\thttp://www.apache.org/licenses/LICENSE-2.0.txt\n\n" +

            "This product also contains software under different licenses. This table below\n" +
            "all the contained libraries (jar files) and the license under which they are \n" +
            "provided to you.\n\n" +

            "At the bottom of this file is a table that shows what each license indicated\n" +
            "below is and where the actual text of the license can be found.\n\n";

    public LicenseFileGenerator() throws LicenseManagerDataException {

        String databaseUrl = SystemVariableUtil.getValue(Constants.DATABASE_URL, null);
        String databaseDriver = SystemVariableUtil.getValue(Constants.DATABASE_DRIVER, null);
        String databaseUsername = SystemVariableUtil.getValue(Constants.DATABASE_USERNAME, null);
        String databasePassword = SystemVariableUtil.getValue(Constants.DATABASE_PASSWORD, null);
        try {
            Class.forName(databaseDriver);
            this.con = DriverManager.getConnection(databaseUrl, databaseUsername, databasePassword);
        } catch (ClassNotFoundException | SQLException e) {
            throw new LicenseManagerDataException("Failed to connect with the database.",e);
        }
    }

    public void generateLicenceFile(String product, String version, String packPath) throws LicenseManagerDataException, IOException {

        try {
            Statement statement = con.createStatement();
             ResultSet rs = statement.executeQuery("SELECT * FROM " +
                    "(SELECT " +
                    "   LM_PRODUCT.PRODUCT_NAME" +
                    "   ,LM_PRODUCT.PRODUCT_VERSION" +
                    "   ,LM_COMPONENT.COMP_ID" +
                    "   ,LM_COMPONENT.COMP_KEY" +
                    "   ,LM_COMPONENT.COMP_TYPE" +
                    "   ,LM_COMPONENT_LICENSE.LICENSE_KEY " +
                    "   FROM " +
                    "   (((LM_PRODUCT INNER JOIN LM_COMPONENT_PRODUCT ON LM_PRODUCT.PRODUCT_ID=LM_COMPONENT_PRODUCT" +
                    ".PRODUCT_ID)" +
                    "INNER JOIN " +
                    "   LM_COMPONENT ON LM_COMPONENT.COMP_KEY=LM_COMPONENT_PRODUCT.COMP_KEY)" +
                    "INNER JOIN LM_COMPONENT_LICENSE ON LM_COMPONENT_LICENSE.COMP_KEY=LM_COMPONENT.COMP_KEY)" +
                    "UNION " +
                    "SELECT " +
                    "   LM_PRODUCT.PRODUCT_NAME," +
                    "   LM_PRODUCT.PRODUCT_VERSION," +
                    "   LM_LIBRARY.LIB_ID," +
                    "   LM_LIBRARY.LIB_FILE_NAME," +
                    "   LM_LIBRARY.LIB_TYPE," +
                    "   LM_LIBRARY_LICENSE.LICENSE_KEY " +
                    "FROM ((((LM_PRODUCT INNER JOIN LM_COMPONENT_PRODUCT ON LM_PRODUCT.PRODUCT_ID=LM_COMPONENT_PRODUCT" +
                    ".PRODUCT_ID)" +
                    "   INNER JOIN LM_COMPONENT_LIBRARY ON LM_COMPONENT_PRODUCT.COMP_KEY=LM_COMPONENT_LIBRARY.COMP_KEY)" +
                    "INNER JOIN LM_LIBRARY ON LM_COMPONENT_LIBRARY.LIB_ID=LM_LIBRARY.LIB_ID)" +
                    "INNER JOIN LM_LIBRARY_LICENSE ON LM_LIBRARY_LICENSE.LIB_ID=LM_LIBRARY.LIB_ID)" +
                    "UNION " +
                    "SELECT " +
                    "   LM_PRODUCT.PRODUCT_NAME," +
                    "   LM_PRODUCT.PRODUCT_VERSION," +
                    "   LM_LIBRARY.LIB_ID," +
                    "   LM_LIBRARY.LIB_FILE_NAME," +
                    "   LM_LIBRARY.LIB_TYPE," +
                    "   LM_LIBRARY_LICENSE.LICENSE_KEY " +
                    "FROM (((LM_PRODUCT INNER JOIN LM_LIBRARY_PRODUCT ON LM_PRODUCT.PRODUCT_ID=LM_LIBRARY_PRODUCT" +
                    ".PRODUCT_ID)" +
                    "INNER JOIN LM_LIBRARY ON LM_LIBRARY.LIB_ID=LM_LIBRARY_PRODUCT.LIB_ID)" +
                    "INNER JOIN LM_LIBRARY_LICENSE ON LM_LIBRARY_LICENSE.LIB_ID=LM_LIBRARY.LIB_ID)" +
                    "UNION " +
                    "SELECT " +
                    "   LM_PRODUCT.PRODUCT_NAME," +
                    "   LM_PRODUCT.PRODUCT_VERSION," +
                    "   LM_LIBRARY.LIB_ID," +
                    "   LM_LIBRARY2.LIB_FILE_NAME," +
                    "   LM_LIBRARY2.LIB_TYPE," +
                    "   LM_LIBRARY_LICENSE.LICENSE_KEY " +
                    "FROM (((((LM_PRODUCT INNER JOIN LM_LIBRARY_PRODUCT ON LM_PRODUCT.PRODUCT_ID=LM_LIBRARY_PRODUCT" +
                    ".PRODUCT_ID)" +
                    "INNER JOIN LM_LIBRARY ON LM_LIBRARY.LIB_ID=LM_LIBRARY_PRODUCT.LIB_ID)" +
                    "INNER JOIN LM_COMPONENT_LIBRARY ON LM_LIBRARY.LIB_FILE_NAME=LM_COMPONENT_LIBRARY.COMP_KEY)" +
                    "INNER JOIN LM_LIBRARY AS LM_LIBRARY2 ON LM_COMPONENT_LIBRARY.LIB_ID=LM_LIBRARY2.LIB_ID)" +
                    "INNER JOIN LM_LIBRARY_LICENSE ON LM_LIBRARY_LICENSE.LIB_ID=LM_COMPONENT_LIBRARY.LIB_ID))AS BS " +
                    "WHERE PRODUCT_NAME='" + product + "' AND PRODUCT_VERSION='" + version + "' ORDER BY COMP_KEY");

            Set<String> keys = new HashSet<String>();

            String formatString = String.format("%-80s%-15s%-10s\n", "Name", "Type", "License");
            file += formatString;
            file += "---------------------------------------------------------------------------------------------------\n";
            while (rs.next()) {
                formatString = String.format("%-80s%-15s%-10s\n",
                        rs.getString("COMP_KEY"),
                        rs.getString("COMP_TYPE"),
                        rs.getString("LICENSE_KEY") + "");
                file += formatString;
                keys.add(rs.getString("LICENSE_KEY"));
            }
            file += "\n\n\nThe license types used by the above libraries and their information is given below:\n\n";
            for (String key : keys) {
                rs = statement.executeQuery("SELECT * FROM LM_LICENSE WHERE LICENSE_KEY='" + key + "'");
                rs.next();
                formatString = String.format("%-15s%s\n%-15s%s\n", rs.getString("LICENSE_KEY"), rs.getString
                        ("LICENSE_NAME"), "", rs.getString("LICENSE_URL"));
                file += formatString;
            }
            FileWriter fw =
                    new FileWriter(packPath + File.separator + "LICENSE(" + product + "-" + version + ").TXT");
            fw.write(file);
            fw.close();
        } catch (SQLException e) {
            throw new LicenseManagerDataException("Failed to retrieve licenses from database.",e);
        }
    }

}
