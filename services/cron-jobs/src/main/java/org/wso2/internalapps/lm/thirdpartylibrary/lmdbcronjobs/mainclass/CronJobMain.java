package org.wso2.internalapps.lm.thirdpartylibrary.lmdbcronjobs.mainclass;

import org.json.JSONArray;
import org.wso2.internalapps.lm.thirdpartylibrary.common.filereader.ConfigurationReader;
import org.wso2.internalapps.lm.thirdpartylibrary.common.filereader.POJO_Config_File;
import org.wso2.internalapps.lm.thirdpartylibrary.common.tables.lmlibrary.SelectTable;
import org.wso2.internalapps.lm.thirdpartylibrary.lmdbcronjobs.threads.ThreadUpdateLatestVersionDate;
import org.wso2.internalapps.lm.thirdpartylibrary.lmdbcronjobs.threads.ThreadUpdateReleaseDate;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class CronJobMain {
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public static void main(String[] args) {
        List<Thread> threadsLatestVersion = new ArrayList<Thread>();
        List<Thread> releaseDate = new ArrayList<Thread>();
        try {
            ConfigurationReader configs = new ConfigurationReader();
            POJO_Config_File configurations = configs.getConfigurations();

            if (configurations.getNUM_THREAD() == null || configurations.getNUM_THREAD().isEmpty()) {
                LOGGER.info("Configurations are missing..");
            } else {
                String THREADNUM = configurations.getNUM_THREAD();
                int threadCount = Integer.parseInt(THREADNUM);

                JSONArray jsonArrayDateNotAvailable = new SelectTable().selectDateNull();
                if (jsonArrayDateNotAvailable.length() > 0){
                    if(jsonArrayDateNotAvailable.length() > threadCount){
                        LOGGER.info("Starting LM_DB_CRONJOB_FIND_RELEASE_DATE THREADS.. Number of Threads : " + THREADNUM);
                        if (threadCount > 0) {
                            int number = jsonArrayDateNotAvailable.length() / threadCount;
                            for (int i = 0; i < threadCount; ) {
                                int startIndex = (number * i);
                                int endIndex;
                                if (i == (threadCount - 1)) {
                                    endIndex = jsonArrayDateNotAvailable.length() - 1;
                                } else {
                                    endIndex = (number - 1) + (number * i);
                                }
                                String threadName = "LM_DB_CRONJOB_FIND_RELEASE_DATE Thread Number " + ++i;
                                ThreadUpdateReleaseDate worker = new ThreadUpdateReleaseDate(threadName, startIndex, endIndex, jsonArrayDateNotAvailable.toString());
                                worker.start();
                                releaseDate.add(worker);
                            }
                            for (Thread t : releaseDate) t.join();
                        }
                    } else {
                        LOGGER.info("Starting LM_DB_CRONJOB_FIND_RELEASE_DATE THREAD");
                        String threadName = "LM_DB_CRONJOB_FIND_RELEASE_DATE ";
                        ThreadUpdateReleaseDate worker = new ThreadUpdateReleaseDate(threadName, 0, jsonArrayDateNotAvailable.length() - 1, jsonArrayDateNotAvailable.toString());
                        worker.start();
                        releaseDate.add(worker);
                        for (Thread t : releaseDate) t.join();
                    }

                } else {
                    LOGGER.info("LM_DB_CRONJOB_FIND_RELEASE_DATE NOT REQUIRED");
                }

                JSONArray jsonArray = new SelectTable().selectGAVDNotNull();

                if (jsonArray.length() > 0) {
                    LOGGER.info("Starting LM_DB_CRONJOB_FIND_LATEST THREADS.. Number of Threads : " + THREADNUM);

                    if (threadCount > 0) {
                        int number = jsonArray.length() / threadCount;
                        for (int i = 0; i < threadCount; ) {
                            int startIndex = (number * i);
                            int endIndex;
                            if (i == (threadCount - 1)) {
                                endIndex = jsonArray.length() - 1;
                            } else {
                                endIndex = (number - 1) + (number * i);
                            }
                            String threadName = "LM_DB_CRONJOB_FIND_LATEST Thread Number " + ++i;
                            ThreadUpdateLatestVersionDate worker = new ThreadUpdateLatestVersionDate(threadName, startIndex, endIndex, jsonArray.toString());
                            worker.start();
                            threadsLatestVersion.add(worker);
                        }
                        for (Thread t : threadsLatestVersion) t.join();
                    }
                }
            }
        } catch (Exception e){
            LOGGER.warning("Error occured: "+ e.getMessage());
        }
    }
}
