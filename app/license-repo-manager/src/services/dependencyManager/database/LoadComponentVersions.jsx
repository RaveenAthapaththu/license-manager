import { Component } from 'react';
import axios from 'axios';
import Config from 'config';

/**
* @class LoadComponentVersion
* @extends {Component}
* @description Get ComponentVersions
*/
class LoadComponentVersions extends Component {
    /**
    * @class LoadComponentVersions
    * @extends {Component}
    * @param {string} cmName  component name
    * @description Load versions of the Components
    */
    componentVersionsDropdown(cmName) {
        const url = Config.databaseService[0].host +
        Config.databaseService[1].basePath +
        Config.databaseService[2].resourcePaths[7].componentVersion + cmName;
        return axios.get(url).then((response) => {
            return (response.data);
        }).catch((error) => {
            throw new Error(error);
        });
    }
}

export default (new LoadComponentVersions());
