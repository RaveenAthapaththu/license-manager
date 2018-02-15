package org.wso2.internalapps.lm.thirdpartylibrary.common.tables.lmlibrary;

import org.wso2.internalapps.lm.thirdpartylibrary.common.dbconnection.DatabaseConnection;
import org.wso2.internalapps.lm.thirdpartylibrary.common.sqlqueries.MySQLQueries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.logging.Logger;

public class CheckTableColumns {
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    DatabaseConnection conn = null;
    Connection connect = null;
    boolean isColsPresent = false;

    public boolean checkColumnsGA(){
        try {
            conn = new DatabaseConnection();
            connect = conn.sqlConnection();
            MySQLQueries querySQL  = new MySQLQueries();

            PreparedStatement preparedStatement = connect
                    .prepareStatement(querySQL.SELECT_LM_LIBRARY_ALL);
            ResultSet resultSet = preparedStatement.executeQuery();

            ResultSetMetaData metaData = resultSet.getMetaData();

            isColsPresent = false;
            String [] cols = {"LIB_ID", "LIB_NAME","LIB_VERSION","LIB_FILE_NAME","LIB_TYPE","LIB_VENDOR","LIB_VENDOR","LIB_URL",
                    "LIB_ARTIFACT_ID", "LIB_GROUP_ID", "LIB_DATE", "LIB_LATEST_VERSION", "LIB_LATEST_DATE"};
            for (int i = 1; i <= metaData.getColumnCount(); i++) {
                for(int j = 0 ; j < cols.length; j++){
                    if (cols[j].equals(metaData.getColumnName(i))) {
                        isColsPresent = true;
                    } else {
                        isColsPresent = false;
                    }
                }

            }
            LOGGER.info("Column Check Successfull");
        } catch (Exception e){
            LOGGER.info("Error Occured :"+ e.getMessage());
            isColsPresent = false;
        }  finally {
            conn.sqlClose(connect);
            return isColsPresent;
        }
    }
}
