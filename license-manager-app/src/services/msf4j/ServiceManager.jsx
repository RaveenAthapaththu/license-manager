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
import Config from "../../configuration"

//URL_FETCH_SERVICES

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
        const url = Config.URL_FETCH_SERVICES + 'pack/uploadedPacks';
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
        const url = Config.URL_FETCH_SERVICES+ 'licenses';
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
    getLicense(packName) {
        const url = Config.URL_FETCH_SERVICES+ 'license/generate';
        const requestConfig = { withCredentials: true, timeout: 40000000 };
        const requestData ={packName:packName};
        return axios.post(url, requestData, requestConfig).then((response) => {
            return response;
        }).catch((error) => {
            throw error
        });
    }

    /**
     * Call the micro service to download the license text.
     * @returns {Promise<AxiosResponse<any>>}
     */
    downloadLicense(packName) {
        const url = Config.URL_FETCH_SERVICES + 'license/download/' + packName;
        const requestConfig = { withCredentials: true, timeout: 40000000 };

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
        const url = Config.URL_FETCH_SERVICES + 'pack/extract';
        const requestConfig = {
            withCredentials: true,
        };
        const requestData = {
            packName:selectedPack,
        };

        return axios.post(url, requestData, requestConfig).then((response) => {
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
    enterJars(data, packName) {
        const url = Config.URL_FETCH_SERVICES + 'pack/add';
        const requestConfig = { withCredentials: true, timeout: 40000000 };
        const requestData = {
            jars : data,
            packName:packName

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
    addLicense(components, libraries, packName) {
        const url = Config.URL_FETCH_SERVICES + 'license/add';
        const requestConfig = { withCredentials: true, timeout: 40000000 };
        const licenseData = {
            components: components,
            libraries: libraries,
            packName: packName,
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
    checkProgress(packName) {
        const url = Config.URL_FETCH_SERVICES + 'longRunningTask/progress/'+ packName;
        const requestConfig = {
            withCredentials: true,
        };

        return axios.get(url, requestConfig).then((response) => {
            return response;
        }).catch((error) => {
            throw error
        });
    }

    getFaultyNamedJars(packname){
        const url = Config.URL_FETCH_SERVICES + 'pack/faultyNamedJars/' + packname;
        const requestConfig = {
            withCredentials: true, timeout: 40000000
        };

        return axios.get(url, requestConfig
        ).then((response) => {
            return response;
        }).catch((error) => {
            throw error
        });
    }

    getLicenseMissingJars(packname){
        const url = Config.URL_FETCH_SERVICES + 'pack/licenseMissingJars/' + packname;
        const requestConfig = {
            withCredentials: true, timeout: 40000000
        };
        return axios.get(url, requestConfig
        ).then((response) => {
            return response;
        }).catch((error) => {
            throw error
        });
    }
}

export default (new ServiceManager());
