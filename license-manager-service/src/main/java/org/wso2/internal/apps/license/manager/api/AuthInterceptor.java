
/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.internal.apps.license.manager.api;

import org.wso2.internal.apps.license.manager.util.Constants;
import org.wso2.msf4j.security.basic.AbstractBasicAuthSecurityInterceptor;
import org.wso2.msf4j.util.SystemVariableUtil;

import java.util.Objects;

/**
 * Authenticating the micro service with Basic Auth via username and password.
 */
public class AuthInterceptor extends AbstractBasicAuthSecurityInterceptor {

    @Override
    protected boolean authenticate(String username, String password) {
        String app_username = SystemVariableUtil.getValue(Constants.LICENSE_MANAGER_APP_USERNAME, null);
        String app_password = SystemVariableUtil.getValue(Constants.LICENSE_MANAGER_APP_PASSWORD, null);
        return Objects.equals(username, app_username) && Objects.equals(password, app_password);
    }
}