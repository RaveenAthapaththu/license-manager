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
import org.wso2.internal.apps.license.manager.client.utils.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;

/**
 * Endpoint for executing the license manager microservices.
 */
public class LicenseManagerService extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = String.valueOf(request.getSession().getAttribute("user"));

        try {
            if (request.getPathInfo().equals(Constants.DOWNLOAD_ENDPOINT)) {
                response.setContentType(MediaType.TEXT_HTML);
                response.setHeader("Content-Disposition",
                        "attachment;filename=LICENSE.txt");
                InputStream is = ServiceExecuter.executeDownloadService(request.getPathInfo(), username);

                int read;
                byte[] bytes = new byte[1024];
                OutputStream out = response.getOutputStream();

                if (is != null) {
                    while ((read = is.read(bytes)) != -1) {
                        out.write(bytes, 0, read);
                    }
                }
                out.flush();
                out.close();
            } else {
                response.setContentType(MediaType.APPLICATION_JSON);
                PrintWriter out = response.getWriter();
                out.print(ServiceExecuter.executeGetService(request.getPathInfo(), username));
            }
        } catch (JSONException | URISyntaxException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = String.valueOf(request.getSession().getAttribute("user"));
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        try {
            out.print(ServiceExecuter.executePostService(request.getPathInfo(), request.getReader().readLine(), username));
        } catch (JSONException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

}
