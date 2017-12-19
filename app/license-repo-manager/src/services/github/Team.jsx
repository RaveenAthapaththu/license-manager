import { Component } from 'react';
import axios from 'axios';
import MainData from '../MainData';

/**
* @class Team
* @extends {Component}
* @description Get team details from GitHub API
*/
class Team extends Component {
    /**
    * select teams from github api
    * @param {String} organization string
    * @returns {Promise} promise
    */
    getAllTeams(organization) {
        const url = MainData.ballerinaGitHubURL + 'gitHub/getTeams?organization=' + organization;
        const requestConfig = { withCredentials: true };
        return axios.get(url, requestConfig).then((response) => {
            return (response.data);
        }).catch((error) => {
            return ([{ id: ' ', name: ' ', [error]: error }]);
        });
    }
    /**
    * select team from github api filter from team id
    * @param {String} id team id
    * @returns {Promise} promise
    */
    getTeamDetailsFromId(id) {
        const url = MainData.ballerinaGitHubURL + 'gitHub/getTeamsFromId?teamId=' + id;
        const requestConfig = { withCredentials: true };
        return axios.get(url, requestConfig).then((response) => {
            return (response.data);
        }).catch((error) => {
            return ([{ id: '', name: ' ', [error]: error }]);
        });
    }
}

export default (new Team());
