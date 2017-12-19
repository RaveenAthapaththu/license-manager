import React, { Component } from 'react';
import DropToUpload from 'react-drop-to-upload';
import axios from 'axios';

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
    * @param {any} files files
    */
    handleDrop(files) {
        console.log('sample');//eslint-disable-line
        console.log(files[0]);//eslint-disable-line
        const url = 'http://localhost:8085/service';
        const formData = new FormData();
        formData.append('file', files[0]);
        axios.post(url, formData).then((response) => {
            console.log(response);//eslint-disable-line
        }).catch((error) => {
            throw new Error(error);
        });
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
