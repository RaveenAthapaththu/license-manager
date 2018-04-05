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

import org.w3c.dom.Document;
import org.wso2.internal.apps.license.manager.impl.exception.LicenseManagerConfigurationException;
import org.wso2.internal.apps.license.manager.impl.models.Configuration;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Contains the functions required for the License Generation process.
 */
public class LicenseManagerUtil {

    /**
     * Load the configuration information.
     */
    public static Configuration loadConfigurations() throws LicenseManagerConfigurationException {

        Configuration configuration = null;
//        String filePath = LicenseManagerConstants.RESOURCE_PATH +File.separator+LicenseManagerConstants
// .CONFIG_FILE_NAME;
//        OMElement conf = loadConfigXML(filePath);

        File configFile = new File(Constants.RESOURCE_PATH + File.separator +
                Constants.CONFIG_FILE_NAME);
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document configDoucment = documentBuilder.parse(configFile);
            configDoucment.getDocumentElement().normalize();

            String databaseDriver = configDoucment.getElementsByTagName(Constants.DATABASE_DRIVER)
                    .item(0).getTextContent();
            String databaseUrl = configDoucment.getElementsByTagName(Constants.DATABASE_URL)
                    .item(0).getTextContent();
            String databaseUsername = configDoucment.getElementsByTagName(Constants.DATABASE_USERNAME)
                    .item(0).getTextContent();
            String databasePassword = configDoucment.getElementsByTagName(Constants.DATABASE_PASSWORD)
                    .item(0).getTextContent();
            String bpmnUrl = configDoucment.getElementsByTagName(Constants.BPMN_URL)
                    .item(0).getTextContent();
            String bpmnToken = configDoucment.getElementsByTagName(Constants.BPMN_TOKEN)
                    .item(0).getTextContent();
            String emailAddress = configDoucment.getElementsByTagName(Constants.BPMN_EMAIL_ADDRESS)
                    .item(0).getTextContent();
            String emailPassword = configDoucment.getElementsByTagName(Constants.BPMN_EMAIL_PASSWORD)
                    .item(0).getTextContent();
            String smtpPort = configDoucment.getElementsByTagName(Constants.BPMN_SMTP_PORT)
                    .item(0).getTextContent();
            String smtpHost = configDoucment.getElementsByTagName(Constants.BPMN_SMTP_HOST)
                    .item(0).getTextContent();
            String publicKey = configDoucment.getElementsByTagName(Constants.BPMN_PUBLIC_KEY)
                    .item(0).getTextContent();
            String bpmnOrigin = configDoucment.getElementsByTagName(Constants.BPMN_ORIGIN)
                    .item(0).getTextContent();
            String pathToFileStorage = configDoucment.getElementsByTagName(Constants
                    .PATH_TO_FILE_STORAGE)
                    .item(0).getTextContent();
            String clientUrl = configDoucment.getElementsByTagName(Constants.CLIENT_URL)
                    .item(0).getTextContent();
            String licenseId = configDoucment.getElementsByTagName(Constants.LICENSE_ID)
                    .item(0).getTextContent();
            configuration = new Configuration(databaseDriver, databaseUrl, databaseUsername, databasePassword, bpmnUrl,
                    bpmnToken, emailAddress, emailPassword, smtpPort, smtpHost, bpmnOrigin, publicKey,
                    pathToFileStorage, clientUrl, licenseId);

        } catch (ParserConfigurationException e) {
            throw new LicenseManagerConfigurationException("Error occurred in parsing Configurations", e);
        } catch (IOException e) {
            throw new LicenseManagerConfigurationException("Configuration file Not Found", e);
        } catch (NullPointerException e) {
            throw new LicenseManagerConfigurationException("One or more required tags not found in " +
                    "the configurations file", e);
        } catch (SAXException e) {
            e.printStackTrace();
        }
        return configuration;
    }

}
