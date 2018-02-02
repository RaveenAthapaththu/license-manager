import { Component } from 'react';
import axios from 'axios';
import MainData from '../MainData';

/**
* @class ProductAreas
* @extends {Component}
* @description PQD product area
*/
class ProductAreas extends Component {
    /**
    * select all product areas
    * @returns {Promise} promise
    */
    selectAll() {
        const url = MainData.ballerinaURL + 'pqd/selectProductAreas';
        const requestConfig = { withCredentials: true };
        return axios.get(url, requestConfig).then((response) => {
            return (response.data);
        }).catch((error) => {
            throw new Error(error);
        });
    }
}

export default (new ProductAreas());
