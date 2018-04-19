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
 * Java object to contain the details of new entry of licenses for components/libraries
 */
public class NewLicenseEntry {
    private String fileName;
    private String name;
    private String version;
    private String licenseKey;

    public NewLicenseEntry(String fileName, String name, String version, String licenseKey) {

        this.fileName = fileName;
        this.name = name;
        this.version = version;
        this.licenseKey = licenseKey;
    }

    public String getFileName() {

        return fileName;
    }

    public void setFileName(String fileName) {

        this.fileName = fileName;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getVersion() {

        return version;
    }

    public void setVersion(String version) {

        this.version = version;
    }

    public String getLicenseKey() {

        return licenseKey;
    }

    public void setLicenseKey(String licenseKey) {

        this.licenseKey = licenseKey;
    }
}
