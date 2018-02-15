import { Component } from 'react';
import axios from 'axios';
import Config from 'config';

/**
* @class LoadLibraryVersion
* @extends {Component}
* @description Get LibraryVersions
*/
class LoadLibraryVersions extends Component {
    /**
    * @class LoadLibraryVersions
    * @extends {Component}
    * @param {string} libraryName  library name
    * @description Load versions of the libraries
    */
    libraryVersionsDropdown(libraryName) {
        const url = Config.databaseService[0].host +
        Config.databaseService[1].basePath +
        Config.databaseService[2].resourcePaths[1].libraryVersions + libraryName;
        return axios.get(url).then((response) => {
            return (response.data);
        }).catch((error) => {
            throw new Error(error);
        });
    }
}

export default (new LoadLibraryVersions());
