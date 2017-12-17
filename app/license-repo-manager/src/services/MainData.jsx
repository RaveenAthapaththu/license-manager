import { Component } from 'react';
import confData from '../../conf/conf.json';

/**
 * @class MainData
 * @extends {Component}
 * @description Sample React component
 */
class MainData extends Component {
    /**
    * @class MainData
    * @extends {Component}
    * @description Sample React component
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
