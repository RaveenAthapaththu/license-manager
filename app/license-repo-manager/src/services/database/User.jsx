import { Component } from 'react';
import axios from 'axios';
import MainData from '../MainData';

/**
* @class User
* @extends {Component}
* @description Get user details
*/
class User extends Component {
    /**
    * get main users
    * @returns {Promise} promise
    */
    getRepositoryMainUsers() {
        const url = MainData.ballerinaDatabaseURL + 'role/selectRepositoryMainUsers';
        const requestConfig = { withCredentials: true };
        return axios.get(url, requestConfig).then((response) => {
            return (response.data);
        }).catch((error) => {
            throw new Error(error);
        });
    }
    /**
    * get main users
    * @returns {Promise} promise
    */
    getLibraryMainUsers() {
        const url = MainData.ballerinaDatabaseURL + 'role/selectLibraryMainUsers';
        const requestConfig = { withCredentials: true };
        return axios.get(url, requestConfig).then((response) => {
            return (response.data);
        }).catch((error) => {
            throw new Error(error);
        });
    }
    /**
    * get admin users
    * @param {String} email email of a user
    * @returns {Promise} promise
    */
    isAdminUser(email) {
        const url = MainData.ballerinaDatabaseURL + 'role/checkRepositoryAdminUser?email=' + email;
        const requestConfig = { withCredentials: true };
        return axios.get(url, requestConfig).then((response) => {
            return (response.data);
        }).catch((error) => {
            throw new Error(error);
        });
    }
}

export default (new User());
