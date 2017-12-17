import { Component } from 'react';
import axios from 'axios';
import MainData from '../MainData';

/**
* @class Organization
* @extends {Component}
* @description Component for gather organization details
*/
class Organization extends Component {
    /**
    * select all organizations
    * @returns {Promise} promise
    */
    getAllOrganizations() {
        const url = MainData.ballerinaDatabaseURL + 'organization/selectAll';
        const requestConfig = { withCredentials: true };
        return axios.get(url, requestConfig).then((response) => {
            return (response.data);
        }).catch((error) => {
            throw new Error(error);
        });
    }
}

export default (new Organization());
