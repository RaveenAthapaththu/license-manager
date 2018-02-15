package org.wso2.internalapps.lm.thirdpartylibrary.common.tables.lmlibrary;

import org.wso2.internalapps.lm.thirdpartylibrary.common.conversions.ResultSetToJSON;
import org.wso2.internalapps.lm.thirdpartylibrary.common.dbconnection.DatabaseConnection;
import org.wso2.internalapps.lm.thirdpartylibrary.common.sqlqueries.MySQLQueries;

import org.json.JSONArray;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Logger;

public class SelectTable {
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    DatabaseConnection conn = null;
    Connection connect = null;

    public JSONArray selectGANull(){
        JSONArray arr = new JSONArray();
        try {
            conn = new DatabaseConnection();
            connect = conn.sqlConnection();
            MySQLQueries querySQL  = new MySQLQueries();

            PreparedStatement preparedStatement = connect
                    .prepareStatement(querySQL.SELECT_LM_LIBRARY_GA_NULL);
            ResultSet resultSet = preparedStatement.executeQuery();
            arr = ResultSetToJSON.convertResultSetToJson(resultSet);

            LOGGER.info("LM_LIBRARY table GA details Checking Successfull");
        } catch (Exception e){
            LOGGER.info("Error Occured :"+ e.getMessage());
        }  finally {
            conn.sqlClose(connect);
            return arr;
        }
    }

    public JSONArray selectGAVDNotNull(){
        JSONArray arr = new JSONArray();
        try {
            conn = new DatabaseConnection();
            connect = conn.sqlConnection();
            MySQLQueries querySQL  = new MySQLQueries();

            PreparedStatement preparedStatement = connect
                    .prepareStatement(querySQL.SELECT_LM_LIBRARY_ALL_GAVD_NOTNULL);
            ResultSet resultSet = preparedStatement.executeQuery();
            arr = ResultSetToJSON.convertResultSetToJson(resultSet);


        } catch (Exception e){
            LOGGER.info("Error Occured :"+ e.getMessage());
        }  finally {
            conn.sqlClose(connect);
            return arr;
        }
    }

    public JSONArray selectDateNull(){
        JSONArray arr = new JSONArray();
        try {
            conn = new DatabaseConnection();
            connect = conn.sqlConnection();
            MySQLQueries querySQL  = new MySQLQueries();

            PreparedStatement preparedStatement = connect
                    .prepareStatement(querySQL.SELECT_LM_LIBRARY_GA_D_NULL);
            ResultSet resultSet = preparedStatement.executeQuery();
            arr = ResultSetToJSON.convertResultSetToJson(resultSet);

            LOGGER.info("LM_LIBRARY table LIB_DATE details Checking Successfull");
        } catch (Exception e){
            LOGGER.info("Error Occured :"+ e.getMessage());
        }  finally {
            conn.sqlClose(connect);
            return arr;
        }
    }
}
