import { Component } from 'react';
import axios from 'axios';
import MainData from '../MainData';

/**
* @class RepositoryType
* @extends {Component}
* @description repository type details
*/
class RepositoryType extends Component {
    /**
    * get all repository types
    * @returns {Promise} promise
    */
    getAllRepositoryTypes() {
        const url = MainData.ballerinaDatabaseURL + 'repoType/selectAll';
        const requestConfig = { withCredentials: true };
        return axios.get(url, requestConfig).then((response) => {
            return (response.data);
        }).catch((error) => {
            throw new Error(error);
        });
    }
}

export default (new RepositoryType());
