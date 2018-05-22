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

import java.util.List;

/**
 * Holds the details of the all the jars in a given pack.
 */
public class JarFilesHolder {

    private List<JarFile> jarFilesInPack;
    private List<JarFile> faultyNamedJars;
    private List<LicenseMissingJar> licenseMissingLibraries;
    private List<LicenseMissingJar> licenseMissingComponents;
    private int productId;
    private String productName;
    private String productVersion;

    public List<JarFile> getJarFilesInPack() {

        return jarFilesInPack;
    }

    public void setJarFilesInPack(List<JarFile> jarFilesInPack) {

        this.jarFilesInPack = jarFilesInPack;
    }

    public List<JarFile> getFaultyNamedJars() {

        return faultyNamedJars;
    }

    public void setFaultyNamedJars(List<JarFile> faultyNamedJars) {

        this.faultyNamedJars = faultyNamedJars;
    }

    public String getProductName() {

        return productName;
    }

    public void setProductName(String productName) {

        this.productName = productName;
    }

    public String getProductVersion() {

        return productVersion;
    }

    public void setProductVersion(String productVersion) {

        this.productVersion = productVersion;
    }

    public List<LicenseMissingJar> getLicenseMissingLibraries() {

        return licenseMissingLibraries;
    }

    public void setLicenseMissingLibraries(List<LicenseMissingJar> licenseMissingLibraries) {

        this.licenseMissingLibraries = licenseMissingLibraries;
    }

    public List<LicenseMissingJar> getLicenseMissingComponents() {

        return licenseMissingComponents;
    }

    public void setLicenseMissingComponents(List<LicenseMissingJar> licenseMissingComponents) {

        this.licenseMissingComponents = licenseMissingComponents;
    }

    public int getProductId() {

        return productId;
    }

    public void setProductId(int productId) {

        this.productId = productId;
    }
}
