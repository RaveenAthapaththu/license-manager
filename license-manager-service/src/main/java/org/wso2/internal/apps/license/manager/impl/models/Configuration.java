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
package org.wso2.internal.apps.license.manager.impl.models;

/**
 * Java object contains the configurations details
 */
public class Configuration {

    private String databaseDriver;
    private String databaseUrl;
    private String databaseUsername;
    private String databasePassword;
    private String bpmnUrl;
    private String bpmnToken;
    private String emailAddress;
    private String emailPassword;
    private String smtpPort;
    private String smtpHost;
    private String bpmnOrigin;
    private String publicKey;
    private String pathToFileStorage;
    private String clientUrl;
    private String licenseId;

    public Configuration(String databaseDriver, String databaseUrl, String databaseUsername, String databasePassword,
                         String bpmnUrl, String bpmnToken, String emailAddress, String emailPassword,
                         String smtpPort, String smtpHost, String bpmnOrigin, String publicKey,
                         String pathToFileStorage, String clientUrl, String licenseId) {
        this.databaseDriver = databaseDriver;
        this.databaseUrl = databaseUrl;
        this.databaseUsername = databaseUsername;
        this.databasePassword = databasePassword;
        this.bpmnUrl = bpmnUrl;
        this.bpmnToken = bpmnToken;
        this.emailAddress = emailAddress;
        this.emailPassword = emailPassword;
        this.smtpPort = smtpPort;
        this.smtpHost = smtpHost;
        this.bpmnOrigin = bpmnOrigin;
        this.publicKey = publicKey;
        this.pathToFileStorage = pathToFileStorage;
        this.clientUrl = clientUrl;
        this.licenseId = licenseId;
    }

    public String getDatabaseDriver() {
        return databaseDriver;
    }

    public void setDatabaseDriver(String databaseDriver) {
        this.databaseDriver = databaseDriver;
    }

    public String getDatabaseUrl() {
        return databaseUrl;
    }

    public void setDatabaseUrl(String databaseUrl) {
        this.databaseUrl = databaseUrl;
    }

    public String getDatabaseUsername() {
        return databaseUsername;
    }

    public void setDatabaseUsername(String databaseUsername) {
        this.databaseUsername = databaseUsername;
    }

    public String getDatabasePassword() {
        return databasePassword;
    }

    public void setDatabasePassword(String databasePassword) {
        this.databasePassword = databasePassword;
    }

    public String getBpmnUrl() {
        return bpmnUrl;
    }

    public void setBpmnUrl(String bpmnUrl) {
        this.bpmnUrl = bpmnUrl;
    }

    public String getBpmnToken() {
        return bpmnToken;
    }

    public void setBpmnToken(String bpmnToken) {
        this.bpmnToken = bpmnToken;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getEmailPassword() {
        return emailPassword;
    }

    public void setEmailPassword(String emailPassword) {
        this.emailPassword = emailPassword;
    }

    public String getSmtpPort() {
        return smtpPort;
    }

    public void setSmtpPort(String smtpPort) {
        this.smtpPort = smtpPort;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    public String getBpmnOrigin() {
        return bpmnOrigin;
    }

    public void setBpmnOrigin(String bpmnOrigin) {
        this.bpmnOrigin = bpmnOrigin;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPathToFileStorage() {
        return pathToFileStorage;
    }

    public void setPathToFileStorage(String pathToFileStorage) {
        this.pathToFileStorage = pathToFileStorage;
    }

    public String getClientUrl() {
        return clientUrl;
    }

    public void setClientUrl(String clientUrl) {
        this.clientUrl = clientUrl;
    }

    public String getLicenseId() {
        return licenseId;
    }

    public void setLicenseId(String licenseId) {
        this.licenseId = licenseId;
    }
}

