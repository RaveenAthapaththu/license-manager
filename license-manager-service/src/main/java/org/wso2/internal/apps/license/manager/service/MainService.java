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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.workingdogs.village.DataSetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.internal.apps.license.manager.impl.enterData.EnterData;
import org.wso2.internal.apps.license.manager.impl.exception.LicenseManagerConfigurationException;
import org.wso2.internal.apps.license.manager.impl.main.JarHolder;
import org.wso2.internal.apps.license.manager.impl.main.LicenseFileGenerator;
import org.wso2.internal.apps.license.manager.impl.main.Main;
import org.wso2.internal.apps.license.manager.impl.main.MyJar;
import org.wso2.internal.apps.license.manager.impl.models.Configuration;
import org.wso2.internal.apps.license.manager.impl.models.DataManager;
import org.wso2.internal.apps.license.manager.impl.models.LicenseRequest;
import org.wso2.internal.apps.license.manager.impl.models.ResponseModel;
import org.wso2.internal.apps.license.manager.util.Constants;
import org.wso2.internal.apps.license.manager.util.FileUtils;
import org.wso2.internal.apps.license.manager.util.LicenseManagerUtil;
import org.wso2.msf4j.MicroservicesRunner;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.Session;
import org.wso2.msf4j.formparam.FormItem;
import org.wso2.msf4j.formparam.FormParamIterator;
import org.wso2.msf4j.util.SystemVariableUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
    private String FILE_PATH = "FILE_PATH";
    private String JAR_HOLDER = "JAR_HOLDER";
    private String ENTER_DATA = "ENTER_DATA";
    private String LICENSE_PATH = "LICENSE_PATH";
    private String LICENSE_FILE_NAME = "LICENSE_FILE_NAME";
    private String VALID_USER = "VALID_USER";
    private JarHolder jarHolder;
    private String packPath;

    @GET
    @Path("/selectLicense")
    @Produces("application/json")
    public Response selectLicenseResource(@Context Request request) {

//        JsonObject responseJson = new JsonObject();
        String origin = null;
        JsonObject responseJson = new JsonObject();
        ResponseModel response = new ResponseModel();
        Gson gson = new Gson();
        Session session = request.getSession();
//        boolean isValid = Boolean.valueOf(session.getAttribute(VALID_USER).toString());
        // TODO: 3/28/18  change
        boolean isValid = true;
        try {
            Configuration configuration = LicenseManagerUtil.loadConfigurations();
            origin = configuration.getClientUrl();
            String driver = configuration.getDatabaseDriver();
            String url = configuration.getDatabaseUrl();
            String userName = configuration.getDatabaseUsername();
            String password = configuration.getDatabasePassword();
            if (!isValid) {
                response.setResponseType("Error");
                response.setResponseMessage("Invalid user request.");
                return Response.ok(gson.toJson(response), MediaType.APPLICATION_JSON)
                        .header("Access-Control-Allow-Origin", origin)
                        .header("Access-Control-Allow-Credentials", true)
                        .build();
            }

            DataManager dataManager = new DataManager(driver, url, userName, password);
//            ArrayList<License> licenses = dataManager.selectAllLicense();
            JsonArray jsonArray = dataManager.selectAllLicense();
            responseJson.addProperty("responseType", "Done");
            responseJson.addProperty("responseMessage", "Done");
            responseJson.add("responseData", jsonArray);
//            response.setResponseType("Done");
//            response.setResponseMessage("Licenses are retrieved from the database.");
//            response.setResponseData(gson.toJson(licenses));
            dataManager.closeConection();
        } catch (LicenseManagerConfigurationException e) {
//            response.setResponseType("Error");
//            response.setResponseMessage("Invalid configurations.");
            responseJson.addProperty("responseType", "Error");
            responseJson.addProperty("responseMessage", "Failed to load the configurations information. ");
            log.error("Failed to load the configurations information. " + e.getMessage(), e);
        } catch (SQLException | ClassNotFoundException | DataSetException e) {
//            response.setResponseType("Error");
//            response.setResponseMessage("Failed to retrieve data from the database.");
            responseJson.addProperty("responseType", "Error");
            responseJson.addProperty("responseMessage", "Failed to retrieve data from the database.");
            log.error("Failed to retrieve data from the database. " + e.getMessage(), e);
        }
        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin", origin)
                .header("Access-Control-Allow-Credentials", true)
                .build();
    }

    @POST
    @Path("/uploadPack")
    @Consumes("multipart/form-data")
    @Produces("application/json")
    public Response uploadPackResource(@Context FormParamIterator formParamIterator, @Context Request request) {

        String origin = null;
        FormItem item = formParamIterator.next();

        Gson gson = new Gson();
        JsonObject response = new JsonObject();

        try {
            Configuration config = LicenseManagerUtil.loadConfigurations();
            String pathToStorage = config.getPathToFileStorage();
            origin = config.getClientUrl();
            new File(pathToStorage).mkdir();
            String fileName = item.getName();
            String zipFilePath = pathToStorage + File.separator + item.getName();
            packPath = pathToStorage + fileName.substring(0, fileName.lastIndexOf('.'));
            Files.copy(item.openStream(), Paths.get(zipFilePath));
            File zipFile = new File(zipFilePath);
            File dir = new File(packPath);
            FileUtils.unzip(zipFile.getAbsolutePath(), dir.getAbsolutePath());

            response.addProperty("responseType", Constants.SUCCESS);
            response.addProperty("message", "Successfully uploaded");
            log.info("File successfully added");
        } catch (NullPointerException e) {
            response.addProperty("responseType", Constants.ERROR);
            response.addProperty("message", "Failed to upload the pack");
            log.error("Pack may be too large to upload " + e.getMessage(), e);
        } catch (FileAlreadyExistsException e) {
            response.addProperty("responseType", Constants.ERROR);
            response.addProperty("message", "Pack already exists");
            log.error("Pack already exits. " + e.getMessage(), e);
        } catch (IOException e) {
            response.addProperty("responseType", Constants.ERROR);
            response.addProperty("message", "Failed to upload the pack");
            log.error("Error while uploading the pack. " + e.getMessage(), e);
        } catch (LicenseManagerConfigurationException e) {
            response.addProperty("responseType", Constants.ERROR);
            response.addProperty("message", "Failed to load configuration details");
            log.error("Error while loading configuration details. " + e.getMessage(), e);
        } finally {
            item.close();
        }
        return Response.ok(response, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin", origin)
                .header("Access-Control-Allow-Credentials", true)
                .build();
    }

    @GET
    @Path("/getPacks")
    public Response listUploadedPacks() {

        String origin = null;
        Gson gson = new Gson();
        ArrayList<String> listOfPacks = new ArrayList<>();
        ResponseModel response = new ResponseModel();

        try {
            Configuration config = LicenseManagerUtil.loadConfigurations();
            String pathToStorage = System.getProperty("LICENSE_GENERATOR_FILE_UPLOAD_PATH");
            SystemVariableUtil.getValue("LICENSE_GENERATOR_FILE_UPLOAD_PATH", "123");
            origin = config.getClientUrl();

            File folder = new File(pathToStorage);
            File[] listOfFiles = folder.listFiles();

            if (listOfFiles != null) {
                for (File file : listOfFiles) {
                    if (file.isFile() && file.getName().endsWith(".zip")) {
                        System.out.println("Zip folder " + file.getName());
                        listOfPacks.add(file.getName());
                    }
                }
            }

        } catch (LicenseManagerConfigurationException e) {
            e.printStackTrace();
        }

        return Response.ok(gson.toJson(listOfPacks), MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin", origin)
                .header("Access-Control-Allow-Credentials", true)
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
        // TODO: 3/28/18 change
        boolean isValid = true;
        try {
            Configuration configuration = LicenseManagerUtil.loadConfigurations();
            origin = configuration.getClientUrl();
            jarHolder = main.checkJars(packPath);
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
        } catch (LicenseManagerConfigurationException e) {
            e.printStackTrace();
        }
        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin", origin)
                .header("Access-Control-Allow-Credentials", true)
                .build();
    }

    @POST
    @Path("/enterJars")
    @Produces("application/json")
    public Response enterJarsResource(@Context Request request, String stringPayload) {

        String origin = "";
        String fileNameWidth = "6%";
        String nameWidth = "6%";
        String versionWidth = "2%";
        String componentTable = "<table style=\"width:40%\"><tr><th style=\"width:" + fileNameWidth + ";" +
                "text-align:left;\">File Name</th><th style=\"width:" + nameWidth + ";text-align:left;\">Name</th><th" +
                " style=\"width:" + versionWidth + ";text-align:left;\">Version</th></tr>";
        String libraryTable = "<table style=\"width:40%\"><tr><th style=\"width:" + fileNameWidth + ";" +
                "text-align:left;\">File Name</th><th style=\"width:" + nameWidth + ";text-align:left;\">Name</th><th" +
                " style=\"width:" + versionWidth + ";text-align:left;\">Version</th></tr>";
        JsonObject responseJson = new JsonObject();
        Main main = new Main();
        JsonParser jsonParser = new JsonParser();
        int licenseId, licenseRequestId = 0, productId, responseCode = 0;
        String driver, url, userName, password, requestBy;
        JsonObject errorJson = new JsonObject();
        JsonArray errorData = new JsonArray();
        Session session = request.getSession();
//        boolean isValid = Boolean.valueOf(session.getAttribute(VALID_USER).toString());
        // TODO: 3/28/18 change
        boolean isValid = true;
        try {
            Configuration configuration = LicenseManagerUtil.loadConfigurations();
            origin = configuration.getClientUrl();
            if (!isValid) {
                errorJson.addProperty("responseType", "Error");
                errorJson.addProperty("responseMessage", "Invalid User Request");
                errorJson.add("component", errorData);
                errorJson.add("library", errorData);
                return Response.ok(errorJson, MediaType.APPLICATION_JSON)
                        .header("Access-Control-Allow-Origin", origin)
                        .header("Access-Control-Allow-Credentials", true)
                        .build();
            }
            driver = configuration.getDatabaseDriver();
            url = configuration.getDatabaseUrl();
            userName = configuration.getDatabaseUsername();
            password = configuration.getDatabasePassword();
            DataManager dataManager = new DataManager(driver, url, userName, password);
            licenseId = Integer.parseInt(configuration.getLicenseId());
            JsonElement jsonElement = jsonParser.parse(stringPayload);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonArray jsonArray = jsonObject.get("jars").getAsJsonArray();
//            requestBy = jsonObject.get("requestBy").getAsString();
            // TODO: 3/28/18 change
            requestBy = "pamoda@wso2.com";
            EnterData enterData = main.enterData(jarHolder);
            session.setAttribute(ENTER_DATA, enterData);
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
                componentTable += "<tr><td style=\"width:" + fileNameWidth + ";text-align:left;\">" +
                        componentList.get(i).getJarFile().getName() + "</td><td style=\"width:" + nameWidth + ";" +
                        "text-align:left;\">" +
                        componentList.get(i).getProjectName() + "</td><td style=\"width:" + versionWidth + ";" +
                        "text-align:left;\">" +
                        componentList.get(i).getVersion() + "</td></tr>";
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
                libraryTable += "<tr><td style=\"width:" + fileNameWidth + ";text-align:left;\">" +
                        libraryList.get(i).getJarFile().getName() + "</td><td style=\"width:" + nameWidth + ";" +
                        "text-align:left;\">" +
                        libraryList.get(i).getProjectName() + "</td><td style=\"width:" + versionWidth + ";" +
                        "text-align:left;\">" +
                        libraryList.get(i).getVersion() + "</td></tr>";
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
//            if (componentList.size() > 0 || libraryList.size() > 0) {
//                log.info(Integer.toString(responseCode));
//                if (responseCode == 200 || responseCode == 201) {
//                    responseJson.addProperty("responseType", "Done");
//                    responseJson.addProperty("responseMessage", "Done");
//                } else {
//                    responseJson.addProperty("responseType", "Error");
//                    responseJson.addProperty("responseMessage", ("Status code - " + Integer.toString(responseCode)));
//                }
//            } else {
//                responseJson.addProperty("responseType", "Done");
//                responseJson.addProperty("responseMessage", "Done");
//            }

            responseJson.addProperty("responseType", "Done");
            responseJson.addProperty("responseMessage", "Done");
            responseJson.add("component", componentJsonArray);
            responseJson.add("library", libraryJsonArray);
            dataManager.closeConection();
        } catch (IOException e) {
            responseJson.addProperty("responseType", "Error");
            responseJson.addProperty("responseMessage", e.getMessage());
            log.error("enterJarsNew(IOException) - " + e.getMessage());
//        } catch (Exception e) {
//            responseJson.addProperty("responseType", "Error");
//            responseJson.addProperty("responseMessage", e.getMessage());
//            log.error("enterJarsNew(Exception) - " + e.getMessage());
        } catch (DataSetException | LicenseManagerConfigurationException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin", origin)
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
        String origin = "";
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
        try {
            Session session = request.getSession();
            // TODO: 3/29/18 for local validation
            // TODO: 3/28/18  validate the user
            session.setAttribute(VALID_USER, true);
            Configuration configuration = LicenseManagerUtil.loadConfigurations();
            origin = configuration.getClientUrl();
            responseJson.addProperty("responseType", "Done");
            responseJson.addProperty("isValid", returnValue);
            responseJson.addProperty("responseMessage", "Done");
            // TODO: 3/29/18 actual usesr validation happens here
//            session.setAttribute(VALID_USER, returnValue);

            email = "pamoda@wso2.com";
            log.info("Login - " + email);
        } catch (LicenseManagerConfigurationException e) {
            e.printStackTrace();
        }

        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin", origin)
                .header("Access-Control-Allow-Credentials", true)
                .build();
    }

    @GET
    @Path("/getUserDetails")
    @Produces("application/json")
    public Response getUserDetails(@Context Request request) {

        Configuration configuration = null;
        try {
            configuration = LicenseManagerUtil.loadConfigurations();
        } catch (LicenseManagerConfigurationException e) {
            e.printStackTrace();
        }
        String origin = null;
        if (configuration != null) {
            origin = configuration.getClientUrl();
        }

        JsonObject response = new JsonObject();
        response.addProperty("isValid", true);
        response.addProperty("userEmail", "pamoda@wso2.com");
        response.addProperty("isAnyAdmin", true);
        response.addProperty("isRepositoryAdmin", true);
        response.addProperty("isLibraryAdmin", true);
        response.addProperty("libraryUserDetails", "");
        return Response.ok(response, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin", origin)
                .header("Access-Control-Allow-Credentials", true)
                .build();
    }

    @GET
    @Path("/getLicense")
    @Produces("application/json")
    public Response getLicenseResource(@Context Request request) {

        JsonObject responseJson = new JsonObject();
        JsonObject errorJson = new JsonObject();
        Session session = request.getSession();
//        boolean isValid = Boolean.valueOf(session.getAttribute(VALID_USER).toString());
        // TODO: 3/28/18 change
        boolean isValid = true;
        String origin = "";
        String driver, url, userName, password, productName, productVersion, path;
        String licenseFilePath;
        String fileName;
        try {
            Configuration configuration = LicenseManagerUtil.loadConfigurations();
            origin = configuration.getClientUrl();
            if (!isValid) {
                errorJson.addProperty("responseType", "Error");
                errorJson.addProperty("responseMessage", "Invalid User Request");
                return Response.ok(errorJson, MediaType.APPLICATION_JSON)
                        .header("Access-Control-Allow-Origin", origin)
                        .header("Access-Control-Allow-Credentials", true)
                        .build();
            }
            driver = configuration.getDatabaseDriver();
            url = configuration.getDatabaseUrl();
            path = configuration.getPathToFileStorage();
            userName = configuration.getDatabaseUsername();
            password = configuration.getDatabasePassword();
//            JarHolder jarHolder = (JarHolder) session.getAttribute(JAR_HOLDER);
            productName = jarHolder.getProductName();
            productVersion = jarHolder.getProductVersion();
            LicenseFileGenerator licenseFileGenerator = new LicenseFileGenerator(driver, url, userName, password);
            licenseFileGenerator.generateLicenceFile(productName, productVersion, path);
            responseJson.addProperty("responseType", "Done");
            responseJson.addProperty("responseMessage", "Done");
        } catch (Exception e) {
            responseJson.addProperty("responseType", "Error");
            responseJson.addProperty("responseMessage", e.getMessage());
            log.error("getLicense(Exception) - " + e.getMessage());
        }
        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin", origin)
                .header("Access-Control-Allow-Credentials", true)
                .build();
    }

    @GET
    @Path("/downloadLicense")
    public Response getFile(@Context Request request) {

        String origin = "";
        String mountPath;
        JsonObject errorJson = new JsonObject();
        Session session = request.getSession();
//        boolean isValid = Boolean.valueOf(session.getAttribute(VALID_USER).toString());
        // TODO: 3/28/18 change
        boolean isValid = true;
        try {
            Configuration configuration = LicenseManagerUtil.loadConfigurations();
            mountPath = configuration.getPathToFileStorage();
            if (!isValid) {
                errorJson.addProperty("responseType", "Error");
                errorJson.addProperty("responseMessage", "Invalid User Request");
                return Response.ok(errorJson, MediaType.APPLICATION_JSON)
                        .header("Access-Control-Allow-Origin", origin)
                        .header("Access-Control-Allow-Credentials", true)
                        .build();
            }
            origin = configuration.getClientUrl();
//            mountPath = session.getAttribute(LICENSE_PATH).toString();
//            String fileName = session.getAttribute(LICENSE_FILE_NAME).toString();
            String productName = jarHolder.getProductName();
            String productVersion = jarHolder.getProductVersion();
            String fileName = "LICENSE(" + productName + "-" + productVersion + ").TXT";

            File file = Paths.get(mountPath, fileName).toFile();
            if (file.exists()) {
                return Response.ok(file)
                        .header("Access-Control-Allow-Origin", origin)
                        .header("Access-Control-Allow-Credentials", true)
                        .build();
            } else {
                log.error("downloadLicense - license file doesn't exists");
            }

//        } catch (FileNotFoundException e) {
//            log.error("downloadLicense(FileNotFoundException) - " + e.getMessage());
//        } catch (IOException e) {
//            log.error("downloadLicense(IOException) - " + e.getMessage());
        } catch (LicenseManagerConfigurationException e) {
            e.printStackTrace();
        }

        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET
    @Path("/selectWaitingLicenseRequests")
    @Produces("application/json")
    public Response selectWaitingLicenseRequests(@Context Request request) {

        JsonObject responseJson = new JsonObject();
        String origin = "";
        String driver, url, userName, password;
        try {
            Configuration configuration = LicenseManagerUtil.loadConfigurations();

            origin = configuration.getClientUrl();
            driver = configuration.getDatabaseDriver();
            url = configuration.getDatabaseUrl();
            userName = configuration.getDatabaseUsername();
            password = configuration.getDatabasePassword();
            DataManager dataManager = new DataManager(driver, url, userName, password);
            JsonArray waitingRequests = dataManager.selectWaitingLicenseRequests();
            responseJson.addProperty("responseType", "");
            responseJson.addProperty("responseMessage", "Done");
            responseJson.add("responseData", waitingRequests);
            dataManager.closeConection();

//        } catch (Exception e) {
//            responseJson.addProperty("responseType", "Error");
//            responseJson.addProperty("responseMessage", e.getMessage());
//            log.error("selectWaitingLicenseRequests(Exception) - " + e.getMessage());
        } catch (LicenseManagerConfigurationException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin", origin)
                .header("Access-Control-Allow-Credentials", true)
                .build();
    }

    @GET
    @Path("/selectWaitingComponents")
    @Produces("application/json")
    public Response selectWaitingComponents(@Context Request request, @QueryParam("licenseRequestId") int
            licenseRequestId) {

        JsonObject responseJson = new JsonObject();
        JsonObject errorJson = new JsonObject();
        Session session = request.getSession();
//        boolean isValid = Boolean.valueOf(session.getAttribute(VALID_USER).toString());
        // TODO: 3/28/18 change
        boolean isValid = true;
        String origin = "";
        String driver, url, userName, password;
        try {
            Configuration configuration = LicenseManagerUtil.loadConfigurations();
            origin = configuration.getClientUrl();
            if (!isValid) {
                errorJson.addProperty("responseType", "Error");
                errorJson.addProperty("responseMessage", "Invalid User Request");
                return Response.ok(errorJson, MediaType.APPLICATION_JSON)
                        .header("Access-Control-Allow-Origin", origin)
                        .header("Access-Control-Allow-Credentials", true)
                        .build();
            }
            driver = configuration.getDatabaseDriver();
            url = configuration.getDatabaseUrl();
            userName = configuration.getDatabaseUsername();
            password = configuration.getDatabasePassword();
            DataManager dataManager = new DataManager(driver, url, userName, password);
            JsonArray waitingComponents = dataManager.selectWaitingLicenseComponents(licenseRequestId);
            responseJson.addProperty("responseType", "Done");
            responseJson.addProperty("responseMessage", "Done");
            responseJson.add("responseData", waitingComponents);
            dataManager.closeConection();

//        } catch (Exception e) {
//            responseJson.addProperty("responseType", "Error");
//            responseJson.addProperty("responseMessage", e.getMessage());
//            log.error("selectWaitingComponents(Exception) - " + e.getMessage());
        } catch (LicenseManagerConfigurationException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin", origin)
                .header("Access-Control-Allow-Credentials", true)
                .build();

    }

    @GET
    @Path("/selectWaitingLibraries")
    @Produces("application/json")
    public Response selectWaitingLibraries(@Context Request request, @QueryParam("licenseRequestId") int
            licenseRequestId) {

        JsonObject responseJson = new JsonObject();
        JsonObject errorJson = new JsonObject();
        Session session = request.getSession();
//        boolean isValid = Boolean.valueOf(session.getAttribute(VALID_USER).toString());
        // TODO: 3/28/18 change
        boolean isValid = true;
        String origin = "";
        String driver, url, userName, password;
        try {
            Configuration configuration = LicenseManagerUtil.loadConfigurations();
            origin = configuration.getClientUrl();
            if (!isValid) {
                errorJson.addProperty("responseType", "Error");
                errorJson.addProperty("responseMessage", "Invalid User Request");
                return Response.ok(errorJson, MediaType.APPLICATION_JSON)
                        .header("Access-Control-Allow-Origin", origin)
                        .header("Access-Control-Allow-Credentials", true)
                        .build();
            }
            driver = configuration.getDatabaseDriver();
            url = configuration.getDatabaseUrl();
            userName = configuration.getDatabaseUsername();
            password = configuration.getDatabasePassword();
            DataManager dataManager = new DataManager(driver, url, userName, password);
            JsonArray waitingLibraries = dataManager.selectWaitingLicenseLibraries(licenseRequestId);
            responseJson.addProperty("responseType", "Done");
            responseJson.addProperty("responseMessage", "Done");
            responseJson.add("responseData", waitingLibraries);
            dataManager.closeConection();

//        } catch (Exception e) {
//            responseJson.addProperty("responseType", "Error");
//            responseJson.addProperty("responseMessage", e.getMessage());
//            log.error("selectWaitingLibraries(Exception) - " + e.getMessage());
        } catch (LicenseManagerConfigurationException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin", origin)
                .header("Access-Control-Allow-Credentials", true)
                .build();
    }

    @POST
    @Path("/acceptLicenseRequest")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @OPTIONS
    public Response acceptLicenseRequestResource(@Context Request request, String stringPayload) {

        String origin = "";
        JsonObject responseJson = new JsonObject();
        JsonObject errorJson = new JsonObject();
        JsonObject requestJson;
        JsonArray componentsJson;
        JsonArray librariesJson;
        JsonParser jsonParser = new JsonParser();
        String driver, url, userName, password;
        int productId, requestId, responseCode = 0;
        String bpmnUrlString;
        String bpmnToken;
        String jwt = null;
        try {
            Configuration configuration = LicenseManagerUtil.loadConfigurations();
            origin = configuration.getClientUrl();
            driver = configuration.getDatabaseDriver();
            url = configuration.getDatabaseUrl();
            userName = configuration.getDatabaseUsername();
            password = configuration.getDatabasePassword();
            DataManager dataManager = new DataManager(driver, url, userName, password);
            requestJson = jsonParser.parse(stringPayload).getAsJsonObject();
            componentsJson = requestJson.getAsJsonArray("components");
            librariesJson = requestJson.getAsJsonArray("libraries");
            productId = requestJson.get("productId").getAsInt();
            requestId = requestJson.get("requestId").getAsInt();
//            jwt = requestJson.get("token").getAsString();
            boolean isValidAdmin = dataManager.isLicenseAdmin(jwt);
            if (!isValidAdmin) {
                errorJson.addProperty("responseType", "Error");
                errorJson.addProperty("responseMessage", "Invalid User Request");
                dataManager.closeConection();
                return Response.ok(errorJson, MediaType.APPLICATION_JSON)
                        .header("Access-Control-Allow-Origin", origin)
                        .header("Access-Control-Allow-Credentials", true)
                        .build();
            }
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
                String name = librariesJson.get(i).getAsJsonObject().get("TEMPLIB_NAME").getAsString();
                String fileName = librariesJson.get(i).getAsJsonObject().get("TEMPLIB_FILE_NAME").getAsString();
                String version = librariesJson.get(i).getAsJsonObject().get("TEMPLIB_VERSION").getAsString();
                String type = librariesJson.get(i).getAsJsonObject().get("TEMPLIB_TYPE").getAsString();
                String parent = librariesJson.get(i).getAsJsonObject().get("TEMPLIB_PARENT").getAsString();
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

        } catch (DataSetException | LicenseManagerConfigurationException | SQLException | ClassNotFoundException e) {
            responseJson.addProperty("responseType", "Error");
            responseJson.addProperty("responseMessage", "Failed");
        }
        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin", origin)
                .header("Access-Control-Allow-Credentials", true)
                .build();
    }

    @POST
    @Path("/rejectLicenseRequest")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @OPTIONS
    public Response rejectLicenseRequestResource(@Context Request request, String stringPayload) {

        String origin = "";
        JsonObject responseJson = new JsonObject();
        JsonObject requestJson;
        JsonObject errorJson = new JsonObject();
        JsonParser jsonParser = new JsonParser();
        String driver, url, userName, password, jwt;
        int productId, requestId, responseCode = 0;
        String bpmnUrlString, bpmnToken, rejectBy, rejectReason;
//        HttpsURLConnection connection;
        try {
            Configuration configuration = LicenseManagerUtil.loadConfigurations();
            origin = configuration.getClientUrl();
            driver = configuration.getDatabaseDriver();
            url = configuration.getDatabaseUrl();
            userName = configuration.getDatabaseUsername();
            password = configuration.getDatabasePassword();
            DataManager dataManager = new DataManager(driver, url, userName, password);
            requestJson = jsonParser.parse(stringPayload).getAsJsonObject();
            rejectBy = requestJson.get("rejectBy").getAsString();
            rejectReason = requestJson.get("rejectReason").getAsString();
            productId = requestJson.get("productId").getAsInt();
            requestId = requestJson.get("requestId").getAsInt();
            jwt = requestJson.get("token").getAsString();
            boolean isValidAdmin = dataManager.isLicenseAdmin(jwt);
            if (!isValidAdmin) {
                errorJson.addProperty("responseType", "Error");
                errorJson.addProperty("responseMessage", "Invalid User Request");
                dataManager.closeConection();
                return Response.ok(errorJson, MediaType.APPLICATION_JSON)
                        .header("Access-Control-Allow-Origin", origin)
                        .header("Access-Control-Allow-Credentials", true)
                        .build();
            }
            dataManager.updateLicenseRequestReject(rejectBy, requestId);
//            String taskId = dataManager.selectLicenseRequestTaskIdFromId(requestId);
//            JsonArray variables = new JsonArray();
//            JsonObject acceptJson = new JsonObject();
//            JsonObject rejectByJson = new JsonObject();
//            JsonObject rejectReasonJson = new JsonObject();
//            JsonObject bpmnRequestJson = new JsonObject();
//            acceptJson.addProperty("name", "outputType");
//            acceptJson.addProperty("value", "Reject");
//            rejectByJson.addProperty("name", "rejectBy");
//            rejectByJson.addProperty("value", rejectBy);
//            rejectReasonJson.addProperty("name", "reasonForReject");
//            rejectReasonJson.addProperty("value", rejectReason);
//            variables.add(acceptJson);
//            variables.add(rejectByJson);
//            variables.add(rejectReasonJson);
//            bpmnRequestJson.addProperty("action", "complete");
//            bpmnRequestJson.add("variables", variables);

//            bpmnUrlString = configuration.getBpmnUrl() + "bpmn/runtime/tasks/" + taskId;
//            bpmnToken = configuration.getBpmnToken();
//            URL bpmnUrl = new URL(bpmnUrlString);
//            connection = (HttpsURLConnection) bpmnUrl.openConnection();
//            connection.setHostnameVerifier(new HostnameVerifier() {
//                public boolean verify(String hostname, SSLSession session) {
//
//                    return true;
//                }
//            });
//            connection.setRequestMethod("POST");
//            connection.setRequestProperty("Content-Type", "application/json");
//            connection.setRequestProperty("Authorization", bpmnToken);
//            connection.setUseCaches(false);
//            connection.setDoOutput(true);
//            DataOutputStream wr = new DataOutputStream(
//                    connection.getOutputStream());
//            wr.writeBytes(bpmnRequestJson.toString());
//            wr.close();
//            InputStream is = connection.getInputStream();
//            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
//            StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
//            String line;
//            while ((line = rd.readLine()) != null) {
//                response.append(line);
//                response.append('\r');
//            }
//            rd.close();
//            is.close();
//            responseCode = connection.getResponseCode();
//            if (responseCode == 200 || responseCode == 201) {
//                responseJson.addProperty("responseType", "Done");
//                responseJson.addProperty("responseMessage", "Done");
//            } else {
//                responseJson.addProperty("responseType", "Error");
//                responseJson.addProperty("responseMessage", ("Status code - " + Integer.toString(responseCode)));
//                log.error(response.toString());
//            }
            dataManager.closeConection();

        } catch (LicenseManagerConfigurationException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin", origin)
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
        String driver, url, userName, password, path, origin;
        String filePath, fileName, jwt;
        try {
            Configuration configuration = LicenseManagerUtil.loadConfigurations();
            origin = configuration.getClientUrl();
            driver = configuration.getDatabaseDriver();
            url = configuration.getDatabaseUrl();
            userName = configuration.getDatabaseUsername();
            password = configuration.getDatabasePassword();
            path = configuration.getPathToFileStorage();
            jwt = request.getHeader("adminJwt");
            DataManager dataManager = new DataManager(driver, url, userName, password);
            boolean isValidAdmin = dataManager.isLicenseAdmin(jwt);
            if (!isValidAdmin) {
                errorJson.addProperty("responseType", "Error");
                errorJson.addProperty("responseMessage", "Invalid User Request");
                dataManager.closeConection();
                return Response.ok(errorJson, MediaType.APPLICATION_JSON)
                        .header("Access-Control-Allow-Origin", origin)
                        .header("Access-Control-Allow-Credentials", true)
                        .build();
            }
            LicenseFileGenerator licenseFileGenerator = new LicenseFileGenerator(driver, url, userName, password);
            licenseFileGenerator.generateLicenceFile(licenseRequest.getProductName(), licenseRequest
                    .getProductVersion(), path);
            fileName = "LICENSE(" + licenseRequest.getProductName() + "-" + licenseRequest.getProductVersion() + ")" +
                    ".TXT";
            filePath = path + fileName;

//            String to = licenseRequest.getMailList();
//            String from = configuration.getEmailAddress();
//            final String username = configuration.getEmailAddress();
//            final String emailPassword = configuration.getEmailPassword();
//            String host = configuration.getSmtpHost();
//            Properties props = new Properties();
//            props.put("mail.smtp.auth", "true");
//            props.put("mail.smtp.starttls.enable", "true");
//            props.put("mail.smtp.host", host);
//            props.put("mail.smtp.port", configuration.getSmtpPort());
//
//            javax.mail.Session session = javax.mail.Session.getInstance(props,
//                    new javax.mail.Authenticator() {
//                        protected PasswordAuthentication getPasswordAuthentication() {
//
//                            return new PasswordAuthentication(username, emailPassword);
//                        }
//                    });
//            Message message = new MimeMessage(session);
//            message.setFrom(new InternetAddress(from));
//            message.setRecipients(Message.RecipientType.TO,
//                    InternetAddress.parse(to));
//            String subject = "License File for " + licenseRequest.getProductName() + " - " + licenseRequest
//                    .getProductVersion();
//            message.setSubject(subject);
//            BodyPart messageBodyPart = new MimeBodyPart();
//            messageBodyPart.setText("This is the license file for above $subject");
//            Multipart multipart = new MimeMultipart();
//            multipart.addBodyPart(messageBodyPart);
//            messageBodyPart = new MimeBodyPart();
//            String filename = filePath;
//            DataSource source = new FileDataSource(filename);
//            messageBodyPart.setDataHandler(new DataHandler(source));
//            messageBodyPart.setFileName(fileName);
//            multipart.addBodyPart(messageBodyPart);
//            message.setContent(multipart);
//            Transport.send(message);
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

        String origin = null;
        try {
            Configuration config = LicenseManagerUtil.loadConfigurations();
            origin = config.getClientUrl();
        } catch (LicenseManagerConfigurationException e) {
            e.printStackTrace();
        }

        log.info("sample");
        return Response.ok("Hello Java", MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Origin", origin)
                .header("Access-Control-Allow-Credentials", true)
                .build();
    }
}
