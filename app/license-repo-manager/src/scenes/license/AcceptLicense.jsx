import React, { Component } from 'react';
import { Link } from 'react-router';
import { Table, TableBody, TableRow, TableHeader, TableRowColumn, TableHeaderColumn } from 'material-ui/Table';
import Subheader from 'material-ui/Subheader';
import RaisedButton from 'material-ui/RaisedButton';
import SelectField from 'material-ui/SelectField';
import MenuItem from 'material-ui/MenuItem';
import Dialog from 'material-ui/Dialog';
import FlatButton from 'material-ui/FlatButton';
import CircularProgress from 'material-ui/CircularProgress';
import DataManager from '../../services/msf4j/DataManager';
import styles from '../../mystyles';
import Pack from '../../services/msf4j/Pack';

/**
* @class AcceptLicense
* @extends {Component}
* @description Show waiting libraries for acceptance
*/
class AcceptLicense extends Component {
    /**
    * @class AcceptLicense
    * @extends {Component}
    * @param {any} props props for constructor
    * @description constructor
    */
    constructor(props) {
        super(props);
        this.state = {
            licenseMissingComponents: [],
            licenseMissingLibraries: [],
            license: [],
            productId: props.location.query.productId,//eslint-disable-line
            requestId: props.location.query.requestId,//eslint-disable-line
            openLicense: false,
            displayLoader: 'none',
            openLicenseReject: false,
            displayFormLicense: 'block',
        };
        this.handleComponentSelect = this.handleComponentSelect.bind(this);
        this.handleLibrarySelect = this.handleLibrarySelect.bind(this);
        this.handleOpenLicense = this.handleOpenLicense.bind(this);
        this.handleCloseLicense = this.handleCloseLicense.bind(this);
        this.handleOpenLicenseReject = this.handleOpenLicenseReject.bind(this);
        this.handleCloseLicenseReject = this.handleCloseLicenseReject.bind(this);
        this.handleOpenError = this.handleOpenError.bind(this);
        this.handleCloseError = this.handleCloseError.bind(this);
        this.handleOpenSuccess = this.handleOpenSuccess.bind(this);
        this.handleCloseSuccess = this.handleCloseSuccess.bind(this);
        this.reloadPage = this.reloadPage.bind(this);
        this.acceptRequest = this.acceptRequest.bind(this);
        this.goBackToRequest = this.goBackToRequest.bind(this);
    }
    /**
    * @class AcceptLicense
    * @extends {Component}
    * @description componentWillMount
    */
    componentWillMount() {
        DataManager.selectLicense().then((response) => {
            this.setState(() => {
                return {
                    license: response.data.responseData,
                };
            });
        }).catch((error) => {
            throw new Error(error);
        });
        DataManager.selectWaitingLibraries(this.state.requestId).then((response) => {
            this.setState(() => {
                return {
                    licenseMissingLibraries: response.data.responseData,
                };
            });
        }).catch((error) => {
            throw new Error(error);
        });
        DataManager.selectWaitingComponents(this.state.requestId).then((response) => {
            this.setState(() => {
                return {
                    licenseMissingComponents: response.data.responseData,
                };
            });
        }).catch((error) => {
            throw new Error(error);
        });
    }
    /**
    * @param {any} e event
    * go back to request
    */
    acceptRequest(e) {
        e.preventDefault();
        e.stopPropagation();
        e.nativeEvent.stopImmediatePropagation();
        this.handleCloseLicense();
        this.setState(() => {
            return {
                displayFormLicense: 'none',
                displayLoader: 'block',
            };
        });
        /* eslint-disable max-len */
        Pack.acceptRequest(this.state.licenseMissingComponents, this.state.licenseMissingLibraries, this.state.productId, this.state.requestId).then((response) => {
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
                    };
                });
            }
        }).catch((error) => {
            throw new Error(error);
        });
    }
    /**
    * handleComponentSelect
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
                    TC_NAME: jar.TC_NAME,
                    TC_VERSION: jar.TC_VERSION,
                    TC_FILE_NAME: jar.TC_FILE_NAME,
                    licenseId: m,
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
    * handleLibrarySelect
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
                    TEMPLIB_FILE_NAME: jar.TEMPLIB_FILE_NAME,
                    TEMPLIB_ID: jar.TEMPLIB_ID,
                    TEMPLIB_TYPE: jar.TEMPLIB_TYPE,
                    TEMPLIB_NAME: jar.TEMPLIB_NAME,
                    TEMPLIB_VERSION: jar.TEMPLIB_VERSION,
                    TEMPLIB_PARENT: jar.TEMPLIB_PARENT,
                    licenseId: m,
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
    * handle open
    * @param {any} e event
    */
    handleOpenLicense(e) {
        e.preventDefault();
        e.stopPropagation();
        e.nativeEvent.stopImmediatePropagation();
        this.setState(() => {
            return {
                openLicense: true,
            };
        });
    }
    /**
    * handle close
    */
    handleCloseLicense() {
        this.setState(() => {
            return {
                openLicense: false,
            };
        });
    }
    /**
    * handle open
    * @param {any} e event
    */
    handleOpenLicenseReject(e) {
        e.preventDefault();
        e.stopPropagation();
        e.nativeEvent.stopImmediatePropagation();
        this.setState(() => {
            return {
                openLicenseReject: true,
            };
        });
    }
    /**
    * handle close
    */
    handleCloseLicenseReject() {
        this.setState(() => {
            return {
                openLicenseReject: false,
            };
        });
    }
    /**
    * handleOpenError
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
    * reload page
    */
    reloadPage() {
        window.location.reload();
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
                displayFormLicense: 'block',
            };
        });
    }
    /**
    * @class AcceptLicense
    * @extends {Component}
    * @description render method
    */
    render() {
        /* eslint-disable */
        let displayComponent = 'none';
        let displayLibrary = 'none';
        const component = [];
        const library = [];
        const license = [];
        const licenseList = this.state.license;
        let k = 0;
        const actionsLicense = [
            <FlatButton
                label="No"
                primary={true}
                onClick={this.handleCloseLicense}
            />,
            <FlatButton
                label="Yes"
                primary={true}
                keyboardFocused={true}
                onClick={this.acceptRequest}
            />,
        ];
        const actionsSuccess = [
            <Link to={'/'}>
                <FlatButton
                    label="Back to main"
                    primary={true}
                />
            </Link>,
            <Link to="/app/waitingLicense">
                <FlatButton
                    label="Waiting requests"
                    primary={true}
                    keyboardFocused={true}
                />
            </Link>,
        ];
        const actionsError = [
            <FlatButton
                label="Back"
                primary={true}
                onClick={this.goBackToRequest}
            />,
            <Link to="/app/waitingLicense">
                <FlatButton
                    label="Waiting requests"
                    primary={true}
                    keyboardFocused={true}
                />
            </Link>,
        ];
        const actionsLicenseReject = [
            <FlatButton
                label="No"
                primary={true}
                onClick={this.handleCloseLicenseReject}
            />,
            <FlatButton
                label="Yes"
                primary={true}
                keyboardFocused={true}
                onClick={this.rejectRequest}
            />,
        ];
        for (let i = 0; i < licenseList.length; i++) {
            license.push(
                <MenuItem
                    value={licenseList[i].LICENSE_ID}
                    key={licenseList[i].LICENSE_ID}
                    primaryText={licenseList[i].LICENSE_KEY}
                />
            );
        }
        if (this.state.licenseMissingComponents.length > 0) {
            let i = 0;
            const jars = this.state.licenseMissingComponents;
            let index = -1;
            for (i = 0; i < jars.length; i++) {
                index = index + 1;
                component.push(
                    <TableRow key={jars[i].ID}>
                        <TableRowColumn key={k}>{jars[i].TC_NAME}</TableRowColumn>
                        <TableRowColumn key={k + 1}>{jars[i].TC_VERSION}</TableRowColumn>
                        <TableRowColumn key={k + 1}>{jars[i].TC_FILE_NAME}</TableRowColumn>
                        <TableRowColumn key={k + 2}>
                            <SelectField
                                value={this.state.licenseMissingComponents[i].licenseId}
                                onChange={this.handleComponentSelect.bind(this, i)}
                                maxHeight={200}
                                floatingLabelText="Select a license"
                            >
                                {license}
                            </SelectField>
                        </TableRowColumn>
                    </TableRow>
                );
                k = k + 3;
            }
            displayComponent = 'block';
            /* eslint-enable */
        } else {
            displayComponent = 'none';
        }
        if (this.state.licenseMissingLibraries.length > 0) {
            /* eslint-disable */
            let i = 0;
            const jars = this.state.licenseMissingLibraries;
            for (i = 0; i < jars.length; i++) {
                library.push(
                    <TableRow key={jars[i].ID}>
                        <TableRowColumn key={k}>{jars[i].TEMPLIB_NAME}</TableRowColumn>
                        <TableRowColumn key={k + 1}>{jars[i].TEMPLIB_VERSION}</TableRowColumn>
                        <TableRowColumn key={k + 1}>{jars[i].TEMPLIB_FILE_NAME}</TableRowColumn>
                        <TableRowColumn key={k + 2}>
                            <SelectField
                                value={this.state.licenseMissingLibraries[i].licenseId}
                                onChange={this.handleLibrarySelect.bind(this, i)}
                                maxHeight={200}
                                style={styles.selectField}
                                floatingLabelText="Select a license"
                            >
                                {license}
                            </SelectField>
                        </TableRowColumn>
                    </TableRow>
                );
                k = k + 3;
            }
            displayLibrary = 'block';
            /* eslint-enable */
        } else {
            displayLibrary = 'none';
        }
        return (
            <div className="container-fluid">
                <h2 className="text-center">License Missing JARs</h2>
                <br />
                {/* eslint-disable */}
                <form onSubmit={this.handleOpenLicense} style={{ display: this.state.displayFormLicense }}>
                    <div style={{ display: displayComponent }}>
                        <Subheader style={styles.subHeader}>Components</Subheader>
                        <Table>
                            <TableHeader displaySelectAll={false} adjustForCheckbox={false}>
                                <TableRow key={0}>
                                    <TableHeaderColumn>Name</TableHeaderColumn>
                                    <TableHeaderColumn>Version</TableHeaderColumn>
                                    <TableHeaderColumn>File Name</TableHeaderColumn>
                                    <TableHeaderColumn>Select License</TableHeaderColumn>
                                </TableRow>
                            </TableHeader>
                            <TableBody displayRowCheckbox={false} ref={(c) => { this.com = c; }}>
                                {component}
                            </TableBody>
                        </Table>
                    </div>
                    <br />
                    <div style={{ display: displayLibrary }}>
                        <Subheader style={styles.subHeader}>Libraries</Subheader>
                        <Table>
                            <TableHeader displaySelectAll={false} adjustForCheckbox={false}>
                                <TableRow key={0}>
                                    <TableHeaderColumn>Name</TableHeaderColumn>
                                    <TableHeaderColumn>Version</TableHeaderColumn>
                                    <TableHeaderColumn>File Name</TableHeaderColumn>
                                    <TableHeaderColumn>Select License</TableHeaderColumn>
                                </TableRow>
                            </TableHeader>
                            <TableBody displayRowCheckbox={false}>
                                {library}
                            </TableBody>
                        </Table>
                    </div>
                    <br />
                    <Link to={'/app/rejectLicense?productId=' + this.state.productId + '&requestId=' + this.state.requestId}>
                        <RaisedButton
                            type="button"
                            label="Reject"
                            style={styles.saveButtonStyle}
                            labelColor='#ffffff'
                            backgroundColor='#BF360C'
                        />
                    </Link>
                    <RaisedButton
                        type="submit"
                        label="Accept and Save"
                        style={styles.saveButtonStyle}
                        labelColor='#ffffff'
                        backgroundColor='#2196F3'
                        disabled={this.state.buttonState}
                    />
                </form>
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
                    title="Accept"
                    actions={actionsLicense}
                    modal={false}
                    open={this.state.openLicense}
                    onRequestClose={this.handleCloseLicense}
                >
                    Are you sure to accept and save this licenses ?
                </Dialog>
                <Dialog
                    title="Reject"
                    actions={actionsLicenseReject}
                    modal={false}
                    open={this.state.openLicenseReject}
                    onRequestClose={this.handleCloseLicenseReject}
                >
                    Are you sure to reject this licenses request ?
                </Dialog>
                <Dialog
                    title="Success"
                    actions={actionsSuccess}
                    modal={false}
                    open={this.state.openSuccess}
                    onRequestClose={this.reloadPage}
                >
                    The request has been successfully accepted
                </Dialog>
                <Dialog
                    title="Error"
                    actions={actionsError}
                    modal={false}
                    open={this.state.openError}
                    onRequestClose={this.goBackToRequest}
                >
                    Error occurs when accepting the request.
                </Dialog>
                {/* eslint-enable */}
            </div>
        );
    }
}

export default AcceptLicense;
