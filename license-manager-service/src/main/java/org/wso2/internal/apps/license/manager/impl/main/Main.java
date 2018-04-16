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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wso2.internal.apps.license.manager.impl.main;

import com.workingdogs.village.DataSetException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;
import org.wso2.internal.apps.license.manager.impl.enterData.EnterData;
import org.wso2.internal.apps.license.manager.impl.exception.LicenseManagerConfigurationException;
import org.wso2.internal.apps.license.manager.impl.exception.LicenseManagerRuntimeException;
import org.wso2.internal.apps.license.manager.util.Constants;
import org.wso2.msf4j.MicroservicesRunner;
import org.wso2.msf4j.util.SystemVariableUtil;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

/**
 * @author pubudu + buddhi
 */
public class Main {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(MicroservicesRunner.class);

    public EnterData enterData(JarHolder jh) throws ClassNotFoundException, SQLException, DataSetException {
        try {
            EnterData enterData = new EnterData(jh);
            enterData.enter();
            return enterData;
        } catch (ClassNotFoundException | SQLException | DataSetException ex) {
            throw ex;
        }

    }

    public JarHolder checkJars(String file) throws LicenseManagerRuntimeException {

        if (StringUtils.isEmpty(file) || !new File(file).exists() || !new File(file).isDirectory()) {
            throw new LicenseManagerRuntimeException("Folder is not found in the location");
        }
        String inputFile = file;
        JarHolder jh = new JarHolder();
        try {
            jh.generateMap(inputFile);
        } catch (IOException e) {
            throw new LicenseManagerRuntimeException("Folder is not found in the location", e);
        }
        return jh;
    }
}
