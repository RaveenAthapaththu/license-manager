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

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import javax.ws.rs.core.MediaType;

/**
 * Call the corresponding micro service.
 */
public class ServiceExecuter {

    public static void test() {

        System.out.println("hello");
    }

    public static JSONObject executeGetService(String endpoint, String username) throws IOException, JSONException,
            URISyntaxException {

        String url = "http://localhost:9091/" + endpoint;

        // Setting up authentication
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("licensemanager",
                "licensemanager");
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(AuthScope.ANY, credentials);

        // Setting the HTTP request
        URIBuilder builder = new URIBuilder(url);
        builder.setParameter("username", username);
        HttpGet request = new HttpGet(builder.build());

        // Calling the micro service
        HttpClient client = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(provider)
                .build();
        HttpResponse response = client.execute(request);

        // Build json response
        HttpEntity entity = response.getEntity();
        JSONObject result = null;
        if (entity != null) {
            // parsing JSON
            result = new JSONObject(EntityUtils.toString(entity));

        }
        return result;
    }

    public static JSONObject executePostService(String endpoint, String payload, String username) throws IOException,
            JSONException, URISyntaxException {

        String url = "http://localhost:9091" + endpoint;

        // Setting up authentication
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("licensemanager",
                "licensemanager");
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(AuthScope.ANY, credentials);

        // Setting the HTTP request
        URIBuilder builder = new URIBuilder(url);
        builder.setParameter("username", username);
        HttpPost request = new HttpPost(builder.build());
        request.setHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
        ObjectMapper mapper = new ObjectMapper();
        String requestBodyInString = mapper.writeValueAsString(payload);
        StringEntity requestBody = new StringEntity(requestBodyInString, "UTF-8");
        requestBody.setContentType("application/json");
        request.setEntity(requestBody);

        // Calling the micro service
        HttpClient client = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(provider)
                .build();
        HttpResponse response = client.execute(request);

        // Build json response
        HttpEntity entity = response.getEntity();
        JSONObject result = null;
        if (entity != null) {
            // parsing JSON
            result = new JSONObject(EntityUtils.toString(entity));

        }
        return result;
    }

    public static InputStream executeDownloadService(String endpoint, String username) throws IOException,
            JSONException,
            URISyntaxException {

        String url = "http://localhost:9091/" + endpoint;

        // Setting up authentication
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials("licensemanager",
                "licensemanager");
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(AuthScope.ANY, credentials);

        // Setting the HTTP request
        URIBuilder builder = new URIBuilder(url);
        builder.setParameter("username", username);
        HttpGet request = new HttpGet(builder.build());

        // Calling the micro service
        HttpClient client = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(provider)
                .build();
        HttpResponse response = client.execute(request);

        // Build json response
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            return entity.getContent();
        } else {
            return null;
        }
    }
}
