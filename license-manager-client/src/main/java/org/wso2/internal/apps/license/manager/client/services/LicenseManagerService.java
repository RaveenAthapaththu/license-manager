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

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.internal.apps.license.manager.client.exception.LicenseManagerException;
import org.wso2.internal.apps.license.manager.client.utils.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

/**
 * Endpoint for executing the license manager microservices.
 */
public class LicenseManagerService extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(LicenseManagerService.class);

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String username = String.valueOf(request.getSession().getAttribute("user"));

        // If the request is for downloading the license text file.
        if (request.getPathInfo().contains(Constants.DOWNLOAD_ENDPOINT)) {
            response.setContentType(MediaType.TEXT_HTML);
            response.setHeader("Content-Disposition", "attachment;filename=LICENSE.txt");
            try (InputStream is = ServiceExecutor.executeDownloadService(request.getPathInfo(), username)) {
                OutputStream out = response.getOutputStream();
                String result = new BufferedReader(new InputStreamReader(is))
                        .lines().collect(Collectors.joining("\n"));
                out.write(result.getBytes());
            } catch (LicenseManagerException e) {
                log.info(String.format("Error Occurred %s", e.getMessage()));
            }
        } else {
            try {
                response.setContentType(MediaType.APPLICATION_JSON);
                PrintWriter out = response.getWriter();
                out.print(ServiceExecutor.executeGetService(request.getPathInfo(), username));
                if (log.isDebugEnabled()) {
                    log.debug("A response is received for GET request sent to {MICROSERVICE}/" + request.getPathInfo()
                            + " by " + username);
                }
            } catch (JSONException e) {
                log.info("Error occurred:", e.getMessage());
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {

        String username = String.valueOf(request.getSession().getAttribute("user"));
        response.setContentType("application/json");
        try {
            PrintWriter out = response.getWriter();
            out.print(ServiceExecutor.executePostService(request.getPathInfo(), request.getReader().readLine(), username));
            if (log.isDebugEnabled()) {
                log.debug("A response is received for POST request sent to {MICROSERVICE}/" + request.getPathInfo()
                        + " by " + username);
            }
        } catch (JSONException | IOException e) {
            log.error("Failed to get the response from the backend service. " + e.getMessage(), e);
        }
    }
}
