import React, { Component } from 'react';
import { Link } from 'react-router';
import Header from '../components/layouts/Header';

/**
* @class Main
* @extends {Component}
* @description Get user details
*/
class Main extends Component {
    /**
    * @class Main
    * @extends {Component}
    * @description Sample React component
    */
    render() {
        return (
            <div className="container-fluid">
                <div className="row" id="header">
                    <div className="col-md-12">
                        <Header />
                    </div>
                </div>
                <br /><br />
                <div className="row">
                    <div className="row">
                        <div className="col-md-1" />
                        <div className="col-md-5">
                            <Link to="/app/requestRepository" className="btn btn-success btn-lg btn-block">
                                <span>
                                    <i className="fa fa-github fa-1x" />
                                </span>
                                &nbsp;&nbsp;&nbsp;
                                <b>Request GitHub Repository</b>
                            </Link>
                        </div>
                        <div id="mainIcons" className="col-md-5">
                            <Link to="/app/requestLibrary" className="btn btn-success btn-lg btn-block">
                                <span>
                                    <i className="fa fa-address-book-o fa-1x" />
                                </span>
                                &nbsp;&nbsp;&nbsp;
                                <b>Request 3rd party Library</b>
                            </Link>
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}

export default Main;
