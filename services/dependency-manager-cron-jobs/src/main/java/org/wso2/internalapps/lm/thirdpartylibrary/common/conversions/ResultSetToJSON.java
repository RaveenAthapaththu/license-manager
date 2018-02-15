package org.wso2.internalapps.lm.thirdpartylibrary.common.conversions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.util.logging.Logger;

public class ResultSetToJSON {
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public static JSONArray convertResultSetToJson(ResultSet resultSet){
        JSONArray jsonArray = new JSONArray();
        try {
            while (resultSet.next()) {
                int total_rows = resultSet.getMetaData().getColumnCount();
                JSONObject obj1 = new JSONObject();
                for (int i = 0; i < total_rows; i++) {

                    obj1.put(resultSet.getMetaData().getColumnLabel(i + 1)
                            , resultSet.getObject(i + 1));

                }
                jsonArray.put(obj1);
            }
            LOGGER.info("Resultset successfully converted to JSON");
            return jsonArray;
        } catch (Exception e){
            LOGGER.warning("Error Occured : " + e.getMessage());
            return jsonArray;
        }
    }
}
