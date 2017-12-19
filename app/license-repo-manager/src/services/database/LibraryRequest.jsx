import { Component } from 'react';
import axios from 'axios';
import MainData from '../MainData';

/**
* @class LibraryRequest
* @extends {Component}
* @description Library request details
*/
class LibraryRequest extends Component {
    /**
    * selectTypes
    * @param {int} id library id
    * @returns {Promise} promise
    */
    selectLibraryRequestFromId(id) {
        const url = MainData.ballerinaDatabaseURL + 'libraryRequest/selectFromId?id=' + id;
        const requestConfig = { withCredentials: true };
        return axios.get(url, requestConfig).then((response) => {
            return (response.data[0]);
        }).catch((error) => {
            throw new Error(error);
        });
    }
    /**
    * selectTypes
    * @param {string} email library id
    * @returns {Promise} promise
    */
    selectLibraryRequestWaitingAndRequestBy(email) {
        const url = MainData.ballerinaDatabaseURL + 'libraryRequest/selectFromRequestByAndWaiting?requestBy=' + email;
        const requestConfig = { withCredentials: true };
        return axios.get(url, requestConfig).then((response) => {
            return (response.data);
        }).catch((error) => {
            throw new Error(error);
        });
    }
    /**
    * select waiting requests
    * @returns {Promise} promise
    */
    selectLibraryWaitingRequests() {
        const url = MainData.ballerinaDatabaseURL + 'libraryRequest/selectWaitingRequests';
        const requestConfig = { withCredentials: true };
        return axios.get(url, requestConfig).then((response) => {
            return (response.data);
        }).catch((error) => {
            throw new Error(error);
        });
    }
}

export default (new LibraryRequest());
