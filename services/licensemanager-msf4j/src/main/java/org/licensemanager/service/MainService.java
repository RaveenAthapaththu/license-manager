/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.licensemanager.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.licensemanager.conf.Configuration;
import org.licensemanager.conf.ConfigurationReader;
import org.licensemanager.store.BusinessProcess;
import org.licensemanager.store.DataManager;
import org.licensemanager.store.LicenseRequest;
import org.licensemanager.work.enterData.EnterData;
import org.licensemanager.work.main.*;
import org.wso2.msf4j.formparam.FormItem;
import org.wso2.msf4j.formparam.FormParamIterator;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.msf4j.MicroservicesRunner;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.*;
/**
 * This is the Microservice resource class.
 * See <a href="https://github.com/wso2/msf4j#getting-started">https://github.com/wso2/msf4j#getting-started</a>
 * for the usage of annotations.
 *
 * @since 1.0-SNAPSHOT
 */
@Path("/")
public class MainService {
    private String FILE_PATH = "FILE_PATH";
    private String JAR_HOLDER = "JAR_HOLDER";
    private String ENTER_DATA = "ENTER_DATA";
    private String LICENSE_PATH = "LICENSE_PATH";
    private String LICENSE_FILE_NAME = "LICENSE_FILE_NAME";
    private String VALID_USER = "VALID_USER";

    ConfigurationReader configurationReader = new ConfigurationReader();
    private static final Logger log = LoggerFactory.getLogger(MicroservicesRunner.class);

    @POST
    @Path("/validateUser")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @OPTIONS
    public Response validateUserResource(@Context Request request,String stringPayload){
        JsonObject responseJson = new JsonObject();
        String origin = "",token;
        String message;
        String key,email;
        JsonObject requestJson;
        JsonParser jsonParser = new JsonParser();
        byte[] keyBytes,payloadBytes;
        String[] tokenValues;
        boolean returnValue = false;
        JsonObject payloadJson;
        try{
            Session session = request.getSession();
            Configuration configuration = configurationReader.getConfigurations();
            origin = configuration.getOrigin();
            requestJson = jsonParser.parse(stringPayload).getAsJsonObject();
            token = requestJson.get("token").getAsString();
            tokenValues = token.split("\\.");
            message = tokenValues[0] + "." + tokenValues[1];
            key = configuration.getPublicKey();
            keyBytes = Base64.getDecoder().decode(key.getBytes());
            KeyFactory kf = KeyFactory.getInstance("RSA");
            RSAPublicKey rsaPublicKey = (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(keyBytes));
            Signature sign = Signature.getInstance("SHA256withRSA");
            sign.initVerify(rsaPublicKey);
            sign.update(message.getBytes("UTF-8"));
            byte[] signBytes = Base64.getDecoder().decode(tokenValues[2].replace('-', '+').replace('_', '/').getBytes(StandardCharsets.UTF_8));
            returnValue = sign.verify(signBytes);
            responseJson.addProperty("responseType","Done");
            responseJson.addProperty("isValid",returnValue);
            responseJson.addProperty("responseMessage","Done");
            session.setAttribute(VALID_USER,returnValue);
            payloadBytes = Base64.getDecoder().decode(tokenValues[1]);
            String payloadString = new String(payloadBytes,StandardCharsets.UTF_8);
            payloadJson = jsonParser.parse(payloadString).getAsJsonObject();
            email = payloadJson.get("http://wso2.org/claims/emailaddress").getAsString();
            log.info("Login - " + email);
        }catch (IOException e) {
            responseJson.addProperty("responseType","Error");
            responseJson.addProperty("isValid",returnValue);
            responseJson.addProperty("responseMessage",e.getMessage());
            log.error("validateUser(IOException) - " + e.getMessage());
        }catch (Exception e) {
            responseJson.addProperty("responseType","Error");
            responseJson.addProperty("isValid",returnValue);
            responseJson.addProperty("responseMessage",e.getMessage());
            log.error("validateUser(Exception) - " + e.getMessage());
        }
        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin",origin)
                .header("Access-Control-Allow-Credentials",true)
                .build();
    }

    @GET
    @Path("/selectLicense")
    @Produces("application/json")
    public Response selectLicenseResource(@Context Request request) {

        JsonObject responseJson = new JsonObject();
        JsonArray errorData = new JsonArray();
        String origin = "";
        String driver,url,userName,password;
        JsonObject errorJson = new JsonObject();
        Session session = request.getSession();
        boolean isValid = Boolean.valueOf(session.getAttribute(VALID_USER).toString());

        try{
            Configuration configuration = configurationReader.getConfigurations();
            origin = configuration.getOrigin();
            if(!isValid){
                errorJson.addProperty("responseType","Error");
                errorJson.addProperty("responseMessage","Invalid User Request");
                responseJson.add("responseData",errorData);
                return Response.ok(errorJson, MediaType.APPLICATION_JSON)
                        .header("Access-Control-Allow-Origin",origin)
                        .header("Access-Control-Allow-Credentials",true)
                        .build();
            }
            driver = configuration.getDriver();
            url = configuration.getUrl();
            userName = configuration.getUserName();
            password = configuration.getPassword();
            DataManager dataManager = new DataManager(driver,url,userName,password);
            JsonArray jsonArray = dataManager.selectAllLicense();
            responseJson.addProperty("responseType","Done");
            responseJson.addProperty("responseMessage","Done");
            responseJson.add("responseData",jsonArray);
            dataManager.closeConection();

        }catch (Exception e) {
            responseJson.addProperty("responseType","Error");
            responseJson.addProperty("responseMessage",e.getMessage());
            log.error("selectLicense - " + e.getMessage());
        }
        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin",origin)
                .header("Access-Control-Allow-Credentials",true)
                .build();
    }

    @GET
    @Path("/checkJars")
    @Produces("application/json")
    public Response checkJarsResource(@Context Request request) {

        Main main = new Main();
        JsonObject responseJson = new JsonObject();
        JsonArray nameMissingJars = new JsonArray();
        String origin = "";
        JsonObject errorJson = new JsonObject();
        JsonArray errorData = new JsonArray();
        Session session = request.getSession();
        boolean isValid = Boolean.valueOf(session.getAttribute(VALID_USER).toString());
        try{
            Configuration configuration = configurationReader.getConfigurations();
            origin = configuration.getOrigin();
            if(!isValid){
                errorJson.addProperty("responseType","Error");
                errorJson.addProperty("responseMessage","Invalid User Request");
                responseJson.add("responseData",errorData);
                return Response.ok(errorJson, MediaType.APPLICATION_JSON)
                        .header("Access-Control-Allow-Origin",origin)
                        .header("Access-Control-Allow-Credentials",true)
                        .build();
            }
            String zipFilePath = session.getAttribute(FILE_PATH).toString();
            String[] fileDestinations = zipFilePath.split("/");
            String extractedDirectory = "";
            for(int i = 0; i < fileDestinations.length - 1; i++){
                extractedDirectory += fileDestinations[i] + "/";
            }
            File zipFile = new File(zipFilePath);
            File dir = new File(extractedDirectory);
            Unzip.unzip(zipFile.getAbsolutePath(),dir.getAbsolutePath());
            String packPath = zipFilePath.substring(0,zipFilePath.length()-4);
            JarHolder jarHolder = main.checkJars(packPath);
            List<MyJar> errorJarList = jarHolder.getErrorJarList();
            for (int i = 0; i < errorJarList.size(); i++){
                JsonObject currentJar = new JsonObject();
                currentJar.addProperty("id",i);
                currentJar.addProperty("name",errorJarList.get(i).getProjectName());
                currentJar.addProperty("version",errorJarList.get(i).getVersion());
                nameMissingJars.add(currentJar);
            }
            session.setAttribute(JAR_HOLDER,jarHolder);
            responseJson.addProperty("responseType","Done");
            responseJson.addProperty("responseMessage","Done");
            responseJson.add("responseData",nameMissingJars);


        }catch (IOException e) {
            responseJson.addProperty("responseType","Error");
            responseJson.addProperty("responseMessage",e.getMessage());
            log.error("checkJars(IOException) - " + e.getMessage());
        }catch (ClassNotFoundException e) {
            responseJson.addProperty("responseType","Error");
            responseJson.addProperty("responseMessage",e.getMessage());
            log.error("checkJars(ClassNotFoundException) - " + e.getMessage());
        }catch (NullPointerException e) {
            responseJson.addProperty("responseType","Error");
            responseJson.addProperty("responseMessage",e.getMessage());
            log.error("checkJars(NullPointerException) - " + e.getMessage());
        }catch (Exception e) {
            responseJson.addProperty("responseType","Error");
            responseJson.addProperty("responseMessage",e.getMessage());
            log.error("checkJars(Exception) - " + e.getMessage());
        }
        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin",origin)
                .header("Access-Control-Allow-Credentials",true)
                .build();
    }

    @POST
    @Path("/uploadPack")
    @Consumes("multipart/form-data")
    @Produces("application/json")
    public Response uploadPackResource(@Context FormParamIterator formParamIterator,@Context Request request ) {

        FormItem item = formParamIterator.next();
        String filePath = "";
        String origin = "";
        JsonObject responseJson = new JsonObject();
        JsonObject errorJson = new JsonObject();
        Session session = request.getSession();
        boolean isValid = Boolean.valueOf(session.getAttribute(VALID_USER).toString());

        try {
            Configuration configuration = configurationReader.getConfigurations();
            origin = configuration.getOrigin();
            if(!isValid){
                errorJson.addProperty("responseType","Error");
                errorJson.addProperty("responseMessage","Invalid User Request");
                return Response.ok(errorJson, MediaType.APPLICATION_JSON)
                        .header("Access-Control-Allow-Origin",origin)
                        .header("Access-Control-Allow-Credentials",true)
                        .build();
            }
            Date today = new Date();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy");
            String todayString = simpleDateFormat.format(today);
            String dest = configuration.getPath() + todayString;
            new File(dest).mkdir();
            filePath = dest + "/" + item.getName();
            Files.copy(item.openStream(), Paths.get(filePath));
            responseJson.addProperty("responseType","Done");
            responseJson.addProperty("responseMessage","Done");

        }catch (IOException e) {
            responseJson.addProperty("responseType","Error");
            responseJson.addProperty("responseMessage",e.getMessage());
            log.error("uploadPack(IOException) - " + e.getMessage());
        }catch (Exception e) {
            responseJson.addProperty("responseType","Error");
            responseJson.addProperty("responseMessage",e.getMessage());
            log.error("uploadPack(Exception) - " + e.getMessage());
        }finally {
            item.close();
        }
        session.setAttribute(FILE_PATH,filePath);
        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin",origin)
                .header("Access-Control-Allow-Credentials",true)
                .build();


    }

    @POST
    @Path("/enterJars")
    @Produces("application/json")
    public Response enterJarsResource(@Context Request request, String stringPayload){
        String origin = "";
        String fileNameWidth = "6%";
        String nameWidth = "6%";
        String versionWidth = "2%";
        String componentTable = "<table style=\"width:40%\"><tr><th style=\"width:"+fileNameWidth+";text-align:left;\">File Name</th><th style=\"width:"+nameWidth+";text-align:left;\">Name</th><th style=\"width:"+versionWidth+";text-align:left;\">Version</th></tr>";
        String libraryTable = "<table style=\"width:40%\"><tr><th style=\"width:"+fileNameWidth+";text-align:left;\">File Name</th><th style=\"width:"+nameWidth+";text-align:left;\">Name</th><th style=\"width:"+versionWidth+";text-align:left;\">Version</th></tr>";
        JsonObject responseJson = new JsonObject();
        Main main = new Main();
        JsonParser jsonParser = new JsonParser();
        int licenseId, licenseRequestId = 0,productId, responseCode = 0;
        HttpsURLConnection connection = null;
        JsonArray payload = new JsonArray();
        JsonObject bpmnRequestJson = new JsonObject();
        JsonObject productNameJson = new JsonObject();
        JsonObject productVersionJson = new JsonObject();
        JsonObject productIdJson = new JsonObject();
        JsonObject licenseRequestIdJson = new JsonObject();
        JsonObject libraryTableJson  = new JsonObject();
        JsonObject componentTableJson = new JsonObject();
        JsonObject mailListJson = new JsonObject();
        JsonObject componentExistsJson = new JsonObject();
        JsonObject libraryExistsJson = new JsonObject();
        JsonObject originJson = new JsonObject();
        JsonObject bpmnResponse;
        String bpmnUrlString, bpmnToken, processId, taskId;
        String driver,url,userName,password,requestBy, productName, productVersion, mailList,componentExists, libraryExists,bpmnOrigin;
        JsonObject errorJson = new JsonObject();
        JsonArray errorData = new JsonArray();
        Session session = request.getSession();
        boolean isValid = Boolean.valueOf(session.getAttribute(VALID_USER).toString());
        try{
            Configuration configuration = configurationReader.getConfigurations();
            bpmnOrigin = configuration.getBpmnOrigin();
            origin = configuration.getOrigin();
            if(!isValid){
                errorJson.addProperty("responseType","Error");
                errorJson.addProperty("responseMessage","Invalid User Request");
                errorJson.add("component",errorData);
                errorJson.add("library",errorData);
                return Response.ok(errorJson, MediaType.APPLICATION_JSON)
                        .header("Access-Control-Allow-Origin",origin)
                        .header("Access-Control-Allow-Credentials",true)
                        .build();
            }
            driver = configuration.getDriver();
            url = configuration.getUrl();
            userName = configuration.getUserName();
            password = configuration.getPassword();
            DataManager dataManager = new DataManager(driver,url,userName,password);
            licenseId = Integer.parseInt(configuration.getLicenseId());
            JsonElement jsonElement = jsonParser.parse(stringPayload);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonArray jsonArray = jsonObject.get("jars").getAsJsonArray();
            requestBy = jsonObject.get("requestBy").getAsString();
            JarHolder jarHolder = (JarHolder) session.getAttribute(JAR_HOLDER);

            EnterData enterData = main.enterData(jarHolder);
            session.setAttribute(ENTER_DATA,enterData);
            List<MyJar> componentList = enterData.getLicenseMissingComponents();
            List<MyJar> libraryList = enterData.getLicenseMissingLibraries();
            productId = enterData.getProductId();
            JsonArray componentJsonArray = new JsonArray();
            JsonArray libraryJsonArray = new JsonArray();
            if(componentList.size() > 0 || libraryList.size() > 0){
                licenseRequestId = dataManager.insertLicenseRequest(requestBy,productId);
            }

            for(int i = 0; i < componentList.size(); i++){
                JsonObject component = new JsonObject();
                component.addProperty("id",i);
                component.addProperty("name",componentList.get(i).getProjectName());
                component.addProperty("version",componentList.get(i).getVersion());
                component.addProperty("license",licenseId);
                componentTable += "<tr><td style=\"width:"+fileNameWidth+";text-align:left;\">"+
                        componentList.get(i).getJarFile().getName()+"</td><td style=\"width:"+nameWidth+";text-align:left;\">"+
                        componentList.get(i).getProjectName()+"</td><td style=\"width:"+versionWidth+";text-align:left;\">"+
                        componentList.get(i).getVersion()+"</td></tr>";
                componentJsonArray.add(component);
                dataManager.insertTempComponent(
                        componentList.get(i).getJarFile().getName(),
                        componentList.get(i).getProjectName(),
                        componentList.get(i).getType(),
                        componentList.get(i).getVersion(),
                        componentList.get(i).getJarFile().getName(),
                        licenseRequestId
                );

            }
            componentTable += "</table>";

            for(int i = 0; i < libraryList.size(); i++){
                JsonObject library = new JsonObject();
                String parent = "";
                library.addProperty("id",i);
                library.addProperty("name",libraryList.get(i).getProjectName());
                library.addProperty("version",libraryList.get(i).getVersion());
                library.addProperty("license",licenseId);
                libraryTable += "<tr><td style=\"width:"+fileNameWidth+";text-align:left;\">"+
                        libraryList.get(i).getJarFile().getName()+"</td><td style=\"width:"+nameWidth+";text-align:left;\">"+
                        libraryList.get(i).getProjectName()+"</td><td style=\"width:"+versionWidth+";text-align:left;\">"+
                        libraryList.get(i).getVersion()+"</td></tr>";
                libraryJsonArray.add(library);

                if(libraryList.get(i).getParent() != null){
                    parent = libraryList.get(i).getParent().getProjectName();
                }
                String type = (libraryList.get(i).getParent()==null)?((libraryList.get(i).isBundle())?"bundle":"jar"):"jarinbundle";
                dataManager.insertTempLib(
                        libraryList.get(i).getProjectName(),
                        libraryList.get(i).getVersion(),
                        libraryList.get(i).getJarFile().getName(),
                        parent,
                        type,
                        licenseRequestId
                );
            }
            libraryTable += "</table>";
            productName = jarHolder.getProductName();
            productVersion = jarHolder.getProductVersion();
            if (componentList.size() > 0 || libraryList.size() > 0){
                if(componentList.size() > 0){
                    componentExists = "yes";
                }else {
                    componentExists = "no";
                }
                if(libraryList.size() > 0){
                    libraryExists = "yes";
                }else {
                    libraryExists = "no";
                }
                mailList = dataManager.selectLibraryAdmins();
                mailList += requestBy;
                productNameJson.addProperty("name","productName");
                productNameJson.addProperty("value",productName);
                productVersionJson.addProperty("name","productVersion");
                productVersionJson.addProperty("value",productVersion);
                productIdJson.addProperty("name","productId");
                productIdJson.addProperty("value",productId);
                licenseRequestIdJson.addProperty("name","licenseRequestId");
                licenseRequestIdJson.addProperty("value",licenseRequestId);
                libraryTableJson.addProperty("name","libraryTable");
                libraryTableJson.addProperty("value",libraryTable);
                componentTableJson.addProperty("name","componentTable");
                componentTableJson.addProperty("value",componentTable);
                mailListJson.addProperty("name","mailList");
                mailListJson.addProperty("value",mailList);
                componentExistsJson.addProperty("name","componentExists");
                componentExistsJson.addProperty("value",componentExists);
                libraryExistsJson.addProperty("name","libraryExists");
                libraryExistsJson.addProperty("value",libraryExists);
                originJson.addProperty("name","origin");
                originJson.addProperty("value",bpmnOrigin);
                payload.add(productNameJson);
                payload.add(productVersionJson);
                payload.add(productIdJson);
                payload.add(licenseRequestIdJson);
                payload.add(libraryTableJson);
                payload.add(componentTableJson);
                payload.add(mailListJson);
                payload.add(componentExistsJson);
                payload.add(libraryExistsJson);
                payload.add(originJson);

                bpmnRequestJson.addProperty("processDefinitionKey", "AdminProcess");
                bpmnRequestJson.addProperty("businessKey", "myBusinessKey");
                bpmnRequestJson.addProperty("tenantId", "-1234");
                bpmnRequestJson.add("variables", payload);

                bpmnUrlString = configuration.getBpmnUrl() + "bpmn/runtime/process-instances/";
                bpmnToken = configuration.getBpmnToken();
                URL bpmnUrl = new URL(bpmnUrlString);
                connection = (HttpsURLConnection) bpmnUrl.openConnection();
                connection.setHostnameVerifier(new HostnameVerifier() {
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Authorization", bpmnToken);
                connection.setUseCaches(false);
                connection.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream (
                        connection.getOutputStream());
                wr.writeBytes(bpmnRequestJson.toString());
                wr.close();

                InputStream is = connection.getInputStream();
                BufferedReader rd = new BufferedReader(new InputStreamReader(is));
                StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
                String line;
                while ((line = rd.readLine()) != null) {
                    response.append(line);
                    response.append('\r');
                }
                rd.close();
                is.close();
                bpmnResponse = jsonParser.parse(response.toString()).getAsJsonObject();
                processId =bpmnResponse.get("id").getAsString();
                taskId = BusinessProcess.getTaskId(processId);
                dataManager.updateLicenseRequestIds(processId,taskId,licenseRequestId);
                responseCode = connection.getResponseCode();
                log.info(Integer.toString(responseCode));
                if(responseCode == 200 || responseCode == 201){
                    responseJson.addProperty("responseType","Done");
                    responseJson.addProperty("responseMessage","Done");
                }else{
                    responseJson.addProperty("responseType","Error");
                    responseJson.addProperty("responseMessage",("Status code - " + Integer.toString(responseCode)));
                    log.error(response.toString());
                }
            }else{
                responseJson.addProperty("responseType","Done");
                responseJson.addProperty("responseMessage","Done");
            }

            responseJson.add("component",componentJsonArray);
            responseJson.add("library",libraryJsonArray);
            dataManager.closeConection();
        }catch (IOException e) {
            responseJson.addProperty("responseType","Error");
            responseJson.addProperty("responseMessage",e.getMessage());
            log.error("enterJarsNew(IOException) - " + e.getMessage());
        }catch (Exception e) {
            responseJson.addProperty("responseType","Error");
            responseJson.addProperty("responseMessage",e.getMessage());
            log.error("enterJarsNew(Exception) - " + e.getMessage());
        }
        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin",origin)
                .header("Access-Control-Allow-Credentials",true)
                .build();
    }

    @GET
    @Path("/getLicense")
    @Produces("application/json")
    public Response getLicenseResource(@Context Request request) {

        JsonObject responseJson = new JsonObject();
        JsonObject errorJson = new JsonObject();
        Session session = request.getSession();
        boolean isValid = Boolean.valueOf(session.getAttribute(VALID_USER).toString());
        String origin = "";
        String driver,url,userName,password,productName,productVersion,path;
        String fileName;
        try{
            Configuration configuration = configurationReader.getConfigurations();
            origin = configuration.getOrigin();
            if(!isValid){
                errorJson.addProperty("responseType","Error");
                errorJson.addProperty("responseMessage","Invalid User Request");
                return Response.ok(errorJson, MediaType.APPLICATION_JSON)
                        .header("Access-Control-Allow-Origin",origin)
                        .header("Access-Control-Allow-Credentials",true)
                        .build();
            }
            driver = configuration.getDriver();
            url = configuration.getUrl();
            userName = configuration.getUserName();
            password = configuration.getPassword();
            JarHolder jarHolder = (JarHolder)session.getAttribute(JAR_HOLDER);
            productName = jarHolder.getProductName();
            productVersion = jarHolder.getProductVersion();
            path = session.getAttribute(FILE_PATH).toString();
            path = path.substring(0, path.length() - 4);
            path = path + "/";
            fileName = "LICENSE("+productName+"-"+ productVersion+").TXT";
            session.setAttribute(LICENSE_PATH, path);
            session.setAttribute(LICENSE_FILE_NAME,fileName);
            LicenseFileGenerator licenseFileGenerator = new LicenseFileGenerator(driver,url,userName,password);
            licenseFileGenerator.generateLicenceFile(productName,productVersion,path);
            responseJson.addProperty("responseType","Done");
            responseJson.addProperty("responseMessage","Done");
        }catch (Exception e) {
            responseJson.addProperty("responseType","Error");
            responseJson.addProperty("responseMessage",e.getMessage());
            log.error("getLicense(Exception) - " + e.getMessage());
        }
        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin",origin)
                .header("Access-Control-Allow-Credentials",true)
                .build();
    }

    @GET
    @Path("/downloadLicense")
    public Response getFile(@Context Request request) {
        String origin = "";
        String mountPath;
        JsonObject errorJson = new JsonObject();
        Session session = request.getSession();
        boolean isValid = Boolean.valueOf(session.getAttribute(VALID_USER).toString());
        try{
            Configuration configuration = configurationReader.getConfigurations();
            if(!isValid){
                errorJson.addProperty("responseType","Error");
                errorJson.addProperty("responseMessage","Invalid User Request");
                return Response.ok(errorJson, MediaType.APPLICATION_JSON)
                        .header("Access-Control-Allow-Origin",origin)
                        .header("Access-Control-Allow-Credentials",true)
                        .build();
            }
            origin = configuration.getOrigin();
            mountPath = session.getAttribute(LICENSE_PATH).toString();
            String fileName = session.getAttribute(LICENSE_FILE_NAME).toString();
            File file = Paths.get(mountPath, fileName).toFile();
            if (file.exists()) {
                return Response.ok(file)
                        .header("Access-Control-Allow-Origin",origin)
                        .header("Access-Control-Allow-Credentials",true)
                        .build();
            }else{
                log.error("downloadLicense - license file doesn't exists");
            }

        }catch (FileNotFoundException e){
            log.error("downloadLicense(FileNotFoundException) - " + e.getMessage());
        }catch (IOException e){
            log.error("downloadLicense(IOException) - " + e.getMessage());
        }

        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET
    @Path("/selectWaitingLicenseRequests")
    @Produces("application/json")
    public Response selectWaitingLicenseRequests(@Context Request request) {

        JsonObject responseJson = new JsonObject();
        String origin = "";
        String driver,url,userName,password;
        try{
            Configuration configuration = configurationReader.getConfigurations();
            origin = configuration.getOrigin();
            driver = configuration.getDriver();
            url = configuration.getUrl();
            userName = configuration.getUserName();
            password = configuration.getPassword();
            DataManager dataManager = new DataManager(driver,url,userName,password);
            JsonArray waitingRequests = dataManager.selectWaitingLicenseRequests();
            responseJson.addProperty("responseType","");
            responseJson.addProperty("responseMessage","Done");
            responseJson.add("responseData",waitingRequests);
            dataManager.closeConection();

        }catch (Exception e) {
            responseJson.addProperty("responseType","Error");
            responseJson.addProperty("responseMessage",e.getMessage());
            log.error("selectWaitingLicenseRequests(Exception) - " + e.getMessage());
        }
        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin",origin)
                .header("Access-Control-Allow-Credentials",true)
                .build();
    }

    @GET
    @Path("/selectWaitingComponents")
    @Produces("application/json")
    public Response selectWaitingComponents(@Context Request request,@QueryParam("licenseRequestId") int licenseRequestId) {

        JsonObject responseJson = new JsonObject();
        JsonObject errorJson = new JsonObject();
        Session session = request.getSession();
        boolean isValid = Boolean.valueOf(session.getAttribute(VALID_USER).toString());
        String origin = "";
        String driver,url,userName,password;
        try{
            Configuration configuration = configurationReader.getConfigurations();
            origin = configuration.getOrigin();
            if(!isValid){
                errorJson.addProperty("responseType","Error");
                errorJson.addProperty("responseMessage","Invalid User Request");
                return Response.ok(errorJson, MediaType.APPLICATION_JSON)
                        .header("Access-Control-Allow-Origin",origin)
                        .header("Access-Control-Allow-Credentials",true)
                        .build();
            }
            driver = configuration.getDriver();
            url = configuration.getUrl();
            userName = configuration.getUserName();
            password = configuration.getPassword();
            DataManager dataManager = new DataManager(driver,url,userName,password);
            JsonArray waitingComponents = dataManager.selectWaitingLicenseComponents(licenseRequestId);
            responseJson.addProperty("responseType","Done");
            responseJson.addProperty("responseMessage","Done");
            responseJson.add("responseData",waitingComponents);
            dataManager.closeConection();


        }catch (Exception e) {
            responseJson.addProperty("responseType","Error");
            responseJson.addProperty("responseMessage",e.getMessage());
            log.error("selectWaitingComponents(Exception) - " + e.getMessage());
        }
        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin",origin)
                .header("Access-Control-Allow-Credentials",true)
                .build();

    }

    @GET
    @Path("/selectWaitingLibraries")
    @Produces("application/json")
    public Response selectWaitingLibraries(@Context Request request,@QueryParam("licenseRequestId") int licenseRequestId) {

        JsonObject responseJson = new JsonObject();
        JsonObject errorJson = new JsonObject();
        Session session = request.getSession();
        boolean isValid = Boolean.valueOf(session.getAttribute(VALID_USER).toString());
        String origin = "";
        String driver,url,userName,password;
        try{
            Configuration configuration = configurationReader.getConfigurations();
            origin = configuration.getOrigin();
            if(!isValid){
                errorJson.addProperty("responseType","Error");
                errorJson.addProperty("responseMessage","Invalid User Request");
                return Response.ok(errorJson, MediaType.APPLICATION_JSON)
                        .header("Access-Control-Allow-Origin",origin)
                        .header("Access-Control-Allow-Credentials",true)
                        .build();
            }
            driver = configuration.getDriver();
            url = configuration.getUrl();
            userName = configuration.getUserName();
            password = configuration.getPassword();
            DataManager dataManager = new DataManager(driver,url,userName,password);
            JsonArray waitingLibraries = dataManager.selectWaitingLicenseLibraries(licenseRequestId);
            responseJson.addProperty("responseType","Done");
            responseJson.addProperty("responseMessage","Done");
            responseJson.add("responseData",waitingLibraries);
            dataManager.closeConection();

        }catch (Exception e) {
            responseJson.addProperty("responseType","Error");
            responseJson.addProperty("responseMessage",e.getMessage());
            log.error("selectWaitingLibraries(Exception) - " + e.getMessage());
        }
        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin",origin)
                .header("Access-Control-Allow-Credentials",true)
                .build();
    }

    @POST
    @Path("/acceptLicenseRequest")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @OPTIONS
    public Response acceptLicenseRequestResource(@Context Request request,String stringPayload){

        String origin = "";
        JsonObject responseJson = new JsonObject();
        JsonObject errorJson = new JsonObject();
        JsonObject requestJson;
        JsonArray componentsJson;
        JsonArray librariesJson;
        JsonParser jsonParser = new JsonParser();
        String driver,url,userName,password;
        int productId,requestId,responseCode = 0;
        String bpmnUrlString,bpmnToken,jwt;
        HttpsURLConnection connection;
        try{
            Configuration configuration = configurationReader.getConfigurations();
            origin = configuration.getOrigin();
            driver = configuration.getDriver();
            url = configuration.getUrl();
            userName = configuration.getUserName();
            password = configuration.getPassword();
            DataManager dataManager = new DataManager(driver,url,userName,password);
            requestJson = jsonParser.parse(stringPayload).getAsJsonObject();
            componentsJson = requestJson.getAsJsonArray("components");
            librariesJson = requestJson.getAsJsonArray("libraries");
            productId = requestJson.get("productId").getAsInt();
            requestId = requestJson.get("requestId").getAsInt();
            jwt = requestJson.get("token").getAsString();
            boolean isValidAdmin = dataManager.isLicenseAdmin(jwt);
            if(!isValidAdmin){
                errorJson.addProperty("responseType","Error");
                errorJson.addProperty("responseMessage","Invalid User Request");
                dataManager.closeConection();
                return Response.ok(errorJson, MediaType.APPLICATION_JSON)
                        .header("Access-Control-Allow-Origin",origin)
                        .header("Access-Control-Allow-Credentials",true)
                        .build();
            }
            dataManager.updateLicenseRequestAccept(requestId);
            for(int i = 0; i < componentsJson.size(); i++){
                String name = componentsJson.get(i).getAsJsonObject().get("TC_NAME").getAsString();
                String fileName = componentsJson.get(i).getAsJsonObject().get("TC_FILE_NAME").getAsString();
                String version = componentsJson.get(i).getAsJsonObject().get("TC_VERSION").getAsString();
                int licenseId = componentsJson.get(i).getAsJsonObject().get("licenseId").getAsInt();
                String licenseKey = dataManager.selectLicenseFromId(licenseId);
                dataManager.insertComponent(name,fileName,version);
                dataManager.insertProductComponent(fileName,productId);
                dataManager.insertComponentLicense(fileName,licenseKey);
            }

            for(int i = 0; i < librariesJson.size(); i++){
                String name = librariesJson.get(i).getAsJsonObject().get("TEMPLIB_NAME").getAsString();
                String fileName = librariesJson.get(i).getAsJsonObject().get("TEMPLIB_FILE_NAME").getAsString();
                String version = librariesJson.get(i).getAsJsonObject().get("TEMPLIB_VERSION").getAsString();
                String type = librariesJson.get(i).getAsJsonObject().get("TEMPLIB_TYPE").getAsString();
                String parent = librariesJson.get(i).getAsJsonObject().get("TEMPLIB_PARENT").getAsString();
                int licenseId = librariesJson.get(i).getAsJsonObject().get("licenseId").getAsInt();
                String licenseKey = dataManager.selectLicenseFromId(licenseId);
                int libId = dataManager.insertLibrary(name,fileName,version,type);
                dataManager.insertLibraryLicense(licenseKey,Integer.toString(libId));
                if(type == "jarinbundle"){
                    dataManager.insertComponentLibrary(parent,libId);
                }else{
                    dataManager.insertProductLibrary(libId,productId);
                }
            }
            String taskId = dataManager.selectLicenseRequestTaskIdFromId(requestId);
            JsonArray variables = new JsonArray();
            JsonObject acceptJson = new JsonObject();
            JsonObject tokenJson = new JsonObject();
            JsonObject bpmnRequestJson = new JsonObject();
            acceptJson.addProperty("name","outputType");
            acceptJson.addProperty("value","Accept");
            tokenJson.addProperty("name","jwToken");
            tokenJson.addProperty("value",jwt);
            variables.add(acceptJson);
            variables.add(tokenJson);
            bpmnRequestJson.addProperty("action","complete");
            bpmnRequestJson.add("variables",variables);

            bpmnUrlString = configuration.getBpmnUrl() + "bpmn/runtime/tasks/" + taskId;
            bpmnToken = configuration.getBpmnToken();
            URL bpmnUrl = new URL(bpmnUrlString);
            connection = (HttpsURLConnection) bpmnUrl.openConnection();
            connection.setHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", bpmnToken);
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream (
                    connection.getOutputStream());
            wr.writeBytes(bpmnRequestJson.toString());
            wr.close();

            //Get Response
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            responseCode = connection.getResponseCode();
            log.info(Integer.toString(responseCode));
            if(responseCode == 200 || responseCode == 201){
                responseJson.addProperty("responseType","Done");
                responseJson.addProperty("responseMessage","Done");
            }else{
                responseJson.addProperty("responseType","Error");
                responseJson.addProperty("responseMessage",("Status code - " + Integer.toString(responseCode)));
                log.error(response.toString());
            }
            rd.close();
            is.close();
            dataManager.closeConection();

        }catch (IOException e) {
            responseJson.addProperty("responseType","Error");
            responseJson.addProperty("responseMessage",e.getMessage());
            log.error("acceptLicenseRequest(IOException) - " + e.getMessage());
        }catch (Exception e) {
            responseJson.addProperty("responseType","Error");
            responseJson.addProperty("responseMessage",e.getMessage());
            log.error("acceptLicenseRequest(Exception) - " + e.getMessage());
        }
        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin",origin)
                .header("Access-Control-Allow-Credentials",true)
                .build();
    }

    @POST
    @Path("/rejectLicenseRequest")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @OPTIONS
    public Response rejectLicenseRequestResource(@Context Request request,String stringPayload){

        String origin = "";
        JsonObject responseJson = new JsonObject();
        JsonObject requestJson;
        JsonObject errorJson = new JsonObject();
        JsonParser jsonParser = new JsonParser();
        String driver,url,userName,password,jwt;
        int productId,requestId,responseCode = 0;
        String bpmnUrlString,bpmnToken,rejectBy,rejectReason;
        HttpsURLConnection connection;
        try{
            Configuration configuration = configurationReader.getConfigurations();
            origin = configuration.getOrigin();
            driver = configuration.getDriver();
            url = configuration.getUrl();
            userName = configuration.getUserName();
            password = configuration.getPassword();
            DataManager dataManager = new DataManager(driver,url,userName,password);
            requestJson = jsonParser.parse(stringPayload).getAsJsonObject();
            rejectBy = requestJson.get("rejectBy").getAsString();
            rejectReason = requestJson.get("rejectReason").getAsString();
            productId = requestJson.get("productId").getAsInt();
            requestId = requestJson.get("requestId").getAsInt();
            jwt = requestJson.get("token").getAsString();
            boolean isValidAdmin = dataManager.isLicenseAdmin(jwt);
            if(!isValidAdmin){
                errorJson.addProperty("responseType","Error");
                errorJson.addProperty("responseMessage","Invalid User Request");
                dataManager.closeConection();
                return Response.ok(errorJson, MediaType.APPLICATION_JSON)
                        .header("Access-Control-Allow-Origin",origin)
                        .header("Access-Control-Allow-Credentials",true)
                        .build();
            }
            dataManager.updateLicenseRequestReject(rejectBy,requestId);
            String taskId = dataManager.selectLicenseRequestTaskIdFromId(requestId);
            JsonArray variables = new JsonArray();
            JsonObject acceptJson = new JsonObject();
            JsonObject rejectByJson = new JsonObject();
            JsonObject rejectReasonJson = new JsonObject();
            JsonObject bpmnRequestJson = new JsonObject();
            acceptJson.addProperty("name","outputType");
            acceptJson.addProperty("value","Reject");
            rejectByJson.addProperty("name","rejectBy");
            rejectByJson.addProperty("value",rejectBy);
            rejectReasonJson.addProperty("name","reasonForReject");
            rejectReasonJson.addProperty("value",rejectReason);
            variables.add(acceptJson);
            variables.add(rejectByJson);
            variables.add(rejectReasonJson);
            bpmnRequestJson.addProperty("action","complete");
            bpmnRequestJson.add("variables",variables);

            bpmnUrlString = configuration.getBpmnUrl() + "bpmn/runtime/tasks/" + taskId;
            bpmnToken = configuration.getBpmnToken();
            URL bpmnUrl = new URL(bpmnUrlString);
            connection = (HttpsURLConnection) bpmnUrl.openConnection();
            connection.setHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", bpmnToken);
            connection.setUseCaches(false);
            connection.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream (
                    connection.getOutputStream());
            wr.writeBytes(bpmnRequestJson.toString());
            wr.close();
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
            String line;
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();
            is.close();
            responseCode = connection.getResponseCode();
            if(responseCode == 200 || responseCode == 201){
                responseJson.addProperty("responseType","Done");
                responseJson.addProperty("responseMessage","Done");
            }else{
                responseJson.addProperty("responseType","Error");
                responseJson.addProperty("responseMessage",("Status code - " + Integer.toString(responseCode)));
                log.error(response.toString());
            }
            dataManager.closeConection();

        }catch (IOException e) {
            responseJson.addProperty("responseType","Error");
            responseJson.addProperty("responseMessage",e.getMessage());
            log.error("rejectLicenseRequest(IOException) - " + e.getMessage());
        }catch (Exception e) {
            responseJson.addProperty("responseType","Error");
            responseJson.addProperty("responseMessage",e.getMessage());
            log.error("rejectLicenseRequest(Exception) - " + e.getMessage());
        }
        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin",origin)
                .header("Access-Control-Allow-Credentials",true)
                .build();
    }

    @POST
    @Path("/sendLicense")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendLicenseResource(@Context Request request,LicenseRequest licenseRequest) {

        JsonObject responseJson = new JsonObject();
        JsonObject errorJson = new JsonObject();
        String driver,url,userName,password,path,origin;
        String filePath,fileName,jwt;
        try {
            Configuration configuration = configurationReader.getConfigurations();
            origin = configuration.getOrigin();
            driver = configuration.getDriver();
            url = configuration.getUrl();
            userName = configuration.getUserName();
            password = configuration.getPassword();
            path = configuration.getPath();
            jwt = request.getHeader("adminJwt");
            DataManager dataManager = new DataManager(driver,url,userName,password);
            boolean isValidAdmin = dataManager.isLicenseAdmin(jwt);
            if(!isValidAdmin){
                errorJson.addProperty("responseType","Error");
                errorJson.addProperty("responseMessage","Invalid User Request");
                dataManager.closeConection();
                return Response.ok(errorJson, MediaType.APPLICATION_JSON)
                        .header("Access-Control-Allow-Origin",origin)
                        .header("Access-Control-Allow-Credentials",true)
                        .build();
            }
            LicenseFileGenerator licenseFileGenerator = new LicenseFileGenerator(driver, url, userName, password);
            licenseFileGenerator.generateLicenceFile(licenseRequest.getProductName(), licenseRequest.getProductVersion(), path);
            fileName = "LICENSE("+licenseRequest.getProductName()+"-"+licenseRequest.getProductVersion()+").TXT";
            filePath = path + fileName;

            String to = licenseRequest.getMailList();
            String from = configuration.getEmailAddress();
            final String username = configuration.getEmailAddress();
            final String emailPassword = configuration.getEmailPassword();
            String host = configuration.getSmtpHost();
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", host);
            props.put("mail.smtp.port", configuration.getSmtpPort());

            javax.mail.Session session = javax.mail.Session.getInstance(props,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username, emailPassword);
                        }
                    });
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(to));
            String subject = "License File for " + licenseRequest.getProductName() + " - " + licenseRequest.getProductVersion();
            message.setSubject(subject);
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText("This is the license file for above $subject");
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);
            messageBodyPart = new MimeBodyPart();
            String filename = filePath;
            DataSource source = new FileDataSource(filename);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(fileName);
            multipart.addBodyPart(messageBodyPart);
            message.setContent(multipart);
            Transport.send(message);
            dataManager.closeConection();


        }catch (IOException ex){
            log.error("sendLicense(IOException) - " + ex.getMessage());
        }catch (ClassNotFoundException ex){
            log.error("sendLicense(ClassNotFoundException) - " + ex.getMessage());
        }catch (SQLException ex){
            log.error("sendLicense(SQLException) - " + ex.getMessage());
        }catch (MessagingException ex) {
            log.error("sendLicense(MessagingException) - " + ex.getMessage());
        }catch (Exception ex) {
            log.error("sendLicense(Exception) - " + ex.getMessage());
        }
        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .build();
    }

    @GET
    @Path("/temp")
    public Response temp(@Context Request request) {

        try{
            Configuration configuration = configurationReader.getConfigurations();
            File folder = new File("./");
            File[] listOfFiles = folder.listFiles();

            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {
                } else if (listOfFiles[i].isDirectory()) {

                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }


        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET
    @Path("/test")
    public String test(@Context Request request) {
        log.info("sample");
        return "Hello Java";


    }

}
