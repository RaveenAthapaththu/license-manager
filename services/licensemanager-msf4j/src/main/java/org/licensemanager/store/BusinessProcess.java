package org.licensemanager.store;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.licensemanager.conf.Configuration;
import org.licensemanager.conf.ConfigurationReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.MicroservicesRunner;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import java.io.*;
import java.net.URL;
import java.util.Properties;

public class BusinessProcess {

    private static final Logger log = LoggerFactory.getLogger(MicroservicesRunner.class);

    public static String getTaskId(String processId){
        Properties confFile = new Properties();
        String taskId = "";
        String bpmnUrl, bpmnToken;
        HttpsURLConnection connection = null;
        JsonObject responseFromBpmn = new JsonObject();
        JsonParser jsonParser = new JsonParser();
        JsonArray tasksJson = new JsonArray();
        try{
            ConfigurationReader configurationReader = new ConfigurationReader();
            Configuration configuration = configurationReader.getConfigurations();
            bpmnUrl = configuration.getBpmnUrl() + "bpmn/runtime/tasks/";
            bpmnToken = configuration.getBpmnToken();
            URL url = new URL(bpmnUrl);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", bpmnToken);
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            responseFromBpmn = jsonParser.parse(response.toString()).getAsJsonObject();
            tasksJson = responseFromBpmn.get("data").getAsJsonArray();
            for(int i = 0; i < tasksJson.size(); i++){
                if(tasksJson.get(i).getAsJsonObject().get("processInstanceId").getAsString().equals(processId)){
                    taskId = tasksJson.get(i).getAsJsonObject().get("id").getAsString();
                    return taskId;
                }
            }

        }catch (IOException e){
            log.error("BusinessProcess(getTaskId) " + e.getMessage());
        }
        return taskId;
    }

    public static void getTasks(){
        Properties confFile = new Properties();
        String taskId = "";
        String bpmnUrl, bpmnToken;
        HttpsURLConnection connection = null;
        JsonObject responseFromBpmn = new JsonObject();
        JsonParser jsonParser = new JsonParser();
        JsonArray tasksJson = new JsonArray();
        try{
            ConfigurationReader configurationReader = new ConfigurationReader();
            Configuration configuration = configurationReader.getConfigurations();
            bpmnUrl = configuration.getBpmnUrl() + "bpmn/runtime/tasks/";
            bpmnToken = configuration.getBpmnToken();
            URL url = new URL(bpmnUrl);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", bpmnToken);
            connection.setUseCaches(false);
            connection.setDoOutput(true);

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            responseFromBpmn = jsonParser.parse(response.toString()).getAsJsonObject();
            tasksJson = responseFromBpmn.get("data").getAsJsonArray();

        }catch (IOException e){
            log.error("BusinessProcess(getTasks) " + e.getMessage());
        }

    }
}
