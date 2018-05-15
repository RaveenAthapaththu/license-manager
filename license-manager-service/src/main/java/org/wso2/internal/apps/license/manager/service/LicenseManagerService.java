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
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.internal.apps.license.manager.exception.LicenseManagerConfigurationException;
import org.wso2.internal.apps.license.manager.exception.LicenseManagerDataException;
import org.wso2.internal.apps.license.manager.impl.JarFileInfoDataHandler;
import org.wso2.internal.apps.license.manager.impl.JarFileInformationHolder;
import org.wso2.internal.apps.license.manager.impl.LicenseFileGenerator;
import org.wso2.internal.apps.license.manager.impl.PackExtractor;
import org.wso2.internal.apps.license.manager.impl.ServiceExecutor;
import org.wso2.internal.apps.license.manager.models.JarFile;
import org.wso2.internal.apps.license.manager.models.LicenseMissingJar;
import org.wso2.internal.apps.license.manager.models.SessionObjectHolder;
import org.wso2.internal.apps.license.manager.models.TaskProgress;
import org.wso2.internal.apps.license.manager.util.Constants;
import org.wso2.internal.apps.license.manager.util.JsonUtils;
import org.wso2.internal.apps.license.manager.util.LicenseManagerUtils;
import org.wso2.internal.apps.license.manager.util.ProgressTracker;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.util.SystemVariableUtil;

import java.io.File;
import java.nio.file.Paths;
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

/**
 * Main Service class which contains all the micro service endpoints.
 */
@Path("/")
public class LicenseManagerService {

    private static final Logger log = LoggerFactory.getLogger(LicenseManagerService.class);
    private ConcurrentHashMap<String, SessionObjectHolder> objectHolderMap = new ConcurrentHashMap<>();

    /**
     * Return the list of available set of licenses in the database.
     *
     * @param request Http request.
     * @return response with licenses.
     */
    @GET
    @Path("/license/availableLicenses")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllLicenseInformation(@Context Request request) {

        JsonObject responseJson = new JsonObject();
        ServiceExecutor serviceExecutor = new ServiceExecutor();

        try {
            JsonArray jsonArray = serviceExecutor.getListOfAllLicenses();
            responseJson.addProperty(Constants.RESPONSE_TYPE, Constants.SUCCESS);
            responseJson.addProperty(Constants.RESPONSE_MESSAGE, "All the licenses were extracted.");
            responseJson.add(Constants.RESPONSE_DATA, jsonArray);
        } catch (LicenseManagerDataException e) {
            responseJson.addProperty(Constants.RESPONSE_TYPE, Constants.ERROR);
            responseJson.addProperty(Constants.RESPONSE_MESSAGE, e.getMessage());
            log.error("Failed to retrieve data from the database. " + e.getMessage(), e);
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
    @Path("/pack/uploadedPacks")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllUploadedPacks() {

        JsonObject responseJson = new JsonObject();
        ServiceExecutor serviceExecutor = new ServiceExecutor();

        try {
            // Obtain the list of the available zip files.
            JsonArray responseData = serviceExecutor.getListOfPacksName();
            responseJson.addProperty(Constants.RESPONSE_TYPE, Constants.SUCCESS);
            responseJson.addProperty(Constants.RESPONSE_MESSAGE, "List of uploaded packs were retrieved.");
            responseJson.add(Constants.RESPONSE_DATA, responseData);

        } catch (LicenseManagerConfigurationException e) {
            responseJson.addProperty(Constants.RESPONSE_TYPE, Constants.ERROR);
            responseJson.addProperty(Constants.RESPONSE_MESSAGE, e.getMessage());
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
    @Path("/pack/selectedPack")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response extractJarsForSelectedPack(@Context Request request,
                                               @QueryParam("username") String username,
                                               String selectedPack) {

        PackExtractor packExtractor = new PackExtractor();
        TaskProgress taskProgress = packExtractor.startPackExtractionProcess(username, selectedPack);
        JsonObject responseJson = new JsonObject();
        responseJson.addProperty(Constants.RESPONSE_TYPE, Constants.SUCCESS);
        responseJson.addProperty(Constants.RESPONSE_STATUS, Constants.RUNNING);
        responseJson.addProperty(Constants.RESPONSE_MESSAGE, taskProgress.getMessage());
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
    public Response updateNameAndVersionOfJars(@Context Request request,
                                               @QueryParam("username") String username,
                                               String stringPayload) {

        JsonObject responseJson = new JsonObject();
        ServiceExecutor serviceExecutor = new ServiceExecutor();
        JarFileInformationHolder jarFileInformationHolder = objectHolderMap.get(username).getJarFileInformationHolder();
        serviceExecutor.updateNameMissingListOfJars(jarFileInformationHolder,
                JsonUtils.getAttributesFromRequestBody(stringPayload, "jars"));

        try {

            JarFileInfoDataHandler jarFileInfoDataHandler = serviceExecutor.insertJarInfoToDb(jarFileInformationHolder);

            // Get the license missing jars ( components and libraries)
            List<LicenseMissingJar> componentList = jarFileInfoDataHandler.getLicenseMissingComponents();
            List<LicenseMissingJar> libraryList = jarFileInfoDataHandler.getLicenseMissingLibraries();

            objectHolderMap.get(username).setLicenseMissingComponents(componentList);
            objectHolderMap.get(username).setLicenseMissingLibraries(libraryList);
            objectHolderMap.get(username).setProductId(jarFileInfoDataHandler.getProductId());

            // Create the response if success
            responseJson.addProperty(Constants.RESPONSE_TYPE, Constants.SUCCESS);
            responseJson.addProperty(Constants.RESPONSE_MESSAGE, "License missing jars were identified.");
            responseJson.add(Constants.LICENSE_MISSING_COMPONENTS, JsonUtils.getComponentsListAsJson(componentList));
            responseJson.add(Constants.LICENSE_MISSING_LIBRARIES, JsonUtils.getLibraryListAsJson(libraryList));
        } catch (LicenseManagerDataException e) {
            responseJson.addProperty(Constants.RESPONSE_TYPE, Constants.ERROR);
            responseJson.addProperty(Constants.RESPONSE_MESSAGE, e.getMessage());
            log.error("Error while inserting jar information into the database. " + e.getMessage(), e);
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
            JarFileInformationHolder jarFileInformationHolder =
                    objectHolderMap.get(username).getJarFileInformationHolder();
            productName = jarFileInformationHolder.getProductName();
            productVersion = jarFileInformationHolder.getProductVersion();
            LicenseFileGenerator licenseFileGenerator = new LicenseFileGenerator(databaseDriver, databaseUrl,
                    databaseUsername, databasePassword);
            licenseFileGenerator.generateLicenceFile(productName, productVersion, fileUploadPath);
            responseJson.addProperty(Constants.RESPONSE_TYPE, Constants.SUCCESS);
            responseJson.addProperty(Constants.RESPONSE_MESSAGE, "Done");
        } catch (Exception e) {
            responseJson.addProperty(Constants.RESPONSE_TYPE, Constants.ERROR);
            responseJson.addProperty(Constants.RESPONSE_MESSAGE, e.getMessage());
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
        JarFileInformationHolder jarFileInformationHolder = objectHolderMap.get(username).getJarFileInformationHolder();
        String productName = jarFileInformationHolder.getProductName();
        String productVersion = jarFileInformationHolder.getProductVersion();
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
    public Response addNewLicenseForJars(@Context Request request,
                                         @QueryParam("username") String username,
                                         String stringPayload) {

        JsonObject responseJson = new JsonObject();

        // Extract data from the request.
        JsonArray componentsJson = JsonUtils.getAttributesFromRequestBody(stringPayload, "components");
        JsonArray librariesJson = JsonUtils.getAttributesFromRequestBody(stringPayload, "libraries");

        SessionObjectHolder sessionObjectHolder = objectHolderMap.get(username);
        int productId = sessionObjectHolder.getProductId();
        ServiceExecutor serviceExecutor = new ServiceExecutor();
        serviceExecutor.updateLicensesOfLicenseMissingJars
                (sessionObjectHolder.getLicenseMissingComponents(), componentsJson);
        serviceExecutor.updateLicensesOfLicenseMissingJars
                (sessionObjectHolder.getLicenseMissingLibraries(), librariesJson);
        try {
            serviceExecutor.insertNewLicensesToDb(sessionObjectHolder.getLicenseMissingComponents(),
                    sessionObjectHolder.getLicenseMissingLibraries(), productId, username);

            responseJson.addProperty(Constants.RESPONSE_TYPE, Constants.SUCCESS);
            responseJson.addProperty(Constants.RESPONSE_MESSAGE, "Licenses were added successfully.");
        } catch (LicenseManagerDataException e) {
            responseJson.addProperty(Constants.RESPONSE_TYPE, Constants.ERROR);
            responseJson.addProperty(Constants.RESPONSE_MESSAGE, "Failed to add licenses." +
                    "Please contact application admin");
            log.error("Failed to add licenses. " + e.getMessage(), e);
        } catch (MessagingException e) {
            responseJson.addProperty(Constants.RESPONSE_TYPE, Constants.SUCCESS);
            responseJson.addProperty(Constants.RESPONSE_MESSAGE, "Failed to send email to the admin.");
            log.error("Error while sending email to application admins. " + e.getMessage(), e);
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
    @Path("/packExtraction/progress")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTaskStatus(@Context Request request,
                                  @QueryParam("username") String username) {

        TaskProgress taskProgress = ProgressTracker.getTaskProgress(username);
        JsonObject responseJson = new JsonObject();
        String statusMessage = taskProgress.getMessage();

        // Build the response based on the status of the task.
        switch (taskProgress.getStatus()) {

            case Constants.COMPLETE:
                responseJson.addProperty(Constants.RESPONSE_TYPE, Constants.SUCCESS);
                responseJson.addProperty(Constants.RESPONSE_STATUS, Constants.COMPLETE);
                responseJson.addProperty(Constants.RESPONSE_MESSAGE, statusMessage);
                break;

            case Constants.RUNNING:
                responseJson.addProperty(Constants.RESPONSE_TYPE, Constants.SUCCESS);
                responseJson.addProperty(Constants.RESPONSE_STATUS, Constants.RUNNING);
                responseJson.addProperty(Constants.RESPONSE_MESSAGE, statusMessage);
                break;

            default:
                responseJson.addProperty(Constants.RESPONSE_TYPE, Constants.ERROR);
                responseJson.addProperty(Constants.RESPONSE_STATUS, Constants.FAILED);
                responseJson.addProperty(Constants.RESPONSE_MESSAGE, statusMessage);
                break;
        }
        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Credentials", true)
                .build();
    }

    /**
     * Get the jars with name and version unidentified.
     *
     * @param request  HTTP request object.
     * @param username Username of the user.
     * @return The API response
     */
    @GET
    @Path("/pack/faultyNamedJars")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFaultyNamedJars(@Context Request request,
                                       @QueryParam("username") String username) {

        TaskProgress taskProgress = ProgressTracker.getTaskProgress(username);
        JsonObject responseJson = new JsonObject();
        JsonArray nameMissingJars;
        String statusMessage = taskProgress.getMessage();

        if (taskProgress.getStatus().equals(Constants.COMPLETE)) {
            SessionObjectHolder userObjectHolder = new SessionObjectHolder();
            JarFileInformationHolder jarFileInformationHolder = (JarFileInformationHolder) taskProgress.getData();
            userObjectHolder.setJarFileInformationHolder(jarFileInformationHolder);
            objectHolderMap.put(username, userObjectHolder);
            List<JarFile> errorJarFileList =
                    LicenseManagerUtils.removeDuplicates(jarFileInformationHolder.getErrorJarFileList());
            nameMissingJars = JsonUtils.getNameMissingJarsAsJson(errorJarFileList);
            responseJson.addProperty(Constants.RESPONSE_TYPE, Constants.SUCCESS);
            responseJson.addProperty(Constants.RESPONSE_MESSAGE, statusMessage);
            responseJson.add(Constants.RESPONSE_DATA, nameMissingJars);
            ProgressTracker.deleteTaskProgress(username);
        } else {
            responseJson.addProperty(Constants.RESPONSE_TYPE, Constants.ERROR);
            responseJson.addProperty(Constants.RESPONSE_MESSAGE, "Failed to get data");
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
