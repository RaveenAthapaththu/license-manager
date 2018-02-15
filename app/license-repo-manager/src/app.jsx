/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import React, { Component } from 'react';
import { Router, Route, IndexRoute, hashHistory } from 'react-router';
import Root from './scenes/Root';
import Main from './scenes/Main';
import RequestRepository from './scenes/repository/RequestRepository';
import AcceptRepository from './scenes/repository/AcceptRepository';
import RejectRepository from './scenes/repository/RejectRepository';
import RequestLibrary from './scenes/library/RequestLibrary';
import AcceptLibrary from './scenes/library/AcceptLibrary';
import RejectLibrary from './scenes/library/RejectLibrary';
import WaitingLibrary from './scenes/library/WaitingLibrary';
import PendingLibrary from './scenes/library/PendingLibrary';
import RequestLicense from './scenes/license/RequestLicense';
import GenerateLicense from './scenes/license/GenerateLicense';
import WaitingLicense from './scenes/license/WaitingLicense';
import AcceptLicense from './scenes/license/AcceptLicense';
import RejectLicense from './scenes/license/RejectLicense';
import PendingRepository from './scenes/repository/PendingRepository';
import WaitingRepository from './scenes/repository/WaitingRepository';
import ErrorPage from './scenes/common/ErrorPage';
import ViewByLibrary from './scenes/dependencymanager/Views/ViewByLibrary';
import ViewByProductComponent from './scenes/dependencymanager/Views/ViewByProductOrComponent';
/**
 * @class App
 * @extends {Component}
 * @description Main App component
 */
class App extends Component {
    /**
     *
     * @returns {object} App view
     * @memberof App
     */
    render() {
        return (
            <Router history={hashHistory}>
                <Route path={'/app'} component={Root} >
                    <IndexRoute component={RequestRepository} />
                    <Route path={'pendingRepository'} component={PendingRepository} />
                    <Route path={'waitingRepository'} component={WaitingRepository} />
                    <Route path={'waitingLibrary'} component={WaitingLibrary} />
                    <Route path={'pendingLibrary'} component={PendingLibrary} />
                    <Route path={'acceptRepository'} component={AcceptRepository} />
                    <Route path={'rejectRepository'} component={RejectRepository} />
                    <Route path={'requestRepository'} component={RequestRepository} />
                    <Route path={'requestLibrary'} component={RequestLibrary} />
                    <Route path={'acceptLibrary'} component={AcceptLibrary} />
                    <Route path={'rejectLibrary'} component={RejectLibrary} />
                    <Route path={'requestLicense'} component={RequestLicense} />
                    <Route path={'generateLicense'} component={GenerateLicense} />
                    <Route path={'waitingLicense'} component={WaitingLicense} />
                    <Route path={'acceptLicense'} component={AcceptLicense} />
                    <Route path={'rejectLicense'} component={RejectLicense} />
                    <Route path={'ViewbyLibrary'} component={ViewByLibrary} />
                    <Route path={'ViewbyProductComponent'} component={ViewByProductComponent} />
                </Route>
                <Route path={'/'} component={Main} />
                <Route path={'/errorPage'} component={ErrorPage} />
            </Router>
        );
    }
}

export default App;
