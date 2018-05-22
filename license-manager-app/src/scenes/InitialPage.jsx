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
import {Link} from 'react-router-dom';
import RaisedButton from 'material-ui/RaisedButton';
import styles from '../styles';
import {Paper} from "material-ui";

/**
 * @class Initial Page
 * @extends {Component}
 * @description Get user details
 */
class InitialPage extends Component {

    constructor(props) {
        super(props);
        this.state = {
            listOfPacks: [],
            selectedPack: '',
            openError: false,
            errorMessage: '',
            displayForm: 'none',
            displayErrorBox: 'none',
            buttonState: false,
        };
    }

    render() {
        return (
            <div className="container col-md-8">
                <Paper style={styles.welcomeNote} zDepth={5}>
                    <div style={{textAlign: "center", fontSize: '50px', padding: '5%',}}>
                        <Paper style={{fontWeight: 'bold'}}>WSO2 License Manager</Paper>
                    </div>

                    <div style={{textAlign: "center", fontSize: '100%', padding:'5%', color : '#5e6469'}}>
                        <p>
                            Please upload the pack to the FTP Server before starting
                        </p>
                    </div>

                    <Link to={'/packManager'}>
                        <RaisedButton
                            type="submit"
                            label="Click to Generate License"
                            primary={true}
                            style={styles.initialButtonStyle}
                        />
                    </Link>
                </Paper>
            </div>
        );
    }
}

export default InitialPage;
