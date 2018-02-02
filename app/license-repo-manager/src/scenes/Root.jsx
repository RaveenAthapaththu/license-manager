import React, { Component } from 'react';
import { hashHistory } from 'react-router';
import MuiThemeProvider from 'material-ui/styles/MuiThemeProvider';
import getMuiTheme from 'material-ui/styles/getMuiTheme';
import darkBaseTheme from 'material-ui/styles/baseThemes/darkBaseTheme';
import AppHeader from '../components/layouts/AppHeader';
import AdminHeader from '../components/layouts/AdminHeader';
import LeftNav from '../components/navs/LeftNav';
import AdminLeftNav from '../components/navs/AdminLeftNav';
import ValidateUser from '../services/authentication/ValidateUser';

/**
* @class Root
* @extends {Component}
* @description Root component
*/
class Root extends Component {
    /**
    * constructor
    * @param {any} props props
    */
    constructor(props) {
        super(props);
        this.state = {
            isAdminUser: null,
            isRepositoryAdmin: null,
            isLibraryAdmin: null,
            isValid: null,
            displayChildren: 'block',
            displayNav: 'block',
            displayHeader: 'block',
            userDetails: [{ isValid: false, userDetails: null }],
        };
    }
    /**
    * @class Root
    * @extends {Component}
    * @description componentWillMount
    */
    componentWillMount() {
        const adminPages = ['/app/acceptRepository', '/app/waitingRequests'];
        const adminPagesLibrary = ['/app/acceptLibrary'];
        const props = this.props;
        ValidateUser.getUserDetails().then((response) => {
            if (response.isValid) {
                this.setState(() => {
                    return {
                        isAdminUser: response.isAnyAdmin,
                        isValidUser: response.isValid,
                        isRepositoryAdmin: response.isRepositoryAdmin,
                        isLibraryAdmin: response.isLibraryAdmin,
                        userDetails: response,
                    };
                });
            } else {
                hashHistory.push('/');
            }
            if (adminPages.indexOf(props.location.pathname) !== -1 && !this.state.isRepositoryAdmin) {
                hashHistory.push('/');
            } else if (adminPages.indexOf(props.location.pathname) !== -1 && this.state.isRepositoryAdmin) {
                this.setState(() => {
                    return {
                        displayChildren: 'block',
                        displayNav: 'block',
                        displayHeader: 'block',
                    };
                });
            }
            if (adminPagesLibrary.indexOf(props.location.pathname) !== -1 && !this.state.isLibraryAdmin) {
                hashHistory.push('/');
            } else if (adminPagesLibrary.indexOf(props.location.pathname) !== -1 && this.state.isLibraryAdmin) {
                this.setState(() => {
                    return {
                        displayChildren: 'block',
                        displayNav: 'block',
                        displayHeader: 'block',
                    };
                });
            }
        });
    }
    /**
    * @class Root
    * @extends {Component}
    * @description render
    */
    render() {
        const props = this.props;
        /* eslint-disable max-len */
        if (this.state.isAdminUser) {
            return (
                <MuiThemeProvider muiTheme={getMuiTheme(darkBaseTheme)}>
                    <div className="container-fluid">
                        <div className="row" id="header">
                            <div className="col-sm-12" style={{ display: this.state.displayHeader, paddingTop: '0px' }} >
                                <AdminHeader userDetails={this.state.userDetails} />
                            </div>
                        </div>
                        <div className="row">
                            <div className="col-sm-2" style={{ display: this.state.displayNav, paddingLeft: '0px', height: '100vh', marginTop: '-8px' }}>
                                {(this.state.isAdminUser === true) ? <AdminLeftNav /> : <LeftNav /> }
                            </div>
                            <div className="col-sm-10" style={{ display: this.state.displayChildren, height: '90vh', overflowY: 'auto', overflowX: 'hidden' }} >
                                {props.children}
                            </div>
                        </div>
                    </div>
                </MuiThemeProvider>
            );
        } else {
            return (
                <MuiThemeProvider muiTheme={getMuiTheme(darkBaseTheme)}>
                    <div className="container-fluid">
                        <div className="row" id="header">
                            <div className="col-sm-12" style={{ display: this.state.displayHeader, paddingTop: '0px' }} >
                                <AppHeader userDetails={this.state.userDetails} />
                            </div>
                        </div>
                        <div className="row">
                            <div className="col-sm-2" style={{ display: this.state.displayNav, paddingLeft: '0px', height: '100vh', marginTop: '-8px' }}>
                                {(this.state.isAdminUser === true) ? <AdminLeftNav /> : <LeftNav /> }
                            </div>
                            <div className="col-sm-10" style={{ display: this.state.displayChildren, height: '90vh', overflowY: 'auto', overflowX: 'hidden' }} >
                                {props.children}
                            </div>
                        </div>
                    </div>
                </MuiThemeProvider>
            );
        }
    }
}

export default Root;
