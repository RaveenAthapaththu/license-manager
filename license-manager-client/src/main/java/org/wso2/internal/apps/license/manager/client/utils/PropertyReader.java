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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Read the properties file and obtain the the information.
 */
public class PropertyReader {

    private static final Logger log = LoggerFactory.getLogger(PropertyReader.class);
    private final static String configFileName = Constants.CONFIG_FILE_NAME;
    private String microServiceUrl;
    private String microServicePassword;
    private String microServiceUsername;
    private String ssoKeyStoreName;
    private String ssoKeyStorePassword;
    private String ssoCertAlias;
    private String ssoRedirectUrl;
    private String trustStoreServiceName;
    private String trustStoreServicePassword;

    public PropertyReader() {

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(configFileName);
        loadConfigs(inputStream);
    }

    /**
     * Load configs from the file
     *
     * @param input - input stream of the file
     */
    private void loadConfigs(InputStream input) {

        Properties prop = new Properties();
        try {
            prop.load(input);
            this.microServiceUrl = prop.getProperty(Constants.MICRO_SERVICE_URL);
            this.microServiceUsername = prop.getProperty(Constants.MICRO_SERVICE_USERNAME);
            this.microServicePassword = prop.getProperty(Constants.MICRO_SERVICE_PASSWORD);
            this.ssoKeyStoreName = prop.getProperty(Constants.KEYSTORE_FILE_NAME);
            this.ssoKeyStorePassword = prop.getProperty(Constants.KEYSTORE_PASSWORD);
            this.ssoCertAlias = prop.getProperty(Constants.CERTIFICATE_ALIAS);
            this.ssoRedirectUrl = prop.getProperty(Constants.SSO_REDIRECT_URL);
            this.trustStoreServiceName = prop.getProperty(Constants.TRUST_STORE_SERVICE_NAME);
            this.trustStoreServicePassword = prop.getProperty(Constants.TRUST_STORE_SERVICE_PASSWORD);

        } catch (FileNotFoundException e) {
            log.error("The configuration file is not found");
        } catch (IOException e) {
            log.error("The File cannot be read");
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

    public String getMicroServiceUrl() {

        return this.microServiceUrl;
    }

    String getMicroServiceUsername() {

        return this.microServiceUsername;
    }

    String getMicroServicePassword() {

        return this.microServicePassword;
    }

    public String getSsoKeyStoreName() {

        return this.ssoKeyStoreName;
    }

    public String getSsoKeyStorePassword() {

        return this.ssoKeyStorePassword;
    }

    public String getSsoCertAlias() {

        return this.ssoCertAlias;
    }

    public String getSsoRedirectUrl() {

        return this.ssoRedirectUrl;
    }

    String getTrustStoreServiceName() {

        return trustStoreServiceName;
    }

    public void setTrustStoreServiceName(String trustStoreServiceName) {

        this.trustStoreServiceName = trustStoreServiceName;
    }

    String getTrustStoreServicePassword() {

        return trustStoreServicePassword;
    }

    public void setTrustStoreServicePassword(String trustStoreServicePassword) {

        this.trustStoreServicePassword = trustStoreServicePassword;
    }
}
