/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.internal.apps.license.manager.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.workingdogs.village.DataSetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.internal.apps.license.manager.impl.enterData.EnterData;
import org.wso2.internal.apps.license.manager.impl.main.JarHolder;
import org.wso2.internal.apps.license.manager.impl.main.LicenseFileGenerator;
import org.wso2.internal.apps.license.manager.impl.main.Main;
import org.wso2.internal.apps.license.manager.impl.main.MyJar;
import org.wso2.internal.apps.license.manager.impl.models.DataManager;
import org.wso2.internal.apps.license.manager.impl.models.LicenseRequest;
import org.wso2.internal.apps.license.manager.impl.models.ResponseModel;
import org.wso2.internal.apps.license.manager.util.Constants;
import org.wso2.internal.apps.license.manager.util.LicenseManagerUtils;
import org.wso2.msf4j.MicroservicesRunner;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Session;
import org.wso2.msf4j.util.SystemVariableUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

//import java.util.Arrays;
//import java.util.logging.Logger;

//import org.wso2.internal.apps.license.manager.conf.Configuration;
//import org.wso2.internal.apps.license.manager.conf.ConfigurationReader;

/**
 * This is the Microservice resource class.
 * See <a href="https://github.com/wso2/msf4j#getting-started">https://github.com/wso2/msf4j#getting-started</a>
 * for the usage of annotations.
 *
 * @since 1.0-SNAPSHOT
 */
@Path("/")
public class MainService {

    private static final Logger log = LoggerFactory.getLogger(MicroservicesRunner.class);
    private ConcurrentHashMap<String, JarHolder> jarHolderConcurrentHashMap = new ConcurrentHashMap<>();
    private String packPath;
    private String session_email = "pamodaaw@wso2.com";

    @GET
    @Path("/selectLicense")
    @Produces("application/json")
    public Response selectLicenseResource(@Context Request request) {

        JsonObject responseJson = new JsonObject();
        ResponseModel response = new ResponseModel();
        String databaseDriver = SystemVariableUtil.getValue(Constants.DATABASE_DRIVER, null);
        String databaseUrl = SystemVariableUtil.getValue(Constants.DATABASE_URL, null);
        String databaseUsername = SystemVariableUtil.getValue(Constants.DATABASE_USERNAME, null);
        String databasePassword = SystemVariableUtil.getValue(Constants.DATABASE_PASSWORD, null);

        try {
            DataManager dataManager = new DataManager(databaseDriver,databaseUrl, databaseUsername, databasePassword);
            JsonArray jsonArray = dataManager.selectAllLicense();
            responseJson.addProperty("responseType", "Done");
            responseJson.addProperty("responseMessage", "Done");
            responseJson.add("responseData", jsonArray);
            dataManager.closeConection();
        } catch (SQLException | ClassNotFoundException | DataSetException e) {
//            response.setResponseType("Error");
//            response.setResponseMessage("Failed to retrieve data from the database.");
            responseJson.addProperty("responseType", "Error");
            responseJson.addProperty("responseMessage", "Failed to retrieve data from the database.");
            log.error("Failed to retrieve data from the database. " + e.getMessage(), e);
        }
        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Credentials", true)
                .build();
    }

    @GET
    @Path("/getPacks")
    public Response listUploadedPacks() {

        ArrayList<String> listOfPacks = new ArrayList<>();
        ResponseModel response = new ResponseModel();
        JsonObject responseJson = new JsonObject();
        JsonArray responseData = new JsonArray();

        String pathToStorage = SystemVariableUtil.getValue(Constants.FILE_UPLOAD_PATH, "/home/pamoda/programming/license_manager/test/");

        File folder = new File(pathToStorage);
        File[] listOfFiles = folder.listFiles();

        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.isFile() && file.getName().endsWith(".zip")) {
                    listOfPacks.add(file.getName());
                }
            }
        }

        for (int i = 0; i < listOfPacks.size(); i++) {
            JsonObject ob = new JsonObject();
            ob.addProperty("name", listOfPacks.get(i));
            responseData.add(ob);
        }
        responseJson.addProperty("responseType", "Done");
        responseJson.addProperty("responseMessage", "Done");
        responseJson.add("responseData", responseData);

        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Credentials", true)
                .build();
    }

    @POST
    @Path("/extractJars")
    @Produces("application/json")
    public Response extractJars(@Context Request request, String stringPayload) {

        Main main = new Main();
        JsonObject responseJson = new JsonObject();
        JsonArray nameMissingJars = new JsonArray();
        String pathToStorage = SystemVariableUtil.getValue(Constants.FILE_UPLOAD_PATH, "/home/pamoda/programming/license_manager/test/");
        String zipFilePath = pathToStorage + stringPayload;
        String filePath = zipFilePath.substring(0, zipFilePath.lastIndexOf('.'));
        File zipFile = new File(zipFilePath);
        File dir = new File(filePath);
        try {
            LicenseManagerUtils.unzip(zipFile.getAbsolutePath(), dir.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            JarHolder jarHolder = main.checkJars(filePath);
            // TODO: 4/9/18 obtain the email from the session
            jarHolderConcurrentHashMap.put(session_email, jarHolder);
            log.info("Jar extraction complete.");
            List<MyJar> errorJarList = jarHolder.getErrorJarList();
            for (int i = 0; i < errorJarList.size(); i++) {
                JsonObject currentJar = new JsonObject();
                currentJar.addProperty("id", i);
                currentJar.addProperty("name", errorJarList.get(i).getProjectName());
                currentJar.addProperty("version", errorJarList.get(i).getVersion());
                nameMissingJars.add(currentJar);
            }
            responseJson.addProperty("responseType", "Done");
            responseJson.addProperty("responseMessage", "Done");
            responseJson.add("responseData", nameMissingJars);

        } catch (IOException e) {
            responseJson.addProperty("responseType", "Error");
            responseJson.addProperty("responseMessage", e.getMessage());
            log.error("checkJars(IOException) - " + e.getMessage());
        } catch (ClassNotFoundException e) {
            responseJson.addProperty("responseType", "Error");
            responseJson.addProperty("responseMessage", e.getMessage());
            log.error("checkJars(ClassNotFoundException) - " + e.getMessage());
        } catch (NullPointerException e) {
            responseJson.addProperty("responseType", "Error");
            responseJson.addProperty("responseMessage", e.getMessage());
            log.error("checkJars(NullPointerException) - " + e.getMessage());
            log.error("checkJars(Exception) - " + e.getMessage());
        }
        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Credentials", true)
                .build();
    }

    @POST
    @Path("/enterJars")
    @Produces("application/json")
    public Response enterJarsResource(@Context Request request, String stringPayload) {

        JsonObject responseJson = new JsonObject();
        Main main = new Main();
        JsonParser jsonParser = new JsonParser();
        int licenseRequestId = 0, productId, responseCode = 0;
        // TODO: 4/9/18 default license ID;
        int licenseId = 31;
        String databaseDriver = SystemVariableUtil.getValue(Constants.DATABASE_DRIVER, null);
        String databaseUrl = SystemVariableUtil.getValue(Constants.DATABASE_URL, null);
        String databaseUserName = SystemVariableUtil.getValue(Constants.DATABASE_USERNAME, null);
        String databasePassword = SystemVariableUtil.getValue(Constants.DATABASE_PASSWORD, null);
        String requestBy;
        JsonObject errorJson = new JsonObject();
        JsonArray errorData = new JsonArray();
        Session session = request.getSession();
        try {
            DataManager dataManager = new DataManager(databaseDriver, databaseUrl, databaseUserName, databasePassword);
            JsonElement jsonElement = jsonParser.parse(stringPayload);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonArray jsonArray = jsonObject.get("jars").getAsJsonArray();
            // TODO: 3/28/18 change
            requestBy = session_email;
            JarHolder jarHolder = jarHolderConcurrentHashMap.get(session_email);
            EnterData enterData = main.enterData(jarHolder);
            List<MyJar> componentList = enterData.getLicenseMissingComponents();
            List<MyJar> libraryList = enterData.getLicenseMissingLibraries();
            productId = enterData.getProductId();
            JsonArray componentJsonArray = new JsonArray();
            JsonArray libraryJsonArray = new JsonArray();
            if (componentList.size() > 0 || libraryList.size() > 0) {
                licenseRequestId = dataManager.insertLicenseRequest(requestBy, productId);
            }

            for (int i = 0; i < componentList.size(); i++) {
                JsonObject component = new JsonObject();
                component.addProperty("id", i);
                component.addProperty("name", componentList.get(i).getProjectName());
                component.addProperty("version", componentList.get(i).getVersion());
                component.addProperty("license", licenseId);
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

            for (int i = 0; i < libraryList.size(); i++) {
                JsonObject library = new JsonObject();
                String parent = "";
                library.addProperty("id", i);
                library.addProperty("name", libraryList.get(i).getProjectName());
                library.addProperty("version", libraryList.get(i).getVersion());
                library.addProperty("license", licenseId);
                libraryJsonArray.add(library);

                if (libraryList.get(i).getParent() != null) {
                    parent = libraryList.get(i).getParent().getProjectName();
                }
                String type = (libraryList.get(i).getParent() == null) ? ((libraryList.get(i).isBundle()) ? "bundle"
                        : "jar") : "jarinbundle";
                dataManager.insertTempLib(
                        libraryList.get(i).getProjectName(),
                        libraryList.get(i).getVersion(),
                        libraryList.get(i).getJarFile().getName(),
                        parent,
                        type,
                        licenseRequestId
                );
            }

            responseJson.addProperty("responseType", "Done");
            responseJson.addProperty("responseMessage", "Done");
            responseJson.add("component", componentJsonArray);
            responseJson.add("library", libraryJsonArray);
            dataManager.closeConection();
        } catch (IOException e) {
            responseJson.addProperty("responseType", "Error");
            responseJson.addProperty("responseMessage", e.getMessage());
            log.error("enterJarsNew(IOException) - " + e.getMessage());
        } catch (DataSetException | SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Credentials", true)
                .build();
    }

    @POST
    @Path("/validateUser")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @OPTIONS
    public Response validateUserResource(@Context Request request, String stringPayload) {

        JsonObject responseJson = new JsonObject();
        String token;
        String message;
        String key;
        String email;
        JsonObject requestJson;
        JsonParser jsonParser = new JsonParser();
        byte[] keyBytes, payloadBytes;
        String[] tokenValues;
        boolean returnValue = false;
        JsonObject payloadJson;
        Session session = request.getSession();
        // TODO: 3/29/18 for local validation
        // TODO: 3/28/18  validate the user
        responseJson.addProperty("responseType", "Done");
        responseJson.addProperty("isValid", returnValue);
        responseJson.addProperty("responseMessage", "Done");
        // TODO: 3/29/18 actual usesr validation happens here
//            session.setAttribute(VALID_USER, returnValue);

        email = "pamoda@wso2.com";
        log.info("Login - " + email);

        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Credentials", true)
                .build();
    }

    @GET
    @Path("/getUserDetails")
    @Produces("application/json")
    public Response getUserDetails(@Context Request request) {

        JsonObject response = new JsonObject();
        response.addProperty("isValid", true);
        response.addProperty("userEmail", "pamoda@wso2.com");
        response.addProperty("isAnyAdmin", true);
        response.addProperty("isRepositoryAdmin", true);
        response.addProperty("isLibraryAdmin", true);
        response.addProperty("libraryUserDetails", "");
        return Response.ok(response, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Credentials", true)
                .build();
    }

    @GET
    @Path("/getLicense")
    @Produces("application/json")
    public Response getLicenseResource(@Context Request request) {

        JsonObject responseJson = new JsonObject();
        String databaseUrl = SystemVariableUtil.getValue(Constants.DATABASE_URL, null);
        String databaseDriver = SystemVariableUtil.getValue(Constants.DATABASE_DRIVER, null);
        String databaseUsername = SystemVariableUtil.getValue(Constants.DATABASE_USERNAME, null);
        String databasePassword = SystemVariableUtil.getValue(Constants.DATABASE_PASSWORD, null);
        String fileUploadPath = SystemVariableUtil.getValue(Constants.FILE_UPLOAD_PATH, "/home/pamoda/programming/license_manager/test/");
        String productName;
        String productVersion;
        String licenseFilePath;
        String fileName;
        try {
            // TODO: 4/9/18 get the session email
            JarHolder jarHolder = jarHolderConcurrentHashMap.get(session_email);
            productName = jarHolder.getProductName();
            productVersion = jarHolder.getProductVersion();
            LicenseFileGenerator licenseFileGenerator = new LicenseFileGenerator(databaseDriver, databaseUrl,
                    databaseUsername, databasePassword);
            licenseFileGenerator.generateLicenceFile(productName, productVersion, fileUploadPath);
            responseJson.addProperty("responseType", "Done");
            responseJson.addProperty("responseMessage", "Done");
        } catch (Exception e) {
            responseJson.addProperty("responseType", "Error");
            responseJson.addProperty("responseMessage", e.getMessage());
            log.error("getLicense(Exception) - " + e.getMessage());
        }
        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Credentials", true)
                .build();
    }

    @GET
    @Path("/downloadLicense")
    public Response getFile(@Context Request request) {

        String mountPath = SystemVariableUtil.getValue(Constants.FILE_UPLOAD_PATH, "/home/pamoda/programming/license_manager/test/");
        JarHolder jarHolder = jarHolderConcurrentHashMap.get(session_email);
        String productName = jarHolder.getProductName();
        String productVersion = jarHolder.getProductVersion();
        String fileName = "LICENSE(" + productName + "-" + productVersion + ").TXT";

        File file = Paths.get(mountPath, fileName).toFile();
        if (file.exists()) {
//            LicenseManagerUtils.deleteFolder(mountPath+fileName);
            LicenseManagerUtils.deleteFolder(mountPath+productName+"-"+productVersion+".zip");
            LicenseManagerUtils.deleteFolder(mountPath+productName+"-"+productVersion);
            return Response.ok(file)
                    .header("Access-Control-Allow-Credentials", true)
                    .build();
        } else {
            log.error("downloadLicense - license file doesn't exists");
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("/selectWaitingLicenseRequests")
    @Produces("application/json")
    public Response selectWaitingLicenseRequests(@Context Request request) {

        JsonObject responseJson = new JsonObject();
        String databaseDriver = SystemVariableUtil.getValue(Constants.DATABASE_DRIVER, null);
        String databaseUrl = SystemVariableUtil.getValue(Constants.DATABASE_URL, null);
        String databaseUsername = SystemVariableUtil.getValue(Constants.DATABASE_USERNAME, null);
        String databasePassword = SystemVariableUtil.getValue(Constants.DATABASE_PASSWORD, null);
        try {
            DataManager dataManager = new DataManager(databaseDriver, databaseUrl, databaseUsername, databasePassword);
            JsonArray waitingRequests = dataManager.selectWaitingLicenseRequests();
            responseJson.addProperty("responseType", "");
            responseJson.addProperty("responseMessage", "Done");
            responseJson.add("responseData", waitingRequests);
            dataManager.closeConection();

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Credentials", true)
                .build();
    }

    @GET
    @Path("/selectWaitingComponents")
    @Produces("application/json")
    public Response selectWaitingComponents(@Context Request request, @QueryParam("licenseRequestId") int
            licenseRequestId) {

        JsonObject responseJson = new JsonObject();
        String databaseUrl = SystemVariableUtil.getValue(Constants.DATABASE_URL, null);
        String databaseDriver = SystemVariableUtil.getValue(Constants.DATABASE_DRIVER, null);
        String databaseUsername = SystemVariableUtil.getValue(Constants.DATABASE_USERNAME, null);
        String databasePassword = SystemVariableUtil.getValue(Constants.DATABASE_PASSWORD, null);
        try {
            DataManager dataManager = new DataManager(databaseDriver, databaseUrl, databaseUsername, databasePassword);
            JsonArray waitingComponents = dataManager.selectWaitingLicenseComponents(licenseRequestId);
            responseJson.addProperty("responseType", "Done");
            responseJson.addProperty("responseMessage", "Done");
            responseJson.add("responseData", waitingComponents);
            dataManager.closeConection();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Credentials", true)
                .build();

    }

    @GET
    @Path("/selectWaitingLibraries")
    @Produces("application/json")
    public Response selectWaitingLibraries(@Context Request request, @QueryParam("licenseRequestId") int
            licenseRequestId) {

        JsonObject responseJson = new JsonObject();
        String databaseUrl = SystemVariableUtil.getValue(Constants.DATABASE_URL, null);
        String databaseDriver = SystemVariableUtil.getValue(Constants.DATABASE_DRIVER, null);
        String databaseUsername = SystemVariableUtil.getValue(Constants.DATABASE_USERNAME, null);
        String databasePassword = SystemVariableUtil.getValue(Constants.DATABASE_PASSWORD, null);
        try {
            DataManager dataManager = new DataManager(databaseDriver, databaseUrl, databaseUsername, databasePassword);
            JsonArray waitingLibraries = dataManager.selectWaitingLicenseLibraries(licenseRequestId);
            responseJson.addProperty("responseType", "Done");
            responseJson.addProperty("responseMessage", "Done");
            responseJson.add("responseData", waitingLibraries);
            dataManager.closeConection();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Credentials", true)
                .build();
    }

    @POST
    @Path("/acceptLicenseRequest")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @OPTIONS
    public Response acceptLicenseRequestResource(@Context Request request, String stringPayload) {

        JsonObject responseJson = new JsonObject();
        JsonObject errorJson = new JsonObject();
        JsonObject requestJson;
        JsonArray componentsJson;
        JsonArray librariesJson;
        JsonParser jsonParser = new JsonParser();
        String databaseUrl = SystemVariableUtil.getValue(Constants.DATABASE_URL, null);
        String databaseDriver = SystemVariableUtil.getValue(Constants.DATABASE_DRIVER, null);
        String databaseUsername = SystemVariableUtil.getValue(Constants.DATABASE_USERNAME, null);
        String databasePassword = SystemVariableUtil.getValue(Constants.DATABASE_PASSWORD, null);
        int productId, requestId, responseCode = 0;
        String jwt = null;
        try {
            DataManager dataManager = new DataManager(databaseDriver, databaseUrl, databaseUsername, databasePassword);
            requestJson = jsonParser.parse(stringPayload).getAsJsonObject();
            componentsJson = requestJson.getAsJsonArray("components");
            librariesJson = requestJson.getAsJsonArray("libraries");
            productId = requestJson.get("productId").getAsInt();
            requestId = requestJson.get("requestId").getAsInt();
            dataManager.updateLicenseRequestAccept(requestId);
            for (int i = 0; i < componentsJson.size(); i++) {
                String name = componentsJson.get(i).getAsJsonObject().get("TC_NAME").getAsString();
                String fileName = componentsJson.get(i).getAsJsonObject().get("TC_FILE_NAME").getAsString();
                String version = componentsJson.get(i).getAsJsonObject().get("TC_VERSION").getAsString();
                int licenseId = componentsJson.get(i).getAsJsonObject().get("licenseId").getAsInt();
                String licenseKey = dataManager.selectLicenseFromId(licenseId);
                dataManager.insertComponent(name, fileName, version);
                dataManager.insertProductComponent(fileName, productId);
                dataManager.insertComponentLicense(fileName, licenseKey);
            }

            for (int i = 0; i < librariesJson.size(); i++) {
                String name = librariesJson.get(i).getAsJsonObject().get("TL_NAME").getAsString();
                String fileName = librariesJson.get(i).getAsJsonObject().get("TL_FILE_NAME").getAsString();
                String version = librariesJson.get(i).getAsJsonObject().get("TL_VERSION").getAsString();
                String type = librariesJson.get(i).getAsJsonObject().get("TL_TYPE").getAsString();
                String parent = librariesJson.get(i).getAsJsonObject().get("TL_PARENT").getAsString();
                int licenseId = librariesJson.get(i).getAsJsonObject().get("licenseId").getAsInt();
                String licenseKey = dataManager.selectLicenseFromId(licenseId);
                int libId = dataManager.insertLibrary(name, fileName, version, type);
                dataManager.insertLibraryLicense(licenseKey, Integer.toString(libId));
                if (type == "jarinbundle") {
                    dataManager.insertComponentLibrary(parent, libId);
                } else {
                    dataManager.insertProductLibrary(libId, productId);
                }
            }
            JsonArray variables = new JsonArray();
            JsonObject acceptJson = new JsonObject();
            JsonObject tokenJson = new JsonObject();
            acceptJson.addProperty("name", "outputType");
            acceptJson.addProperty("value", "Accept");
            tokenJson.addProperty("name", "jwToken");
            tokenJson.addProperty("value", jwt);
            variables.add(acceptJson);
            variables.add(tokenJson);
            responseJson.addProperty("responseType", "Done");
            responseJson.addProperty("responseMessage", "Done");

            dataManager.closeConection();

        } catch (DataSetException | SQLException | ClassNotFoundException e) {
            responseJson.addProperty("responseType", "Error");
            responseJson.addProperty("responseMessage", "Failed");
        }
        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Credentials", true)
                .build();
    }

    @POST
    @Path("/rejectLicenseRequest")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @OPTIONS
    public Response rejectLicenseRequestResource(@Context Request request, String stringPayload) {

        JsonObject responseJson = new JsonObject();
        JsonObject requestJson;
        JsonObject errorJson = new JsonObject();
        JsonParser jsonParser = new JsonParser();
        String databaseUrl = SystemVariableUtil.getValue(Constants.DATABASE_URL, null);
        String databaseDriver = SystemVariableUtil.getValue(Constants.DATABASE_DRIVER, null);
        String databaseUsername = SystemVariableUtil.getValue(Constants.DATABASE_USERNAME, null);
        String databasePassword = SystemVariableUtil.getValue(Constants.DATABASE_PASSWORD, null);
        String jwt;
        int productId, requestId, responseCode = 0;
        String bpmnUrlString, bpmnToken, rejectBy, rejectReason;
//        HttpsURLConnection connection;
        try {
            DataManager dataManager = new DataManager(databaseDriver, databaseUrl, databaseUsername, databasePassword);
            requestJson = jsonParser.parse(stringPayload).getAsJsonObject();
            rejectBy = requestJson.get("rejectBy").getAsString();
            rejectReason = requestJson.get("rejectReason").getAsString();
            productId = requestJson.get("productId").getAsInt();
            requestId = requestJson.get("requestId").getAsInt();
            dataManager.updateLicenseRequestReject(rejectBy, requestId);
            dataManager.closeConection();

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Credentials", true)
                .build();
    }

    @POST
    @Path("/sendLicense")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendLicenseResource(@Context Request request, LicenseRequest licenseRequest) {

        JsonObject responseJson = new JsonObject();
        JsonObject errorJson = new JsonObject();
        String databaseUrl = SystemVariableUtil.getValue(Constants.DATABASE_URL, null);
        String databaseDriver = SystemVariableUtil.getValue(Constants.DATABASE_DRIVER, null);
        String databaseUsername = SystemVariableUtil.getValue(Constants.DATABASE_USERNAME, null);
        String databasePassword = SystemVariableUtil.getValue(Constants.DATABASE_PASSWORD, null);
        String fileUploadPath = SystemVariableUtil.getValue(Constants.FILE_UPLOAD_PATH, "/home/pamoda/programming/license_manager/test/");
        String filePath, fileName, jwt;
        try {
            DataManager dataManager = new DataManager(databaseDriver, databaseUrl, databaseUsername, databasePassword);
            LicenseFileGenerator licenseFileGenerator = new LicenseFileGenerator(databaseDriver, databaseUrl,
                    databaseUsername, databasePassword);
            licenseFileGenerator.generateLicenceFile(licenseRequest.getProductName(), licenseRequest
                    .getProductVersion(), fileUploadPath);
            fileName = "LICENSE(" + licenseRequest.getProductName() + "-" + licenseRequest.getProductVersion() + ")" +
                    ".TXT";
            filePath = fileUploadPath + fileName;
            dataManager.closeConection();

        } catch (ClassNotFoundException ex) {
            log.error("sendLicense(ClassNotFoundException) - " + ex.getMessage());
        } catch (SQLException ex) {
            log.error("sendLicense(SQLException) - " + ex.getMessage());
        } catch (Exception ex) {
            log.error("sendLicense(Exception) - " + ex.getMessage());
        }
        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .build();
    }

    @GET
    @Path("/test")
    public Response test(@Context Request request) {

        log.info("sample");
        return Response.ok("Hello Java", MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Credentials", true)
                .build();
    }
}
