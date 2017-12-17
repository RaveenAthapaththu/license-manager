import { Component } from 'react';
import axios from 'axios';
import MainData from '../MainData';

/**
* @class GitHubRepositoryTask
* @extends {Component}
* @description GitHubRepositoryTask
*/
class GitHubRepositoryTask extends Component {
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
}


export default (new GitHubRepositoryTask());
