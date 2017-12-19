import { Component } from 'react';
import axios from 'axios';
import MainData from '../MainData';

/**
* @class Repository
* @extends {Component}
* @description Handle Repository table operations
*/
class Repository extends Component {
    /**
    * update task and bpmn ids
    * @param {JSON} dataArray json array
    * @returns {Promise} promise
    */
    updateTaskAndProcessIds(dataArray) {
        const url = MainData.ballerinaDatabaseURL + 'repository/updateBpmnAndTaskIds';
        const requestData = {
            data: dataArray,
        };
        const requestConfig = { withCredentials: true };
        return axios.post(url, JSON.stringify(requestData), requestConfig).then((response) => {
            if (response.data.type === 'Error') {
                throw new Error(' Your BPMN error occur ' + response.data.message);
            }
        }).catch((error) => {
            throw new Error(error);
        });
    }

    /**
    * update reject details
    * @param {JSON} dataArray json array
    * @returns {Promise} promise
    */
    updateRejectDetails(dataArray) {
        const url = MainData.ballerinaDatabaseURL + 'repository/updateRejectDetails';
        const requestData = {
            data: dataArray,
        };
        const requestConfig = { withCredentials: true };
        return axios.post(url, JSON.stringify(requestData), requestConfig).then((response) => {
            if (response.data.type === 'Error') {
                throw new Error(' Your BPMN error occur ' + response.data.message);
            }
        }).catch((error) => {
            throw new Error(error);
        });
    }

    /**
    * update all
    * @param {JSON} dataArray json array
    * @param {const} repositoryId json array
    * @returns {Promise} promise
    */
    updateAll(dataArray, repositoryId) {
        const url = MainData.ballerinaDatabaseURL + 'repository/updateAll';
        const requestData = {
            data: dataArray,
            repoId: repositoryId,
        };
        const requestConfig = { withCredentials: true };
        return axios.post(url, JSON.stringify(requestData), requestConfig).then((response) => {
            if (response.data.type === 'Error') {
                throw new Error(' Your BPMN error occur ' + response.data.message);
            }
        }).catch((error) => {
            throw new Error(error);
        });
    }
    /**
    * select data from name
    * @param {JSON} data json array
    * @returns {Promise} promise
    */
    selectDataFromName(data) {
        const url = MainData.ballerinaDatabaseURL + 'repository/selectFromName?name=' + data;
        const requestConfig = { withCredentials: true };
        return axios.get(url, requestConfig).then((response) => {
            return (response.data);
        }).catch((error) => {
            throw new Error(error);
        });
    }
    /**
    * select data from request by
    * @param {JSON} data json array
    * @returns {Promise} promise
    */
    selectDataFromRequestBy(data) {
        const url = MainData.ballerinaDatabaseURL + 'repository/selectFromRequestByAndWaiting?requestBy=' + data;
        const requestConfig = { withCredentials: true };
        return axios.get(url, requestConfig).then((response) => {
            return (response.data);
        }).catch((error) => {
            throw new Error(error);
        });
    }
    /**
    * select data from id
    * @param {JSON} data json array
    * @returns {Promise} promise
    */
    selectDataFromId(data) {
        const url = MainData.ballerinaDatabaseURL + 'repository/selectFromId?id=' + data;
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
    selectWaitingRequests() {
        const url = MainData.ballerinaDatabaseURL + 'repository/selectWaitingRequests';
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
    selectAll() {
        const url = MainData.ballerinaDatabaseURL + 'repository/selectAll';
        const requestConfig = { withCredentials: true };
        return axios.get(url, requestConfig).then((response) => {
            return (response.data);
        }).catch((error) => {
            throw new Error(error);
        });
    }
}


export default (new Repository());
