package org.wso2.internalapps.lm.thirdpartylibrary.dbnotifieremail.checktables;

import org.wso2.internalapps.lm.thirdpartylibrary.common.dbconnection.DatabaseConnection;
import org.wso2.internalapps.lm.thirdpartylibrary.common.sqlqueries.MySQLQueries;

import java.sql.*;
import java.util.logging.Logger;

public class CheckLM_LIBRARY {
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    private Connection connect = null;
    private PreparedStatement preparedStatement = null;
    private ResultSet resultSet = null;

    public ResultSet readLMLibraryTable(){
        try {
            DatabaseConnection conn = new DatabaseConnection();
            connect = conn.sqlConnection();

            LOGGER.info("Checking LM_LIBRARY");

            preparedStatement = connect
                    .prepareStatement(new MySQLQueries().SELECT_LM_LIBRARY_GA_NULL);
            resultSet = preparedStatement.executeQuery();

            return resultSet;
        } catch (Exception e) {
            LOGGER.info("Error occured: "+ e.getMessage());
        }
        return null;
    }
}
