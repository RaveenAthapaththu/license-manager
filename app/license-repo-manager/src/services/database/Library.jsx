import { Component } from 'react';
import axios from 'axios';
import MainData from '../MainData';

/**
* @class Library
* @extends {Component}
* @description Get license details
*/
class Library extends Component {
    /**
    * selectTypes
    * @param {String} name library name
    * @param {String} version library version
    * @returns {Promise} promise
    */
    selectLibraryAndRequestFromNameAndVersion(name, version) {
        const url = MainData.ballerinaDatabaseURL +
        'libraryAndRequest/selectFromNameAndVersion?name=' + name + '&version=' + version;
        const requestConfig = { withCredentials: true };
        return axios.get(url, requestConfig).then((response) => {
            return (response.data);
        }).catch((error) => {
            throw new Error(error);
        });
    }
}

export default (new Library());
