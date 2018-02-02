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
import Repository from '../../services/database/Repository';
import Organization from '../../services/database/Organization';
import User from '../../services/database/User';
import RepositoryType from '../../services/database/RepositoryType';
import License from '../../services/database/License';
import ProductAreas from '../../services/database/ProductAreas';
import Team from '../../services/github/Team';
import Common from '../../services/github/Common';
import StringValidations from '../../services/validations/StringValidations';
import GitHubRepositoryCreation from '../../services/bpmn/GitHubRepositoryCreation';
import styles from '../../mystyles';


/**
 * @class RequestRepository
 * @extends {Component}
 * @description Request repository
 */
class RequestRepository extends Component {
    /**
    * @class RequestRepository
    * @extends {Component}
    * @param {any} props props for constructor
    * @description constructor
    */
    constructor(props) {
        super(props);
        this.state = {
            mainUsers: [],
            languages: ['Java'],
            licenseNames: null,
            repositoryTypes: null,
            organizations: null,
            teams: null,
            validateRepository: ' ',
            buttonState: false,
            displayFieldset: 'none',
            displayLoader: 'none',
            displayProgress: 'block',
            displayProductArea: 'none',
            groupIdInputRequired: false,
            groupIdInputSpan: ' ',
            productAreaSelectSpan: ' ',
            userDetails: [],
            open: false,
            openError: false,
            openSuccess: false,
            productAreas: [],
        };
        this.validateInputRepositoryName = this.validateInputRepositoryName.bind(this);
        this.submitRequest = this.submitRequest.bind(this);
        this.makeGroupIdRequired = this.makeGroupIdRequired.bind(this);
        this.makeProductAreaRequired = this.makeProductAreaRequired.bind(this);
        this.goBackToRequest = this.goBackToRequest.bind(this);
        this.setTeams = this.setTeams.bind(this);
        this.handleOpen = this.handleOpen.bind(this);
        this.handleClose = this.handleClose.bind(this);
        this.handleOpenError = this.handleOpenError.bind(this);
        this.handleCloseError = this.handleCloseError.bind(this);
        this.handleOpenSuccess = this.handleOpenSuccess.bind(this);
        this.handleCloseSuccess = this.handleCloseSuccess.bind(this);
        this.reloadPage = this.reloadPage.bind(this);
    }
    /**
    * @class RequestRepository
    * @extends {Component}
    * @description componentWillMount
    */
    componentWillMount() {
        /* eslint-disable max-len */
        Promise.all([User.getRepositoryMainUsers(), RepositoryType.getAllRepositoryTypes(), Common.getAllLanguages(), License.getAllLicenseNames(), ValidateUser.getUserDetails(), Organization.getAllOrganizations()]).then((response) => {
            this.setState(() => {
                return {
                    mainUsers: response[0],
                    repositoryTypes: response[1],
                    languages: response[2],
                    licenseNames: response[3],
                    userDetails: response[4],
                    organizations: response[5],
                };
            });
            this.setTeams();
            this.setState(() => {
                return {
                    displayFieldset: 'block',
                    displayLoader: 'none',
                    displaySuceessBox: 'none',
                    displayErrorBox: 'none',
                    displayProgress: 'none',
                };
            });
        }).catch(() => {
            this.setState(() => {
                return {
                    displayLoader: 'none',
                };
            });
        });

        ProductAreas.selectAll().then((response) => {
            this.setState(() => {
                return {
                    productAreas: response,
                };
            });
        });
    }
    /**
    * set teams after selecting organization
    */
    setTeams() {
        const options = this.selectOrganization.options;
        const selectOrganization = options[options.selectedIndex].text;
        Team.getAllTeams(selectOrganization).then((response) => {
            this.setState(() => {
                return {
                    teams: response,
                };
            });
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
    * @param {e} e event
    */
    validateInputRepositoryName(e) {
        const inputRepositoryName = e.target.value;
        Repository.selectDataFromName(inputRepositoryName).then((response) => {
            if (response.length > 0) {
                this.setState(() => {
                    return {
                        validateRepository: 'Sorry! This repository name already exists!',
                        buttonState: true,
                    };
                });
            } else {
                this.setState(() => {
                    return {
                        validateRepository: ' ',
                        buttonState: false,
                    };
                });
            }
        });
    }
    /**
    * make group id required
    */
    makeGroupIdRequired() {
        const checkedValue = this.inputNexus.checked;
        if (checkedValue === true) {
            this.setState(() => {
                return {
                    groupIdInputRequired: true,
                    groupIdInputSpan: <span className="required">*</span>,
                };
            });
        } else {
            this.setState(() => {
                return {
                    groupIdInputRequired: false,
                    groupIdInputSpan: ' ',
                };
            });
        }
    }
    /**
    * make group id required
    */
    makeProductAreaRequired() {
        const checkedValue = this.inputBuildable.checked;
        if (checkedValue === true) {
            this.setState(() => {
                return {
                    displayProductArea: 'block',
                    productAreaSelectSpan: <span className="required">*</span>,
                };
            });
        } else {
            this.setState(() => {
                return {
                    displayProductArea: 'none',
                    productAreaSelectSpan: ' ',
                };
            });
        }
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
                displayErrorBox: 'none',
                displaySuceessBox: 'none',
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
        let productArea = null;
        const repositoryTypeOptions = this.selectRepositoryType.options;
        const organizationOptions = this.selectOrganization.options;
        const teamOptions = this.selectTeam.options;
        const licenseOptions = this.selectLicense.options;
        const languageOptions = this.selectLanguage.options;
        const repositoryTypeText = repositoryTypeOptions[repositoryTypeOptions.selectedIndex].text;
        const organizationText = organizationOptions[organizationOptions.selectedIndex].text;
        const teamText = teamOptions[teamOptions.selectedIndex].text;
        const licenseText = licenseOptions[licenseOptions.selectedIndex].text;
        const languageText = languageOptions[languageOptions.selectedIndex].text;
        const repositoryName = StringValidations.escapeCharacters(this.inputRepositoryName.value.toString());
        const repositoryType = this.selectRepositoryType.value;
        const organization = this.selectOrganization.value;
        const team = this.selectTeam.value;
        const license = this.selectLicense.value;
        const language = StringValidations.escapeCharacters(this.selectLanguage.value);
        const groupId = StringValidations.escapeCharacters(this.inputGroupId.value.toString());
        const buildable = this.inputBuildable.checked;
        const nexus = this.inputNexus.checked;
        const isPrivate = this.inputPrivate.checked;
        const description = StringValidations.escapeCharacters(this.textDescription.value.toString());
        const requestedBy = StringValidations.escapeCharacters(this.state.userDetails.userEmail);

        if (buildable === true) {
            if (this.state.productAreas.length === 0) {
                productArea = '';
            } else {
                const productAreaOptions = this.selectProducArea.options;
                productArea = productAreaOptions[productAreaOptions.selectedIndex].text;
            }
        }
        const data = [
            repositoryName,
            language,
            buildable,
            nexus,
            isPrivate,
            description,
            groupId,
            license,
            team,
            organization,
            repositoryType,
            requestedBy,
            productArea,
        ];
        const mailData = [
            repositoryName,
            languageText,
            buildable,
            nexus,
            isPrivate,
            description,
            groupId,
            licenseText,
            teamText,
            organizationText,
            repositoryTypeText,
            requestedBy,
            productArea,
        ];
        this.handleClose();
        this.setState(() => {
            return {
                displayFieldset: 'none',
                displayLoader: 'block',
            };
        });
        GitHubRepositoryCreation.startProcess(data, mailData).then((response) => {
            if (response.data.responseType === 'Done') {
                this.handleClose();
                this.handleOpenSuccess();
                this.setState(() => {
                    return {
                        displayFieldset: 'none',
                        displayLoader: 'none',
                    };
                });
            } else {
                this.handleClose();
                this.handleOpenError();
                this.setState(() => {
                    return {
                        displayFieldset: 'none',
                        displayLoader: 'none',
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
                <h2 className="text-center">Request GitHub Repository Here</h2>
                <fieldset style={{ display: this.state.displayFieldset }}>
                    {/* eslint-disable max-len */}
                    <br />
                    <div className="form-group">
                        <label htmlFor="inputRepositoryName" className="col-lg-2 control-label">
                            <span className="required">*</span>&nbsp;Repository Name
                        </label>
                        <div className="col-lg-10">
                            <input
                                onChange={this.validateInputRepositoryName}
                                type="text"
                                className="form-control"
                                ref={(c) => { this.inputRepositoryName = c; }}
                                placeholder="carbon-identity-framework"
                                required="true"
                            />
                            <span className="validate" id="validateInputRepositoryName">
                                {this.state.validateRepository}
                            </span>
                        </div>
                    </div>

                    <div className="form-group">
                        <label htmlFor="selectRepositoryType" className="col-lg-2 control-label">
                            <span className="required">*</span>&nbsp;Repository Type
                        </label>
                        <div className="col-lg-10" >
                            {/* eslint-disable */}
                            <select className="form-control" ref={(c) => { this.selectRepositoryType = c; }} >
                                {(this.state.repositoryTypes !== null) ? this.state.repositoryTypes.map(repositoryType =>
                                    (<option key={repositoryType.REPOSITORYTYPE_ID} value={repositoryType.REPOSITORYTYPE_ID}>
                                        {repositoryType.REPOSITORYTYPE_NAME}
                                    </option>)) : <option></option>}
                            </select>
                            {/* eslint-enable */}
                        </div>
                    </div>

                    <div className="form-group">
                        <label htmlFor="selectOrganization" className="col-lg-2 control-label">
                            <span className="required">*</span>&nbsp;Organization
                        </label>
                        <div className="col-lg-10" onChange={this.setTeams}>
                            {/* eslint-disable */}
                            <select className="form-control" ref={(c) => { this.selectOrganization = c; }} >
                                {(this.state.organizations !== null) ? this.state.organizations.map((organization)=>
                                <option key={organization.ORGANIZATION_ID}
                                value={organization.ORGANIZATION_ID}>{organization.ORGANIZATION_NAME}
                                </option>) : <option></option> }
                            </select>
                            {/* eslint-enable */}
                        </div>
                    </div>

                    <div className="form-group">
                        <label htmlFor="selectTeam" className="col-lg-2 control-label">
                            <span className="required">*</span>&nbsp;Team Name
                        </label>
                        <div className="col-lg-10">
                            {/* eslint-disable */}
                            <select className="form-control" ref={(c) => { this.selectTeam = c; }} >
                                {(this.state.teams !== null) ? this.state.teams.map((team,i)=> <option key={team.id} value={team.id} >{team.name}</option>) : <option></option>}
                            </select>
                            {/* eslint-enable */}
                        </div>
                    </div>

                    <div className="form-group">
                        <label htmlFor="selectLicense" className="col-lg-2 control-label">&nbsp;License</label>
                        <div className="col-lg-10">
                            <select className="form-control" ref={(c) => { this.selectLicense = c; }}>
                                {/* eslint-disable */}
                                {(this.state.licenseNames !== null) ? this.state.licenseNames.map(license =>
                                    <option key={license.LICENSE_ID} value={license.LICENSE_ID} data={license.LICENSE_NAME}>
                                    {license.LICENSE_NAME}
                                </option>) : <option></option>}
                                {/* eslint-enable */}
                            </select>
                        </div>
                    </div>

                    <div className="form-group">
                        <label htmlFor="selectLanguage" className="col-lg-2 control-label">Language</label>
                        <div className="col-lg-10">
                            <select className="form-control" ref={(c) => { this.selectLanguage = c; }}>
                                {/* eslint-disable */}
                                {this.state.languages.map((language, i) => (language === 'Java')?<option key={i} selected value={language}>{language}</option> :<option key={i} value={language}>{language}</option>)}
                                {/* eslint-enable */}
                            </select>
                        </div>
                    </div>

                    <div className="form-group">
                        {/* eslint-disable max-len */}
                        <label htmlFor="inputLanguage" className="col-lg-2 control-label">Configurations</label>
                        <div className="col-lg-10">
                            <div className="checkbox">
                                <label htmlFor="inputBuildable">
                                    <input
                                        type="checkbox"
                                        ref={(c) => { this.inputBuildable = c; }}
                                        onChange={this.makeProductAreaRequired}
                                    /> Component Buildable
                                </label>
                                <br /><br />
                                <label htmlFor="inputPrivate">
                                    <input type="checkbox" ref={(c) => { this.inputPrivate = c; }} /> Make Private Repository
                                </label>
                                <br /><br />
                                <label htmlFor="inputNexus">
                                    <input
                                        onChange={this.makeGroupIdRequired}
                                        type="checkbox"
                                        ref={(c) => { this.inputNexus = c; }}
                                    />Create Nexus Repository
                                </label>
                            </div>
                        </div>
                    </div>

                    <div className="form-group" style={{ display: this.state.displayProductArea }}>
                        <label htmlFor="selectProducArea" className="col-lg-2 control-label">
                            <span className="required">*</span>&nbsp;Product Area
                        </label>
                        <div className="col-lg-10">
                            {/* eslint-disable */}
                            <select className="form-control" ref={(c) => { this.selectProducArea = c; }} >
                                {(this.state.productAreas !== null) ? this.state.productAreas.map((productArea)=>
                                <option key={productArea.pqd_area_id}
                                value={productArea.pqd_area_name}>{productArea.pqd_area_name}
                                </option>) : <option></option> }
                            </select>
                            {/* eslint-enable */}
                        </div>
                    </div>

                    <div className="form-group">
                        <label htmlFor="inputGroupId" className="col-lg-2 control-label">
                            {this.state.groupIdInputSpan}&nbsp;Group ID
                        </label>
                        <div className="col-lg-10" >
                            <input
                                type="text"
                                className="form-control"
                                ref={(c) => { this.inputGroupId = c; }}
                                placeholder="org.wso2.example"
                                required={this.state.groupIdInputRequired}
                            />
                        </div>
                    </div>

                    <div className="form-group">
                        <label htmlFor="textDescription" className="col-lg-2 control-label">Description</label>
                        <div className="col-lg-10">
                            <textarea
                                className="form-control"
                                rows="3"
                                ref={(c) => { this.textDescription = c; }}
                                placeholder="Description for README"
                            />
                        </div>
                    </div>

                    <div className="form-group">
                        {/* eslint-disable max-len */}
                        <div className="col-lg-10 col-lg-offset-2">
                            <RaisedButton type="submit" label="Request" style={styles.buttonStyle} labelColor='#ffffff' backgroundColor='#2196F3' disabled={this.state.buttonState} />
                            &nbsp;&nbsp;
                            <RaisedButton type="button" label="Cancel" style={styles.buttonStyle} labelColor='#ffffff' backgroundColor='#BDBDBD' />
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

export default RequestRepository;
