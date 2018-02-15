package org.wso2.internalapps.lm.thirdpartylibrary.common.restapicalls;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.entity.*;
import org.apache.http.HttpResponse;
import org.wso2.internalapps.lm.thirdpartylibrary.common.filereader.ConfigurationReader;
import org.wso2.internalapps.lm.thirdpartylibrary.common.filereader.POJO_Config_File;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

public class RouterService {
    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    public static String queryMavenCentralGA(String libID, String GID, String AID){
        try {
            ConfigurationReader configs = new ConfigurationReader();
            POJO_Config_File configurations = configs.getConfigurations();


            String url = configurations.getROUTER_URL()+"dependencyManager/router/jar?VersionReq=true&GroupID="+GID;

            HttpClient client = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);

            client = WebClientDevWrapper.wrapClient(client);

            String json = "{\"groupID\":\"" + GID+ "\",\"artifactID\":\"" + AID + "\"}";
            StringEntity entity = new StringEntity(json);
            entity.setContentType("application/json");
            httpPost.setEntity(entity);

            HttpResponse responsePost = client.execute(httpPost);

            if(responsePost.getStatusLine().getStatusCode() == 200){
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(responsePost.getEntity().getContent()));
                String inputLine;
                StringBuffer response = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                String resp = response.toString();
                return resp;
            } else {
                LOGGER.info("Library Details => ID: "+libID+", GroupID: "+GID +", ArtifactID: "+ AID +" => "+"Response Status : Failed");
                return "";
            }
        }catch (Exception e){
            LOGGER.info("Error Occured : " + e.getMessage());
            return "";
        }
    }
}
