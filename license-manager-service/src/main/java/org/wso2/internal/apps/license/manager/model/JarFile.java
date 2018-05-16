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

package org.wso2.internal.apps.license.manager.model;

import java.io.File;

/**
 * Java object containing jar file details.
 */
public class JarFile {

    private String projectName;
    private String type;
    private String version;
    private String product;
    private String vendor;
    private String description;
    private String url;
    private File jarFile, extractedFolder;
    private JarFile parent;
    private boolean isBundle = false;
    private boolean isValidName = false;

    public void setIsBundle(boolean isBundle) {

        this.isBundle = isBundle;
    }

    public boolean isBundle() {

        return isBundle;
    }

    public String getUrl() {

        return url;
    }

    public void setUrl(String url) {

        this.url = url;
    }

    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    public String getVendor() {

        return vendor;
    }

    public void setVendor(String vendor) {

        this.vendor = vendor;
    }

    public JarFile getParent() {

        return parent;
    }

    public void setParent(JarFile parent) {

        this.parent = parent;

    }

    public File getExtractedFolder() {

        return extractedFolder;
    }

    public void setExtractedFolder(File extractedFolder) {

        this.extractedFolder = extractedFolder;
    }

    public String getProjectName() {

        return projectName;
    }

    public void setProjectName(String projectName) {

        this.projectName = projectName;

    }

    public File getJarFile() {

        return jarFile;

    }

    public void setJarFile(File jarFile) {

        this.jarFile = jarFile;

    }

    public String getProduct() {

        return product;
    }

    public void setProduct(String product) {

        this.product = product;

    }

    public String getType() {

        return type;
    }

    public void setType(String type) {

        this.type = type;

    }

    public String getVersion() {

        return version;
    }

    public void setVersion(String version) {

        this.version = version;

    }

    public boolean isValidName() {

        return this.isValidName;
    }

    public void setValidName(boolean validName) {

        this.isValidName = validName;
    }
}
