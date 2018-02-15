package org.wso2.internalapps.lm.thirdpartylibrary.common.restapicalls;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

public class MavenCentralSearch {
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    public static String queryMavenCentralGA(String libID, String libName, String libVersion){
        try {
            String url = "http://search.maven.org/solrsearch/select?q=a:"+libName+"+AND+v:"+libVersion+"&core=gav&rows=1000&wt=json";

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
                return null;
            }
        }catch (Exception e){
            LOGGER.warning("Error Occured : " + e.getMessage());
            return null;
        }
    }

    public static String queryMavenCentralGAV(String libID, String GID, String AID, String libVersion){
        try {
            String url = "http://search.maven.org/solrsearch/select?q=g:"+GID+"+AND+a:"+AID+"+AND+v:"+libVersion+"&core=gav&rows=1000&wt=json";

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
        }catch (Exception e){
            LOGGER.warning("Error Occured : " + e.getMessage());
            return "";
        }
    }

    public static String queryMavenCentralLatest(String libID, String GID, String AID){
        try {

            String url = "http://search.maven.org/solrsearch/select?q=g:"+GID+"+AND+a:"+AID+"&rows=1000&wt=json";

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
                LOGGER.info("Library Details => ID: "+libID+", Name: "+AID +" => "+"Response Status : Failed");
                return "";
            }
        }catch (Exception e){
            LOGGER.warning("Error Occured : " + e.getMessage());
            return "";
        }
    }
}
