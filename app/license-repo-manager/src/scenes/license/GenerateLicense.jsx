import React, { Component } from 'react';
import { Link, hashHistory } from 'react-router';
import {
    Table,
    TableHeader,
    TableHeaderColumn,
    TableRow,
    TableBody,
    TableRowColumn,
} from 'material-ui/Table';
import {
    Step,
    Stepper,
    StepLabel,
} from 'material-ui/Stepper';
import { List, ListItem } from 'material-ui/List';
import TextField from 'material-ui/TextField';
import RaisedButton from 'material-ui/RaisedButton';
import Dialog from 'material-ui/Dialog';
import FlatButton from 'material-ui/FlatButton';
import CircularProgress from 'material-ui/CircularProgress';
import SelectField from 'material-ui/SelectField';
import MenuItem from 'material-ui/MenuItem';
import ErrorIcon from 'material-ui/svg-icons/alert/error';
import { red500 } from 'material-ui/styles/colors';
import Pack from '../../services/msf4j/Pack';
import DataManager from '../../services/msf4j/DataManager';
import styles from '../../mystyles';
import textFile from '../../assets/images/txt-file.png';

/**
* @class NameErrorJarsLicense
* @extends {Component}
* @description Get user details
*/
class GenerateLicense extends Component {
    /**
    * @class NameErrorJarsLicense
    * @extends {Component}
    * @param {any} props props for constructor
    * @description Sample React component
    */
    constructor(props) {
        super(props);
        this.state = {
            packName: props.location.query.packName,//eslint-disable-line
            userEmail: props.location.query.userEmail,//eslint-disable-line
            nameMissingJars: [],
            nameJars: [],
            open: false,
            openError: false,
            openSuccess: false,
            openLicense: false,
            errorIcon: '',
            displayProgress: 'block',
            displayForm: 'none',
            displayLoader: 'none',
            displayFormLicense: 'none',
            displayDownload: 'none',
            displayComponentTable: 'none',
            displayLibraryTable: 'none',
            displayErrorBox: 'none',
            buttonState: false,
            header: 'Please add Name and Version for following JARs',
            licenseMissingComponents: [],
            licenseMissingLibraries: [],
            license: [],
            stepIndex: 1,
        };
        this.handleOpen = this.handleOpen.bind(this);
        this.handleClose = this.handleClose.bind(this);
        this.handleOpenError = this.handleOpenError.bind(this);
        this.handleCloseError = this.handleCloseError.bind(this);
        this.handleOpenSuccess = this.handleOpenSuccess.bind(this);
        this.handleCloseSuccess = this.handleCloseSuccess.bind(this);
        this.handleNext = this.handleNext.bind(this);
        this.handlePrev = this.handlePrev.bind(this);
        this.reloadPage = this.reloadPage.bind(this);
        this.enterJars = this.enterJars.bind(this);
        this.setName = this.setName.bind(this);
        this.setVersion = this.setVersion.bind(this);
        this.generateLicense = this.generateLicense.bind(this);
        this.backToMain = this.backToMain.bind(this);
    }
    /**
    * @class NameErrorJarsLicense
    * @extends {Component}
    * @description componentWillMount
    */
    componentWillMount() {
        Pack.getNameMissingJars().then((response) => {
            if (response.data.responseData.length === 0) {
                Pack.enterJars(response.data.responseData, this.state.userEmail).then((responseNxt) => {
                    if (responseNxt.data.responseType === 'Done') {
                        if (responseNxt.data.component.length === 0 && responseNxt.data.library.length === 0) {
                            this.setState(() => {
                                return {
                                    displayDownload: 'block',
                                    displayFormLicense: 'none',
                                    displayLoader: 'none',
                                    displayProgress: 'none',
                                    header: 'Download License Here',
                                    licenseMissingComponents: responseNxt.data.component,
                                    licenseMissingLibraries: responseNxt.data.library,
                                    stepIndex: 3,
                                };
                            });
                        } else if (responseNxt.data.library.length > 0 || responseNxt.data.component.length > 0) {
                            this.setState(() => {
                                return {
                                    displayErrorBox: 'block',
                                    header: 'Error! Missing License',
                                    displayFormLicense: 'none',
                                    displayLoader: 'none',
                                    displayProgress: 'none',
                                    errorIcon: <ErrorIcon color={red500} />,
                                    licenseMissingComponents: [],
                                    licenseMissingLibraries: [],
                                    stepIndex: 2,
                                };
                            });
                        } else {
                            this.setState(() => {
                                return {
                                    displayFormLicense: 'block',
                                    displayLoader: 'none',
                                    displayProgress: 'none',
                                    header: 'Select License for these JARs',
                                    licenseMissingComponents: responseNxt.data.component,
                                    licenseMissingLibraries: responseNxt.data.library,
                                    stepIndex: 2,
                                };
                            });
                        }
                    } else {
                        this.handleOpenError();
                        this.setState(() => {
                            return {
                                displayFormLicense: 'none',
                                displayProgress: 'none',
                                displayLoader: 'none',
                                header: 'Unknown Error occured.',
                            };
                        });
                    }
                }).catch((error) => {
                    throw new Error(error);
                });
            } else {
                this.setState(() => {
                    return {
                        nameMissingJars: response.data.responseData,
                        nameJars: response.data.responseData,
                        displayProgress: 'none',
                        displayForm: 'block',
                    };
                });
            }
        }).catch((error) => {
            throw new Error(error);
        });

        DataManager.selectLicense().then((response) => {
            this.setState(() => {
                return {
                    license: response.data.responseData,
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
    setVersion(e) {
        const id = parseInt(e.target.name, 10);
        const versionValue = e.target.value;
        const nameJarsList = this.state.nameJars.map((jar, i) => {
            if (i !== id) {
                return jar;
            } else {
                const jarFile = {
                    id: i,
                    name: jar.name,
                    version: versionValue,
                };
                return (jarFile);
            }
        });
        this.setState(() => {
            return {
                nameJars: nameJarsList,
            };
        });
    }
    /**
    * @param {any} e event
    * go back to request
    */
    setName(e) {
        const id = parseInt(e.target.name, 10);
        const nameValue = e.target.value;
        const nameJarsList = this.state.nameJars.map((jar, i) => {
            if (i !== id) {
                return jar;
            } else {
                const jarFile = {
                    id: i,
                    name: nameValue,
                    version: jar.version,
                };
                return (jarFile);
            }
        });
        this.setState(() => {
            return {
                nameJars: nameJarsList,
            };
        });
    }
    /**
    * @param {any} e event
    * go back to request
    */
    enterJars(e) {
        e.preventDefault();
        e.stopPropagation();
        e.nativeEvent.stopImmediatePropagation();
        this.handleClose();
        this.setState(() => {
            return {
                displayForm: 'none',
                displayLoader: 'block',
            };
        });
        Pack.enterJars(this.state.nameJars, this.state.userEmail).then((response) => {
            if (response.data.responseType === 'Done') {
                if (response.data.component.length === 0 && response.data.library.length === 0) {
                    this.setState(() => {
                        return {
                            displayDownload: 'block',
                            displayFormLicense: 'none',
                            displayLoader: 'none',
                            displayProgress: 'none',
                            header: 'Download License Here',
                            licenseMissingComponents: response.data.component,
                            licenseMissingLibraries: response.data.library,
                            stepIndex: 3,
                        };
                    });
                } else if (response.data.library.length > 0 || response.data.component.length > 0) {
                    this.setState(() => {
                        return {
                            displayFormLicense: 'none',
                            displayLoader: 'none',
                            displayProgress: 'none',
                            displayErrorBox: 'block',
                            header: 'Error! Missing License',
                            errorIcon: <ErrorIcon color={red500} />,
                            licenseMissingComponents: response.data.component,
                            licenseMissingLibraries: response.data.library,
                            stepIndex: 2,
                        };
                    });
                } else {
                    this.setState(() => {
                        return {
                            displayFormLicense: 'block',
                            displayLoader: 'none',
                            displayProgress: 'none',
                            header: 'Select License for these JARs',
                            licenseMissingComponents: response.data.component,
                            licenseMissingLibraries: response.data.library,
                            stepIndex: 2,
                        };
                    });
                }
            } else {
                this.handleOpenError();
                this.setState(() => {
                    return {
                        displayFormLicense: 'none',
                        displayLoader: 'none',
                        header: '',
                        stepIndex: 3,
                    };
                });
            }
        }).catch((error) => {
            throw new Error(error);
        });
    }
    /**
    * @param {any} e event
    * go back to request
    */
    generateLicense(e) {
        e.preventDefault();
        e.stopPropagation();
        e.nativeEvent.stopImmediatePropagation();
        this.setState(() => {
            return {
                displayForm: 'none',
                displayDownload: 'none',
                displayLoader: 'block',
            };
        });
        /* eslint-disable */
        Pack.getLicense().then((response) => {
            Pack.dowloadLicense().then((responseFile) => {
                const url = window.URL.createObjectURL(new Blob([responseFile.data]));
                const link = document.createElement('a');
                const fileNameLength = this.state.packName.length;
                const fileName = 'License(' + this.state.packName.substring(0, fileNameLength - 4) + ').TXT';
                link.href = url;
                link.setAttribute('download', fileName);
                document.body.appendChild(link);
                link.click();
                this.setState(() => {
                    return {
                        displayForm: 'none',
                        displayDownload: 'block',
                        displayLoader: 'none',
                    };
                });
            }).catch((error) => {
                throw new Error(error);
            });
        }).catch((error) => {
            throw new Error(error);
        });
        /* eslint-enable */
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
    * handle Next
    */
    handleNext() {
        let stepIndexNo = this.state.stepIndex;
        stepIndexNo += 1;
        this.setState(() => {
            return {
                stepIndex: stepIndexNo,
            };
        });
    }
    /**
    * handle Prev
    */
    handlePrev() {
        let stepIndexNo = this.state.stepIndex;
        stepIndexNo -= 1;
        if (this.state.stepIndex > 0) {
            this.setState(() => {
                return {
                    stepIndex: stepIndexNo,
                };
            });
        }
    }
    /**
    * reload page
    */
    reloadPage() {
        window.location.reload();
    }
    /**
    * reload page
    */
    backToMain() {
        hashHistory.push('/');
    }
    /**
    * @class WaitingRequests
    * @extends {Component}
    * @description Sample React component
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
                onClick={this.enterJars}
            />,
        ];
        const actionsError = [
            <Link to={'/app/requestLicense'}>
                <FlatButton
                    label="Back"
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
        const table = [];
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
                    value={licenseList[i].LICENSE_ID}
                    key={licenseList[i].LICENSE_ID}
                    primaryText={licenseList[i].LICENSE_NAME}
                />
            );
        }
        /* eslint-enable */
        if (this.state.nameMissingJars.length > 0) {
            /* eslint-disable */
            let i = 0;
            const jars = this.state.nameMissingJars;
            for (i = 0; i < jars.length; i++) {
                table.push(
                    <TableRow key={jars[i].ID}>
                        <TableRowColumn key={k}>{jars[i].id}</TableRowColumn>
                        <TableRowColumn key={k + 1}>{jars[i].name}</TableRowColumn>
                        <TableRowColumn key={k + 2}>
                            <TextField
                                name={jars[i].id.toString()}
                                onChange={this.setName}
                                hintText='Enter name'
                            />
                        </TableRowColumn>
                        <TableRowColumn key={k + 3}>
                            <TextField
                                name={jars[i].id.toString()}
                                onChange={this.setVersion}
                                hintText='Enter version'
                            />
                        </TableRowColumn>
                    </TableRow>
                );
                k = k + 4;
            }
            /* eslint-enable */
        }
        if (this.state.licenseMissingComponents.length > 0) {
            /* eslint-disable */
            let i = 0;
            const jars = this.state.licenseMissingComponents;
            let index = -1;
            for (i = 0; i < jars.length; i++) {
                index = index + 1;
                component.push(
                    <TableRow key={jars[i].ID}>
                        <TableRowColumn key={k}>{jars[i].id}</TableRowColumn>
                        <TableRowColumn key={k + 1}>{jars[i].name}</TableRowColumn>
                        <TableRowColumn key={k + 2}>
                            <SelectField
                                value={this.state.licenseMissingComponents[i].license}
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
                        <TableRowColumn key={k}>{jars[i].id}</TableRowColumn>
                        <TableRowColumn key={k + 1}>{jars[i].name}</TableRowColumn>
                        <TableRowColumn key={k + 2}>
                            <SelectField
                                value={this.state.licenseMissingLibraries[i].license}
                                onChange={this.handleLibrarySelect.bind(this, i)}
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
            displayLibrary = 'block';
            /* eslint-enable */
        } else {
            displayLibrary = 'none';
        }
        return (
            <div className="container-fluid">
                <h2 className="text-center">{this.state.header}</h2>
                <br />
                <Stepper activeStep={this.state.stepIndex}>
                    <Step>
                        <StepLabel>Upload Pack</StepLabel>
                    </Step>
                    <Step>
                        <StepLabel>Check JAR Names</StepLabel>
                    </Step>
                    <Step>
                        <StepLabel icon={this.state.errorIcon}>Check Missing License</StepLabel>
                    </Step>
                    <Step>
                        <StepLabel>Download License</StepLabel>
                    </Step>
                </Stepper>
                <form onSubmit={this.handleOpen} style={{ display: this.state.displayForm }}>
                    <Table>
                        <TableHeader displaySelectAll={false} adjustForCheckbox={false}>
                            <TableRow key={0}>
                                <TableHeaderColumn>ID</TableHeaderColumn>
                                <TableHeaderColumn>JAR</TableHeaderColumn>
                                <TableHeaderColumn>Input Name</TableHeaderColumn>
                                <TableHeaderColumn>Input Version</TableHeaderColumn>
                            </TableRow>
                        </TableHeader>
                        <TableBody displayRowCheckbox={false}>
                            {table}
                        </TableBody>
                    </Table>
                    <br />
                    <div className='test' style={styles.downloadArea}>
                        <RaisedButton
                            type="button"
                            label="Cancel"
                            onClick={this.reloadPage}
                            style={styles.saveButtonStyle}
                            labelColor='#ffffff'
                            backgroundColor='#BDBDBD'
                        />
                        <RaisedButton
                            type="submit"
                            label="Save"
                            style={styles.saveButtonStyle}
                            labelColor='#ffffff'
                            backgroundColor='#2196F3'
                            disabled={this.state.buttonState}
                        />
                    </div>
                </form>
                {/* eslint-disable max-len */}
                <form onSubmit={this.generateLicense} style={{ display: this.state.displayDownload }}>
                    <br />
                    <br />
                    <div style={styles.downloadArea} >
                        <img src={textFile} style={styles.textFile} alt="" />
                        <br />
                        <span>
                            <b>
                                {'License(' + this.state.packName.substring(0, this.state.packName.length - 4) + ').TXT'}
                            </b>
                        </span>
                    </div>
                    <br />
                    <RaisedButton
                        type="submit"
                        label="Download"
                        style={styles.buttonStyle}
                        labelColor='#ffffff'
                        backgroundColor='#2196F3'
                        disabled={this.state.buttonState}
                    />
                    &nbsp;
                    <RaisedButton
                        type="button"
                        label="Back to main"
                        onClick={this.backToMain}
                        style={styles.buttonStyle}
                        labelColor='#ffffff'
                        backgroundColor='#BDBDBD'
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

                <div className="container-fluid" style={{ display: this.state.displayErrorBox }}>
                    <br /><br /><br />
                    <List style={styles.list}>
                        <ListItem
                            style={styles.listItem}
                            leftIcon={<ErrorIcon color={red500} />}
                            primaryText="Error! license missing libraries exists"
                            secondaryText="Send requests for approve license! if they approve it, license file send back to you via email"
                        />
                    </List>
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
                    Are you sure to save this data ?
                </Dialog>
            </div>
        );
    }
}

export default GenerateLicense;
