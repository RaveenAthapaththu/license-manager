import { Component } from 'react';
import axios from 'axios';
import Config from 'config';

/**
* @class LoadComponentLibraries
* @extends {Component}
* @description Get Libraries Used by given Component
*/
class LoadComponentLibraries extends Component {
    /**
    * @class LoadComponentLibraries
    * @extends {Component}
    * @param {string} cName  component name
    * @param {string} cVersion  component version
    * @description Load Libraries Used by given Component
    */
    getLibrariesOfComponent(cName, cVersion) {
        const url = Config.databaseService[0].host +
        Config.databaseService[1].basePath +
        Config.databaseService[2].resourcePaths[6].librariesofComponent + cName;
        return axios.get(url, {
            params: {
                compVersion: cVersion,
            },
        }).then((response) => {
            return (response.data);
        }).catch((error) => {
            throw new Error(error);
        });
    }
}

export default (new LoadComponentLibraries());
