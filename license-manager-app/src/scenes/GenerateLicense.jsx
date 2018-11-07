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
import {Link, withRouter} from 'react-router-dom';
import RaisedButton from 'material-ui/RaisedButton';
import Dialog from 'material-ui/Dialog';
import FlatButton from 'material-ui/FlatButton';
import ServiceManager from '../services/msf4j/ServiceManager';
import styles from '../styles';
import textFile from '../assets/images/txt-file.png';
import StepComponent from "../components/StepComponent";
import HeaderComponent from "../components/HeaderComponent";

/**
 * @class GenerateLicense
 * @extends {Component}
 * @description Generates the licenses text and provide to download.
 */
class GenerateLicense extends Component {

    constructor(props) {
        super(props);
        this.state = {
            packName: this.props.location.state.packName,
            errorMessageOpened: false,
            displayDownload: 'block',
            displayErrorBox: 'none',
            buttonState: false,
            header: 'Download the license text',
            stepIndex: 3,
            errorMessage: '',
        };
        this.openError = this.openError.bind(this);
        this.closeError = this.closeError.bind(this);
        this.reloadPage = this.reloadPage.bind(this);
        this.generateLicense = this.generateLicense.bind(this);
        this.backToFront = this.backToFront.bind(this);
    }

    componentWillMount() {
    }

    /**
     * Sends the API call to generate the licenses text.
     * @param e button click event
     */
    generateLicense(e) {
        e.preventDefault();
        e.stopPropagation();
        e.nativeEvent.stopImmediatePropagation();

        ServiceManager.getLicense(this.state.packName).then((response) => {
            if (response.data.responseType === "done") {
                ServiceManager.downloadLicense(this.state.packName).then((responseFile) => {
                    const url = window.URL.createObjectURL(new Blob([responseFile.data]));
                    const link = document.createElement('a');
                    const fileNameLength = this.state.packName.length;
                    const fileName = 'LICENSE-' + this.state.packName.substring(0, fileNameLength - 4) + '.txt';
                    link.href = url;
                    link.setAttribute('download', fileName);
                    document.body.appendChild(link);
                    link.click();
                    this.setState(() => {
                        return {
                            displayDownload: 'block',
                        };
                    });
                }).catch(() => {
                    this.handleError("Network Error");
                });
            } else {
                this.handleError("Failed to generate license text");

            }
        }).catch(() => {
            this.handleError("Network Error");
        });
    }

    /**
     * Set a customized error message and open the dialog.
     * @param message
     */
    handleError(message) {
        this.setState(() => {
            return {
                errorMessage: message,
            };
        });
        this.openError();
    }

    /**
     * Handle open error message.
     */
    openError() {
        this.setState(() => {
            return {
                errorMessageOpened: true,
            };
        });
    }

    /**
     * Handle close error message.
     */
    closeError() {
        this.setState(() => {
            return {
                errorMessageOpened: false,
            };
        });
    }

    /**
     * Reloads the page.
     */
    reloadPage() {
        window.location.reload();
    }

    /**
     * Redirect to the main page.
     */
    backToFront() {
        this.props.history.push({
            pathname: '/'
        });
    }

    render() {
        const actionsError = [
            <Link to={'/packManager'}>
                <FlatButton
                    label="Back"
                    primary={true}
                />
            </Link>,

        ];
        return (
            <div className="container">
                <HeaderComponent header={this.state.header}/>
                <StepComponent step={this.state.stepIndex}/>
                <form onSubmit={this.generateLicense} style={{display: this.state.displayDownload}}>
                    <div style={styles.downloadArea}>
                        <img src={textFile} style={styles.textFile} alt=""/>
                        <br/>
                        <span>
                            <b>{'LICENSE-' + this.state.packName.substring(0, this.state.packName.length - 4) + '.txt'}</b>
                        </span>
                    </div>
                    <br/>
                    <RaisedButton
                        type="submit"
                        label="Download"
                        style={styles.buttonStyle}
                        primary={true}
                        disabled={this.state.buttonState}
                    />
                    <RaisedButton
                        type="button"
                        label="Back to main"
                        onClick={this.backToFront}
                        style={styles.buttonStyle}
                    />
                </form>
                <Dialog
                    title="Error"
                    actions={actionsError}
                    modal={false}
                    open={this.state.errorMessageOpened}
                >
                    {this.state.errorMessage}
                </Dialog>

            </div>

        );
    }
}

GenerateLicense = withRouter(GenerateLicense);
export default GenerateLicense;
