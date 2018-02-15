import { Component } from 'react';
import axios from 'axios';
import Config from 'config';

/**
* @class LoadProductLibraries
* @extends {Component}
* @description Get Libraries Used by given Product
*/
class LoadProductLibraries extends Component {
    /**
    * @class LoadProductLibraries
    * @extends {Component}
    * @param {string} pName  product name
    * @param {string} pVersion  product version
    * @description Load Libraries Used by given Product
    */
    getLibrariesOfProduct(pName, pVersion) {
        const url = Config.databaseService[0].host +
        Config.databaseService[1].basePath +
        Config.databaseService[2].resourcePaths[8].librariesofProduct + pName;
        return axios.get(url, {
            params: {
                prodVersion: pVersion,
            },
        }).then((response) => {
            return (response.data);
        }).catch((error) => {
            throw new Error(error);
        });
    }
}

export default (new LoadProductLibraries());
