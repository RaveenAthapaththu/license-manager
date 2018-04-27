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

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.internal.apps.license.manager.service.MainService;

import javax.ws.rs.core.Response;

/**
 * TODO: Class level comments
 */
public class APIMethodTest {

    private MainService mainService = new MainService();

    @Test
    public void testSftpConnection() {

        Response response = mainService.listUploadedPacks();
        Assert.assertEquals(response.getStatus(), 200);
    }

    @Test
    public void testExtractPack() {

//        mainService.downloadPackFromFTP();
    }
}