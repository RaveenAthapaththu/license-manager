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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.internal.apps.license.manager.models.TaskProgress;
import org.wso2.internal.apps.license.manager.service.MainService;

import javax.ws.rs.core.Response;

/**
 * Test the API method implementations
 */
public class ApiMethodTest {

    private static final Logger log = LoggerFactory.getLogger(ApiMethodTest.class);

    private MainService mainService = new MainService();

    @Test
    public void testSftpConnection() {

        Response response = mainService.listUploadedPacks();
        Assert.assertEquals(response.getStatus(), 200);
    }

    @Test
    public void testExtractPack() {

//        LicenseManagerUtils.startPackExtractionProcess("pamoda@wso2.com",
//                "wso2ei-6.1.1.zip");
//        boolean running = true;
//        while (running) {
//            TaskProgress taskProgress = ProgressTracker.getTaskProgress("pamoda@wso2.com");
//            log.info(taskProgress.getStatus());
//            System.out.println(taskProgress.getMessage());
//            running = taskProgress.getStatus().equals(Constants.RUNNING);
//            try {
//                Thread.sleep(5000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
    }
}
