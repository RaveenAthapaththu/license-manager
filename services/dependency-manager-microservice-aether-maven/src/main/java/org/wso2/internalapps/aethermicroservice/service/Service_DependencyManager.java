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

package org.wso2.internalapps.aethermicroservice.service;

import org.wso2.internalapps.aethermicroservice.JSONFormats.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.wso2.internalapps.aethermicroservice.configFileReader.ConfigFilePOJO;
import org.wso2.internalapps.aethermicroservice.serviceSupportClasses.*;
import java.util.logging.Logger;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * This is the Microservice resource class.
 * See <a href="https://github.com/wso2/msf4j#getting-started">https://github.com/wso2/msf4j#getting-started</a>
 * for the usage of annotations.
 *
 * @since 1.0-SNAPSHOT
 */
@Path("/aethermicroservice")
public class Service_DependencyManager {

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    private ConfigFilePOJO configurations;

    public Service_DependencyManager(ConfigFilePOJO configs) {
        this.configurations = configs;
    }

    @GET
    @Path("/test")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get() {
        // For the purpose of checking
        LOGGER.info("Testing the Maven Central Aether Micorservice");

        JsonObject reply;
        String message = "{ \"MessageTest\":\"Hello, this is wso2 dependency manager dealing with maven repo\"}";

        JsonParser parser = new JsonParser();
        reply = parser.parse(message).getAsJsonObject();

        LOGGER.info("Building reply Message successful");

        return Response.ok(reply,MediaType.APPLICATION_JSON).build();
    }

    @POST
    @Path("/getLatest")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLatestVersion(gaJSON jsonObj) {

        LOGGER.info("Finding Latest version for library of group ID:"+jsonObj.groupID+" and artifact ID:"+jsonObj.artifactID);

        long startTime = System.currentTimeMillis();
        String latestVersion = LatestVersion.getVersion(jsonObj.groupID,jsonObj.artifactID,configurations);

        JsonObject reply;
        if(latestVersion.contains("ErrorMsg")){

            LOGGER.info("Could not Find");
            return Response.status(Response.Status.NOT_FOUND).entity("NotFound").build();
        }else{

            LOGGER.info("Finished retreiving latest version");
            JsonParser parser = new JsonParser();
            reply = parser.parse(latestVersion).getAsJsonObject();
        }
        long endTime = System.currentTimeMillis();
        LOGGER.info("Time taken:"+(endTime-startTime));
        return Response.ok(reply,MediaType.APPLICATION_JSON).build();
    }

    @POST
    @Path("/getVersions")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAvailableVersions(gaJSON jsonObj) {

        LOGGER.info("Finding available versions for library of group ID:"+jsonObj.groupID+" and artifact ID:"+jsonObj.artifactID);

        long startTime = System.currentTimeMillis();
        StringBuilder versionsAvailable = AvailableVersions.getAvailableVersions(jsonObj.groupID,jsonObj.artifactID,configurations);
        String availableVersions = versionsAvailable.toString();
        JsonObject reply;

        if(availableVersions.contains("ErrorMsg")){
            LOGGER.info("Could not Find");
            return Response.status(Response.Status.NOT_FOUND).entity("NotFound").build();
        }else{

            LOGGER.info("Finished retreiving available versions");
            JsonParser parser = new JsonParser();
            reply = parser.parse(availableVersions).getAsJsonObject();
        }
        long endTime = System.currentTimeMillis();
        LOGGER.info("Time taken:"+(endTime-startTime));
        return Response.ok(reply,MediaType.APPLICATION_JSON).build();
    }


    @POST
    @Path("/getDHeirarchy")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDHeirarchy(gavJSON jsonObj) {

        LOGGER.info("Finding heirarchy for library of group ID:"+jsonObj.groupID+" and artifact ID:"+jsonObj.artifactID+" and version:"+jsonObj.version);

        long startTime = System.currentTimeMillis();
        StringBuilder JsonStringBuilderTree = DependencyHeirarchy.getDependencyHeirarchy(jsonObj.groupID,jsonObj.artifactID,jsonObj.version,configurations);
        String JsonStringTree= JsonStringBuilderTree.toString();

        JsonObject reply;

        if(JsonStringTree.contains("Error")){
            LOGGER.info("Could not Find");
            return Response.status(Response.Status.NOT_FOUND).entity("NotFound").build();
        }else{
            LOGGER.info("Finished retreiving heirarchy");
            JsonParser parser = new JsonParser();
            reply = parser.parse(JsonStringBuilderTree.deleteCharAt(JsonStringBuilderTree.length()-1).toString()).getAsJsonObject();

        }
        long endTime = System.currentTimeMillis();
        LOGGER.info("Time taken:"+(endTime-startTime));
        return Response.ok(reply,MediaType.APPLICATION_JSON).build();
    }
}

