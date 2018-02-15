package org.wso2.internalapps.lm.thirdpartylibrary.common.restapicalls;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

public class WSO2NexusSearch {
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    public static String queryWSO2NexusAV(String libID, String libName, String libVersion){
        try {
            String url = "https://maven.wso2.org/nexus/service/local/lucene/search?a="+libName+"&v="+libVersion;

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
                LOGGER.info("Library Details => ID: "+libID+", Name: "+libName +", Version: "+ libVersion +" => "+"Response Status : Failed");
                return "";
            }
        }catch (Exception e) {
            LOGGER.warning("Error Occured : " + e.getMessage());
            return "";
        }
    }

    public static String queryWSO2NexusGAV(String libID, String GID, String AID, String libVersion){
        try {
            String url = "https://maven.wso2.org/nexus/service/local/lucene/search?g="+GID+"&a="+AID+"&v="+libVersion;

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
                LOGGER.info("Library Details => ID: "+libID+", Name: "+AID +", Version: "+ libVersion +" => "+"Response Status : Failed");
                return "";
            }
        }catch (Exception e) {
            LOGGER.warning("Error Occured : " + e.getMessage());
            return "";
        }
    }
}
