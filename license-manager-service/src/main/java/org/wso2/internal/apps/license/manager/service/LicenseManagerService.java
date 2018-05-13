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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.internal.apps.license.manager.exception.LicenseManagerConfigurationException;
import org.wso2.internal.apps.license.manager.impl.JarHolder;
import org.wso2.internal.apps.license.manager.impl.LicenseFileGenerator;
import org.wso2.internal.apps.license.manager.impl.ProductJarManager;
import org.wso2.internal.apps.license.manager.models.Jar;
import org.wso2.internal.apps.license.manager.models.LicenseMissingJar;
import org.wso2.internal.apps.license.manager.models.NewLicenseEntry;
import org.wso2.internal.apps.license.manager.models.SessionObjectHolder;
import org.wso2.internal.apps.license.manager.models.TaskProgress;
import org.wso2.internal.apps.license.manager.util.Constants;
import org.wso2.internal.apps.license.manager.util.DBHandler;
import org.wso2.internal.apps.license.manager.util.EmailUtils;
import org.wso2.internal.apps.license.manager.util.JsonUtils;
import org.wso2.internal.apps.license.manager.util.LicenseManagerUtils;
import org.wso2.internal.apps.license.manager.util.ProgressTracker;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.util.SystemVariableUtil;

import java.io.File;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import javax.mail.MessagingException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

//todo inculde checkstyles

/**
 * Main Service class which contains all the micro service endpoints.
 */
@Path("/")
public class MainService {

// todo change mainservice

    private static final Logger log = LoggerFactory.getLogger(MainService.class);
    private ConcurrentHashMap<String, SessionObjectHolder> objectHolderMap = new ConcurrentHashMap<>();

    /**
     * Return the list of available set of licenses in the database.
     *
     * @param request Http request.
     * @return response with licenses.
     */
    @GET
    @Path("/license/list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllLicenseInformation(@Context Request request) {

        JsonObject responseJson = new JsonObject();
        DBHandler dbHandler = null;
        try {
            dbHandler = new DBHandler();
            JsonArray jsonArray = dbHandler.selectAllLicense();
            responseJson.addProperty("responseType", Constants.SUCCESS);
            responseJson.addProperty("responseMessage", "Done");
            responseJson.add("responseData", jsonArray);
        } catch (SQLException | ClassNotFoundException e) {

            responseJson.addProperty("responseType", Constants.ERROR);
            responseJson.addProperty("responseMessage", "Failed to retrieve data from the database");
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

    /**
     * Return the list of packs uploaded to the FTP server.
     *
     * @return API response
     */
    @GET
    @Path("/pack/list")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listUploadedPacks() {

        ArrayList<String> listOfPacks;
        JsonObject responseJson = new JsonObject();
        JsonArray responseData = new JsonArray();

        try {
            // Obtain the list of the available zip files.
            listOfPacks = LicenseManagerUtils.getListOfPacksName();
            for (String listOfPack : listOfPacks) {
                JsonObject ob = new JsonObject();
                ob.addProperty("name", listOfPack);
                responseData.add(ob);
            }
            responseJson.addProperty("responseType", Constants.SUCCESS);
            responseJson.addProperty("responseMessage", "Done");
            responseJson.add("responseData", responseData);

        } catch (LicenseManagerConfigurationException e) {
            responseJson.addProperty("responseType", Constants.ERROR);
            responseJson.addProperty("responseMessage", e.getMessage());
            log.error("Failed to get the list of uploaded pack. " + e.getMessage(), e);
        }
        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Credentials", true)
                .build();
    }

    /**
     * Start the downloading and extracting the selected pack in a new thread.
     *
     * @param request      Post request
     * @param username     logged user
     * @param selectedPack selected pack
     * @return success/failure of starting thread
     */
    @POST
    @Path("/pack/jars")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response extractJars(@Context Request request,
                                @QueryParam("username") String username,
                                String selectedPack) {

        TaskProgress taskProgress = LicenseManagerUtils.startPackExtractionProcess(username, selectedPack);
        JsonObject responseJson = new JsonObject();
        responseJson.addProperty("responseType", Constants.SUCCESS);
        responseJson.addProperty("responseStatus", Constants.RUNNING);
        responseJson.addProperty("responseMessage", taskProgress.getMessage());
        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Credentials", true)
                .build();
    }

    /**
     * Submit the names and versions of the name missing jars and identifies the license missing jars.
     *
     * @param request       POST request
     * @param username      logged user
     * @param stringPayload list of jars with new names and version
     * @return list of jars in which the licenses are missing
     */
    @POST
    @Path("/pack/nameDefinedJars")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response enterJarsResource(@Context Request request,
                                      @QueryParam("username") String username,
                                      String stringPayload) {

        JsonObject responseJson = new JsonObject();
        JsonParser jsonParser = new JsonParser();

        try {
            JsonElement jsonElement = jsonParser.parse(stringPayload);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonArray jsonArray = jsonObject.get("jars").getAsJsonArray();
            JarHolder jarHolder = objectHolderMap.get(username).getJarHolder();

            // Define the name and the version from the user input.
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

            // Enter jars to DB
            ProductJarManager productJarManager = new ProductJarManager(jarHolder);
            productJarManager.enterJarsIntoDB();

            // Get the license missing jars ( components and libraries)
            List<LicenseMissingJar> componentList = productJarManager.getLicenseMissingComponents();
            List<LicenseMissingJar> libraryList = productJarManager.getLicenseMissingLibraries();
            objectHolderMap.get(username).setLicenseMissingComponents(componentList);
            objectHolderMap.get(username).setLicenseMissingLibraries(libraryList);
            objectHolderMap.get(username).setProductId(productJarManager.getProductId());
            JsonArray componentJsonArray = JsonUtils.getComponentsListAsJson(componentList);
            JsonArray libraryJsonArray = JsonUtils.getLibraryListAsJson(libraryList);

            // Create the response if success
            responseJson.addProperty("responseType", Constants.SUCCESS);
            responseJson.addProperty("responseMessage", "Done");
            responseJson.add("component", componentJsonArray);
            responseJson.add("library", libraryJsonArray);
        } catch (SQLException | ClassNotFoundException e) {
            responseJson.addProperty("responseType", Constants.ERROR);
            responseJson.addProperty("responseMessage", "Failed to load data");
            log.error("Failed to retrieve data from the database. " + e.getMessage(), e);
            e.printStackTrace();
        }
        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Credentials", true)
                .build();
    }

    /**
     * Request to generate the license text to previously selected pack.
     *
     * @param request  POST request
     * @param username logged user
     * @return success/failure of generating the license text
     */
    @POST
    @Path("/license/text")
    @Produces(MediaType.APPLICATION_JSON)
    public Response generateLicenseText(@Context Request request,
                                        @QueryParam("username") String username) {

        JsonObject responseJson = new JsonObject();
        String databaseUrl = SystemVariableUtil.getValue(Constants.DATABASE_URL, null);
        String databaseDriver = SystemVariableUtil.getValue(Constants.DATABASE_DRIVER, null);
        String databaseUsername = SystemVariableUtil.getValue(Constants.DATABASE_USERNAME, null);
        String databasePassword = SystemVariableUtil.getValue(Constants.DATABASE_PASSWORD, null);
        String fileUploadPath = SystemVariableUtil.getValue(Constants.FILE_DOWNLOAD_PATH, null);
        String productName;
        String productVersion;
        try {
            JarHolder jarHolder = objectHolderMap.get(username).getJarHolder();
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
            log.error("Failed to generate licenses. " + e.getMessage(), e);
        }
        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Credentials", true)
                .build();
    }

    /**
     * Request to download the license text file.
     *
     * @param request  GET request
     * @param username logged user
     * @return the license text file
     */
    @GET
    @Path("/license/textToDownload")
    public Response getLicenseTextFile(@Context Request request,
                                       @QueryParam("username") String username) {

        String mountPath = SystemVariableUtil.getValue(Constants.FILE_DOWNLOAD_PATH, null);
        JarHolder jarHolder = objectHolderMap.get(username).getJarHolder();
        String productName = jarHolder.getProductName();
        String productVersion = jarHolder.getProductVersion();
        String fileName = "LICENSE(" + productName + "-" + productVersion + ").TXT";

        File file = Paths.get(mountPath, fileName).toFile();
        if (file.exists()) {

            // Clean the storage.
            LicenseManagerUtils.cleanFileStorage(productName + "-" + productVersion);
            objectHolderMap.remove(username);

            return Response.ok(file)
                    .header("Access-Control-Allow-Credentials", true)
                    .build();
        } else {
            log.error("License file does not exist");
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    /**
     * Add licenses for the jars which did not have licenses.
     *
     * @param request       POST request
     * @param username      logged user
     * @param stringPayload licenses for the jar
     * @return success/failure of adding licenses
     */
    @POST
    @Path("license/newLicenses")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addLicenseForJars(@Context Request request,
                                      @QueryParam("username") String username,
                                      String stringPayload) {

        DBHandler dbHandler = null;
        JsonObject responseJson = new JsonObject();
        JsonParser jsonParser = new JsonParser();
        List<NewLicenseEntry> newLicenseEntryComponentList = new ArrayList<>();
        List<NewLicenseEntry> newLicenseEntryLibraryList = new ArrayList<>();

        // Extract data from the request.
        JsonObject requestJson = jsonParser.parse(stringPayload).getAsJsonObject();
        JsonArray componentsJson = requestJson.getAsJsonArray("components");
        JsonArray librariesJson = requestJson.getAsJsonArray("libraries");

        SessionObjectHolder sessionObjectHolder = objectHolderMap.get(username);
        int productId = sessionObjectHolder.getProductId();
        try {
            dbHandler = new DBHandler();

            // Insert new licenses for the components.
            for (int i = 0; i < componentsJson.size(); i++) {
                int index = componentsJson.get(i).getAsJsonObject().get("index").getAsInt();
                String componentName = sessionObjectHolder.getLicenseMissingComponents().get(index).getJar().getJarFile()
                        .getName();
                String name = componentsJson.get(i).getAsJsonObject().get("name").getAsString();
                String version = componentsJson.get(i).getAsJsonObject().get("version").getAsString();
                String licenseKey = componentsJson.get(i).getAsJsonObject().get("licenseKey").getAsString();
                dbHandler.insertComponent(name, componentName, version);
                dbHandler.insertProductComponent(componentName, productId);
                dbHandler.insertComponentLicense(componentName, licenseKey);
                NewLicenseEntry newEntry = new NewLicenseEntry(componentName, licenseKey);
                newLicenseEntryComponentList.add(newEntry);
            }

            // Insert new licenses for the libraries.
            for (int i = 0; i < librariesJson.size(); i++) {
                String componentKey = null;
                Jar parent = null;
                int index = librariesJson.get(i).getAsJsonObject().get("index").getAsInt();
                String name = librariesJson.get(i).getAsJsonObject().get("name").getAsString();
                String version = librariesJson.get(i).getAsJsonObject().get("version").getAsString();
                String type = librariesJson.get(i).getAsJsonObject().get("type").getAsString();
                String licenseKey = librariesJson.get(i).getAsJsonObject().get("licenseKey").getAsString();

                String libraryFileName = sessionObjectHolder.getLicenseMissingLibraries().get(index).getJar().getJarFile()
                        .getName();

                if (sessionObjectHolder.getLicenseMissingLibraries().get(index).getJar().getParent() != null) {
                    parent = sessionObjectHolder.getLicenseMissingLibraries().get(index).getJar().getParent();
                    componentKey = parent.getJarFile().getName();
                }
                int libId = dbHandler.getLibraryId(name, libraryFileName, version, type);
                dbHandler.insertLibraryLicense(licenseKey, libId);

                // If the parent is wso2 insert it as a component-library relationship
                if (parent != null && parent.getType().equals(Constants.JAR_TYPE_WSO2)) {
                    dbHandler.insertComponentLibrary(componentKey, libId);
                } else {
                    dbHandler.insertProductLibrary(libId, productId);
                }

                NewLicenseEntry newEntry = new NewLicenseEntry(libraryFileName, licenseKey);
                newLicenseEntryLibraryList.add(newEntry);
            }

            // If there are new licenses added successfully, send a mail to the admin.
            if (newLicenseEntryComponentList.size() > 0 || newLicenseEntryLibraryList.size() > 0) {
                EmailUtils.sendEmail(username, newLicenseEntryComponentList, newLicenseEntryLibraryList);
            }

            responseJson.addProperty("responseType", Constants.SUCCESS);
            responseJson.addProperty("responseMessage", "Done");

        } catch (SQLException | ClassNotFoundException e) {
            responseJson.addProperty("responseType", Constants.ERROR);
            responseJson.addProperty("responseMessage", "Failed to add licenses. " +
                    "Please contact application admin");
            log.error("Failed to add licenses. " + e.getMessage(), e);
        } catch (MessagingException e) {
            responseJson.addProperty("responseType", Constants.SUCCESS);
            responseJson.addProperty("responseMessage", "Failed to send email to the admin");
            log.error("Error while sending email to application admins. " + e.getMessage(), e);
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

    /**
     * Get the report progress.
     *
     * @param request  HTTP request object.
     * @param username Username of the user.
     * @return The API response
     */
    @GET
    @Path("/progress")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTaskStatus(@Context Request request,
                                  @QueryParam("username") String username) {

        TaskProgress taskProgress = ProgressTracker.getTaskProgress(username);
        JsonObject responseJson = new JsonObject();
        JsonArray nameMissingJars;
        String statusMessage = taskProgress.getMessage();

        // Build the response based on the status of the task.
        switch (taskProgress.getStatus()) {

            case Constants.COMPLETE:
                SessionObjectHolder userObjectHolder = new SessionObjectHolder();
                JarHolder jarHolder = (JarHolder) taskProgress.getData();
                userObjectHolder.setJarHolder(jarHolder);
                objectHolderMap.put(username, userObjectHolder);

                List<Jar> errorJarList = LicenseManagerUtils.removeDuplicates(jarHolder.getErrorJarList());
                nameMissingJars = JsonUtils.getNameMissingJarsAsJson(errorJarList);

                responseJson.addProperty("responseType", Constants.SUCCESS);
                responseJson.addProperty("responseStatus", Constants.COMPLETE);
                responseJson.addProperty("responseMessage", statusMessage);
                responseJson.add("responseData", nameMissingJars);
                ProgressTracker.deleteTaskProgress(username);
                break;

            case Constants.RUNNING:
                responseJson.addProperty("responseType", Constants.SUCCESS);
                responseJson.addProperty("responseStatus", Constants.RUNNING);
                responseJson.addProperty("responseMessage", statusMessage);
                break;

            default:
                responseJson.addProperty("responseType", Constants.ERROR);
                responseJson.addProperty("responseStatus", Constants.FAILED);
                responseJson.addProperty("responseMessage", statusMessage);
                break;
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
