import { Component } from 'react';
import axios from 'axios';
import MainData from '../MainData';
import Token from './Token';

/**
* @class ValidateUser
* @extends {Component}
* @description validate user details
*/
class ValidateUser extends Component {
    /**
    * get valid user details
    * @returns {Promise} promise
    */
    getUserDetails() {
        const url = MainData.ballerinaURL + 'authentication/getUserDetails';
        const requestHeaders = { withCredentials: true };
        return axios.get(url, requestHeaders).then((response) => {
            return response.data;
        }).catch((error) => {
            throw new Error(error);
        });
    }
    /**
    * check valid users
    * @returns {Promise} promise
    */
    isValidUser() {
        const jwt = Token.getToken();
        const requestHeaders = { withCredentials: true };
        const url = MainData.ballerinaURL + 'authentication/isValidUser';
        const requestData = {
            token: jwt,
        };
        return axios.post(url, JSON.stringify(requestData), requestHeaders).then((response) => {
            return response.data;
        }).catch((error) => {
            throw new Error(error);
        });
    }
}

export default (new ValidateUser());
