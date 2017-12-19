import { Component } from 'react';
import confData from '../../conf/conf.json';

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
        this.ballerinaDatabaseURL = confData.serviceUrl + 'databaseService/';
        this.ballerinaGitHubURL = confData.serviceUrl;
        this.ballerinaURL = confData.serviceUrl;
        this.bpmnImgURL = confData.businessUrl + 'bpmn/runtime/process-instances/';
    }
}

export default (new MainData());
