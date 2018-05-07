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
package org.wso2.internal.apps.license.manager.client.utils;

/**
 * Contains the constants for License Manager application
 */
public class Constants {

    // Constants for configuration details.
    public static final String CONFIG_FILE_NAME = "config.properties";

    public static final String BACKEND_URL = "backend_url";
    public static final String BACKEND_USERNAME = "backend_username";
    public static final String BACKEND_PASSWORD = "backend_password";
    public static final String KEYSTORE_FILE_NAME= "sso_keystore_file_name";
    public static final String KEYSTORE_PASSWORD = "sso_keystore_password";
    public static final String CERTIFICATE_ALIAS = "sso_certificate_alias";
    public static final String SSO_REDIRECT_URL = "sso_redirect_url";

    public static final String DOWNLOAD_ENDPOINT = "/license/textToDownload";

}
