import { Component } from 'react';
import axios from 'axios';
import MainData from '../MainData';

/**
* @class LibraryType
* @extends {Component}
* @description Library type data
*/
class LibraryType extends Component {
    /**
    * selectTypes
    * @returns {Promise} promise
    */
    selectDeafult() {
        const url = MainData.ballerinaDatabaseURL + 'libType/selectDefault';
        const requestConfig = { withCredentials: true };
        return axios.get(url, requestConfig).then((response) => {
            return (response.data);
        }).catch((error) => {
            throw new Error(error);
        });
    }
    /**
    * selectTypes
    * @param {int} id category
    * @returns {Promise} promise
    */
    selectFromCategory(id) {
        const url = MainData.ballerinaDatabaseURL + 'libType/selectFromCategory?id=' + id;
        const requestConfig = { withCredentials: true };
        return axios.get(url, requestConfig).then((response) => {
            return (response.data);
        }).catch((error) => {
            throw new Error(error);
        });
    }
}

export default (new LibraryType());
