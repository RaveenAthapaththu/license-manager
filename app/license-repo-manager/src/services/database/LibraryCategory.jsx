import { Component } from 'react';
import axios from 'axios';
import MainData from '../MainData';

/**
* @class LibraryCategory
* @extends {Component}
* @description Get library categories
*/
class LibraryCategory extends Component {
    /**
    * selectTypes
    * @returns {Promise} promise
    */
    selectAll() {
        const url = MainData.ballerinaDatabaseURL + 'libCategory/selectAll';
        const requestConfig = { withCredentials: true };
        return axios.get(url, requestConfig).then((response) => {
            return (response.data);
        }).catch((error) => {
            throw new Error(error);
        });
    }
}

export default (new LibraryCategory());
