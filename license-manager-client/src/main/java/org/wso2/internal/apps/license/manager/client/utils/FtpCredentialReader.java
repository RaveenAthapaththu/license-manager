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

package org.wso2.internal.apps.license.manager.client.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.internal.apps.license.manager.client.exception.LicenseManagerException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Read the ftp credentials from the configuration properties file.
 */
public class FtpCredentialReader {

    private static final Logger log = LoggerFactory.getLogger(PropertyReader.class);
    private final static String configFileName = Constants.CONFIG_FILE_NAME;
    private String ftpServerAddress;
    private String ftpServerUsername;
    private String ftpServerPassword;
    private String ftpServerPort;

    public FtpCredentialReader() throws LicenseManagerException {

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(configFileName);
        loadConfigs(inputStream);
    }

    /**
     * Load configs from the file
     *
     * @param input - input stream of the file
     */
    private void loadConfigs(InputStream input) throws LicenseManagerException {

        Properties prop = new Properties();
        try {
            prop.load(input);
            this.ftpServerAddress = prop.getProperty(Constants.FTP_SERVER_ADDRESS);
            this.ftpServerUsername = prop.getProperty(Constants.FTP_SERVER_USERNAME);

            this.ftpServerPassword = prop.getProperty(Constants.FTP_SERVER_PASSWORD);
            this.ftpServerPort = prop.getProperty(Constants.FTP_SERVER_PORT);

        } catch (IOException e) {
            throw new LicenseManagerException("Can not read ftp server configurations.", e);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    log.error("The File InputStream is not closed");
                }
            }
        }

    }

    public String getFtpServerAddress() {

        return ftpServerAddress;
    }

    public String getFtpServerUsername() {

        return ftpServerUsername;
    }

    public String getFtpServerPassword() {

        return ftpServerPassword;
    }

    public String getFtpServerPort() {

        return ftpServerPort;
    }
}
