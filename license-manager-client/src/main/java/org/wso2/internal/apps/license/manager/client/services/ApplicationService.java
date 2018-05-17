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

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Endpoint for executing backend services of the application.
 */
public class ApplicationService extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(ApplicationService.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse response) throws IOException {

        String username = String.valueOf(req.getSession().getAttribute("user"));
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        JsonObject userDetails = new JsonObject();
        userDetails.addProperty("username", username);
        out.print(userDetails);
        if (log.isDebugEnabled()) {
            log.debug("Successfully sent the user details for the user " + username);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse response) {
        // Do nothing.
    }

}
