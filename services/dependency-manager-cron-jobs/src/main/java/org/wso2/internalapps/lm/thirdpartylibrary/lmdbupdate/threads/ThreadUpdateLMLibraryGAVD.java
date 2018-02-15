package org.wso2.internalapps.lm.thirdpartylibrary.lmdbupdate.threads;

import java.io.StringReader;
import java.sql.Date;
import java.util.logging.Logger;

import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.JSONArray;
import org.wso2.internalapps.lm.thirdpartylibrary.common.conversions.TimeConversion;
import org.wso2.internalapps.lm.thirdpartylibrary.common.tables.lmlibrary.UpdateTable;
import org.wso2.internalapps.lm.thirdpartylibrary.common.restapicalls.MavenCentralSearch;
import org.wso2.internalapps.lm.thirdpartylibrary.common.restapicalls.WSO2NexusSearchDateUploaded;
import org.wso2.internalapps.lm.thirdpartylibrary.common.restapicalls.WSO2NexusSearch;

import static org.wso2.internalapps.lm.thirdpartylibrary.lmdbupdate.mainclass.UpdateLibraryGAMain.fileString;

public class ThreadUpdateLMLibraryGAVD extends Thread{
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private String threadname;
    private int startIndex;
    private int endIndex;
    private String stringJsonArray;

    public ThreadUpdateLMLibraryGAVD(String threadname, int startIndex, int endIndex, String stringJsonArray){
        this.threadname = threadname;
        this.startIndex = startIndex;
        this.endIndex =  endIndex;
        this.stringJsonArray = stringJsonArray;
    }

    public void run(){
        try {
            LOGGER.info("Starting LM_DB_UPDATE Thread :" + threadname);
            System.out.println("Starting index :" + startIndex);
            System.out.println("Ending index :" + endIndex);

            JSONParser parse = new JSONParser();
            JSONArray jsonArray = (JSONArray)parse.parse(stringJsonArray);
            UpdateTable updateObj = new UpdateTable();

            for(int i = startIndex; i <= endIndex; i++){
                JSONObject  jsonObject = (JSONObject)jsonArray.get(i);

                String libName = jsonObject.get("LIB_NAME").toString();
                String libVersion = jsonObject.get("LIB_VERSION").toString();
                String libID = jsonObject.get("LIB_ID").toString();
                System.out.println(libID+ "   "+libName+"  "+libVersion);

                //Query maven-central for GID and AID
                String mavenCentralSearchResp = MavenCentralSearch.queryMavenCentralGA(libID, libName,libVersion);

                if(mavenCentralSearchResp != null){

                    JSONParser parser = new JSONParser();
                    JSONObject jsonRespObj = (JSONObject)parser.parse(mavenCentralSearchResp);

                    JSONObject responseJSON = (JSONObject) jsonRespObj.get("response");
                    String countResults = responseJSON.get("numFound").toString();
                    int countResult = Integer.parseInt(countResults);

                    if (countResult == 0) {
                        //Query WSO2-Nexus
                        System.out.println("WSO2 Query ");
                        String wso2NexusSearchResp = WSO2NexusSearch.queryWSO2NexusAV(libID, libName,libVersion);

                        SAXBuilder saxBuilder = new SAXBuilder();
                        Document doc = saxBuilder.build(new StringReader(wso2NexusSearchResp));
                        String TotalCount = doc.getRootElement().getChildText("totalCount");

                        if(Integer.parseInt(TotalCount) > 0){
                            System.out.println("WSO2 Query Success");
                            if(doc.getRootElement().getChild("data").getChildren().size() == 1){
                                String releaseDate = WSO2NexusSearchDateUploaded.queryWSO2NexusDate(
                                        libID,
                                        doc.getRootElement().getChild("data").getChild("artifact").getChildText("groupId"),
                                        doc.getRootElement().getChild("data").getChild("artifact").getChildText("artifactId"),
                                        libVersion,
                                        doc.getRootElement().getChild("data").getChild("artifact").getChildText("latestReleaseRepositoryId")
                                );
                                if(releaseDate.isEmpty()){
                                    updateObj.updateGroupIDArtifactID(
                                            libID,
                                            doc.getRootElement().getChild("data").getChild("artifact").getChildText("groupId"),
                                            doc.getRootElement().getChild("data").getChild("artifact").getChildText("artifactId")
                                    );
                                } else {
                                    SAXBuilder sBuilder = new SAXBuilder();
                                    Document docmnt = sBuilder.build(new StringReader(releaseDate));
                                    Date relDate = new TimeConversion().timeConversion(
                                            docmnt.getRootElement().getChild("data").getChildText("uploaded")
                                    );
                                    if(releaseDate != null){
                                        updateObj.updateGroupIDArtifactIDDate(
                                                libID,
                                                doc.getRootElement().getChild("data").getChild("artifact").getChildText("groupId"),
                                                doc.getRootElement().getChild("data").getChild("artifact").getChildText("artifactId"),
                                                relDate
                                        );
                                    } else {
                                        updateObj.updateGroupIDArtifactID(
                                                libID,
                                                doc.getRootElement().getChild("data").getChild("artifact").getChildText("groupId"),
                                                doc.getRootElement().getChild("data").getChild("artifact").getChildText("artifactId"));
                                    }
                                }
                            } else {
                                for (int j = 0; j < doc.getRootElement().getChild("data").getChildren().size(); j++){
                                    String fileline =
                                            libID + "," +
                                            doc.getRootElement().getChild("data").getChildren().get(j).getChildText("groupId")+","+
                                                    doc.getRootElement().getChild("data").getChildren().get(j).getChildText("artifactId")+","+
                                            doc.getRootElement().getChild("data").getChildren().get(j).getChildText("version")+",\n";
                                    fileString.append(fileline);
                                }
                            }
                        }
                    } else if(countResult > 0){
                        JSONArray responseArray = (JSONArray)responseJSON.get("docs");
                        if (countResult == 1){
                            System.out.println("Maven Query Success");
                            JSONObject objectResult = (JSONObject)responseArray.get(0);
                            Date releaseDate = new TimeConversion().timeConversion(objectResult.get("timestamp").toString());
                            if(releaseDate != null){
                                updateObj.updateGroupIDArtifactIDDate(
                                        libID,
                                        objectResult.get("g").toString(),
                                        objectResult.get("a").toString(),
                                        releaseDate
                                );
                            } else {
                                updateObj.updateGroupIDArtifactID(
                                        libID,
                                        objectResult.get("g").toString(),
                                        objectResult.get("a").toString());
                            }
                        } else {
                            for(int k=0;k<countResult;k++){
                                JSONObject obj = (JSONObject)responseArray.get(k);
                                String fileStr = libID + "," +
                                        obj.get("g").toString() + "," +
                                        obj.get("a").toString() + "," +
                                        obj.get("v").toString() + ",\n";
                                fileString.append(fileStr);
                            }
                        }
                    }
                }  else    {
                    String wso2NexusSearchResp = WSO2NexusSearch.queryWSO2NexusAV(libID, libName,libVersion);

                    SAXBuilder saxBuilder = new SAXBuilder();
                    Document doc = saxBuilder.build(new StringReader(wso2NexusSearchResp));
                    String TotalCount = doc.getRootElement().getChildText("totalCount");

                    if(Integer.parseInt(TotalCount) > 0){
                        if(doc.getRootElement().getChild("data").getChildren().size() == 1){
                            String releaseDate = WSO2NexusSearchDateUploaded.queryWSO2NexusDate(
                                    libID,
                                    doc.getRootElement().getChild("data").getChild("artifact").getChildText("groupId"),
                                    doc.getRootElement().getChild("data").getChild("artifact").getChildText("artifactId"),
                                    libVersion,
                                    doc.getRootElement().getChild("data").getChild("artifact").getChildText("latestReleaseRepositoryId")
                            );
                            if(releaseDate.isEmpty()){
                                updateObj.updateGroupIDArtifactID(
                                        libID,
                                        doc.getRootElement().getChild("data").getChild("artifact").getChildText("groupId"),
                                        doc.getRootElement().getChild("data").getChild("artifact").getChildText("artifactId")
                                );
                            } else {
                                SAXBuilder sBuilder = new SAXBuilder();
                                Document docmnt = sBuilder.build(new StringReader(releaseDate));
                                Date relDate = new TimeConversion().timeConversion(
                                        docmnt.getRootElement().getChild("data").getChildText("uploaded")
                                );
                                if(releaseDate != null){
                                    updateObj.updateGroupIDArtifactIDDate(
                                            libID,
                                            doc.getRootElement().getChild("data").getChild("artifact").getChildText("groupId"),
                                            doc.getRootElement().getChild("data").getChild("artifact").getChildText("artifactId"),
                                            relDate
                                    );
                                } else {
                                    updateObj.updateGroupIDArtifactID(
                                            libID,
                                            doc.getRootElement().getChild("data").getChild("artifact").getChildText("groupId"),
                                            doc.getRootElement().getChild("data").getChild("artifact").getChildText("artifactId"));
                                }
                            }
                        } else {
                            for (int j = 0; j < doc.getRootElement().getChild("data").getChildren().size(); j++){
                                String fileline =
                                        libID + "," +
                                                doc.getRootElement().getChild("data").getChildren().get(j).getChildText("groupId")+","+
                                                doc.getRootElement().getChild("data").getChildren().get(j).getChildText("artifactId")+","+
                                                doc.getRootElement().getChild("data").getChildren().get(j).getChildText("version")+",\n";
                                fileString.append(fileline);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.info("Error Occured :"+ e.getMessage());
        } finally {
            LOGGER.info("Stopping LM_DB_UPDATE Thread :" + threadname);
        }
    }
}
