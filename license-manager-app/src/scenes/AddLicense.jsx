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
import {Table, TableBody, TableHeader, TableHeaderColumn, TableRow, TableRowColumn,} from 'material-ui/Table';
import Subheader from 'material-ui/Subheader';
import RaisedButton from 'material-ui/RaisedButton';
import Dialog from 'material-ui/Dialog';
import FlatButton from 'material-ui/FlatButton';
import SelectField from 'material-ui/SelectField';
import MenuItem from 'material-ui/MenuItem';
import ServiceManager from '../services/msf4j/ServiceManager';
import styles from '../styles';
import StepComponent from "../components/StepComponent";
import ProgressComponent from "../components/ProgressComponent";
import HeaderComponent from "../components/HeaderComponent";
import {Checkbox, Paper} from "material-ui";

/**
 * @class AddLicense
 * @extends {Component}
 * @description Add licenses for the jars if missing any
 */
class AddLicense extends Component {

    constructor(props) {
        super(props);
        this.state = {
            nameDefinedJars: props.location.state ? props.location.state.nameMissingJars : null,
            packName: props.location.state ? props.location.state.packName : null,
            errorMessageOpened: false,
            confirmMessageOpened: false,
            recheckMessageOpened: false,
            displayProgress: 'block',
            displayFormLicense: 'none',
            displayDownload: 'none',
            displayComponentTable: 'none',
            displayLibraryTable: 'none',
            checked: false,
            header: 'Add Licenses for the jars',
            licenseMissingComponents: [],
            licenseMissingLibraries: [],
            license: [],
            stepIndex: 2,
            errorMessage: '',
            statusMessage: ''

        };
        this.handleAddLicense = this.handleAddLicense.bind(this);
        this.handleComponentSelect = this.handleComponentSelect.bind(this);
        this.handleLibrarySelect = this.handleLibrarySelect.bind(this);
        this.openError = this.openError.bind(this);
        this.closeError = this.closeError.bind(this);
        this.openConfirmation = this.openConfirmation.bind(this);
        this.closeConfirmation = this.closeConfirmation.bind(this);
        this.redirectToNext = this.redirectToNext.bind(this);
        this.handleError = this.handleError.bind(this);
        this.updateCheck = this.updateCheck.bind(this);
        this.validateFormData = this.validateFormData.bind(this);
        this.backToMain = this.backToMain.bind(this);
    }

    componentWillMount() {
        if (this.state.nameDefinedJars === '' || this.state.packName === '') {
            this.backToMain();
        }

        ServiceManager.enterJars(this.state.nameDefinedJars).then((response) => {
            if (response.data.responseType === 'done') {
                this.setState(() => {
                    return {
                        statusMessage: response.data.responseMessage,
                    };
                });
                let intervalID = setInterval(function () {
                    ServiceManager.checkProgress().then((responseNext) => {
                        if (responseNext.data.responseStatus === 'complete' && responseNext.data.responseType === 'done') {
                            ServiceManager.getLicenseMissingJars().then((responseJars) => {
                                if (responseJars.data.responseType === 'done') {
                                    if (responseJars.data.component.length === 0 && responseJars.data.library.length === 0) {
                                        this.redirectToNext();
                                    } else {
                                        ServiceManager.selectLicense().then((response) => {
                                            if (response.data.responseType === "done") {
                                                if (response.data.responseData.length !== 0) {
                                                    this.setState(() => {
                                                        return {
                                                            license: response.data.responseData,
                                                        };
                                                    });
                                                }
                                            } else {
                                                this.handleError(response.data.responseMessage);
                                            }
                                        }).catch(() => {
                                            this.handleError("Network Error while loading licenses");
                                        });
                                        this.setState(() => {
                                            return {
                                                displayFormLicense: 'block',
                                                displayProgress: 'none',
                                                header: 'Add Licenses for the jars',
                                                licenseMissingComponents: responseJars.data.component,
                                                licenseMissingLibraries: responseJars.data.library,
                                                stepIndex: 2,
                                            };
                                        });
                                    }
                                } else {
                                    this.handleError(responseJars.data.responseMessage)
                                }
                            }).catch(() => {
                                this.handleError("Network Error");
                                clearTimeout(intervalID);
                            });
                            clearTimeout(intervalID);
                        } else if (responseNext.data.responseStatus === 'running' && responseNext.data.responseType === 'done') {
                            this.setState(() => {
                                return {
                                    statusMessage: responseNext.data.responseMessage,
                                };
                            });
                        } else {
                            this.handleError(responseNext.data.responseMessage);
                            clearTimeout(intervalID);
                        }
                    }).catch(() => {
                        this.handleError("Network Error");
                        clearTimeout(intervalID);
                    });
                }.bind(this), 5000);
            } else {
                this.handleError(response.data.responseMessage);
            }

        }).catch(() => {
            this.handleError("Network Error");
        });
    }

    /**
     * Set a customize message and open error dialog.
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
     * Handle the form submission to add licenses.
     * @param e
     */
    handleAddLicense(e) {
        e.preventDefault();
        e.stopPropagation();
        e.nativeEvent.stopImmediatePropagation();
        this.setState(() => {
            return {
                displayProgress: 'block',
                displayFormLicense: 'none',
                confirmMessageOpened: false,
            };
        });
        ServiceManager.addLicense(this.state.licenseMissingComponents, this.state.licenseMissingLibraries).then((response) => {
            let intervalID = setInterval(function () {
                ServiceManager.checkProgress().then((responseNext) => {
                    if (responseNext.data.responseStatus === 'complete' && responseNext.data.responseType === 'done') {
                        this.redirectToNext();
                        clearTimeout(intervalID);
                    } else if (responseNext.data.responseStatus === 'running' && responseNext.data.responseType === 'done') {
                        this.setState(() => {
                            return {
                                statusMessage: responseNext.data.responseMessage,
                            };
                        });
                    } else {
                        this.handleError(responseNext.data.responseMessage);
                        clearTimeout(intervalID);
                    }
                }).catch(() => {
                    this.handleError("Network Error");
                    clearTimeout(intervalID);
                });
            }.bind(this), 5000);
        }).catch(() => {
            this.handleError("Network Error");
        });
    }

    /**
     * Open a dialog to confirm the submission of licenses.
     * @param e
     */
    openConfirmation(e) {
        e.preventDefault();
        e.stopPropagation();
        e.nativeEvent.stopImmediatePropagation();
        let isDataValid = this.validateFormData();

        if (isDataValid) {
            this.setState(() => {
                return {
                    confirmMessageOpened: true,
                };
            });
        }
        else {
            this.setState(() => {
                return {
                    recheckMessageOpened: true,
                };
            });
        }

    }

    /**
     * Close submit confirmation dialog.
     */
    closeConfirmation() {
        this.setState(() => {
            return {
                confirmMessageOpened: false,
                recheckMessageOpened: false
            };
        });
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
     * Redirect to the next page based on the response.
     */
    redirectToNext() {
        this.props.history.push({
            pathname: '/licenseGenerator',
            state: {
                packName: this.state.packName,
            }
        });
    }

    /**
     * Handle the check box click event.
     */
    updateCheck() {
        this.setState((oldState) => {
            return {
                checked: !oldState.checked
            };
        });
    }

    /**
     * Check whether licenses are selected for all the components and jars.
     */
    validateFormData() {
        const components = this.state.licenseMissingComponents;
        const libraries = this.state.licenseMissingLibraries;
        let areAllComponentsFilled = true;
        let areAllLibrariesFilled = true;
        let isDataValid = false;
        if (components.length > 0) {
            for (let i = 0; i < components.length; i++) {
                if (components[i].licenseKey === "NEW") {
                    areAllComponentsFilled = false;
                    break;
                }
            }
        }
        if (libraries.length > 0) {
            for (let i = 0; i < libraries.length; i++) {
                if (libraries[i].licenseKey === "NEW") {
                    areAllLibrariesFilled = false;
                    break;
                }
            }
        }
        if (areAllComponentsFilled && areAllLibrariesFilled) {
            isDataValid = true
        }
        return isDataValid;
    }

    /**
     * Handle the event when licenses are selected for a component.
     * @param {any} event event
     * @param {any} j event
     * @param {any} n event
     * @param {any} m event
     */
    handleComponentSelect(event, j, n, m) {
        const comp = this.state.licenseMissingComponents.map((jar, i) => {
            if (i !== event) {
                return jar;
            } else {
                const jarFile = {
                    index: jar.index,
                    name: jar.name,
                    version: jar.version,
                    type: jar.type,
                    previousLicense: jar.previousLicense,
                    licenseKey: m,
                };
                return (jarFile);
            }
        });
        this.setState(() => {
            return {
                licenseMissingComponents: comp,
            };
        });
    }

    /**
     * Handle the event when licenses are selected for a library.
     * @param {any} event event
     * @param {any} j event
     * @param {any} n event
     * @param {any} m event
     */
    handleLibrarySelect(event, j, n, m) {
        const lib = this.state.licenseMissingLibraries.map((jar, i) => {
            if (i !== event) {
                return jar;
            } else {
                const jarFile = {
                    index: jar.index,
                    name: jar.name,
                    version: jar.version,
                    type: jar.type,
                    previousLicense: jar.previousLicense,
                    licenseKey: m,
                };
                return (jarFile);
            }
        });
        this.setState(() => {
            return {
                licenseMissingLibraries: lib,
            };
        });
    }

    /**
     * reload page
     */
    backToMain() {
        this.props.history.push({
            pathname: '/packManager'
        });
    }

    render() {
        const licenseConfirmActions = [
            <FlatButton
                label="Save"
                primary={true}
                onClick={this.handleAddLicense}
            />,
            <FlatButton
                label="Recheck"
                primary={true}
                onClick={this.closeConfirmation}
            />
        ];
        const recheckLicenses = [
            <FlatButton
                label="Back"
                primary={true}
                onClick={this.closeConfirmation}
            />
        ];
        const actionsError = [
            <Link to={'/packManager'}>
                <FlatButton
                    label="Back"
                    primary={true}
                />
            </Link>,

        ];
        const component = [];
        const library = [];
        const license = [];
        const licenseList = this.state.license;

        let displayComponent = 'none';
        let displayLibrary = 'none';
        let k = 0;
        for (let i = 0; i < licenseList.length; i++) {
            license.push(
                <MenuItem
                    value={licenseList[i].LICENSE_KEY}
                    key={licenseList[i].LICENSE_ID}
                    primaryText={licenseList[i].LICENSE_KEY}
                />
            );
        }
        if (this.state.licenseMissingComponents.length > 0) {
            const jars = this.state.licenseMissingComponents;
            for (let i = 0; i < jars.length; i++) {
                let licenseTextColor;

                if (jars[i].previousLicense === 'NEW' && jars[i].licenseKey === 'apache2') {
                    licenseTextColor = {color: '#000000'}
                } else if (jars[i].previousLicense !== 'apache2' || jars[i].licenseKey !== 'apache2') {
                    licenseTextColor = {color: '#1B4F72'}
                }
                component.push(
                    <TableRow style={licenseTextColor} key={i}>
                        <TableRowColumn style={{fontSize: '14px', width: '35%'}}
                                        key={k}>{jars[i].name}</TableRowColumn>
                        <TableRowColumn style={{fontSize: '14px', width: '15%'}}
                                        key={k + 1}>{jars[i].version}</TableRowColumn>
                        <TableRowColumn style={{fontSize: '14px', width: '15%'}}
                                        key={k + 2}>{jars[i].type}</TableRowColumn>
                        <TableRowColumn style={{fontSize: '14px', width: '15%'}}
                                        key={k + 3}>{jars[i].previousLicense}</TableRowColumn>
                        <TableRowColumn style={{fontSize: '14px', width: '20%'}} key={k + 4}>
                            <SelectField
                                style={styles.selectField}
                                labelStyle={licenseTextColor}
                                autoWidth={true}
                                value={this.state.licenseMissingComponents[i].licenseKey}
                                onChange={this.handleComponentSelect.bind(this, i)}
                                maxHeight={200}
                                underlineStyle={{borderColor: '#00bcd461'}}
                                selectedMenuItemStyle={{color: '#17A589'}}
                            >
                                {license}
                            </SelectField>
                        </TableRowColumn>
                    </TableRow>
                );
                k = k + 3;
            }
            displayComponent = 'block';
        } else {
            displayComponent = 'none';
        }
        if (this.state.licenseMissingLibraries.length > 0) {
            let i;
            const jars = this.state.licenseMissingLibraries;
            for (i = 0; i < jars.length; i++) {
                let licenseTextColor;

                if (jars[i].previousLicense === 'NEW' && jars[i].licenseKey === 'apache2') {
                    licenseTextColor = {color: '#000000'}
                } else if (jars[i].previousLicense !== 'apache2' || jars[i].licenseKey !== 'apache2') {
                    licenseTextColor = {color: '#1B4F72'}
                }

                library.push(
                    <TableRow style={licenseTextColor} key={i}>
                        <TableRowColumn style={{fontSize: '14px', width: '35%'}}
                                        key={k}>{jars[i].name}</TableRowColumn>
                        <TableRowColumn style={{fontSize: '14px', width: '15%'}}
                                        key={k + 1}>{jars[i].version}</TableRowColumn>
                        <TableRowColumn style={{fontSize: '14px', width: '15%'}}
                                        key={k + 2}>{jars[i].type}</TableRowColumn>
                        <TableRowColumn style={{fontSize: '14px', width: '15%'}}
                                        key={k + 3}>{jars[i].previousLicense}</TableRowColumn>
                        <TableRowColumn style={{fontSize: '14px', width: '20%'}} key={k + 4}>
                            <SelectField
                                style={styles.selectField}
                                labelStyle={licenseTextColor}
                                value={this.state.licenseMissingLibraries[i].licenseKey}
                                onChange={this.handleLibrarySelect.bind(this, i)}
                                maxHeight={200}
                                underlineStyle={{borderColor: '#00bcd461'}}
                                selectedMenuItemStyle={{color: '#17A589'}}
                            >
                                {license}
                            </SelectField>
                        </TableRowColumn>
                    </TableRow>
                );
                k = k + 4;
            }
            displayLibrary = 'block';
        } else {
            displayLibrary = 'none';
        }
        return (
            <div className="container">
                <HeaderComponent header={this.state.header}/>
                <StepComponent
                    step={this.state.stepIndex}
                />
                <form onSubmit={this.openConfirmation} style={{display: this.state.displayFormLicense}}>
                    <div style={{display: displayComponent}}>
                        <Subheader style={styles.subHeader}>Components</Subheader>
                        <Table style={{overflow: 'auto'}}>>
                            <TableHeader displaySelectAll={false} adjustForCheckbox={false}>
                                <TableRow key={0}>
                                    <TableHeaderColumn style={{fontSize: '20px', color: "#000000", width: '35%'}}>
                                        Name
                                    </TableHeaderColumn>
                                    <TableHeaderColumn style={{fontSize: '20px', color: "#000000", width: '15%'}}>
                                        Version
                                    </TableHeaderColumn>
                                    <TableHeaderColumn style={{fontSize: '20px', color: "#000000", width: '15%'}}>
                                        Type
                                    </TableHeaderColumn>
                                    <TableHeaderColumn style={{fontSize: '20px', color: "#000000", width: '15%'}}>
                                        License
                                    </TableHeaderColumn>
                                    <TableHeaderColumn style={{fontSize: '20px', color: "#000000", width: '20%'}}>
                                        Select License</TableHeaderColumn>
                                </TableRow>
                            </TableHeader>
                            <TableBody displayRowCheckbox={false} showRowHover={true}>
                                {component}
                            </TableBody>
                        </Table>
                    </div>
                    <br/>
                    <div style={{display: displayLibrary}}>
                        <Subheader style={styles.subHeader}>Libraries</Subheader>
                        <Table style={{overflow: 'auto'}}>
                            <TableHeader displaySelectAll={false} adjustForCheckbox={false}>
                                <TableRow key={0}>
                                    <TableHeaderColumn style={{fontSize: '20px', color: "#000000", width: '35%'}}>
                                        Name
                                    </TableHeaderColumn>
                                    <TableHeaderColumn style={{fontSize: '20px', color: "#000000", width: '15%'}}>
                                        Version
                                    </TableHeaderColumn>
                                    <TableHeaderColumn style={{fontSize: '20px', color: "#000000", width: '15%'}}>
                                        Type
                                    </TableHeaderColumn>
                                    <TableHeaderColumn style={{fontSize: '20px', color: "#000000", width: '15%'}}>
                                        License
                                    </TableHeaderColumn>
                                    <TableHeaderColumn style={{fontSize: '20px', color: "#000000", width: '20%'}}>
                                        Select License
                                    </TableHeaderColumn>
                                </TableRow>
                            </TableHeader>
                            <TableBody displayRowCheckbox={false} showRowHover={true}>
                                {library}
                            </TableBody>
                        </Table>
                    </div>
                    <br/>
                    <Checkbox
                        label='I guarantee that all the licenses added are correct.'
                        checked={this.state.checked}
                        onCheck={this.updateCheck.bind(this)}
                    />
                    <RaisedButton
                        type="submit"
                        label="Accept and Save"
                        style={styles.saveButtonStyle}
                        primary={true}
                        disabled={!this.state.checked}
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

                <div className="container-fluid" style={{display: this.state.displayProgress}}>
                    <Paper style={styles.statusNote}>
                        <strong>Please Wait</strong><br/>
                        <p>{this.state.statusMessage}</p>
                    </Paper>
                    <ProgressComponent/>
                </div>

                <Dialog
                    title="Confirm"
                    actions={licenseConfirmActions}
                    modal={false}
                    open={this.state.confirmMessageOpened}
                >
                    Make sure you have added the correct licenses.
                </Dialog>

                <Dialog
                    title="Alert"
                    actions={recheckLicenses}
                    modal={false}
                    open={this.state.recheckMessageOpened}
                >
                    Please add licenses to all jars.
                </Dialog>
            </div>

        );
    }
}

AddLicense = withRouter(AddLicense);
export default AddLicense;
