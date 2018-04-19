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

/**
 * Contains constant values needed for the License Generation.
 */
public class Constants {

    // Environment variables.
    public static final String DATABASE_DRIVER = "DATABASE_DRIVER";
    public static final String DATABASE_URL = "DATABASE_URL";
    public static final String DATABASE_USERNAME = "DATABASE_USERNAME";
    public static final String DATABASE_PASSWORD = "DATABASE_PASSWORD";
    public static final String FILE_UPLOAD_PATH = "LICENSE_MANAGER_FILE_UPLOAD_PATH";
    // Environment variables for sending emails.
    public static final String EMAIL_USERNAME = "LICENSE_MANAGER_EMAIL_USERNAME";
    public static final String EMAIL_PASSWORD = "LICENSE_MANAGER_EMAIL_PASSWORD";
    public static final String LICENSE_MANAGER_ADMINS = "LICENSE_MANAGER_ADMINS";
    // Environment variables for authentication.
    public static final String LICENSE_MANAGER_APP_USERNAME = "LICENSE_MANAGER_APP_USERNAME";
    public static final String LICENSE_MANAGER_APP_PASSWORD = "LICENSE_MANAGER_APP_PASSWORD";
    // Constants related to response
    public static final String SUCCESS = "Done";
    public static final String ERROR = "Error";
    // Constants in extracting licenses
    public static final String JAR_TYPE_WSO2 = "wso2";
    public static final String JAR_TYPE_BUNDLE = "bundle";
    public static final String JAR_TYPE_JAR_IN_BUNDLE = "jarinbundle";
    public static final String JAR_TYPE_JAR = "jar";
    public Constants() {
        // Do nothing.
    }

}
