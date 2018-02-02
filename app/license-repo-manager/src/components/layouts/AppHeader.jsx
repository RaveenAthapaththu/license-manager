import React, { Component } from 'react';
import { Link } from 'react-router';
import Notifications from 'material-ui/svg-icons/social/notifications';
import Person from 'material-ui/svg-icons/social/person';
import IconButton from 'material-ui/IconButton';
import IconMenu from 'material-ui/IconMenu';
import MenuItem from 'material-ui/MenuItem';
import Repository from '../../services/database/Repository';
import LibraryRequest from '../../services/database/LibraryRequest';
import ValidateUser from '../../services/authentication/ValidateUser';
import logo from '../../assets/images/logo-inverse.svg';

/**
* @class AppHeader
* @extends {Component}
* @description Normal user header
*/
class AppHeader extends Component {
    /**
    * constructor
    */
    constructor() {
        super();
        this.state = {
            pendingRequests: 0,
            pendingLibrary: 0,
            userDetails: [],
        };
        this.setPendingRequests = this.setPendingRequests.bind(this);
        this.setPendingLibrary = this.setPendingLibrary.bind(this);
        this.logout = this.logout.bind();
    }
    /**
    * @class AppHeader
    * @extends {Component}
    * @description Normal user header
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
    * @class AppHeader
    * @extends {Component}
    * @description Normal user header
    */
    render() {
        const props = this.props;
        return (
            <nav className="navbar navbar-default" style={{ minHeight: '50px', height: '50px' }}>
                {/* eslint-disable */}
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
                                <IconMenu
                                    iconButtonElement={<IconButton><Person style={{ paddingTop: 0, paddingBottom: 0 }} /></IconButton>}
                                    anchorOrigin={{ horizontal: 'right', vertical: 'top' }}
                                    targetOrigin={{ horizontal: 'right', vertical: 'bottom' }}
                                >
                                    <MenuItem primaryText={props.userDetails.userEmail} disabled={true} style={{ color: '#ffffff' }} />
                                    <MenuItem primaryText="Sign out" onClick={this.logout} />
                                </IconMenu>
                            </li>
                        </ul>
                    </div>
                </div>
                {/* eslint-enable */}
            </nav>
        );
    }
}

export default AppHeader;
