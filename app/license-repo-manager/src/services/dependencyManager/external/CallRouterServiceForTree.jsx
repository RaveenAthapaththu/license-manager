import { Component } from 'react';
import axios from 'axios';
import Config from 'config';
/**
* @class RouterService
* @extends {Component}
* @description Get Latest Version or Dependency Heirarchy according to the request
*/
class RouterServiceTree extends Component {
    /**
    * @class RouterServiceTree
    * @extends {Component}
    * @param {string} rqGID  Group ID
    * @param {string} rqAID  Artifact ID
    * @param {string} rqVersion  version
    * @param {string} libType  library type
    * @param {string} vsnReq  Version Request or not (true/false)
    * @description Get Dependency Heirarchy according to the request
    */
    callRouterServiceForTree(rqGID, rqAID, rqVersion, libType, vsnReq) {
        const url = Config.routingService[0].host +
        Config.routingService[1].basePath +
        Config.routingService[2].resourcePaths[0].requestResolve
        + libType + '?VersionReq=' + vsnReq + '&GroupID=' + rqGID;
        const data = {
            groupID: rqGID,
            artifactID: rqAID,
            version: rqVersion,
        };
        return axios.post(url, data).then((response) => {
            return (response.data);
        }).catch((error) => {
            let errResponse;
            if (error.response.status === 404) {
                errResponse = {
                    ErrorMsg: 'NotFound',
                };
            } else if (error.response.status === 500) {
                errResponse = {
                    ErrorMsg: 'Error',
                };
            } else {
                errResponse = {
                    ErrorMsg: 'Error',
                };
            }
            return (errResponse);
        });
    }
}

export default (new RouterServiceTree());
