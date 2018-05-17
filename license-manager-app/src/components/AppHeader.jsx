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

import React, {Component} from 'react';
import ValidateUser from '../services/authentication/ValidateUser';
import logo from '../assets/images/logo-inverse.svg';

/**
 * @class AppHeader
 * @extends {Component}
 * @description Header of the application for any user.
 */
class AppHeader extends Component {
    /**
     * constructor
     */
    constructor() {
        super();
        this.state = {
            username: null,
        };
    }

    componentWillMount() {

        ValidateUser.getUserDetails().then((response) => {
            this.setState(() => {
                return {
                    username: response.username,
                };
            });
        })

    }

    render() {
        return (
            <nav className="navbar navbar-inverse container-fluid" style={{backgroundColor: "#000000", height: '100%'}}>
                    <div className="navbar-header">
                        <a href='/'><img id="logo" style={{height: '50px'}} src={logo} alt="wso2"/></a>
                        <a className="navbar-brand" href='/'
                           style={{color: "#FBFCFC", fontSize: '40px', paddingLeft: '20px'}}>License Manager</a>
                    </div>
                    <ul className="nav navbar-nav navbar-right">
                        <li><a style={{color: "#FBFCFC"}}>{this.state.username}</a></li>
                    </ul>
            </nav>
        );
    }
}

export default AppHeader;
