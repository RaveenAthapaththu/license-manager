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
import org.wso2.internal.apps.license.manager.model.JarFile;
import org.wso2.internal.apps.license.manager.model.LicenseMissingJar;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Methods related to the JSON objects
 */
public class JsonUtils {

    /**
     * Create an array of json objects containing the license missing components.
     * { index : xxx, name : xxx, version : xxx, type : xxx, previousLicense : xxx, licenseKey : xxx }
     *
     * @param licenseMissingJars array list of components
     * @return json array of jars
     */
    public static JsonArray getLicenseMissingJarsAsJsonArray(List<LicenseMissingJar> licenseMissingJars) {

        JsonArray componentJsonArray = new JsonArray();
        for (int i = 0; i < licenseMissingJars.size(); i++) {
            JsonObject licenseMissingJar = new JsonObject();
            licenseMissingJar.addProperty("index", i);
            licenseMissingJar.addProperty("name", licenseMissingJars.get(i).getJarFile().getProjectName());
            licenseMissingJar.addProperty("version", licenseMissingJars.get(i).getJarFile().getVersion());
            licenseMissingJar.addProperty("type", licenseMissingJars.get(i).getJarFile().getType());
            licenseMissingJar.addProperty("previousLicense", licenseMissingJars.get(i).getLicenseKey());
            licenseMissingJar.addProperty("licenseKey", licenseMissingJars.get(i).getLicenseKey());
            componentJsonArray.add(licenseMissingJar);
        }
        return componentJsonArray;
    }



    /**
     * Create a json array from the list of faulty named jars.
     * { index : xxx, jarFileName : xxx, name : xxx, version : xxx }
     *
     * @param errorJarFileList array of faulty named jars
     * @return json array of faulty named jars
     */
    public static JsonArray getFaultyNamedJarsAsJsonArray(List<JarFile> errorJarFileList) {

        JsonArray faultyNamedJars = new JsonArray();
        for (int i = 0; i < errorJarFileList.size(); i++) {
            JsonObject currentJar = new JsonObject();
            currentJar.addProperty("index", i);
            currentJar.addProperty("jarFileName", errorJarFileList.get(i).getJarFile().getName());
            currentJar.addProperty("name", errorJarFileList.get(i).getProjectName());
            currentJar.addProperty("version", errorJarFileList.get(i).getVersion());
            faultyNamedJars.add(currentJar);
        }
        return faultyNamedJars;
    }

    /**
     * Create a json array from the list of uploaded pack.
     * {name : nameOfThePack}
     *
     * @param listOfPacks list of packs as an array list of strings
     * @return json array
     */
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

    /**
     * Get the json array from a given string when the attribute name is given.
     *
     * @param requestBody   string containing a json object
     * @param attributeName attribute which should be retrieved
     * @return jason array object retrieved from the string
     */
    public static JsonArray getAttributesFromRequestBody(String requestBody, String attributeName) {

        JsonParser jsonParser = new JsonParser();

        JsonElement jsonElement = jsonParser.parse(requestBody);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        return jsonObject.get(attributeName).getAsJsonArray();
    }

    /**
     * Create a json array from the retrieved result set for licenses from the database.
     * { LICENSE_ID : xxx, LICENSE_KEY : xxx, LICENSE_NAME : xxx }
     *
     * @param resultSet all licenses
     * @return json array for licenses
     * @throws SQLException if the obtaining values from the result set fails.
     */
    public static JsonArray createJsonArrayFromLicenseResultSet(ResultSet resultSet) throws SQLException {

        JsonArray licensesJsonArray = new JsonArray();
        while (resultSet.next()) {
            JsonObject licenseJson = new JsonObject();
            licenseJson.addProperty("LICENSE_ID", resultSet.getInt(SqlRelatedConstants.LICENSE_ID));
            licenseJson.addProperty("LICENSE_KEY", resultSet.getString(SqlRelatedConstants.PRIMARY_KEY_LICENSE));
            licenseJson.addProperty("LICENSE_NAME", resultSet.getString(SqlRelatedConstants.LICENSE_NAME));
            licensesJsonArray.add(licenseJson);
        }
        return licensesJsonArray;
    }

}

