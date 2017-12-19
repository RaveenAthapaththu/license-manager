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
import { Link } from 'react-router';
import CircularProgress from 'material-ui/CircularProgress';
import RaisedButton from 'material-ui/RaisedButton';
import Dialog from 'material-ui/Dialog';
import FlatButton from 'material-ui/FlatButton';
import ValidateUser from '../../services/authentication/ValidateUser';
import User from '../../services/database/User';
import Library from '../../services/database/Library';
import LibraryType from '../../services/database/LibraryType';
import LibraryCategory from '../../services/database/LibraryCategory';
import StringValidations from '../../services/validations/StringValidations';
import LibraryProcess from '../../services/bpmn/LibraryProcess';
import styles from '../../styles';

/**
 * @class RequestLibrary
 * @extends {Component}
 * @description Request library
 */
class RequestLibrary extends Component {
    /**
    * @class RequestLibrary
    * @extends {Component}
    * @param {any} props props for constructor
    * @description constructor
    */
    constructor(props) {
        super(props);
        this.state = {
            libraryMainUsers: [],
            libraryTypes: [],
            libraryCategories: [],
            libraryCategorySelected: null,
            buttonState: false,
            displayFieldset: 'block',
            displayLoader: 'none',
            displayProgress: 'none',
            validateLibrary: '',
            userDetails: [],
            open: false,
            openError: false,
            openSuccess: false,
            isRequired: true,
        };
        this.validateInputLibrary = this.validateInputLibrary.bind(this);
        this.submitRequest = this.submitRequest.bind(this);
        this.setTypes = this.setTypes.bind(this);
        this.goBackToRequest = this.goBackToRequest.bind(this);
        this.reloadPage = this.reloadPage.bind(this);
        this.handleOpen = this.handleOpen.bind(this);
        this.handleClose = this.handleClose.bind(this);
        this.handleOpenError = this.handleOpenError.bind(this);
        this.handleCloseError = this.handleCloseError.bind(this);
    }
    /**
    * @class RequestRepository
    * @extends {Component}
    * @description componentWillMount
    */
    componentWillMount() {
        ValidateUser.getUserDetails().then((response) => {
            this.setState(() => {
                return {
                    userDetails: response,
                };
            });
        });
        User.getLibraryMainUsers().then((response) => {
            this.setState(() => {
                return {
                    libraryMainUsers: response,
                };
            });
        });
        LibraryType.selectDeafult().then((response) => {
            this.setState(() => {
                return {
                    libraryTypes: response,
                };
            });
        });
        LibraryCategory.selectAll().then((response) => {
            this.setState(() => {
                return {
                    libraryCategories: response,
                    libraryCategorySelected: response[0].LIBCATEGORY_NAME,
                };
            });
        });
    }
    /**
    * set teams after selecting organization
    */
    setTypes() {
        const options = this.selectLibraryCategory.options;
        const selectLibraryCategoryId = options[options.selectedIndex].value;
        LibraryType.selectFromCategory(selectLibraryCategoryId).then((response) => {
            this.setState(() => {
                return {
                    libraryTypes: response,
                    libraryCategorySelected: options[options.selectedIndex].text,
                };
            });
            if (this.state.libraryCategorySelected === 'Java') {
                this.setState(() => {
                    return {
                        isRequired: true,
                    };
                });
            } else {
                this.setState(() => {
                    return {
                        isRequired: false,
                    };
                });
            }
        });
    }
    /**
    * handle open
    * @param {any} e event
    */
    handleOpen(e) {
        e.preventDefault();
        e.stopPropagation();
        e.nativeEvent.stopImmediatePropagation();
        this.setState(() => {
            return {
                open: true,
            };
        });
    }
    /**
    * handle close
    */
    handleClose() {
        this.setState(() => {
            return {
                open: false,
            };
        });
    }
    /**
    * handle open
    */
    handleOpenError() {
        this.setState(() => {
            return {
                openError: true,
            };
        });
    }
    /**
    * handle close
    */
    handleCloseError() {
        this.setState(() => {
            return {
                openError: false,
            };
        });
    }
    /**
    * handle open
    */
    handleOpenSuccess() {
        this.setState(() => {
            return {
                openSuccess: true,
            };
        });
    }
    /**
    * handle close
    */
    handleCloseSuccess() {
        this.setState(() => {
            return {
                openSuccess: false,
            };
        });
    }
    /**
    * validation function for input repository name
    */
    validateInputLibrary() {
        const inputLibraryName = this.inputLibraryName.value.toString();
        const inputLibraryVersion = this.inputVersionWeUse.value.toString();
        Library.selectLibraryAndRequestFromNameAndVersion(inputLibraryName, inputLibraryVersion).then((response) => {
            if (response.length > 0) {
                this.setState(() => {
                    return {
                        validateLibrary: 'Sorry! This Library already exists!',
                        buttonState: true,
                    };
                });
            } else {
                this.setState(() => {
                    return {
                        validateLibrary: ' ',
                        buttonState: false,
                    };
                });
            }
        });
    }
    /**
    * @param {any} e event
    * go back to request
    */
    goBackToRequest(e) {
        e.preventDefault();
        e.stopPropagation();
        e.nativeEvent.stopImmediatePropagation();
        this.handleCloseError();
        this.setState(() => {
            return {
                displayFieldset: 'block',
            };
        });
    }
    /**
    * reload page
    */
    reloadPage() {
        window.location.reload();
    }
    /**
    * @param {any} e event
    * go back to request
    * @returns {Promise} promise
    */
    submitRequest(e) {
        e.preventDefault();
        e.stopPropagation();
        e.nativeEvent.stopImmediatePropagation();
        this.handleClose();
        this.setState(() => {
            return {
                displayFieldset: 'none',
                displayLoader: 'block',
            };
        });
        const libraryTypeOptions = this.selectLibraryType.options;
        const libraryCategoryOptions = this.selectLibraryCategory.options;
        const libraryName = StringValidations.escapeCharacters(this.inputLibraryName.value);
        const libraryType = libraryTypeOptions[libraryTypeOptions.selectedIndex].text;
        const libraryTypeId = libraryTypeOptions[libraryTypeOptions.selectedIndex].value;
        const libraryCategory = libraryCategoryOptions[libraryCategoryOptions.selectedIndex].text;
        const libraryCategoryId = libraryCategoryOptions[libraryCategoryOptions.selectedIndex].value;
        const libraryVersion = StringValidations.escapeCharacters(this.inputVersionWeUse.value);
        const libraryFileName = StringValidations.escapeCharacters(this.inputLibraryFileName.value);
        const libraryLatestVersion = StringValidations.escapeCharacters(this.inputLatestVersion.value);
        const libraryCompany = StringValidations.escapeCharacters(this.inputCompany.value);
        const librarySponsored = ((this.checkSponsored.children[0].checked) ? true : false);//eslint-disable-line
        const libraryPurpose = StringValidations.escapeCharacters(this.textPurpose.value);
        const libraryDescription = StringValidations.escapeCharacters(this.textDescription.value);
        const libraryAlternatives = StringValidations.escapeCharacters(this.testAlternatives.value);
        const requestByEmail = StringValidations.escapeCharacters(this.state.userDetails.userEmail);
        if (requestByEmail === '' || requestByEmail === null) {
            this.setState(() => {
                return {
                    displayFieldset: 'none',
                    displayLoader: 'none',
                    displaySuceessBox: 'none',
                    displayErrorBox: 'block',
                };
            });
            return false;
        }
        let libraryGroupId = '';
        let libraryArtifactId = '';
        if (this.state.libraryCategorySelected !== 'Java') {
            libraryGroupId = null;
            libraryArtifactId = null;
        } else {
            libraryGroupId = StringValidations.escapeCharacters(this.inputGroupId.value);
            libraryArtifactId = StringValidations.escapeCharacters(this.inputArtifactId.value);
        }
        const data = {
            libName: libraryName,
            libType: libraryType,
            libTypeId: libraryTypeId,
            libCategory: libraryCategory,
            libCategoryId: libraryCategoryId,
            libGroupId: libraryGroupId,
            libArtifactId: libraryArtifactId,
            libUseVersion: libraryVersion,
            libLatestVersion: libraryLatestVersion,
            libFileName: libraryFileName,
            libCompany: libraryCompany,
            libSponsored: librarySponsored,
            libPurpose: libraryPurpose,
            libDescription: libraryDescription,
            libAlternatives: libraryAlternatives,
            libRequestBy: requestByEmail,
        };
        LibraryProcess.startProcess(data).then((response) => {
            if (response.data.responseType === 'Done') {
                this.handleOpenSuccess();
                this.setState(() => {
                    return {
                        displayFieldset: 'none',
                        displayLoader: 'none',
                    };
                });
            } else {
                this.handleOpenError();
                this.setState(() => {
                    return {
                        displayFieldset: 'none',
                        displayLoader: 'none',
                        displaySuceessBox: 'none',
                        displayErrorBox: 'block',
                    };
                });
            }
        });
        return false;
    }
    /**
    * @class RequestRepository
    * @extends {Component}
    * @description render method
    */
    render() {
        /* eslint-disable */
        const actions = [
            <FlatButton
                label="No"
                primary={true}
                onClick={this.handleClose}
            />,
            <FlatButton
                label="Yes"
                primary={true}
                keyboardFocused={true}
                onClick={this.submitRequest}
            />,
        ];
        const actionsError = [
            <FlatButton
                label="Back"
                primary={true}
                onClick={this.goBackToRequest}
            />,
            <FlatButton
                label="Another Request"
                primary={true}
                keyboardFocused={true}
                onClick={this.reloadPage}
            />,
        ];
        const actionsSuccess = [
            <Link to={'/'}>
                <FlatButton
                    label="Back to main"
                    primary={true}
                />
            </Link>,
            <FlatButton
                label="Another Request"
                primary={true}
                keyboardFocused={true}
                onClick={this.reloadPage}
            />,
        ];
        /* eslint-enable */
        return (
            <form className="form-horizontal" onSubmit={this.handleOpen}>
                {/* eslint-disable max-len */}
                <h2 className="text-center">3rd Party Library Request</h2>
                <fieldset style={{ display: this.state.displayFieldset }}>
                    {/* eslint-disable max-len */}
                    <br />
                    <div className="form-group">
                        <label htmlFor="inputLibraryName" className="col-lg-2 control-label">
                            <span className="required">*</span>&nbsp;Library Name
                        </label>
                        <div className="col-lg-10">
                            <input
                                type="text"
                                className="form-control"
                                ref={(c) => { this.inputLibraryName = c; }}
                                placeholder="org.eclipse.osgi"
                                onChange={this.validateInputLibrary}
                                required="true"
                            />
                        </div>
                    </div>

                    <div className="form-group">
                        <label htmlFor="selectLibraryCategory" className="col-lg-2 control-label">
                            <span className="required">*</span>&nbsp;Library Category
                        </label>
                        <div className="col-lg-10" onChange={this.setTypes}>
                            {/* eslint-disable */}
                            <select className="form-control" ref={(c) => { this.selectLibraryCategory = c; }} >
                                {this.state.libraryCategories.map((libraryCategory, i)=>
                                    (<option key={i} value={libraryCategory.LIBCATEGORY_ID}>
                                        {libraryCategory.LIBCATEGORY_NAME}
                                    </option>))}
                            </select>
                            {/* eslint-enable */}
                        </div>
                    </div>

                    <div className="form-group">
                        <label htmlFor="selectLibraryType" className="col-lg-2 control-label">
                            <span className="required">*</span>&nbsp;Library Type
                        </label>
                        <div className="col-lg-10" >
                            {/* eslint-disable */}
                            <select className="form-control" ref={(c) => { this.selectLibraryType = c; }} >
                                {this.state.libraryTypes.map((libraryType, i)=>
                                    (<option key={i} value={libraryType.LIBTYPE_ID}>
                                        {libraryType.LIBTYPE_NAME}
                                    </option>))}
                            </select>
                            {/* eslint-enable */}
                        </div>
                    </div>

                    <div
                        className="form-group"
                        style={{ display: ((this.state.libraryCategorySelected) === 'Java') ? 'block' : 'none' }}
                    >
                        <label htmlFor="inputGroupId" className="col-lg-2 control-label">
                            <span className="required">*</span>&nbsp;Group ID
                        </label>
                        <div className="col-lg-10" >
                            <input
                                type="text"
                                className="form-control"
                                ref={(c) => { this.inputGroupId = c; }}
                                placeholder="org.wso2.example"
                                required={this.state.isRequired}
                            />
                        </div>
                    </div>

                    <div
                        className="form-group"
                        style={{ display: ((this.state.libraryCategorySelected) === 'Java') ? 'block' : 'none' }}
                    >
                        <label htmlFor="inputArtifactId" className="col-lg-2 control-label">
                            <span className="required">*</span>&nbsp;Artifact ID
                        </label>
                        <div className="col-lg-10" >
                            <input
                                type="text"
                                className="form-control"
                                ref={(c) => { this.inputArtifactId = c; }}
                                placeholder="org.wso2.example"
                                required={this.state.isRequired}
                            />
                        </div>
                    </div>

                    <div className="form-group">
                        <label htmlFor="inputVersionWeUse" className="col-lg-2 control-label">
                            <span className="required">*</span>&nbsp;Version we use
                        </label>
                        <div className="col-lg-10">
                            <input
                                type="text"
                                className="form-control"
                                ref={(c) => { this.inputVersionWeUse = c; }}
                                placeholder="2.6.1"
                                onChange={this.validateInputLibrary}
                                required="true"
                            />
                            <span className="validate">
                                {this.state.validateLibrary}
                            </span>
                        </div>
                    </div>

                    <div className="form-group">
                        <label htmlFor="inputLibraryFileName" className="col-lg-2 control-label">
                            <span className="required">*</span>&nbsp;Library File Name
                        </label>
                        <div className="col-lg-10">
                            <input
                                type="text"
                                className="form-control"
                                ref={(c) => { this.inputLibraryFileName = c; }}
                                placeholder="jsecurity-0.9.0.jar"
                                required="true"
                            />
                        </div>
                    </div>

                    <div className="form-group">
                        <label htmlFor="inputLatestVersion" className="col-lg-2 control-label">
                            Latest Version
                        </label>
                        <div className="col-lg-10">
                            <input
                                type="text"
                                className="form-control"
                                ref={(c) => { this.inputLatestVersion = c; }}
                                placeholder="2.6.3"
                            />
                        </div>
                    </div>

                    <div className="form-group">
                        <label htmlFor="inputCompany" className="col-lg-2 control-label">
                            Company
                        </label>
                        <div className="col-lg-10">
                            <input
                                type="text"
                                className="form-control"
                                ref={(c) => { this.inputCompany = c; }}
                                placeholder="Eclipse"
                            />
                        </div>
                    </div>

                    <div className="form-group">
                        {/* eslint-disable max-len */}
                        <label htmlFor="checkSponsored" className="col-lg-2 control-label"> Sponsor by the Company </label>
                        <div className="col-lg-10" ref={(c) => { this.checkSponsored = c; }}>
                            <input type="radio" name="sponsored" value="Yes" /> Yes <br />
                            <input type="radio" name="sponsored" value="No" checked="true" /> No <br />
                        </div>
                    </div>

                    <div className="form-group">
                        <label htmlFor="textPurpose" className="col-lg-2 control-label">
                            <span className="required">*</span>&nbsp;Purpose
                        </label>
                        <div className="col-lg-10">
                            <textarea
                                className="form-control"
                                rows="3"
                                ref={(c) => { this.textPurpose = c; }}
                                placeholder="Purpose for using"
                                required="true"
                            />
                        </div>
                    </div>

                    <div className="form-group">
                        <label htmlFor="textDescription" className="col-lg-2 control-label">
                            <span className="required">*</span>&nbsp;Description
                        </label>
                        <div className="col-lg-10">
                            <textarea
                                className="form-control"
                                rows="3"
                                ref={(c) => { this.textDescription = c; }}
                                placeholder="About License"
                                required="true"
                            />
                        </div>
                    </div>

                    <div className="form-group">
                        <label htmlFor="textAlternatives" className="col-lg-2 control-label">
                            Alternatives
                        </label>
                        <div className="col-lg-10">
                            <textarea
                                className="form-control"
                                rows="3"
                                ref={(c) => { this.testAlternatives = c; }}
                                placeholder="Alternative Libraries"
                            />
                        </div>
                    </div>

                    <div className="form-group">
                        {/* eslint-disable max-len */}
                        <div className="col-lg-10 col-lg-offset-2">
                            <RaisedButton type="submit" label="Request" style={styles.buttonStyle} labelColor='#ffffff' backgroundColor='#2196F3' disabled={this.state.buttonState} />
                            &nbsp;&nbsp;
                            <RaisedButton type="button" label="Cancel" style={styles.buttonStyle} labelColor='#ffffff' backgroundColor='#BDBDBD' onClick={this.reloadPage} />
                        </div>
                    </div>
                </fieldset>
                <div className="container-fluid" style={{ display: this.state.displayLoader }}>
                    <br /><br /><br />
                    <div className="row">
                        <div className="col-lg-5" />
                        <div className="col-lg-4">
                            <CircularProgress color="#f47c24" size={100} thickness={7} />
                        </div>
                        <div className="col-lg-3" />
                    </div>
                </div>

                <Dialog
                    title="Success"
                    actions={actionsSuccess}
                    modal={false}
                    open={this.state.openSuccess}
                    onRequestClose={this.reloadPage}
                >
                    The request has been successfully submited for approval via e-mail
                </Dialog>

                <Dialog
                    title="Error"
                    actions={actionsError}
                    modal={false}
                    open={this.state.openError}
                    onRequestClose={this.goBackToRequest}
                >
                    Error occurs when submit the request.
                </Dialog>
                <div className="container-fluid" style={{ display: this.state.displayProgress }}>
                    <br /><br /><br />
                    <div className="row">
                        <div className="col-lg-5" />
                        <div className="col-lg-4">
                            <CircularProgress color="#f47c24" size={100} thickness={7} />
                        </div>
                        <div className="col-lg-3" />
                    </div>
                </div>
                <Dialog
                    title="Confirm"
                    actions={actions}
                    modal={false}
                    open={this.state.open}
                    onRequestClose={this.handleClose}
                >
                    Are you sure to submit this request ?
                </Dialog>
            </form>
        );
    }
}

export default RequestLibrary;
