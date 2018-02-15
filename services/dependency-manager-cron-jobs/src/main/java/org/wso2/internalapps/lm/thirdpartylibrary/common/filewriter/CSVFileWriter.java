package org.wso2.internalapps.lm.thirdpartylibrary.common.filewriter;

import java.io.File;
import java.io.PrintWriter;
import java.util.logging.Logger;

public class CSVFileWriter {

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public void writeCSV(String filepath, String writeString){
        try{
            PrintWriter pw = new PrintWriter(new File(filepath));
            pw.write(writeString);
            pw.close();
            LOGGER.info("Successful in writing csv file.. Path : "+ filepath);
        } catch (Exception e){
            LOGGER.info("Error in writing : "+ e.getMessage());
        }
    }
}
