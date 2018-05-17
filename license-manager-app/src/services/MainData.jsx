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

import { Component } from 'react';

/**
 * @class MainData
 * @extends {Component}
 * @description Main config data exchanging component
 */
class MainData extends Component {
    /**
    * @class MainData
    * @extends {Component}
    * @description constructor
    */
    constructor() {
        super();
        const Config = require('../configuration.json');
        // fetchData(Config.serverUrl + '/Enterprises/...')
        this.microServiceURL = Config.URL_FETCH_SERVICES;
        this.appServiceURL = Config.URL_VALIDATE_USER;
    }
}

export default (new MainData());
