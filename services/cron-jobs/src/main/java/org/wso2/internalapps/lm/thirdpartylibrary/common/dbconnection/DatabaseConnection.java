package org.wso2.internalapps.lm.thirdpartylibrary.common.dbconnection;

import org.wso2.internalapps.lm.thirdpartylibrary.common.filereader.ConfigurationReader;
import org.wso2.internalapps.lm.thirdpartylibrary.common.filereader.POJO_Config_File;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.logging.Logger;

public class DatabaseConnection {

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private Connection connection = null;

    public Connection sqlConnection(){
        try {
            LOGGER.info("Preparing SQL Connection");

            ConfigurationReader configs = new ConfigurationReader();
            POJO_Config_File configurations = configs.getConfigurations();

            if( configurations.getDBURL()== null  || configurations.getDBURL().isEmpty()  ||
                configurations.getUSER()== null || configurations.getUSER().isEmpty() ||
                configurations.getPASS() == null  || configurations.getPASS().isEmpty()){

                LOGGER.info("Cannot establish connection.. Configurations are missing..");

            } else {
                String DBURL = configurations.getDBURL();
                String USER = configurations.getUSER();
                String PASS = configurations.getPASS();

                connection = DriverManager
                        .getConnection(DBURL,USER,PASS);

                DriverManager.setLoginTimeout(216000);
                LOGGER.info("DB Connection established..");
            }
        } catch (Exception e) {
            LOGGER.warning("Error occured: "+ e.getMessage());
        }
        return connection;
    }

    public void sqlClose(Connection connect) {
        try {
            if (connect != null) {
                connect.close();
                LOGGER.info("Database Connection closed");
            }
        } catch (Exception e) {
            LOGGER.warning("Error in database close: "+ e.getMessage());
        }
    }
}
