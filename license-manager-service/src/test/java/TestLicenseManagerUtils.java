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

import org.testng.annotations.Test;
import org.wso2.internal.apps.license.manager.exception.LicenseManagerRuntimeException;
import org.wso2.internal.apps.license.manager.impl.JarHolder;
import org.wso2.internal.apps.license.manager.util.LicenseManagerUtils;

import java.io.IOException;

/**
 * TODO: Class level comments
 */
public class TestLicenseManagerUtils {

    @Test
    public void testCheckInnerJars() {

        try {
            boolean jarsExists = LicenseManagerUtils.checkInnerJars("/home/pamoda/programming/backup/wso2is-analytics-5.4.0/repository" +
                    "/components/plugins/org.wso2.carbon.tomcat.ext_4.4.20.jar");
            System.out.println(jarsExists);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testExtractPack() {

        try {
            boolean jarsExists = LicenseManagerUtils.checkInnerJars("/home/pamoda/programming/backup/wso2is-analytics-5.4.0/repository" +
                    "/components/plugins/org.wso2.carbon.tomcat.ext_4.4.20.jar");
            System.out.println(jarsExists);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Test
    public void testExtractJars() {

        try {
            JarHolder jarHolder = LicenseManagerUtils.checkJars("/home/pamoda/programming/backup/wso2test-1.2.1");
            System.out.println(jarHolder);
        } catch (LicenseManagerRuntimeException e) {
            e.printStackTrace();
        }
    }
}
