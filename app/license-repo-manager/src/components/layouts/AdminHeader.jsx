import React, { Component } from 'react';
import { Link } from 'react-router';
import LibraryBooks from 'material-ui/svg-icons/av/library-books';
import IconButton from 'material-ui/IconButton';
import IconMenu from 'material-ui/IconMenu';
import MenuItem from 'material-ui/MenuItem';
import Person from 'material-ui/svg-icons/social/person';
import Notifications from 'material-ui/svg-icons/social/notifications';
import ContentCopy from 'material-ui/svg-icons/content/content-copy';
import Description from 'material-ui/svg-icons/action/description';
import Repository from '../../services/database/Repository';
import LibraryRequest from '../../services/database/LibraryRequest';
import ValidateUser from '../../services/authentication/ValidateUser';
import DataManager from '../../services/msf4j/DataManager';
import logo from '../../assets/images/logo-inverse.svg';

/**
* @class AdminHeader
* @extends {Component}
* @description Admin Header Component
*/
class AdminHeader extends Component {
    /**
    * constructor
    * @param {any} props props for constructor
    */
    constructor(props) {
        super(props);
        this.state = {
            waitingRequests: 0,
            waitingLibraryRequests: 0,
            waitingLicenseRequests: 0,
            pendingRequests: 0,
            pendingLibrary: 0,
            userDetails: props.userDetails,//eslint-disable-line
        };
        this.setPendingRequests = this.setPendingRequests.bind(this);
        this.setPendingLibrary = this.setPendingLibrary.bind(this);
    }
    /**
    * @class AdminHeader
    * @extends {Component}
    * @description Admin Header Component componentWillMount
    */
    componentWillMount() {
        ValidateUser.isValidUser().then((response) => {
            this.setState(() => {
                return {
                    userDetails: response,
                };
            });
            this.setPendingRequests(response.userEmail);
            this.setPendingLibrary(response.userEmail);
        });
        if (this.props.userDetails.isRepositoryAdmin) {//eslint-disable-line
            Repository.selectWaitingRequests().then((response) => {
                this.setState(() => {
                    return {
                        waitingRequests: response.length,
                    };
                });
            });
        }
        LibraryRequest.selectLibraryWaitingRequests().then((response) => {
            this.setState(() => {
                return {
                    waitingLibraryRequests: response.length,
                };
            });
        });
        DataManager.selectWaitingLicenseRequests().then((response) => {
            this.setState(() => {
                return {
                    waitingLicenseRequests: response.data.responseData.length,
                };
            });
        }).catch((error) => {
            throw new Error(error);
        });
    }
    /**
    * setPendingRequests
    * @param {String} requestBy requestBy
    */
    setPendingRequests(requestBy) {
        Repository.selectDataFromRequestBy(requestBy).then((response) => {
            this.setState(() => {
                return {
                    pendingRequests: response.length,
                };
            });
        });
    }
    /**
    * setPendingRequests
    * @param {String} requestBy requestBy
    */
    setPendingLibrary(requestBy) {
        LibraryRequest.selectLibraryRequestWaitingAndRequestBy(requestBy).then((response) => {
            this.setState(() => {
                return {
                    pendingLibrary: response.length,
                };
            });
        });
    }
    /**
    * logout
    */
    logout() {
        window.close('', '_self');
    }
    /**
    * @class AdminHeader
    * @extends {Component}
    * @description render method
    */
    render() {
        const props = this.props;
        return (
            <nav className="navbar navbar-default" style={{ minHeight: '50px', height: '50px' }}>
                {/* eslint-disable max-len */}
                <div className="container-fluid">
                    <div className="navbar-header">
                        <img id="logo" style={{ height: '30px' }} src={logo} alt="wso2" /><strong style={{ color: 'white', fontSize: '20px', marginTop: '1px' }}>License and Repository Manager</strong>
                    </div>

                    <div className="collapse navbar-collapse" id="bs-example-navbar-collapse-1">
                        <ul className="nav navbar-nav" />
                        <ul className="nav navbar-nav navbar-right">
                            <li>
                                <Link style={{ paddingTop: 5, paddingBottom: 0 }} to={'/app/pendingLibrary'} id="pendingRequests">
                                    {(this.state.pendingLibrary === 0) ? null : <IconButton style={{ paddingTop: 0, paddingBottom: 0 }} tooltip="Requested Library Requests"><Notifications style={{ paddingTop: 0, paddingBottom: 0 }} /></IconButton>}
                                    <span className="badge">
                                        {(this.state.pendingLibrary === 0) ? null : this.state.pendingLibrary }
                                    </span>
                                </Link>
                            </li>
                            <li>
                                <Link style={{ paddingTop: 5, paddingBottom: 0 }} to={'/app/pendingRepository'} id="pendingRequests">
                                    {(this.state.pendingRequests === 0) ? null : <IconButton style={{ paddingTop: 0, paddingBottom: 0 }} tooltip="Requested Repository Requests"><Notifications style={{ paddingTop: 0, paddingBottom: 0 }} /></IconButton>}
                                    <span className="badge">
                                        {(this.state.pendingRequests === 0) ? null : this.state.pendingRequests }
                                    </span>
                                </Link>
                            </li>
                            <li>
                                <Link style={{ paddingTop: 5, paddingBottom: 0 }} to={'/app/waitingLibrary'} >
                                    {(this.state.waitingLibraryRequests === 0) ? null : <IconButton style={{ paddingTop: 0, paddingBottom: 0 }} tooltip="Library Requests"><ContentCopy style={{ paddingTop: 0, paddingBottom: 0 }} /></IconButton>}
                                    <span className="badge">
                                        {(this.state.waitingLibraryRequests === 0) ? null : this.state.waitingLibraryRequests }
                                    </span>
                                </Link>
                            </li>
                            <li>
                                <Link style={{ paddingTop: 5, paddingBottom: 0 }} to={'/app/waitingRepository'} id="pendingRequests">
                                    {(this.state.waitingRequests === 0) ? null : <IconButton style={{ paddingTop: 0, paddingBottom: 0 }} tooltip="Repository Requests"><LibraryBooks style={{ paddingTop: 0, paddingBottom: 0 }} /></IconButton>}
                                    <span className="badge">
                                        {(this.state.waitingRequests === 0) ? null : this.state.waitingRequests }
                                    </span>
                                </Link>
                            </li>
                            <li>
                                <Link style={{ paddingTop: 5, paddingBottom: 0 }} to={'/app/waitingLicense'} id="pendingRequests">
                                    {(this.state.waitingLicenseRequests === 0) ? null : <IconButton style={{ paddingTop: 0, paddingBottom: 0 }} tooltip="License Requests"><Description style={{ paddingTop: 0, paddingBottom: 0 }} /></IconButton>}
                                    <span className="badge">
                                        {(this.state.waitingLicenseRequests === 0) ? null : this.state.waitingLicenseRequests }
                                    </span>
                                </Link>
                            </li>
                            <li>
                                <IconMenu
                                    iconButtonElement={<IconButton><Person style={{ paddingTop: 0, paddingBottom: 0 }} /></IconButton>}
                                    anchorOrigin={{ horizontal: 'right', vertical: 'top' }}
                                    targetOrigin={{ horizontal: 'right', vertical: 'bottom' }}
                                >
                                    <MenuItem primaryText={props.userDetails.userEmail} style={{ color: '#ffffff' }} />
                                    <MenuItem primaryText="Sign out" onClick={this.logout} />
                                </IconMenu>
                            </li>
                        </ul>
                    </div>
                </div>
            </nav>
        );
    }
}

export default AdminHeader;
