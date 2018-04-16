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
import org.wso2.internal.apps.license.manager.impl.exception.LicenseManagerRuntimeException;
import org.wso2.internal.apps.license.manager.impl.main.Jar;
import org.wso2.internal.apps.license.manager.impl.main.JarHolder;
import org.wso2.internal.apps.license.manager.impl.main.LicenseFileGenerator;
import org.wso2.internal.apps.license.manager.impl.main.ProductJarManager;
import org.wso2.internal.apps.license.manager.impl.models.SessionObjectHolder;
import org.wso2.internal.apps.license.manager.util.Constants;
import org.wso2.internal.apps.license.manager.util.DBHandler;
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
    private ConcurrentHashMap<String, SessionObjectHolder> objectHolderMap = new ConcurrentHashMap<>();
    private String packPath;
    private String session_email = "pamodaaw@wso2.com";

    @GET
    @Path("/selectLicense")
    @Produces("application/json")
    public Response selectLicenseResource(@Context Request request) {

        JsonObject responseJson = new JsonObject();
        DBHandler dbHandler = null;
        try {
            dbHandler = new DBHandler();
            JsonArray jsonArray = dbHandler.selectAllLicense();
            responseJson.addProperty("responseType", Constants.SUCCESS);
            responseJson.addProperty("responseMessage", "Done");
            responseJson.add("responseData", jsonArray);
        } catch (SQLException | ClassNotFoundException | DataSetException e) {

            responseJson.addProperty("responseType", Constants.ERROR);
            responseJson.addProperty("responseMessage", "Failed to retrieve data from the database.");
            log.error("Failed to retrieve data from the database. " + e.getMessage(), e);
        } finally {
            if (dbHandler != null) {
                try {
                    dbHandler.closeConnection();
                } catch (SQLException e) {
                    log.error("Failed to close the database connection. " + e.getMessage(), e);
                }

            }
        }
        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Credentials", true)
                .build();
    }

    @GET
    @Path("/getPacks")
    public Response listUploadedPacks() {

        ArrayList<String> listOfPacks = new ArrayList<>();
        JsonObject responseJson = new JsonObject();
        JsonArray responseData = new JsonArray();

        String pathToStorage = SystemVariableUtil.getValue(Constants.FILE_UPLOAD_PATH, null);

        File folder = new File(pathToStorage);
        File[] listOfFiles = folder.listFiles();

        if (listOfFiles != null) {
            for (File file : listOfFiles) {
                if (file.isFile() && file.getName().endsWith(".zip")) {
                    listOfPacks.add(file.getName());
                }
            }
        }

        for (String listOfPack : listOfPacks) {
            JsonObject ob = new JsonObject();
            ob.addProperty("name", listOfPack);
            responseData.add(ob);
        }
        responseJson.addProperty("responseType", Constants.SUCCESS);
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

        JsonObject responseJson = new JsonObject();
        JsonArray nameMissingJars = new JsonArray();
        String pathToStorage = SystemVariableUtil.getValue(Constants.FILE_UPLOAD_PATH, null);
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
            JarHolder jarHolder = LicenseManagerUtils.checkJars(filePath);
            SessionObjectHolder userObjectHolder = new SessionObjectHolder();
            userObjectHolder.setJarHolder(jarHolder);
            // TODO: 4/9/18 obtain the email from the session
            objectHolderMap.put(session_email, userObjectHolder);
            log.info("Jar extraction complete.");
            List<Jar> errorJarList = jarHolder.getErrorJarList();
            for (int i = 0; i < errorJarList.size(); i++) {
                JsonObject currentJar = new JsonObject();
                currentJar.addProperty("index", i);
                currentJar.addProperty("name", errorJarList.get(i).getProjectName());
                currentJar.addProperty("version", errorJarList.get(i).getVersion());
                nameMissingJars.add(currentJar);
            }
            responseJson.addProperty("responseType", Constants.SUCCESS);
            responseJson.addProperty("responseMessage", "Done");
            responseJson.add("responseData", nameMissingJars);

        } catch (LicenseManagerRuntimeException e) {
            responseJson.addProperty("responseType", Constants.ERROR);
            responseJson.addProperty("responseMessage", "Internal Server Error. Failed to extract jars.");
            log.error("Error while extracting jars. " + e.getMessage());
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
        JsonParser jsonParser = new JsonParser();
        // TODO: 4/9/18 default license ID;
        int licenseId = 1;
        try {
            JsonElement jsonElement = jsonParser.parse(stringPayload);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonArray jsonArray = jsonObject.get("jars").getAsJsonArray();
            // TODO: 3/28/18 change
            JarHolder jarHolder = objectHolderMap.get(session_email).getJarHolder();
            for (int i = 0; i < jsonArray.size(); i++) {
                JsonObject jar = jsonArray.get(i).getAsJsonObject();
                int index = jar.get("index").getAsInt();
                jarHolder.getErrorJarList().get(index).setProjectName(jar.get("name").getAsString());
                jarHolder.getErrorJarList().get(index).setVersion(jar.get("version").getAsString());
            }

            // Add name defined jars into the jar list of the jar holder.
            for (Jar jar : jarHolder.getErrorJarList()) {
                jarHolder.getJarList().add(jar);

            }

            ProductJarManager productJarManager = new ProductJarManager(jarHolder);
            productJarManager.enterJarsIntoDB();
            List<Jar> componentList = productJarManager.getLicenseMissingComponents();
            List<Jar> libraryList = productJarManager.getLicenseMissingLibraries();
            objectHolderMap.get(session_email).setLicenseMissingComponents(componentList);
            objectHolderMap.get(session_email).setLicenseMissingLibraries(libraryList);
            objectHolderMap.get(session_email).setProductId(productJarManager.getProductId());
            JsonArray componentJsonArray = new JsonArray();
            JsonArray libraryJsonArray = new JsonArray();

            for (int i = 0; i < componentList.size(); i++) {
                JsonObject component = new JsonObject();
                component.addProperty("index", i);
                component.addProperty("name", componentList.get(i).getProjectName());
                component.addProperty("version", componentList.get(i).getVersion());
                component.addProperty("type", componentList.get(i).getType());
                component.addProperty("licenseId", licenseId);
                componentJsonArray.add(component);
            }

            for (int i = 0; i < libraryList.size(); i++) {
                JsonObject library = new JsonObject();
                String libraryType = (libraryList.get(i).getParent() == null) ?
                        ((libraryList.get(i).isBundle()) ? Constants.JAR_TYPE_BUNDLE : Constants.JAR_TYPE_JAR) :
                        Constants.JAR_TYPE_JAR_IN_BUNDLE;
                library.addProperty("index", i);
                library.addProperty("name", libraryList.get(i).getProjectName());
                library.addProperty("version", libraryList.get(i).getVersion());
                library.addProperty("type", libraryType);
                library.addProperty("licenseId", licenseId);
                libraryJsonArray.add(library);
            }

            responseJson.addProperty("responseType", Constants.SUCCESS);
            responseJson.addProperty("responseMessage", "Done");
            responseJson.add("component", componentJsonArray);
            responseJson.add("library", libraryJsonArray);
        } catch (SQLException | ClassNotFoundException | DataSetException e) {
            responseJson.addProperty("responseType", Constants.ERROR);
            responseJson.addProperty("responseMessage", "Internal Server Error. Can not load data.");
            log.error("Failed to retrieve data from the database. " + e.getMessage(), e);
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
        responseJson.addProperty("responseType", Constants.SUCCESS);
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
        String fileUploadPath = SystemVariableUtil.getValue(Constants.FILE_UPLOAD_PATH, null);
        String productName;
        String productVersion;
        try {
            // TODO: 4/9/18 get the session email
            JarHolder jarHolder = objectHolderMap.get(session_email).getJarHolder();
            productName = jarHolder.getProductName();
            productVersion = jarHolder.getProductVersion();
            LicenseFileGenerator licenseFileGenerator = new LicenseFileGenerator(databaseDriver, databaseUrl,
                    databaseUsername, databasePassword);
            licenseFileGenerator.generateLicenceFile(productName, productVersion, fileUploadPath);
            responseJson.addProperty("responseType", Constants.SUCCESS);
            responseJson.addProperty("responseMessage", "Done");
        } catch (Exception e) {
            responseJson.addProperty("responseType", Constants.ERROR);
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

        String mountPath = SystemVariableUtil.getValue(Constants.FILE_UPLOAD_PATH, null);
        JarHolder jarHolder = objectHolderMap.get(session_email).getJarHolder();
        String productName = jarHolder.getProductName();
        String productVersion = jarHolder.getProductVersion();
        String fileName = "LICENSE(" + productName + "-" + productVersion + ").TXT";

        File file = Paths.get(mountPath, fileName).toFile();
        if (file.exists()) {
//            LicenseManagerUtils.deleteFolder(mountPath+fileName);
            LicenseManagerUtils.deleteFolder(mountPath + productName + "-" + productVersion + ".zip");
            LicenseManagerUtils.deleteFolder(mountPath + productName + "-" + productVersion);
            objectHolderMap.remove(session_email);
            return Response.ok(file)
                    .header("Access-Control-Allow-Credentials", true)
                    .build();
        } else {
            log.error("License file does not exist");
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @POST
    @Path("/addLicense")
    @Produces(MediaType.APPLICATION_JSON)
    @OPTIONS
    public Response addLicenseForJars(@Context Request request, String stringPayload) {

        JsonObject responseJson = new JsonObject();
        JsonObject requestJson;
        JsonArray componentsJson;
        JsonArray librariesJson;
        JsonParser jsonParser = new JsonParser();
        DBHandler dbHandler = null;
        SessionObjectHolder sessionObjectHolder = objectHolderMap.get(session_email);
        int productId = sessionObjectHolder.getProductId();
        try {
            dbHandler = new DBHandler();
            requestJson = jsonParser.parse(stringPayload).getAsJsonObject();
            componentsJson = requestJson.getAsJsonArray("components");
            librariesJson = requestJson.getAsJsonArray("libraries");
            for (int i = 0; i < componentsJson.size(); i++) {
                int index = componentsJson.get(i).getAsJsonObject().get("index").getAsInt();
                String componentName = sessionObjectHolder.getLicenseMissingComponents().get(index).getJarFile()
                        .getName();
                String name = componentsJson.get(i).getAsJsonObject().get("name").getAsString();
                String version = componentsJson.get(i).getAsJsonObject().get("version").getAsString();
                int licenseId = componentsJson.get(i).getAsJsonObject().get("licenseId").getAsInt();
                String licenseKey = dbHandler.selectLicenseFromId(licenseId);
                dbHandler.insertComponent(name, componentName, version);
                dbHandler.insertProductComponent(componentName, productId);
                dbHandler.insertComponentLicense(componentName, licenseKey);
            }

            for (int i = 0; i < librariesJson.size(); i++) {
                int index = librariesJson.get(i).getAsJsonObject().get("index").getAsInt();
                String libraryFileName = sessionObjectHolder.getLicenseMissingLibraries().get(index).getJarFile()
                        .getName();
                String componentKey = null;
                Jar parent = null;
                if (sessionObjectHolder.getLicenseMissingLibraries().get(index).getParent() != null) {
                    parent = sessionObjectHolder.getLicenseMissingLibraries().get(index).getParent();
                    componentKey = parent.getJarFile().getName();
                }
                String name = librariesJson.get(i).getAsJsonObject().get("name").getAsString();
                String version = librariesJson.get(i).getAsJsonObject().get("version").getAsString();
                String type = librariesJson.get(i).getAsJsonObject().get("type").getAsString();
                int licenseId = librariesJson.get(i).getAsJsonObject().get("licenseId").getAsInt();
                String licenseKey = dbHandler.selectLicenseFromId(licenseId);

                int libId = dbHandler.getLibraryId(name, libraryFileName, version, type);
                dbHandler.insertLibraryLicense(licenseKey, Integer.toString(libId));

                if (parent != null && parent.getType().equals(Constants.JAR_TYPE_WSO2)) {
                    dbHandler.insertComponentLibrary(componentKey, libId);
                } else {
                    dbHandler.insertProductLibrary(libId, productId);
                }

            }
            responseJson.addProperty("responseType", Constants.SUCCESS);
            responseJson.addProperty("responseMessage", "Done");

        } catch (DataSetException | SQLException | ClassNotFoundException e) {
            responseJson.addProperty("responseType", Constants.ERROR);
            responseJson.addProperty("responseMessage", "Failed");
            log.error("Failed to add licenses." + e.getMessage(), e);
        } finally {
            if (dbHandler != null) {
                try {
                    dbHandler.closeConnection();
                } catch (SQLException e) {
                    log.error("Failed to close the database connection. " + e.getMessage(), e);
                }
            }
            sessionObjectHolder.getLicenseMissingComponents().clear();
            sessionObjectHolder.getLicenseMissingLibraries().clear();
        }
        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Credentials", true)
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
