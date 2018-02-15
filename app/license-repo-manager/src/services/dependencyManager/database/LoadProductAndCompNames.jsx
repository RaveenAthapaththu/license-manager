import { Component } from 'react';
import axios from 'axios';
import Config from 'config';

/**
* @class LoadProductsAndCompNames
* @extends {Component}
* @description Get ProductsAndCompNames
*/
class LoadProductsAndCompNames extends Component {
    /**
    * @class LoadProductsAndCompNames
    * @extends {Component}
    * @description Load names of the components and Products
    */
    selectNamesfortheDropdown() {
        const url = Config.databaseService[0].host +
        Config.databaseService[1].basePath +
        Config.databaseService[2].resourcePaths[4].productComponentNames;
        return axios.get(url).then((response) => {
            return (response.data);
        }).catch((error) => {
            throw new Error(error);
        });
    }
}

export default (new LoadProductsAndCompNames());
