package org.wso2.internalapps.lm.thirdpartylibrary.common.restapicalls;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

public class WSO2NexusSearchDateUploaded {
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public static String queryWSO2NexusDate(String libID, String GroupID, String ArtifactID, String libVersion,String repoID){
        try {
            String url = getRepoURL(GroupID, ArtifactID, libVersion,repoID);

            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            int responseCode = con.getResponseCode();
            if(responseCode == 200){
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                String resp = response.toString();

                return resp;
            } else {
                LOGGER.info("Library Details => ID: "+libID+", Name: "+ArtifactID +", Version: "+ libVersion +" => "+"Response Status : Failed");
                return "";
            }
        }catch (Exception e) {
            LOGGER.info("Error Occured : " + e.getMessage());
            return "";
        }
    }

    public static String getRepoURL(String GroupID, String ArtifactID, String libVersion,String repoID){
        try {
            StringBuilder searchUrl = new StringBuilder();
            searchUrl.append("https://maven.wso2.org/nexus/service/local/repositories/"+repoID+"/content");
            String  [] groupIDSpliited = GroupID.split("\\.");
            for(int i = 0; i< groupIDSpliited.length; i++){
                searchUrl.append("/"+groupIDSpliited[i]);
            }
            searchUrl.append("/"+ArtifactID);
            searchUrl.append("/"+libVersion);
            searchUrl.append("/"+ArtifactID+"-"+libVersion+".jar?describe=info");
            return searchUrl.toString();
        }catch (Exception e) {
            LOGGER.info("Error Occured : " + e.getMessage());
            return "";
        }
    }
}
