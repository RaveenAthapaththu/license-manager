import { Component } from 'react';
import axios from 'axios';
import MainData from '../MainData';

/**
* @class DataManager
* @extends {Component}
* @description all package management services
*/
class DataManager extends Component {
    /**
    * selectLicense
    * @returns {Promise} promise
    */
    selectLicense() {
        const url = MainData.microServiceURL + 'selectLicense';
        const requestConfig = { withCredentials: true };
        return axios.get(url, requestConfig).then((response) => {
            return response;
        }).catch((error) => {
            console.log(error);//eslint-disable-line
        });
    }
    /**
    * selectWaitingLicenseRequests
    * @returns {Promise} promise
    */
    selectWaitingLicenseRequests() {
        const url = MainData.microServiceURL + 'selectWaitingLicenseRequests';
        const requestConfig = { withCredentials: true };
        return axios.get(url, requestConfig).then((response) => {
            return response;
        }).catch((error) => {
            console.log(error);//eslint-disable-line
        });
    }
    /**
    * selectWaitingLibraries
    * @param {int} licenseRequestId licenseRequestI
    * @returns {Promise} promise
    */
    selectWaitingLibraries(licenseRequestId) {
        const url = MainData.microServiceURL + 'selectWaitingLibraries?licenseRequestId=' + licenseRequestId;
        const requestConfig = { withCredentials: true };
        return axios.get(url, requestConfig).then((response) => {
            return response;
        }).catch((error) => {
            console.log(error);//eslint-disable-line
        });
    }
    /**
    * selectWaitingComponents
    * @param {int} licenseRequestId licenseRequestId
    * @returns {Promise} promise
    */
    selectWaitingComponents(licenseRequestId) {
        const url = MainData.microServiceURL + 'selectWaitingComponents?licenseRequestId=' + licenseRequestId;
        const requestConfig = { withCredentials: true };
        return axios.get(url, requestConfig).then((response) => {
            return response;
        }).catch((error) => {
            console.log(error);//eslint-disable-line
        });
    }
}

export default (new DataManager());
