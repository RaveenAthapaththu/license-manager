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

import org.wso2.msf4j.util.SystemVariableUtil;

/**
 * Contains constant values needed for the License Generation.
 */
public class Constants {
    public Constants() {
        // Do nothing.
    }

    // String constants.
    public static final String CONFIG_FILE_NAME = "license-manager-config.xml";
    public static final String RESOURCE_PATH = "resources";

    //configuration tags
    public static final String DATABASE_DRIVER = "DATABASE_DRIVER";
    public static final String DATABASE_URL ="DATABASE_URL";
    public static final String DATABASE_USERNAME = "DATABASE_USERNAME";
    public static final String DATABASE_PASSWORD = "DATABASE_PASSWORD";
    public static final String BPMN_URL = "bpmnUrl";
    public static final String BPMN_TOKEN = "bpmnToken";
    public static final String BPMN_EMAIL_ADDRESS = "emailAddress";
    public static final String BPMN_EMAIL_PASSWORD = "emailPassword";
    public static final String BPMN_SMTP_PORT = "smtpPort";
    public static final String BPMN_SMTP_HOST = "smtpHost";
    public static final String BPMN_PUBLIC_KEY = "publicKey";
    public static final String BPMN_ORIGIN = "bpmnOrigin";
    public static final String FILE_UPLOAD_PATH= "LICENSE_MANAGER_FILE_UPLOAD_PATH\"";

    public static final String CLIENT_URL = "clientUrl";
    public static final String LICENSE_ID = "licenseId";

    // Integer constants.
    public static final int y =0;

    // Constants related to response
    public static final String SUCCESS = "Done";
    public static final String ERROR = "Error";

    //Database
    public static final String WAITING = "WAITING";
}
