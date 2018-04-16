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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.wso2.internal.apps.license.manager.impl.main;

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
 * @author pubudu
 */
public class LicenseFileGenerator {

    Connection con;
    Logger logger;

    String file = "\n" +
            "This product is licensed by WSO2 Inc. under Apache License 2.0. The license\n" +
            "can be downloaded from the following locations:\n" +
            "\thttp://www.apache.org/licenses/LICENSE-2.0.html\n" +
            "\thttp://www.apache.org/licenses/LICENSE-2.0.txt\n\n" +

            "This product also contains software under different licenses. This table below\n" +
            "all the contained libraries (jar files) and the license under which they are \n" +
            "provided to you.\n\n" +

            "At the bottom of this file is a table that shows what each license indicated\n" +
            "below is and where the actual text of the license can be found.\n\n";

    public LicenseFileGenerator(String driver, String url, String uname,
                                String password) throws ClassNotFoundException,
            SQLException {

        Class.forName(driver);
        this.con = DriverManager.getConnection(url, uname, password);
//        this.logger = log;
    }

    public void generateLicenceFile(String product, String version, String packPath) throws SQLException {

        Statement stmt;
        stmt = con.createStatement();
        Set<String> keys = new HashSet<String>();
        ResultSet rs = stmt.executeQuery("SELECT * FROM " +
                "(SELECT " +
                "   LM_PRODUCT.PRODUCT_NAME" +
                "   ,LM_PRODUCT.PRODUCT_VERSION" +
                "   ,LM_COMPONENT.COMP_ID" +
                "   ,LM_COMPONENT.COMP_KEY" +
                "   ,LM_COMPONENT.COMP_TYPE" +
                "   ,LM_COMPONENT_LICENSE.LICENSE_KEY " +
                "   FROM " +
                "   (((LM_PRODUCT INNER JOIN LM_COMPONENT_PRODUCT ON LM_PRODUCT.PRODUCT_ID=LM_COMPONENT_PRODUCT.PRODUCT_ID)" +
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
                "FROM ((((LM_PRODUCT INNER JOIN LM_COMPONENT_PRODUCT ON LM_PRODUCT.PRODUCT_ID=LM_COMPONENT_PRODUCT.PRODUCT_ID)" +
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
                "FROM (((LM_PRODUCT INNER JOIN LM_LIBRARY_PRODUCT ON LM_PRODUCT.PRODUCT_ID=LM_LIBRARY_PRODUCT.PRODUCT_ID)" +
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
                "FROM (((((LM_PRODUCT INNER JOIN LM_LIBRARY_PRODUCT ON LM_PRODUCT.PRODUCT_ID=LM_LIBRARY_PRODUCT.PRODUCT_ID)" +
                "INNER JOIN LM_LIBRARY ON LM_LIBRARY.LIB_ID=LM_LIBRARY_PRODUCT.LIB_ID)" +
                "INNER JOIN LM_COMPONENT_LIBRARY ON LM_LIBRARY.LIB_FILE_NAME=LM_COMPONENT_LIBRARY.COMP_KEY)" +
                "INNER JOIN LM_LIBRARY AS LM_LIBRARY2 ON LM_COMPONENT_LIBRARY.LIB_ID=LM_LIBRARY2.LIB_ID)" +
                "INNER JOIN LM_LIBRARY_LICENSE ON LM_LIBRARY_LICENSE.LIB_ID=LM_COMPONENT_LIBRARY.LIB_ID))AS BS " +
                "WHERE PRODUCT_NAME='"+product+"' AND PRODUCT_VERSION='"+version+"' ORDER BY COMP_KEY");

        String formatString = String.format("%-80s%-15s%-10s\n", "Name", "Type", "License");
        file += formatString;
        file += "---------------------------------------------------------------------------------------------------------\n";
        while (rs.next()) {
            formatString = String.format("%-80s%-15s%-10s%-10s\n",
                    rs.getString("COMP_KEY"),
                    rs.getString("COMP_TYPE"),
                    rs.getString("LICENSE_KEY"),
                    rs.getInt("COMP_ID") + "");
            file += formatString;
            keys.add(rs.getString("LICENSE_KEY"));
        }
        file += "\n\n\nThe license types used by the above libraries and their information is given below:\n\n";
        for (Iterator<String> i = keys.iterator(); i.hasNext(); ) {
            rs = stmt.executeQuery("SELECT * FROM LM_LICENSE WHERE LICENSE_KEY='" + i.next() + "'");
            rs.next();
            formatString = String.format("%-15s%s\n%-15s%s\n", rs.getString("LICENSE_KEY"), rs.getString
                    ("LICENSE_NAME"), "", rs.getString("LICENSE_URL"));
            file += formatString;
        }
//        System.out.println(file);
        try {
            FileWriter fw = new FileWriter(packPath + File.separator + "LICENSE(" + product + "-" + version + ").TXT");
            fw.write(file);
            fw.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            Logger.getLogger(LicenseFileGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
