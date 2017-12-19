import React, { Component } from 'react';
import DropToUpload from 'react-drop-to-upload';

/**
* @class WaitingRequests
* @extends {Component}
* @description Get user details
*/
class RequestLicense extends Component {
    /**
    * @class RequestLicense
    * @extends {Component}
    * @param {any} props props for constructor
    * @description Sample React component
    */
    constructor(props) {
        super(props);
        this.state = {
            openUploadModal: false,
            files: [],
        };
        this.handleDrop = this.handleDrop.bind(this);
    }
    /**
    * set teams after selecting organization
    */
    handleDrop() {
        console.log('sample');//eslint-disable-line
    }
    /**
    * @class WaitingRequests
    * @extends {Component}
    * @description Sample React component
    */
    render() {
        return (
            <div className="container-fluid">
                <h2 className="text-center">Requested License</h2>
                <DropToUpload
                    onDrop={this.handleDrop}
                >
                    Drop file here to upload
                </DropToUpload>
            </div>
        );
    }
}

export default RequestLicense;
