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

import org.wso2.internal.apps.license.manager.impl.main.Jar;
import org.wso2.internal.apps.license.manager.impl.main.JarHolder;

import java.util.List;

/**
 * Java object to contain the objects required for the license generation per a session
 */
public class SessionObjectHolder {

    private JarHolder jarHolder;
    private int productId;
    private List<Jar> licenseMissingLibraries;
    private List<Jar> licenseMissingComponents;

    public JarHolder getJarHolder() {

        return jarHolder;
    }

    public void setJarHolder(JarHolder jarHolder) {

        this.jarHolder = jarHolder;
    }

    public List<Jar> getLicenseMissingLibraries() {

        return licenseMissingLibraries;
    }

    public void setLicenseMissingLibraries(List<Jar> licenseMissingLibraries) {

        this.licenseMissingLibraries = licenseMissingLibraries;
    }

    public List<Jar> getLicenseMissingComponents() {

        return licenseMissingComponents;
    }

    public void setLicenseMissingComponents(List<Jar> licenseMissingComponents) {

        this.licenseMissingComponents = licenseMissingComponents;
    }

    public int getProductId() {

        return productId;
    }

    public void setProductId(int productId) {

        this.productId = productId;
    }
}
