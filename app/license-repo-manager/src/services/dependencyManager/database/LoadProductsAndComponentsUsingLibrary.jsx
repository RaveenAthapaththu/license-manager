import { Component } from 'react';
import axios from 'axios';
import Config from 'config';

/**
* @class LoadProductsUsingLibrary
* @extends {Component}
* @description Get Products Using given Library
*/
class LoadProductsUsingLibrary extends Component {
    /**
    * @class LoadProductsUsingLibrary
    * @extends {Component}
    * @param {string} lName  library name
    * @param {string} lVersion  library version
    * @description Load products and components using given library
    */
    getProductsAndComponentsUsingLibrary(lName, lVersion) {
        const url = Config.databaseService[0].host +
        Config.databaseService[1].basePath +
        Config.databaseService[2].resourcePaths[3].productsAndComponentsUsingLibrary
        + lName;
        return axios.get(url, {
            params: {
                libraryVersion: lVersion,
            },
        }).then((response) => {
            return (response.data);
        }).catch((error) => {
            throw new Error(error);
        });
    }
}

export default (new LoadProductsUsingLibrary());
