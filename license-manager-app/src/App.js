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
import {BrowserRouter, Route, Switch} from 'react-router-dom';
import ManagePacks from './scenes/ManagePacks';
import ManageJars from './scenes/ManageJars';
import AddLicense from './scenes/AddLicense';
import GenerateLicense from './scenes/GenerateLicense';
import frontPage from './scenes/InitialPage'
import lightBaseTheme from "material-ui/styles/baseThemes/lightBaseTheme";
import getMuiTheme from "material-ui/styles/getMuiTheme";
import {MuiThemeProvider} from "material-ui";
import AppHeader from "./components/AppHeader";

// import './App.css';

class App extends Component {
    render() {
        return (

            <MuiThemeProvider muiTheme={getMuiTheme(lightBaseTheme)}>
                <div className="container-fluid" style={{paddingLeft: '0px', paddingRight: '0px'}}>

                    <div className="nav container-fluid app-header">
                        <AppHeader/>
                    </div>

                    <div className="container">
                        <div className="col-sm-12 app-body">
                            <BrowserRouter basename='/t/wso2internalstg/licenseManager'>
                                <Switch>
                                    <Route path={'/packManager'} component={ManagePacks}/>
                                    <Route path={'/jarManager'} component={ManageJars}/>
                                    <Route path={'/licenseGenerator'} component={GenerateLicense}/>
                                    <Route path={'/licenseAdder'} component={AddLicense}/>
                                    <Route path={'/'} component={frontPage}/>
                                </Switch>
                            </BrowserRouter>
                        </div>
                    </div>
                </div>
            </MuiThemeProvider>
        );
    }
}

export default App;
