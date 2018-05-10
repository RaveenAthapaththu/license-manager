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

import org.json.JSONException;
import org.testng.annotations.Test;
import org.wso2.internal.apps.license.manager.client.services.ServiceExecuter;

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

/**
 * Test the email sending
 */
public class MicroserviceTest {

    @Test
    public void testService() throws IOException, JSONException {

        System.out.println(ServiceExecuter.executePostService("//pack/jars", "wso2is-5.4.0.zip","pamodaaw@wso2.com"));
//        System.out.println(ServiceExecuter.executePostService("/pack/nameDefinedJars", "{\"jars\":[\n" +
//                "        {\"name\":\"patch.jar\", \"index\":0, \"version\":\"1.0.0\"},{\"name\":\"patch.jar\", " +
//                "\"index\":1, \"version\":\"1.0.0\\\"\"}]}", "pamodaaw@wso2.com"));
    }

    @Test
    public void testService2() throws JSONException {
//
//        System.out.println(ServiceExecuter.executePostService("/enterJars",
//                "{asdf}"));
            ServiceExecuter.executeGetService("/pack/list", "pamodaaw@wso2.com");
    }
}
