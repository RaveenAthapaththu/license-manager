import React, { Component } from 'react';
import ValidateUser from '../../services/authentication/ValidateUser';
import logo from '../../assets/images/logo-inverse.svg';

/**
* @class Header
* @extends {Component}
* @description Initial header
*/
class Header extends Component {
    /**
    * constructor
    */
    constructor() {
        super();
        this.state = {
            userDetails: [],
        };
    }
    /**
    * @class Header
    * @extends {Component}
    * @description Initial header
    */
    componentWillMount() {
        ValidateUser.isValidUser().then((response) => {
            this.setState(() => {
                return {
                    userDetails: response,
                };
            });
        });
    }
    /**
    * @class Header
    * @extends {Component}
    * @description Initial header
    */
    render() {
        return (
            <nav className="navbar navbar-default" style={{ minHeight: '50px', height: '50px' }}>
                {/* eslint-disable max-len */}
                <div className="container-fluid">
                    <div className="navbar-header">
                        <img id="logo" style={{ height: '30px' }} src={logo} alt="wso2" /><strong style={{ color: 'white', fontSize: '20px', marginTop: '1px' }}>License and Repository Manager</strong>
                    </div>
                </div>
            </nav>
        );
    }
}

export default Header;
