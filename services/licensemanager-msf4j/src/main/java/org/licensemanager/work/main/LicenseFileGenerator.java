/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.licensemanager.work.main;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.MicroservicesRunner;

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

/**
 *
 * @author pubudu
 */
public class LicenseFileGenerator {
    Connection con;
    private static final Logger log = LoggerFactory.getLogger(MicroservicesRunner.class);
    String file = "\n"+
"This product is licensed by WSO2 Inc. under Apache License 2.0. The license\n"+
"can be downloaded from the following locations:\n"+
        "\thttp://www.apache.org/licenses/LICENSE-2.0.html\n"+
        "\thttp://www.apache.org/licenses/LICENSE-2.0.txt\n\n"+

"This product also contains software under different licenses. This table below\n"+
"all the contained libraries (jar files) and the license under which they are \n"+
"provided to you.\n\n"+

"At the bottom of this file is a table that shows what each license indicated\n"+
"below is and where the actual text of the license can be found.\n\n";
    
     
    public LicenseFileGenerator(String driver, String url, String uname,
            String password) throws ClassNotFoundException,
            SQLException {
        Class.forName(driver);
        this.con = DriverManager.getConnection(url, uname, password);
    }

    public void generateLicenceFile(String product,String version,String path) throws SQLException{
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


        String formatString=String.format("%-80s%-15s%-10s\n","Name","Type","License");
        file+=formatString;
        int ij = 0;
        file+="---------------------------------------------------------------------------------------------------------\n";
        while(rs.next()){
            ij += 1;
            formatString = String.format("%-80s%-15s%-10s%-10s\n",
                    rs.getString("COMP_KEY"),
                    rs.getString("COMP_TYPE"),
                    rs.getString("LICENSE_KEY"),
                    rs.getInt("COMP_ID")+"");
            file+=formatString;
            keys.add(rs.getString("LICENSE_KEY"));
        }
        String nextValue = "";
        file+="\n\n\nThe license types used by the above libraries and their information is given below:\n\n";
        for(Iterator<String> i = keys.iterator();i.hasNext();){
            nextValue = i.next();

            rs = stmt.executeQuery("SELECT * FROM LM_LICENSE WHERE LICENSE_KEY='"+ nextValue +"'");
            rs.next();
            formatString = String.format("%-15s%s\n%-15s%s\n",rs.getString("LICENSE_KEY"),rs.getString("LICENSE_NAME"),"",rs.getString("LICENSE_URL"));
            file+=formatString;
        }
        try {
            path = path + "LICENSE("+product+"-"+version+").TXT";
            FileWriter fw = new FileWriter(path);
            fw.write(file);
            fw.close();
        } catch (IOException ex) {
            log.error("LiceseFileGenerator - " + ex.getMessage());
        }
    }    
}
