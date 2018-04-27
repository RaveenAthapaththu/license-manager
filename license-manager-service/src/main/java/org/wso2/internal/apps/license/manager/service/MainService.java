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
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.workingdogs.village.DataSetException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.internal.apps.license.manager.impl.exception.LicenseManagerRuntimeException;
import org.wso2.internal.apps.license.manager.impl.main.Jar;
import org.wso2.internal.apps.license.manager.impl.main.JarHolder;
import org.wso2.internal.apps.license.manager.impl.main.LicenseFileGenerator;
import org.wso2.internal.apps.license.manager.impl.main.ProductJarManager;
import org.wso2.internal.apps.license.manager.impl.models.NewLicenseEntry;
import org.wso2.internal.apps.license.manager.impl.models.SessionObjectHolder;
import org.wso2.internal.apps.license.manager.util.Constants;
import org.wso2.internal.apps.license.manager.util.DBHandler;
import org.wso2.internal.apps.license.manager.util.EmailUtils;
import org.wso2.internal.apps.license.manager.util.LicenseManagerUtils;
import org.wso2.msf4j.Request;
import org.wso2.msf4j.util.SystemVariableUtil;

import java.io.File;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
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
 * This is the Microservice resource class.
 * See <a href="https://github.com/wso2/msf4j#getting-started">https://github.com/wso2/msf4j#getting-started</a>
 * for the usage of annotations.
 *
 * @since 1.0-SNAPSHOT
 */
@Path("/")
public class MainService {

    private static final Logger log = LoggerFactory.getLogger(MainService.class);
    private ConcurrentHashMap<String, SessionObjectHolder> objectHolderMap = new ConcurrentHashMap<>();

    @GET
    @Path("/selectLicense")
    @Produces(MediaType.APPLICATION_JSON)
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
    @Path("/getLocalPacks")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listLocallyUploadedPacks() {

        log.info("api call is initiated. \n");
        ArrayList<String> listOfPacks = new ArrayList<>();
        JsonObject responseJson = new JsonObject();
        JsonArray responseData = new JsonArray();

        String pathToStorage = SystemVariableUtil.getValue(Constants.FILE_UPLOAD_PATH, null);
        log.info(pathToStorage);

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

    /**
     * Return the list of packs uploaded to the FTP server.
     *
     * @return API response
     */
    @GET
    @Path("/getPacks")
    @Produces(MediaType.APPLICATION_JSON)
    public Response listUploadedPacks() {

        String ftpHost = SystemVariableUtil.getValue(Constants.FTP_HOST, null);
        int ftpPort = Integer.valueOf(SystemVariableUtil.getValue(Constants.FTP_PORT, null));
        String ftpUsername = SystemVariableUtil.getValue(Constants.FTP_USERNAME, null);
        String ftpPassword = SystemVariableUtil.getValue(Constants.FTP_PASSWORD, null);
        String ftpFilePath = SystemVariableUtil.getValue(Constants.FTP_FILE_LOCATION, null);

        ArrayList<String> listOfPacks = new ArrayList<>();
        Session session = null;
        ChannelSftp sftpChannel = null;
        JsonObject responseJson = new JsonObject();
        JsonArray responseData = new JsonArray();
        JSch jsch = new JSch();

        try {
            // Initiate the SFTP connection with the server.
            session = jsch.getSession(ftpUsername, ftpHost, ftpPort);
            Hashtable<String, String> config = new Hashtable<>();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.setPassword(ftpPassword);
            session.connect();
            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();

            // Obtain the list of the available zip files.
            Vector fileList = sftpChannel.ls(ftpFilePath);
            for (Object aFileListVector : fileList) {
                ChannelSftp.LsEntry entry = (ChannelSftp.LsEntry) aFileListVector;
                if (entry.getFilename().endsWith(".zip")) {
                    listOfPacks.add(entry.getFilename());
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

        } catch (JSchException | SftpException e) {
            responseJson.addProperty("responseType", Constants.ERROR);
            responseJson.addProperty("responseMessage", "Failed to connect with FTP server.");
            log.error("Failed to connect with FTP server. " + e.getMessage(), e);

        } finally {
            // Close the connections.
            if (session != null) {
                session.disconnect();
            }
            if (sftpChannel != null) {
                sftpChannel.exit();
            }
        }
        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Credentials", true)
                .build();
    }

    @GET
    @Path("/extractPack")
    @Produces(MediaType.APPLICATION_JSON)
    public Response downloadPackFromFTP() {

        String user = "licensemanager";
        String password = ",Wp3TOf^-ITd2?4zUU2X";
        String host = "ftp.support.wso2.com";
        int port = 22;
        Session session = null;
        ChannelSftp sftpChannel = null;
        JsonObject responseJson = new JsonObject();
        JsonArray responseData = new JsonArray();
        JSch jsch = new JSch();

        try {
            log.info("Download process started...");

            session = jsch.getSession(user, host, port);
            Hashtable<String, String> config = new Hashtable<>();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.setPassword(password);
            session.connect();

            sftpChannel = (ChannelSftp) session.openChannel("sftp");
            sftpChannel.connect();
            String pathToStorage = SystemVariableUtil.getValue(Constants.FILE_UPLOAD_PATH, null);
            sftpChannel.get("/licensemanager/wso2ei-6.1.1.zip", pathToStorage);
            String zipFilePath = pathToStorage + "wso2ei-6.1.1.zip";
            String filePath = zipFilePath.substring(0, zipFilePath.lastIndexOf('.'));
            File zipFile = new File(zipFilePath);
            File dir = new File(filePath);
            log.info("Pack unzipping started...");
            LicenseManagerUtils.unzip(zipFile.getAbsolutePath(), dir.getAbsolutePath());
            log.info("Pack unzipping complete.");
            responseJson.addProperty("responseType", Constants.SUCCESS);
            responseJson.addProperty("responseMessage", "Done");
            responseJson.add("responseData", responseData);
        } catch (JSchException | SftpException e) {
            responseJson.addProperty("responseType", Constants.ERROR);
            responseJson.addProperty("responseMessage", "Failed to get file from the server.");
            log.error("Failed to connect with FTP server. " + e.getMessage(), e);
        } catch (LicenseManagerRuntimeException e) {
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.disconnect();
            }
            if (sftpChannel != null) {
                sftpChannel.exit();
            }
        }
        return Response.ok(responseJson, MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Credentials", true)
                .build();
    }

    @POST
    @Path("/extractJars")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response extractJars(@Context Request request,
                                @QueryParam("username") String username,
                                String stringPayload) {

        JsonObject responseJson = new JsonObject();
        JsonArray nameMissingJars = new JsonArray();
        SessionObjectHolder userObjectHolder = new SessionObjectHolder();
        String pathToStorage = SystemVariableUtil.getValue(Constants.FILE_UPLOAD_PATH, null);
        String zipFilePath = pathToStorage + stringPayload;
        String filePath = zipFilePath.substring(0, zipFilePath.lastIndexOf('.'));
        File zipFile = new File(zipFilePath);
        File dir = new File(filePath);
        try {
            log.info("Pack unzipping started...");
            LicenseManagerUtils.unzip(zipFile.getAbsolutePath(), dir.getAbsolutePath());
            log.info("Pack unzipping complete.");
            log.info("Jar extraction started...");
            JarHolder jarHolder = LicenseManagerUtils.checkJars(filePath);
            log.info("Jar extraction complete.");
            userObjectHolder.setJarHolder(jarHolder);
            objectHolderMap.put(username, userObjectHolder);
            List<Jar> errorJarList = jarHolder.getErrorJarList();

            // Convert the java List to a json object array.
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
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response enterJarsResource(@Context Request request,
                                      @QueryParam("username") String username,
                                      String stringPayload) {

        JsonObject responseJson = new JsonObject();
        JsonParser jsonParser = new JsonParser();
        // TODO: 4/9/18 default licenseID. retrieve licenses from the database if the jar exists with different version.
        int licenseId = 1;
        try {
            JsonElement jsonElement = jsonParser.parse(stringPayload);
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            JsonArray jsonArray = jsonObject.get("jars").getAsJsonArray();
            JarHolder jarHolder = objectHolderMap.get(username).getJarHolder();
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
            objectHolderMap.get(username).setLicenseMissingComponents(componentList);
            objectHolderMap.get(username).setLicenseMissingLibraries(libraryList);
            objectHolderMap.get(username).setProductId(productJarManager.getProductId());
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

    @GET
    @Path("/getLicense")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLicenseResource(@Context Request request,
                                       @QueryParam("username") String username) {

        JsonObject responseJson = new JsonObject();
        String databaseUrl = SystemVariableUtil.getValue(Constants.DATABASE_URL, null);
        String databaseDriver = SystemVariableUtil.getValue(Constants.DATABASE_DRIVER, null);
        String databaseUsername = SystemVariableUtil.getValue(Constants.DATABASE_USERNAME, null);
        String databasePassword = SystemVariableUtil.getValue(Constants.DATABASE_PASSWORD, null);
        String fileUploadPath = SystemVariableUtil.getValue(Constants.FILE_UPLOAD_PATH, null);
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

    @GET
    @Path("/downloadLicense")
    public Response getFile(@Context Request request,
                            @QueryParam("username") String username) {

        String mountPath = SystemVariableUtil.getValue(Constants.FILE_UPLOAD_PATH, null);
        JarHolder jarHolder = objectHolderMap.get(username).getJarHolder();
        String productName = jarHolder.getProductName();
        String productVersion = jarHolder.getProductVersion();
        String fileName = "LICENSE(" + productName + "-" + productVersion + ").TXT";

        File file = Paths.get(mountPath, fileName).toFile();
        if (file.exists()) {

            // Delete the folders after generating licenses text.
            LicenseManagerUtils.deleteFolder(mountPath + productName + "-" + productVersion + ".zip");
            LicenseManagerUtils.deleteFolder(mountPath + productName + "-" + productVersion);
            objectHolderMap.remove(username);
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
                String componentName = sessionObjectHolder.getLicenseMissingComponents().get(index).getJarFile()
                        .getName();
                String name = componentsJson.get(i).getAsJsonObject().get("name").getAsString();
                String version = componentsJson.get(i).getAsJsonObject().get("version").getAsString();
                int licenseId = componentsJson.get(i).getAsJsonObject().get("licenseId").getAsInt();
                String licenseKey = dbHandler.selectLicenseFromId(licenseId);
                dbHandler.insertComponent(name, componentName, version);
                dbHandler.insertProductComponent(componentName, productId);
                dbHandler.insertComponentLicense(componentName, licenseKey);
                NewLicenseEntry newEntry = new NewLicenseEntry(componentName, name, version, licenseKey);
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
                int licenseId = librariesJson.get(i).getAsJsonObject().get("licenseId").getAsInt();

                String libraryFileName = sessionObjectHolder.getLicenseMissingLibraries().get(index).getJarFile()
                        .getName();
                if (sessionObjectHolder.getLicenseMissingLibraries().get(index).getParent() != null) {
                    parent = sessionObjectHolder.getLicenseMissingLibraries().get(index).getParent();
                    componentKey = parent.getJarFile().getName();
                }
                String licenseKey = dbHandler.selectLicenseFromId(licenseId);
                int libId = dbHandler.getLibraryId(name, libraryFileName, version, type);
                dbHandler.insertLibraryLicense(licenseKey, Integer.toString(libId));
                if (parent != null && parent.getType().equals(Constants.JAR_TYPE_WSO2)) {
                    dbHandler.insertComponentLibrary(componentKey, libId);
                } else {
                    dbHandler.insertProductLibrary(libId, productId);
                }
                NewLicenseEntry newEntry = new NewLicenseEntry(libraryFileName, name, version, licenseKey);
                newLicenseEntryLibraryList.add(newEntry);
            }

            // If there are new licenses added successfully, send a mail to the admin.
            if (newLicenseEntryComponentList.size() > 0 || newLicenseEntryLibraryList.size() > 0) {
                EmailUtils.sendEmail(username, newLicenseEntryComponentList, newLicenseEntryLibraryList);
            }

            responseJson.addProperty("responseType", Constants.SUCCESS);
            responseJson.addProperty("responseMessage", "Done");

        } catch (DataSetException | SQLException | ClassNotFoundException e) {
            responseJson.addProperty("responseType", Constants.ERROR);
            responseJson.addProperty("responseMessage", "Failed to add licenses. " +
                    "Please contact application admin");
            log.error("Failed to add licenses." + e.getMessage(), e);
        } catch (MessagingException e) {
            responseJson.addProperty("responseType", Constants.SUCCESS);
            responseJson.addProperty("responseMessage", "Failed to send email to the admin.");
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

//    /**
//     * Get the report progress.
//     *
//     * @param request   HTTP request object.
//     * @param taskID    Unique task ID assigned at task startup.
//     * @param username  Username of the user.
//     * @return The API response
//     */
//    @GET
//    @Path("/progress")
//    @Produces(MediaType.APPLICATION_JSON)
//    public Response getTaskStatus(@Context Request request,
//                                      @QueryParam("id") String taskID,
//                                      @QueryParam("username") String username,
//                                      String stringPayload) {
////        final TaskProgress taskProgress = ProgressTracker.getTaskProgress(email, taskName, taskID);
////
////        APIResponse apiResponse = new APIResponse();
////        apiResponse.setStatus(Constant.APIResponseStatus.TASK_ACTIVE);
////        apiResponse.setMessage("Task found");
////        apiResponse.setData(taskProgress);
////        return new Gson().toJson(apiResponse);
//    }

    @GET
    @Path("/test")
    public Response test(@Context Request request) {

        log.info("sample");
        return Response.ok("Hello Java", MediaType.APPLICATION_JSON)
                .header("Access-Control-Allow-Credentials", true)
                .build();
    }
}
