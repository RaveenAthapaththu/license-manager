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

public class License {
    private int LICENSE_ID;
    private String LICENSE_KEY;
    private String NAME;

    public License() {
    }

    public int getLICENSE_ID() {

        return LICENSE_ID;
    }

    public void setLICENSE_ID(int LICENSE_ID) {

        this.LICENSE_ID = LICENSE_ID;
    }

    public String getLICENSE_KEY() {

        return LICENSE_KEY;
    }

    public void setLICENSE_KEY(String LICENSE_KEY) {

        this.LICENSE_KEY = LICENSE_KEY;
    }

    public String getNAME() {

        return NAME;
    }

    public void setNAME(String NAME) {

        this.NAME = NAME;
    }
}

