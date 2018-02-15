package org.wso2.internalapps.lm.thirdpartylibrary.common.tables.lmlibrary;

import org.wso2.internalapps.lm.thirdpartylibrary.common.dbconnection.DatabaseConnection;
import org.wso2.internalapps.lm.thirdpartylibrary.common.sqlqueries.MySQLQueries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.logging.Logger;

public class AlterTableColumns {
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    DatabaseConnection conn = null;
    Connection connect = null;
    boolean returnValue = false;

    public boolean addColumnsGAD(){
        try {
            conn = new DatabaseConnection();
            connect = conn.sqlConnection();
            MySQLQueries querySQL  = new MySQLQueries();

            LOGGER.info("ADDING Columns to LM_LIBRARY");

            PreparedStatement preparedStatement = connect
                    .prepareStatement(querySQL.ALTER_LM_LIBRARY_GAD);
            preparedStatement.executeUpdate();

            LOGGER.info("Successfull");
            returnValue = true;
        } catch (Exception e){
            LOGGER.info("Error Occured :"+ e.getMessage());
            returnValue = false;
        }  finally {
            conn.sqlClose(connect);
            return returnValue;
        }
    }
}
