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
package org.wso2.internal.apps.license.manager.client.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.internal.apps.license.manager.client.exception.LicenseManagerException;
import org.wso2.internal.apps.license.manager.client.msf4jhttp.PropertyReader;
import org.wso2.internal.apps.license.manager.client.utils.ServiceUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import javax.ws.rs.core.MediaType;

/**
 * Call the corresponding micro service.
 */
public class ServiceExecuter {

    private static final Log log = LogFactory.getLog(ServiceExecuter.class);

    /**
     * Call the backend service for the GET requests.
     *
     * @param endpoint endpoint of the service
     * @param username logged user
     * @return response from the backend/error object
     * @throws JSONException if creating a json from the response entity fails.
     */
    public static JSONObject executeGetService(String endpoint, String username) throws JSONException {

        PropertyReader properties = new PropertyReader();
        String url = properties.getBackendUrl() + endpoint;
        JSONObject result = null;

        try {
            // Create the request.
            URIBuilder builder = new URIBuilder(url);
            builder.setParameter("username", username);
            HttpGet request = new HttpGet(builder.build());

            // Calling the micro service.
            CloseableHttpClient httpClient = ServiceUtils.createTrustedHttpClient();
            HttpResponse response = httpClient.execute(request);

            // Build json response
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                // parsing JSON
                result = new JSONObject(EntityUtils.toString(entity));
            }
        } catch (URISyntaxException | IOException | JSONException e) {
            result = new JSONObject();
            result.put("responseType", "Error");
            result.put("responseMessage", "Failed to get response from server");
            log.error("Failed to get response from server. " + e.getMessage(), e);
        } catch (LicenseManagerException e) {
            result = new JSONObject();
            result.put("responseType", "Error");
            result.put("responseMessage", e.getMessage());
            log.error(e.getMessage(), e);
        }
        return result;
    }

    /**
     * Call the backend service for the POST requests.
     *
     * @param endpoint endpoint of the service
     * @param payload  request body
     * @param username logged user
     * @return response from the backend/error object
     * @throws JSONException if creating a json from the response entity fails.
     */
    public static JSONObject executePostService(String endpoint, String payload, String username) throws JSONException {

        PropertyReader properties = new PropertyReader();
        String url = properties.getBackendUrl() + endpoint;
        JSONObject result = null;

        try {
            // Create the request.
            URIBuilder builder = new URIBuilder(url);
            builder.setParameter("username", username);
            HttpPost request = new HttpPost(builder.build());
            request.setHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
            ObjectMapper mapper = new ObjectMapper();
            String requestBodyInString = mapper.writeValueAsString(payload);
            StringEntity requestBody = new StringEntity(requestBodyInString, "UTF-8");
            requestBody.setContentType(MediaType.APPLICATION_JSON);
            request.setEntity(requestBody);

            // Calling the micro service
            CloseableHttpClient httpClient = ServiceUtils.createTrustedHttpClient();
            HttpResponse response = httpClient.execute(request);

            // Build json response
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                // parsing JSON
                result = new JSONObject(EntityUtils.toString(entity));
            }
        } catch (URISyntaxException | IOException | JSONException e) {
            result = new JSONObject();
            result.put("responseType", "Error");
            result.put("responseMessage", "Failed to get response from server");
            log.error("Failed to get response from server. " + e.getMessage(), e);
        } catch (LicenseManagerException e) {
            result = new JSONObject();
            result.put("responseType", "Error");
            result.put("responseMessage", e.getMessage());
            log.error(e.getMessage(), e);
        }
        return result;
    }

    /**
     * Call a backend service and download the License Text.
     *
     * @param endpoint endpoint of the service
     * @param username logged user
     * @return response from the backend/error object
     * @throws JSONException if creating a json from the response entity fails.
     */
    static InputStream executeDownloadService(String endpoint, String username) throws LicenseManagerException {

        PropertyReader properties = new PropertyReader();
        String url = properties.getBackendUrl() + endpoint;
        try {

            // Setting the HTTP request
            URIBuilder builder = new URIBuilder(url);
            builder.setParameter("username", username);
            HttpGet request = new HttpGet(builder.build());

            // Calling the micro service
            CloseableHttpClient client = ServiceUtils.createTrustedHttpClient();
            HttpResponse response = client.execute(request);

            // Get the file from the response entity.
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                return entity.getContent();
            } else {
                return null;
            }
        } catch (URISyntaxException | IOException e) {
            log.error("Failed to get download the license text from the server. " + e.getMessage(), e);
            throw new LicenseManagerException("Failed to download the file");
        }
    }
}
