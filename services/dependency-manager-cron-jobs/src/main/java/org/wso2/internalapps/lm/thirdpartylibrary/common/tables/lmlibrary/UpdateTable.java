package org.wso2.internalapps.lm.thirdpartylibrary.common.tables.lmlibrary;

import org.wso2.internalapps.lm.thirdpartylibrary.common.dbconnection.DatabaseConnection;
import org.wso2.internalapps.lm.thirdpartylibrary.common.sqlqueries.MySQLQueries;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Date;
import java.util.logging.Logger;

public class UpdateTable {
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    DatabaseConnection conn = null;
    Connection connect = null;

    public void updateGroupIDArtifactID(String id, String gID, String aID){
        try {
            conn = new DatabaseConnection();
            connect = conn.sqlConnection();
            MySQLQueries querySQL  = new MySQLQueries();

            LOGGER.info("Update ID (GA  only) : "+ id);
            PreparedStatement preparedStatement = connect
                    .prepareStatement(querySQL.UPDATE_LM_LIBRARY_GA);
            preparedStatement.setString(1, aID);
            preparedStatement.setString(2, gID);
            preparedStatement.setString(3, id);
            preparedStatement.executeUpdate();
            LOGGER.info("Successfull");
        } catch (Exception e){
            LOGGER.warning("Error Occured :"+ e.getMessage());
        }  finally {
            conn.sqlClose(connect);
        }
    }

    public void updateGroupIDArtifactIDDate(String id, String gID, String aID, Date dateRelease){
        try {
            conn = new DatabaseConnection();
            connect = conn.sqlConnection();
            MySQLQueries querySQL  = new MySQLQueries();
            java.sql.Date sqlDate = new java.sql.Date(dateRelease.getTime());

            LOGGER.info("Update ID (GAD  only) : "+ id);
            PreparedStatement preparedStatement = connect
                    .prepareStatement(querySQL.UPDATE_LM_LIBRARY_GAD);
            preparedStatement.setString(1, aID);
            preparedStatement.setString(2, gID);
            preparedStatement.setDate(3,sqlDate);
            preparedStatement.setString(4, id);

            preparedStatement.executeUpdate();
            LOGGER.info("Successfull");
        } catch (Exception e){
            LOGGER.warning("Error Occured :"+ e.getMessage());
        }  finally {
            conn.sqlClose(connect);
        }
    }

    public void updateLatestVersionDate(String id, String version, Date dateRelease){
        try {
            conn = new DatabaseConnection();
            connect = conn.sqlConnection();
            MySQLQueries querySQL  = new MySQLQueries();
            java.sql.Date sqlDate = new java.sql.Date(dateRelease.getTime());

            LOGGER.info("Update ID (LatestVersion and Date only): "+ id);
            PreparedStatement preparedStatement = connect
                    .prepareStatement(querySQL.UPDATE_LM_LIBRARY_LATEST_DATE);
            preparedStatement.setString(1, version);
            preparedStatement.setDate(2,sqlDate);
            preparedStatement.setString(3, id);

            preparedStatement.executeUpdate();
            LOGGER.info("Successfull");
        } catch (Exception e){
            LOGGER.warning("Error Occured :"+ e.getMessage());
        }  finally {
            conn.sqlClose(connect);
        }
    }

    public void updateLatestVersion(String id, String latestVersion){
        try {
            conn = new DatabaseConnection();
            connect = conn.sqlConnection();
            MySQLQueries querySQL  = new MySQLQueries();

            LOGGER.info("Update ID (LatestVersion only): "+ id);
            PreparedStatement preparedStatement = connect
                    .prepareStatement(querySQL.UPDATE_LM_LIBRARY_LATEST);
            preparedStatement.setString(1, latestVersion);
            preparedStatement.setString(2, id);

            preparedStatement.executeUpdate();
            LOGGER.info("Successfull");
        } catch (Exception e){
            LOGGER.warning("Error Occured :"+ e.getMessage());
        }  finally {
            conn.sqlClose(connect);
        }
    }

    public void updateReleaseDate(String id, Date dateRelease){
        try {
            conn = new DatabaseConnection();
            connect = conn.sqlConnection();
            MySQLQueries querySQL  = new MySQLQueries();
            System.out.println("a"+dateRelease.getTime());
            java.sql.Date sqlDate = new java.sql.Date(dateRelease.getTime());
            System.out.println("Here 2");
            LOGGER.info("Update ID (Release Date only): "+ id);
            PreparedStatement preparedStatement = connect
                    .prepareStatement(querySQL.UPDATE_LM_LIBRARY_DATE);
            preparedStatement.setDate(1,sqlDate);
            preparedStatement.setString(2, id);

            preparedStatement.executeUpdate();
            LOGGER.info("Successfull");
        } catch (Exception e){
            LOGGER.warning("Error Occured :"+ e.getMessage());
        }  finally {
            conn.sqlClose(connect);
        }
    }
}
