package org.wso2.internalapps.lm.thirdpartylibrary.common.conversions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.sql.Date;
import java.util.logging.Logger;

public class TimeConversion {
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    public Date timeConversion(String dateInMillis){
        try{

            long timeInMillis = Long.parseLong(dateInMillis);

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(timeInMillis);

            DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
            String time = formatter.format(calendar.getTime()).toString();
            Date sqlDate= new Date(formatter.parse(time).getTime());

            return sqlDate;
        } catch (Exception e){
            LOGGER.warning("Error in conversion to Date : "+e.getMessage());
            return null;
        }
    }
}
