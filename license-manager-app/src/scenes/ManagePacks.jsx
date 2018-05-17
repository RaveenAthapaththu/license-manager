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
import FlatButton from 'material-ui/FlatButton';
import RaisedButton from 'material-ui/RaisedButton';
import Dialog from 'material-ui/Dialog';
import ServiceManager from '../services/msf4j/ServiceManager';
import styles from '../styles';
import {Paper, RadioButton, RadioButtonGroup} from "material-ui";
import HeaderComponent from "../components/HeaderComponent";

/**
 * @class ManagePacks
 * @extends {Component}
 * @description Lists the available packs and allows user to select one for license generation.
 */
class ManagePacks extends Component {

    constructor(props) {
        super(props);
        this.state = {
            listOfPacks: [],
            selectedPack: '',
            errorMessageOpened: false,
            errorMessage: '',
            displayForm: 'none',
            displayErrorBox: 'none',
            buttonState: false,
        };
        this.selectPack = this.selectPack.bind(this);
        this.reloadPage = this.reloadPage.bind(this);
        this.handleOpenError = this.handleOpenError.bind(this);
        this.handleCloseError = this.handleCloseError.bind(this);
        this.handleError = this.handleError.bind(this);
    }

    componentWillMount() {
        ServiceManager.getUploadedPacks().then((response) => {
            if (response.data.responseType === 'done') {
                this.setState(() => {
                    return {
                        listOfPacks: response.data.responseData,
                        displayForm: 'block',
                    };
                });
            } else {
                this.handleError(response.data.responseMessage);
            }
        }).catch(() => {
            this.handleError('Network Error');
        });
    }

    /**
     * For all errors display customized message.
     * @param message
     */
    handleError(message) {
        this.setState(() => {
            return {
                errorMessage: message,
            };
        });
        this.handleOpenError();
    }

    /**
     * Handle open error message.
     */
    handleOpenError() {
        this.setState(() => {
            return {
                errorMessageOpened: true,
            };
        });
    }

    /**
     * Handle close error message.
     */
    handleCloseError() {
        this.setState(() => {
            return {
                errorMessageOpened: false,
            };
        });
    }

    /**
     * Handle the pack selection.
     * @param e     pack select event.
     */
    selectPack(e) {
        this.setState({
            selectedPack: e.currentTarget.value,
            buttonState: true,
        });
    }

    /**
     * Reload page
     */
    reloadPage() {
        window.location.reload();
    }

    render() {
        const actionsError = [
            <Link to={'/'}>
                <FlatButton
                    label="Back"
                    primary={true}
                />
            </Link>,

        ];

        const packs = this.state.listOfPacks;
        const listOfPackNames = packs.map((pack) =>
            <RadioButton
                value={pack.name}
                label={pack.name}
            />
        );
        const path = this.state.buttonState ? '/jarManager' : '/packManager';
        return (

            <div className="container col-md-8">
                <HeaderComponent header="Select a Pack to Generate Licence"/>
                <div>
                    <Paper style={styles.initialNote}>
                        <strong>Note: </strong> Please upload your pack to the given location.
                    </Paper>

                </div>

                <div style={{padding: '5%'}}>
                    <form style={{display: this.state.displayForm}}>

                        <RadioButtonGroup onChange={this.selectPack} value={this.state.selectedPack}>
                            {listOfPackNames}
                        </RadioButtonGroup>

                        <Link
                            to={{
                                pathname: path,
                                query: {selectedPack: this.state.selectedPack}
                            }}
                        >
                            <RaisedButton
                                type="submit"
                                label="Generate"
                                primary={true}
                                style={styles.generateButtonStyle}
                                disabled={!this.state.buttonState}
                            />
                        </Link>
                    </form>

                    <Dialog
                        title="Error"
                        actions={actionsError}
                        modal={false}
                        open={this.state.errorMessageOpened}
                        onRequestClose={this.backToFront}
                    >
                        {this.state.errorMessage}
                    </Dialog>

                </div>
            </div>
        );
    }
}

export default ManagePacks;
