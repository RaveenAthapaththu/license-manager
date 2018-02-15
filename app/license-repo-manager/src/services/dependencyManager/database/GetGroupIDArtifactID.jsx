import { Component } from 'react';
import axios from 'axios';
import Config from 'config';

/**
* @class GetGroupIDArtifactID
* @extends {Component}
* @description Get Group and Atifact ID
*/
class GetGroupIDArtifactID extends Component {
    /**
    * @class GetGroupIDArtifactID
    * @extends {Component}
    * @param {string} rqName  library name
    * @param {string} rqVersion  library version
    * @description get Group ID and Artifact ID
    */
    getGroupArtifactID(rqName, rqVersion) {
        const url = Config.databaseService[0].host +
        Config.databaseService[1].basePath +
        Config.databaseService[2].resourcePaths[2].libraryGroupIDArtifactID + rqName;
        return axios.get(url, {
            params: {
                reqVersion: rqVersion,
            },
        }).then((response) => {
            return (response.data);
        }).catch((error) => {
            throw new Error(error);
        });
    }
}

export default (new GetGroupIDArtifactID());
