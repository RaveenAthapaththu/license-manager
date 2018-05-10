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

package org.wso2.internal.apps.license.manager.client.msf4jhttp;

import org.apache.log4j.Logger;
import org.wso2.internal.apps.license.manager.client.utils.Constants;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/*
 * Read the properties of the application
 */
public class PropertyReader {

    private final static Logger logger = Logger.getLogger(PropertyReader.class);
    private final static String configFileName = Constants.CONFIG_FILE_NAME;
    private String backendUrl;
    private String backendPassword;
    private String backendUsername;
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
            this.backendUrl = prop.getProperty(Constants.BACKEND_URL);
            this.backendUsername = prop.getProperty(Constants.BACKEND_USERNAME);
            this.backendPassword = prop.getProperty(Constants.BACKEND_PASSWORD);
            this.ssoKeyStoreName = prop.getProperty(Constants.KEYSTORE_FILE_NAME);
            this.ssoKeyStorePassword = prop.getProperty(Constants.KEYSTORE_PASSWORD);
            this.ssoCertAlias = prop.getProperty(Constants.CERTIFICATE_ALIAS);
            this.ssoRedirectUrl = prop.getProperty(Constants.SSO_REDIRECT_URL);
            this.trustStoreServiceName = prop.getProperty(Constants.TRUST_STORE_SERVICE_NAME);
            this.trustStoreServicePassword = prop.getProperty(Constants.TRUST_STORE_SERVICE_PASSWORD);

        } catch (FileNotFoundException e) {
            logger.error("The configuration file is not found");
        } catch (IOException e) {
            logger.error("The File cannot be read");
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    logger.error("The File InputStream is not closed");
                }
            }
        }

    }

    public String getBackendUrl() {

        return this.backendUrl;
    }

    public String getBackendUsername() {

        return this.backendUsername;
    }

    public String getBackendPassword() {

        return this.backendPassword;
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

    public String getTrustStoreServiceName() {

        return trustStoreServiceName;
    }

    public void setTrustStoreServiceName(String trustStoreServiceName) {

        this.trustStoreServiceName = trustStoreServiceName;
    }

    public String getTrustStoreServicePassword() {

        return trustStoreServicePassword;
    }

    public void setTrustStoreServicePassword(String trustStoreServicePassword) {

        this.trustStoreServicePassword = trustStoreServicePassword;
    }
}
