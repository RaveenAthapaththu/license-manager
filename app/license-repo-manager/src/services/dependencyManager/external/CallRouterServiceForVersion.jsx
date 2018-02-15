import { Component } from 'react';
import axios from 'axios';
import Config from 'config';
/**
* @class RouterService
* @extends {Component}
* @description Get Latest Version or Dependency Heirarchy according to the request
*/
class RouterServiceVersion extends Component {
    /**
    * @class RouterServiceVersion
    * @extends {Component}
    * @param {string} rqGID  Group ID
    * @param {string} rqAID  Artifact ID
    * @param {string} libType  library type
    * @param {string} vsnReq  Version Request or not (true/false)
    * @description Get Get Latest Version according to the request
    */
    callRouterServiceForLatestVersion(rqGID, rqAID, libType, vsnReq) {
        const url = Config.routingService[0].host +
        Config.routingService[1].basePath +
        Config.routingService[2].resourcePaths[0].requestResolve
        + libType + '?VersionReq=' + vsnReq + '&GroupID=' + rqGID;
        const data = {
            groupID: rqGID,
            artifactID: rqAID,
        };
        // var headers = { //eslint-disable-line
        //     'Content-Type': 'application/json',
        // };
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

export default (new RouterServiceVersion());
