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

import {Component} from "react";
import MainData from "../MainData";
import Config from "../../configuration"
import axios from "axios/index";

/**
 * @class FtpCredentials
 * @extends {Component}
 * @description validate user details
 */
class FtpCredentials extends Component {
    /**
     * Get FTP credentials
     * @returns {Promise} promise
     */
    getFtpCredentials() {
        const url = Config.URL_VALIDATE_USER + "ftpCredentials";
        const requestHeaders = { withCredentials: true };
        return axios.get(url, requestHeaders).then((response) => {
            return response;
        }).catch((error) => {
            throw error;
        });
    }
}

export default (new FtpCredentials());