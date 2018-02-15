package org.wso2.internalapps.lm.thirdpartylibrary.lmdbupdate.mainclass;

import org.wso2.internalapps.lm.thirdpartylibrary.common.filereader.ConfigurationReader;
import org.wso2.internalapps.lm.thirdpartylibrary.common.filereader.POJO_Config_File;

import org.json.JSONArray;
import org.wso2.internalapps.lm.thirdpartylibrary.common.filewriter.CSVFileWriter;
import org.wso2.internalapps.lm.thirdpartylibrary.common.tables.lmlibrary.AlterTableColumns;
import org.wso2.internalapps.lm.thirdpartylibrary.common.tables.lmlibrary.CheckTableColumns;
import org.wso2.internalapps.lm.thirdpartylibrary.common.tables.lmlibrary.SelectTable;
import org.wso2.internalapps.lm.thirdpartylibrary.lmdbupdate.threads.ThreadUpdateLMLibraryGAVD;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class UpdateLibraryGAMain {

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    public static StringBuilder fileString = new StringBuilder();

    public static void main(String[] args) {
        List<Thread> threads = new ArrayList<Thread>();
        try {

            ConfigurationReader configs = new ConfigurationReader();
            POJO_Config_File configurations = configs.getConfigurations();

            if( configurations.getCSV_FILEPATH()== null  || configurations.getCSV_FILEPATH().isEmpty()  ||
                    configurations.getCSV_FILENAME()== null || configurations.getCSV_FILENAME().isEmpty() ||
                    configurations.getNUM_THREAD() == null  || configurations.getNUM_THREAD().isEmpty()){

                LOGGER.info("Configurations are missing..");

            } else {

                boolean checkCols = new CheckTableColumns().checkColumnsGA();

                int count = 0;

                while (!checkCols){
                    checkCols = new AlterTableColumns().addColumnsGAD();
                    count++;
                    if(count > 10){
                        break;
                    }
                }

                if(checkCols){
                    String THREADNUM = configurations.getNUM_THREAD();
                    int threadCount = Integer.parseInt(THREADNUM);

                    LOGGER.info("Checking LM_LIBRARY tables GAV details");

                    JSONArray jsonArray = new SelectTable().selectGANull();
                    System.out.println(jsonArray.length());
                    if(jsonArray.length() > 0){
                        LOGGER.info("Starting LM_DB_UPDATE_THREADS.. Number of Threads : " + THREADNUM);

                        if(threadCount > 0){

                            int number = jsonArray.length() / threadCount;
                            for (int i = 0; i < threadCount;){
                                int startIndex   = (number * i);
                                int endIndex;
                                if(i  ==  (threadCount-1)){
                                    endIndex  = jsonArray.length()-1;
                                } else {
                                    endIndex  = (number-1) + (number*i);
                                }
                                String threadName = "LM_DB_UPDATE_THREAD Number "+ ++i;
                                ThreadUpdateLMLibraryGAVD worker = new ThreadUpdateLMLibraryGAVD(threadName, startIndex, endIndex, jsonArray.toString());
                                worker.start();
                                threads.add(worker);
                            }

                            for(Thread t: threads) t.join();
                            CSVFileWriter csvWriter = new CSVFileWriter();
                            csvWriter.writeCSV(configurations.getCSV_FILEPATH()+configurations.getCSV_FILENAME(),fileString.toString());
                        }
                    }
                } else {
                    LOGGER.info("Failed in adding Columns. Thread execution failed.");
                }
            }
        } catch (Exception e) {
            LOGGER.info("Error occured: "+ e.getMessage());
        }
    }
}
