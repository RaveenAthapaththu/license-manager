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
import org.wso2.internal.apps.license.manager.client.exception.LicenseManagerException;
import org.wso2.internal.apps.license.manager.client.utils.FtpCredentialReader;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FtpCredentialService extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(FtpCredentialService.class);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse response) throws IOException {

        JsonObject ftpCredentials = new JsonObject();
        JsonObject responseObject = new JsonObject();
        try {
            FtpCredentialReader ftpCredentialReader = new FtpCredentialReader();
            ftpCredentials.addProperty("address", ftpCredentialReader.getFtpServerAddress());
            ftpCredentials.addProperty("username", ftpCredentialReader.getFtpServerUsername());
            ftpCredentials.addProperty("password", ftpCredentialReader.getFtpServerPassword());
            ftpCredentials.addProperty("port", ftpCredentialReader.getFtpServerPort());
            responseObject.addProperty("responseType", "done");
            responseObject.add("responseData", ftpCredentials);

            PrintWriter out = response.getWriter();
            out.print(responseObject);
            if (log.isDebugEnabled()) {
                log.debug("Obtained the FTP server credentials successfully from the configuration file.");
            }
        } catch (LicenseManagerException e) {
            ftpCredentials.addProperty("responseType", "error");
            ftpCredentials.addProperty("responseMessage", e.getMessage());
            PrintWriter out = response.getWriter();
            out.print(ftpCredentials);
            log.error(e.getMessage(), e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse response) {
        // Do nothing.
    }

}
