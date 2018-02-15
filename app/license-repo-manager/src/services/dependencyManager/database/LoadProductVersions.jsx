import { Component } from 'react';
import axios from 'axios';
import Config from 'config';

/**
* @class LoadProductVersion
* @extends {Component}
* @description Get ProductVersions
*/
class LoadProductVersions extends Component {
    /**
    * @class LoadProductVersions
    * @extends {Component}
    * @param {string} prName  product name
    * @description Load versions of the products
    */
    productVersionsDropdown(prName) {
        const url = Config.databaseService[0].host +
        Config.databaseService[1].basePath +
        Config.databaseService[2].resourcePaths[9].productVersion + prName;
        return axios.get(url).then((response) => {
            return (response.data);
        }).catch((error) => {
            throw new Error(error);
        });
    }
}

export default (new LoadProductVersions());
