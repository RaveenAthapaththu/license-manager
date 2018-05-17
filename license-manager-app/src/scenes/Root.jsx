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
import MuiThemeProvider from 'material-ui/styles/MuiThemeProvider';
import getMuiTheme from 'material-ui/styles/getMuiTheme';
import lightBaseTheme from 'material-ui/styles/baseThemes/lightBaseTheme';
import AppHeader from '../components/AppHeader';

/**
 * @class Root
 * @extends {Component}
 * @description Root component where the relevant components will mount.
 */
class Root extends Component {

    constructor(props) {
        super(props);
        this.state = {
            isValid: null,
            displayChildren: 'block',
            displayNav: 'block',
            displayHeader: 'block',
            userDetail: null,
        };
    }

    componentWillMount() {
        // ValidateUser.getUserDetails().then((response) => {
        //     if (response.isValid) {
        //         this.setState(() => {
        //             return {
        //                 userDetail: response,
        //             };
        //         });
        //     } else {
        //         hashHistory.push('/');
        //     }
        // });
    }

    render() {
        const props = this.props;
        return (
            <MuiThemeProvider muiTheme={getMuiTheme(lightBaseTheme)}>
                <div className="container-fluid" style={{paddingLeft: '0px', paddingRight: '0px'}}>

                    <div className="nav"
                         style={{display: this.state.displayHeader, paddingLeft: '0px', paddingRight: '0px', height: '80px'}}>
                        <AppHeader userDetail={this.state.userDetail}/>
                    </div>

                    <div className="container" style={{display: this.state.displayChildren}}>
                        <div className="col-sm-12"
                             style={{
                                 height: '100%',
                                 width:'100%',
                                 marginLeft: '1%',
                                 marginRight: '1%',
                                 paddingLeft: '2%',
                                 paddingRight: '2%'
                             }}>
                            {props.children}
                        </div>
                    </div>
                </div>
            </MuiThemeProvider>
        );

    }
}

export default Root;
