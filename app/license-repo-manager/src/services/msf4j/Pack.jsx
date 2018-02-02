import { Component } from 'react';
import axios from 'axios';
import MainData from '../MainData';
import Token from '../authentication/Token';

/**
* @class Pack
* @extends {Component}
* @description all package management services
*/
class Pack extends Component {
    /**
    * getNameMissingJars
    * @returns {Promise} promise
    */
    getNameMissingJars() {
        const url = MainData.microServiceURL + 'checkJars';
        const requestConfig = { withCredentials: true, timeout: 40000000 };
        return axios.get(url, requestConfig).then((response) => {
            return response;
        }).catch((error) => {
            console.log(error);//eslint-disable-line
        });
    }
    /**
    * getNameMissingJars
    * @returns {Promise} promise
    */
    getLicense() {
        const url = MainData.microServiceURL + 'getLicense';
        const requestConfig = { withCredentials: true, timeout: 40000000 };
        return axios.get(url, requestConfig).then((response) => {
            return response;
        }).catch((error) => {
            console.log(error);//eslint-disable-line
        });
    }
    /**
    * getNameMissingJars
    * @returns {Promise} promise
    */
    dowloadLicense() {
        const url = MainData.microServiceURL + 'downloadLicense';
        const requestConfig = { withCredentials: true, timeout: 40000000 };
        return axios.get(url, requestConfig).then((response) => {
            return response;
        }).catch((error) => {
            console.log(error);//eslint-disable-line
        });
    }
    /**
    * enterJars
    * @param {json} data json
    * @param {string} email email
    * @returns {Promise} promise
    */
    enterJars(data, email) {
        console.log(data);//eslint-disable-line
        const url = MainData.microServiceURL + 'enterJars';
        const requestConfig = {
            withCredentials: true,
        };
        const requestData = {
            jars: data,
            requestBy: email,
        };
        return axios.post(url, JSON.stringify(requestData), requestConfig).then((response) => {
            return response;
        }).catch((error) => {
            console.log(error);//eslint-disable-line
        });
    }
    /**
    * get all languages
    * @param {FormData} file file
    * @returns {Promise} promise
    */
    upload(file) {
        const url = MainData.microServiceURL + 'uploadPack';
        const formData = new FormData();
        const requestConfig = { withCredentials: true, timeout: 40000000 };
        formData.append('file', file);
        return axios.post(url, formData, requestConfig).then((response) => {
            return response;
        }).catch((error) => {
            throw new Error(error);
        });
    }
    /**
    * enterComponents
    * @param {json} comps json
    * @param {json} libs json
    * @param {int} pId int
    * @param {int} rId int
    * @returns {Promise} promise
    */
    acceptRequest(comps, libs, pId, rId) {
        console.log(data);//eslint-disable-line
        const url = MainData.microServiceURL + 'acceptLicenseRequest';
        const jwt = Token.getToken();
        const requestConfig = {
            withCredentials: true,
        };
        const data = {
            components: comps,
            libraries: libs,
            productId: pId,
            requestId: rId,
            token: jwt,
        };
        return axios.post(url, JSON.stringify(data), requestConfig).then((response) => {
            return response;
        }).catch((error) => {
            console.log(error);//eslint-disable-line
        });
    }
    /**
    * enterComponents
    * @param {string} rejBy string
    * @param {string} rejReason string
    * @param {int} pId int
    * @param {int} rId int
    * @returns {Promise} promise
    */
    rejectRequest(rejBy, rejReason, pId, rId) {
        console.log(data);//eslint-disable-line
        const url = MainData.microServiceURL + 'rejectLicenseRequest';
        const jwt = Token.getToken();
        const requestConfig = {
            withCredentials: true,
        };
        const data = {
            rejectBy: rejBy,
            rejectReason: rejReason,
            productId: pId,
            requestId: rId,
            token: jwt,
        };
        return axios.post(url, JSON.stringify(data), requestConfig).then((response) => {
            return response;
        }).catch((error) => {
            console.log(error);//eslint-disable-line
        });
    }
    /**
    * validateUser
    * @returns {Promise} promise
    */
    validateUser() {
        const url = MainData.microServiceURL + 'validateUser';
        const jwt = Token.getToken();
        const requestConfig = {
            withCredentials: true,
        };
        const data = {
            token: jwt,
        };
        return axios.post(url, JSON.stringify(data), requestConfig).then((response) => {
            return response;
        }).catch((error) => {
            console.log(error);//eslint-disable-line
        });
    }
}

export default (new Pack());
