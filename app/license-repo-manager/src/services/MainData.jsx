import { Component } from 'react';
import CONFIG from 'CONFIG';

/**
 * @class MainData
 * @extends {Component}
 * @description Main config data exchanging component
 */
class MainData extends Component {
    /**
    * @class MainData
    * @extends {Component}
    * @description constructor
    */
    constructor() {
        super();
        this.ballerinaDatabaseURL = CONFIG.serviceUrl + 'databaseService/';
        this.ballerinaGitHubURL = CONFIG.serviceUrl;
        this.ballerinaURL = CONFIG.serviceUrl;
        this.bpmnImgURL = CONFIG.businessUrl + 'bpmn/runtime/process-instances/';
        this.microServiceURL = CONFIG.microServiceUrl;
    }
}

export default (new MainData());
