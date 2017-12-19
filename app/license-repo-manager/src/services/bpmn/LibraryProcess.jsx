import { Component } from 'react';
import axios from 'axios';
import MainData from '../MainData';
import Token from '../authentication/Token';

/**
* @class LibraryProcess
* @extends {Component}
* @description LibraryProcess
*/
class LibraryProcess extends Component {
    /**
    * get task
    * @returns {Promise} promise
    */
    getTasks() {
        const url = MainData.bpmnTaskUrl;
        const requestConfig = { withCredentials: true };
        return axios.get(url, requestConfig).then((response) => {
            return response.data;
        }).catch((error) => {
            throw new Error(error);
        });
    }
    /**
    * escape charachters
    * @param {String} requestData requestData
    * @returns {String} str
    */
    startProcess(requestData) {
        const url = MainData.ballerinaURL + 'bpmn/library/request';
        const jwt = Token.getToken();
        const requestConfig = { withCredentials: true };
        const dataTosend = {
            token: jwt,
            data: requestData,
        };
        return axios.post(url, JSON.stringify(dataTosend), requestConfig).then((response) => {
            return response;
        }).catch((error) => {
            throw new Error(error);
        });
    }
    /**
    * escape charachters
    * @param {int} libRequestId libraryrequest id
    * @returns {String} str
    */
    acceptRequest(libRequestId) {
        const url = MainData.ballerinaURL + 'bpmn/library/accept';
        const jwt = Token.getToken();
        const data = {
            libraryRequestId: libRequestId,
            token: jwt,
        };
        const requestConfig = { withCredentials: true };
        return axios.post(url, JSON.stringify(data), requestConfig).then((response) => {
            return response;
        }).catch((error) => {
            throw new Error(error);
        });
    }
    /**
    * escape charachters
    * @param {int} libRequestId libraryrequest id
    * @param {string} reason reason fro rejection
    * @returns {String} str
    */
    rejectRequest(libRequestId, reason) {
        const url = MainData.ballerinaURL + 'bpmn/library/reject';
        const jwt = Token.getToken();
        const data = {
            token: jwt,
            libraryRequestId: libRequestId,
            libraryRejectReason: reason,
        };
        const requestConfig = { withCredentials: true };
        return axios.post(url, JSON.stringify(data), requestConfig).then((response) => {
            return response;
        }).catch((error) => {
            throw new Error(error);
        });
    }
}

export default (new LibraryProcess());
