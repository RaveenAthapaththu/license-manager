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

package org.wso2.internal.apps.license.manager.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.wso2.internal.apps.license.manager.models.Jar;
import org.wso2.internal.apps.license.manager.models.LicenseMissingJar;

import java.util.ArrayList;
import java.util.List;

/**
 * Methods related to the JSON objects
 */
public class JsonUtils {

    /**
     * Create an array of json objects containing the license missing components.
     *
     * @param componentList array list of components
     * @return json array of components
     */
    public static JsonArray getComponentsListAsJson(List<LicenseMissingJar> componentList) {

        JsonArray componentJsonArray = new JsonArray();

        for (int i = 0; i < componentList.size(); i++) {
            JsonObject component = new JsonObject();
            component.addProperty("index", i);
            component.addProperty("name", componentList.get(i).getJar().getProjectName());
            component.addProperty("version", componentList.get(i).getJar().getVersion());
            component.addProperty("type", componentList.get(i).getJar().getType());
            component.addProperty("previousLicense", componentList.get(i).getLicenseKey());
            component.addProperty("licenseKey", componentList.get(i).getLicenseKey());
            componentJsonArray.add(component);
        }
        return componentJsonArray;
    }

    /**
     * Create an array of json objects containing the license missing libraries.
     *
     * @param libraryList array list of libraries
     * @return json array of libraries
     */
    public static JsonArray getLibraryListAsJson(List<LicenseMissingJar> libraryList) {

        JsonArray libraryJsonArray = new JsonArray();
        for (int i = 0; i < libraryList.size(); i++) {
            JsonObject library = new JsonObject();
            String libraryType = (libraryList.get(i).getJar().getParent() == null) ?
                    ((libraryList.get(i).getJar().isBundle()) ? Constants.JAR_TYPE_BUNDLE : Constants.JAR_TYPE_JAR) :
                    Constants.JAR_TYPE_JAR_IN_BUNDLE;
            library.addProperty("index", i);
            library.addProperty("name", libraryList.get(i).getJar().getProjectName());
            library.addProperty("version", libraryList.get(i).getJar().getVersion());
            library.addProperty("type", libraryType);
            library.addProperty("previousLicense", libraryList.get(i).getLicenseKey());
            library.addProperty("licenseKey", libraryList.get(i).getLicenseKey());
            libraryJsonArray.add(library);
        }
        return libraryJsonArray;
    }

    /**
     * Create a json array from the list of name missing jars.
     *
     * @param errorJarList array of name missing jars
     * @return json array of name missing jars
     */
    public static JsonArray getNameMissingJarsAsJson(List<Jar> errorJarList) {

        JsonArray nameMissingJars = new JsonArray();
        for (int i = 0; i < errorJarList.size(); i++) {
            JsonObject currentJar = new JsonObject();
            currentJar.addProperty("index", i);
            currentJar.addProperty("jarFileName", errorJarList.get(i).getJarFile().getName());
            currentJar.addProperty("name", errorJarList.get(i).getProjectName());
            currentJar.addProperty("version", errorJarList.get(i).getVersion());
            nameMissingJars.add(currentJar);
        }
        return nameMissingJars;
    }

    public static JsonArray getListOfPacksUploadedAsJson(ArrayList<String> listOfPacks) {

        JsonArray uploadedPacks = new JsonArray();

        // Obtain the list of the available zip files.
        for (String listOfPack : listOfPacks) {
            JsonObject ob = new JsonObject();
            ob.addProperty("name", listOfPack);
            uploadedPacks.add(ob);
        }
        return uploadedPacks;
    }

    public static JsonArray getAttributesFromRequestBody(String requestBody, String attributeName) {

        JsonParser jsonParser = new JsonParser();

        JsonElement jsonElement = jsonParser.parse(requestBody);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonArray jarsWithNames = jsonObject.get(attributeName).getAsJsonArray();
        return jarsWithNames;
    }

}
