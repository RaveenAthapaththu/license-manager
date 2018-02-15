import { Component } from 'react';
import axios from 'axios';
import Config from 'config';

/**
* @class LibraryNames
* @extends {Component}
* @description Get LibraryNames
*/
class LoadLibraryNames extends Component {
    /**
    * @class LoadLibraryNames
    * @extends {Component}
    * @description Load names of the libraries
    */
    selectLibraryNamesfortheDropdown() {
        const url = Config.databaseService[0].host +
        Config.databaseService[1].basePath +
        Config.databaseService[2].resourcePaths[0].libraryNames;
        return axios.get(url).then((response) => {
            return (response.data);
        }).catch((error) => {
            throw new Error(error);
        });
    }
}

export default (new LoadLibraryNames());
