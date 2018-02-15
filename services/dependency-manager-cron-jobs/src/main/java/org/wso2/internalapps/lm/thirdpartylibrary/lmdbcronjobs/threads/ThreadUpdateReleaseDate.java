package org.wso2.internalapps.lm.thirdpartylibrary.lmdbcronjobs.threads;

import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.wso2.internalapps.lm.thirdpartylibrary.common.conversions.TimeConversion;
import org.wso2.internalapps.lm.thirdpartylibrary.common.restapicalls.MavenCentralSearch;
import org.wso2.internalapps.lm.thirdpartylibrary.common.restapicalls.WSO2NexusSearch;
import org.wso2.internalapps.lm.thirdpartylibrary.common.restapicalls.WSO2NexusSearchDateUploaded;
import org.wso2.internalapps.lm.thirdpartylibrary.common.tables.lmlibrary.UpdateTable;

import java.io.StringReader;
import java.sql.Date;
import java.util.logging.Logger;

public class ThreadUpdateReleaseDate extends Thread{
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private String threadname;
    private int startIndex;
    private int endIndex;
    private String stringJsonArray;

    public ThreadUpdateReleaseDate(String threadname, int startIndex, int endIndex, String stringJsonArray){
        this.threadname = threadname;
        this.startIndex = startIndex;
        this.endIndex =  endIndex;
        this.stringJsonArray = stringJsonArray;
    }

    public void run(){
        try{
            LOGGER.info("Starting LM_DB_CRONJOB_FIND_RELEASE_DATE Thread :" + threadname);

            JSONParser parse = new JSONParser();
            JSONArray jsonArray = (JSONArray)parse.parse(stringJsonArray);
            UpdateTable updateObj = new UpdateTable();

            for(int i = startIndex; i <= endIndex; i++){
                boolean  found  =  false;
                Date relDate  =   null;
                JSONObject jsonObject = (JSONObject)jsonArray.get(i);
                String libGID = jsonObject.get("LIB_GROUP_ID").toString();
                String libAID = jsonObject.get("LIB_ARTIFACT_ID").toString();
                String libVersion = jsonObject.get("LIB_VERSION").toString();
                String libID = jsonObject.get("LIB_ID").toString();

                //query maven-central
                String queryMaven = MavenCentralSearch.queryMavenCentralGAV(libID,libGID,libAID,libVersion);
                if(!queryMaven.isEmpty()){
                    JSONParser parser = new JSONParser();
                    JSONObject jsonRespObj = (JSONObject)parser.parse(queryMaven);

                    JSONObject responseJSON = (JSONObject) jsonRespObj.get("response");
                    String countResults = responseJSON.get("numFound").toString();
                    int countResult = Integer.parseInt(countResults);

                    if(countResult == 1){
                        JSONArray responseArray = (JSONArray)responseJSON.get("docs");
                        JSONObject obj = (JSONObject)responseArray.get(0);
                        relDate  =  new TimeConversion().timeConversion(obj.get("timestamp").toString());
                        if(relDate !=  null){
                            found  =  true;
                            System.out.println(libID);
                            System.out.println(relDate);
                            updateObj.updateReleaseDate(
                                    libID,
                                    relDate
                            );
                        }
                    }
                    if (countResult == 0 || found == false) {
                        String wso2NexusSearchResp = WSO2NexusSearch.queryWSO2NexusGAV(libID,libGID,libAID,libVersion);
                        if(!wso2NexusSearchResp.isEmpty()){
                            SAXBuilder saxBuilder = new SAXBuilder();
                            Document doc = saxBuilder.build(new StringReader(wso2NexusSearchResp));
                            String TotalCount = doc.getRootElement().getChildText("totalCount");
                            if(Integer.parseInt(TotalCount) > 0) {
                                String releaseDate = WSO2NexusSearchDateUploaded.queryWSO2NexusDate(
                                        libID,
                                        libGID,
                                        libAID,
                                        libVersion,
                                        doc.getRootElement().getChild("data").getChild("artifact").getChildText("latestReleaseRepositoryId")
                                );
                                if(!releaseDate.isEmpty()) {
                                    SAXBuilder sBuilder = new SAXBuilder();
                                    Document docmnt = sBuilder.build(new StringReader(releaseDate));
                                    Date rDate = new TimeConversion().timeConversion(
                                            docmnt.getRootElement().getChild("data").getChildText("uploaded"));
                                    if (rDate != null) {
                                        System.out.println(libID);
                                        System.out.println(rDate);
                                        updateObj.updateReleaseDate(
                                                libID,
                                                relDate
                                        );
                                    }
                                }
                            }
                        }else {
                            LOGGER.info("Release Not Found => LIB_ID: "+libID+" and name: "+libAID);
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warning("Error Occured : " + e.getMessage());
        } finally {
            LOGGER.info("Stopping LM_DB_CRONJOB_FIND_RELEASE_DATE Thread :" + threadname);
        }
    }
}

