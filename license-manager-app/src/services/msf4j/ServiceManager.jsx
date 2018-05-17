/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import { Component } from 'react';
import axios from 'axios';
import MainData from '../MainData';

/**
* @class ServiceManager
* @extends {Component}
* @description all package management services
*/
class ServiceManager extends Component {

    /**
     * Call the micro service to obtain the list of packs uploaded to the FTP server.
     * @returns {Promise<AxiosResponse<any>>}
     */
    getUploadedPacks() {
        const url = MainData.microServiceURL + 'pack/uploadedPacks';
        const requestConfig = { withCredentials: true, timeout: 40000000 };

        return axios.get(url, requestConfig).then((response) => {
            return response;
        }).catch((error) => {
            throw error
        });
    }

    /**
     * Call the micro service to obtain the list of licenses available.
     * @returns {Promise<AxiosResponse<any>>}
     */
    selectLicense() {
        const url = MainData.microServiceURL + 'license/availableLicenses';
        const requestConfig = { withCredentials: true };
        return axios.get(url, requestConfig).then((response) => {
            return response;
        }).catch((error) => {
            throw error
        });
    }

    /**
     * Call the micro service to generate the license text.
     * @returns {Promise<AxiosResponse<any>>}
     */
    getLicense() {
        const url = MainData.microServiceURL + 'license/text';
        const requestConfig = { withCredentials: true, timeout: 40000000 };
        return axios.post(url, requestConfig).then((response) => {
            return response;
        }).catch((error) => {
            throw error
        });
    }

    /**
     * Call the micro service to download the license text.
     * @returns {Promise<AxiosResponse<any>>}
     */
    downloadLicense() {
        const url = MainData.microServiceURL + 'license/textToDownload';
        const requestConfig = { withCredentials: true, timeout: 30000 };
        return axios.get(url, requestConfig).then((response) => {
            return response;
        }).catch((error) => {
            throw error
        });
    }

    /**
     * Call the micro service to the extract the jars of the selected pack.
     * @param selectedPack the pack selected for license generation.
     * @returns {Promise<AxiosResponse<any>>}
     */
    extractJars(selectedPack) {
        const url = MainData.microServiceURL + 'pack/selectedPack';
        const requestConfig = {
            withCredentials: true,
        };
        return axios.post(url, selectedPack, requestConfig).then((response) => {
            return response;
        }).catch((error) => {
            throw error
        });
    }

    /**
     * Call the micro service to enter the name and version defined set of jars.
     * @param data  name and version defined jars.
     * @returns {Promise<AxiosResponse<any>>}
     */
    enterJars(data) {
        const url = MainData.microServiceURL + 'pack/nameDefinedJars';
        const requestConfig = {
            withCredentials: true,
        };
        const requestData = {
            jars : data,
        };
        return axios.post(url, requestData, requestConfig).then((response) => {
            return response;
        }).catch((error) => {
            throw error
        });
    }

    /**
     * Call the micro service to enter the licenses for the jars.
     * @param components    components for which the licenses are added.
     * @param libraries     libraries for which the licenses are added.
     * @returns {Promise<AxiosResponse<any>>}
     */
    addLicense(components, libraries) {
        const url = MainData.microServiceURL + 'license/newLicenses';
        const requestConfig = {
            withCredentials: true,
        };
        const licenseData = {
            components: components,
            libraries: libraries,
        };
        return axios.post(url, JSON.stringify(licenseData), requestConfig).then((response) => {
            return response;
        }).catch((error) => {
            throw error
        });
    }

    /**
     * Call the micro service to check the progress of the jar extraction task.
     * @returns {Promise<AxiosResponse<any>>}
     */
    checkProgress() {
        const url = MainData.microServiceURL + 'packExtraction/progress';
        const requestConfig = {
            withCredentials: true,
        };
        return axios.get(url, requestConfig).then((response) => {
            return response;
        }).catch((error) => {
            throw error
        });
    }

    getFaultyNamedJars(){
        const url = MainData.microServiceURL + 'pack/faultyNamedJars';
        const requestConfig = {
            withCredentials: true,
        };
        return axios.get(url, requestConfig).then((response) => {
            return response;
        }).catch((error) => {
            throw error
        });
    }
}

export default (new ServiceManager());
