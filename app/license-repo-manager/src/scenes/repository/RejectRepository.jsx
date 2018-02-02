import React, { Component } from 'react';
import { Link } from 'react-router';
import CircularProgress from 'material-ui/CircularProgress';
import RaisedButton from 'material-ui/RaisedButton';
import Dialog from 'material-ui/Dialog';
import FlatButton from 'material-ui/FlatButton';
import Repository from '../../services/database/Repository';
import GitHubRepositoryCreation from '../../services/bpmn/GitHubRepositoryCreation';
import ValidateUser from '../../services/authentication/ValidateUser';
import styles from '../../mystyles';

/**
* @class RejectRepository
* @extends {Component}
* @description Reject repository request
*/
class RejectRepository extends Component {
    /**
    * @class RejectRepository
    * @extends {Component}
    * @param {any} props props for constructor
    * @description constructor
    */
    constructor(props) {
        super(props);
        this.state = {
            repositoryId: props.location.query.repositoryId,// eslint-disable-line
            repositoryDetails: null,
            displayFieldset: 'block',
            displayAlrearyAccept: 'none',
            displayProgress: 'none',
            userDetails: [],
            open: false,
            openError: false,
            openSuccess: false,
        };
        this.rejectRequest = this.rejectRequest.bind(this);
        this.handleOpen = this.handleOpen.bind(this);
        this.handleClose = this.handleClose.bind(this);
        this.handleOpenError = this.handleOpenError.bind(this);
        this.handleCloseError = this.handleCloseError.bind(this);
        this.handleOpenSuccess = this.handleOpenSuccess.bind(this);
        this.handleCloseSuccess = this.handleCloseSuccess.bind(this);
        this.reloadPage = this.reloadPage.bind(this);
        this.goBackToRequest = this.goBackToRequest.bind(this);
    }
    /**
    * @class RejectRepository
    * @extends {Component}
    * @description componentWillMount
    */
    componentWillMount() {
        Repository.selectDataFromId(this.state.repositoryId).then((response) => {
            this.setState(() => {
                if ((response[0].REPOSITORY_ACCEPTED_BY === null) && (response[0].REPOSITORY_DEACTIVATED_BY === null)) {
                    return {
                        displayFieldset: 'block',
                        displayAlrearyAccept: 'none',
                        displayErrorBox: 'none',
                        displaySuceessBox: 'none',
                        repositoryDetails: response[0],
                    };
                } else {
                    return {
                        displayFieldset: 'none',
                        displayAlrearyAccept: 'block',
                        displayErrorBox: 'none',
                        displaySuceessBox: 'none',
                        repositoryDetails: response[0],
                    };
                }
            });
        });
        ValidateUser.getUserDetails().then((response) => {
            this.setState(() => {
                return {
                    userDetails: response,
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
    * @param {any} e event
    * accept
    * @returns {Promise} promise
    */
    rejectRequest(e) {
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
        const reasonForRejecting = this.textReasonForRejecting.value.toString();
        const taskId = this.state.repositoryDetails.REPOSITORY_BPMN_TASK_ID;
        const repoId = this.state.repositoryDetails.REPOSITORY_ID;
        try {
            GitHubRepositoryCreation.rejectRequest(reasonForRejecting, repoId, taskId).then((response) => {
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
            });
        } catch (err) {
            this.setState(() => {
                return {
                    displaySuceessBox: 'none',
                    displayAlrearyAccept: 'none',
                    displayFieldset: 'none',
                    displayErrorBox: 'block',
                };
            });
        }
        return false;
    }
    /**
    * reload page
    */
    reloadPage() {
        window.location.reload();
    }
    /**
    * @class RejectRepository
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
                onClick={this.rejectRequest}
            />,
        ];
        const actionsError = [
            <FlatButton
                label="Back"
                primary={true}
                onClick={this.goBackToRequest}
            />,
            <Link to="/app/waitingRequests">
                <FlatButton
                    label="Waiting requests"
                    primary={true}
                    keyboardFocused={true}
                />
            </Link>,
        ];
        const actionsSuccess = [
            <Link to={'/'}>
                <FlatButton
                    label="Back to main"
                    primary={true}
                />
            </Link>,
            <Link to="/app/waitingRequests">
                <FlatButton
                    label="Waiting requests"
                    primary={true}
                    keyboardFocused={true}
                />
            </Link>,
        ];
        return (
            <form className="form-horizontal" onSubmit={this.handleOpen} >
                <h2 className="text-center">Request GitHub Repository Here</h2>
                <fieldset style={{ display: this.state.displayFieldset }} >
                    <br />
                    <div className="form-group">
                        <label
                            htmlFor="textReasonForRejecting"
                            className="col-lg-2 control-label"
                        >
                            Reason for rejecting :
                        </label>
                        <div className="col-lg-10">
                            <textarea
                                className="form-control"
                                rows="3"
                                ref={(c) => { this.textReasonForRejecting = c; }}
                                placeholder="Reason for rejecting"
                                required
                            />
                        </div>
                    </div>
                    <div className="form-group">
                        <div className="col-lg-2" />
                        <div className="col-lg-10">
                            <RaisedButton type="submit" label="Reject" style={styles.buttonStyle} labelColor='#ffffff' backgroundColor='#BF360C' disabled={this.state.buttonState} />
                                &nbsp;&nbsp;&nbsp;
                            <RaisedButton type="button" label="Cancel" style={styles.buttonStyle} labelColor='#ffffff' backgroundColor='#BDBDBD' onClick={this.reloadPage} />
                        </div>
                    </div>
                </fieldset>

                <div
                    className="alert alert-dismissible alert-warning"
                    style={{ display: this.state.displayAlrearyAccept }}
                >
                    <button type="button" className="close" data-dismiss="alert">&times;</button>
                    {/* eslint-disable */}
                    <strong>This repository request already {((this.state.repositoryDetails !== null))? ((this.state.repositoryDetails.REPOSITORY_ACCEPTED_BY === null)?(' deactivated by ' + this.state.repositoryDetails.REPOSITORY_DEACTIVATED_BY + ' because of ' + this.state.repositoryDetails.REPOSITORY_DEACTIVATED_REASON):(' accepted by ' + this.state.repositoryDetails.REPOSITORY_ACCEPTED_BY)):' '}</strong>
                    {/* eslint-enable */}
                </div>

                <Dialog
                    title="Success"
                    actions={actionsSuccess}
                    modal={false}
                    open={this.state.openSuccess}
                    onRequestClose={this.reloadPage}
                >
                    The request has been successfully rejected
                </Dialog>

                <Dialog
                    title="Error"
                    actions={actionsError}
                    modal={false}
                    open={this.state.openError}
                    onRequestClose={this.goBackToRequest}
                >
                    Error occurs when rejecting the request.
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
                    Are you sure to reject this request ?
                </Dialog>
            </form>
        );
    }
}

export default RejectRepository;
