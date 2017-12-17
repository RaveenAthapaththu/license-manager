package org.wso2.internalapps.licensemanager.services;

import ballerina.net.http;
import org.wso2.internalapps.licensemanager.conf;
import ballerina.lang.messages;
import ballerina.lang.errors;
import org.wso2.internalapps.licensemanager.database;
import ballerina.lang.jsons;
import ballerina.utils.logger;
import ballerina.doc;

http:ClientConnector httpConnectorBpmn;
string bpmnStartUrl = conf:getConfigData("BPMN_URL");
string bpmnBasicAuthToken = conf:getConfigData("BPMN_TOKEN");
string originURL = conf:getConfigData("REDIRECT_ORIGIN");

@doc:Description {value:"Create BPMN client connector"}
function setBpmnConnection(){
    httpConnectorBpmn = create http:ClientConnector(bpmnStartUrl);
}

@doc:Description {value:"Start repository request BPMN process instance"}
@doc:Param {value:"requestData: Normal data that used to added to DB with keys"}
@doc:Param {value:"mailData: Data that used to send e-mails by BPMN"}
@doc:Param {value:"response: Return JSON of BPMN"}
function bpmnRequestRepository(json requestData,json mailData,string token)(json responseJson){

    message requestMessage = {};
    message responseFromBpmn = {};
    json requestPayloadJson;
    json responseFromBpmnJson;
    json repositoryMainUsers;
    json variables;
    string url;
    string sendToList = " ";
    string repositoryName;
    int taskId;
    int processInstanceId;
    int i = 0;
    int repositoryMainUsersJsonLength;
    int databaseUpdateReturnValue;
    boolean completed = true;

    try{
        if(httpConnectorBpmn == null){
            setBpmnConnection();
        }
        repositoryMainUsers = database:roleSelectRepositoryMainUsers();
        logger:info(repositoryMainUsers);
        repositoryMainUsersJsonLength = lengthof repositoryMainUsers;
        while(i < repositoryMainUsersJsonLength){
            sendToList = sendToList + jsons:toString(repositoryMainUsers[i].ROLE_EMAIL) + ", ";
            i = i + 1;
        }
        url = "bpmn/runtime/process-instances/";
        variables = [
                        {"name": "data","value":requestData},
                        {"name": "mailData","value":mailData},
                        {"name": "sendToList","value":sendToList},
                        {"name": "jwt","value":token},
                        {"name": "origin","value":originURL}
                    ];
        messages:setHeader(requestMessage,"Authorization",bpmnBasicAuthToken);
        requestPayloadJson = {
                                 "processDefinitionKey": "repositoryCreationProcess",
                                 "businessKey": "myBusinessKey",
                                 "tenantId": "-1234",
                                 "variables":variables
                             };
        messages:setJsonPayload(requestMessage,requestPayloadJson);
        logger:info(requestMessage);
        responseFromBpmn = http:ClientConnector.post(httpConnectorBpmn,url,requestMessage);
        logger:info(responseFromBpmn);
        responseFromBpmnJson = messages:getJsonPayload(responseFromBpmn);
        completed, _ = <boolean> jsons:toString(responseFromBpmnJson.completed);
        if(!completed){
            processInstanceId,_ = <int>jsons:toString(responseFromBpmnJson.id);
            taskId = getTaskIdFromProcessId(processInstanceId);
            repositoryName = jsons:toString(requestData[0]);
            databaseUpdateReturnValue = database:repositoryUpdateTaskAndProcessIds(taskId,processInstanceId,repositoryName);
            if(databaseUpdateReturnValue > 0){
                responseJson = {"responseType":"Done","responseMessage":"done"};
                logger:info("Successfully submit repository request");
            }else{
                responseJson = {"responseType":"Error","responseMessage":"Task ID and Process ID update fails"};
                logger:error("Repository request : Task and Process IDs update fails");
            }
        }else{
            responseJson = {"responseType":"Error","responseMessage":"BPMN Error occurs"};
            logger:error("Repository request : BPMN Error occurs");
        }
    }catch(errors:Error err){
        responseJson = {"responseType":"Error","responseMessage":err.msg};
        logger:error(err);

    }
    return;
}

@doc:Description {value:"Accept particular repository request"}
@doc:Param {value:"repoId: Repository request ID in LM_REPOSITORY table"}
@doc:Param {value:"taskId: Task ID of business process"}
@doc:Param {value:"responseJson: Return JSON of BPMN"}
function acceptRepositoryRequest(string repoId, string taskId, string token)(json responseJson){
    message response = {};
    message request = {};
    json requestJson;
    json variables;
    string url;
    int statusCode;
    try{
        if(httpConnectorBpmn == null){
            setBpmnConnection();
        }
        variables = [
                        {
                            "name": "outputType",
                            "value": "Done"
                        },
                        {
                            "name": "repositoryId",
                            "value": repoId
                        },
                        {
                            "name": "adminJwt",
                            "value": token
                        }
                    ];
        requestJson = {
                          "action": "complete",
                          "variables": variables
                      };

        messages:setJsonPayload(request,requestJson);
        messages:setHeader(request,"Authorization",bpmnBasicAuthToken);
        url = "bpmn/runtime/tasks/" + taskId;
        response = httpConnectorBpmn.post(url,request);
        statusCode = http:getStatusCode(response);
        if(statusCode == 200 || statusCode == 201){
            responseJson = {"responseType":"Done","responseMessage":"done"};
            logger:info("Successfully accept repository request");
        }else{
            responseJson = {"responseType":"Error","responseMessage":"Error"};
            logger:info("Error encountered");
        }

    }catch(errors:Error err){

        responseJson = {"responseType":"Error","responseMessage":err.msg};
        logger:error(err);

    }
    return;
}

@doc:Description {value:"Reject particular repository request"}
@doc:Param {value:"taskId: Task ID of business process"}
@doc:Param {value:"rejectBy: E-mail of the person who reject the request"}
@doc:Param {value:"reasonForRejecting: Reason for reject the request"}
@doc:Param {value:"responseJson: Return JSON of BPMN"}
function rejectRepositoryRequest(string taskId, string rejectBy, string reasonForRejecting )(json responseJson){
    message response = {};
    message request = {};
    json requestJson;
    json variables;
    string url;
    int statusCode;
    try{
        if(httpConnectorBpmn == null){
            setBpmnConnection();
        }
        variables = [
                        {
                            "name": "outputType",
                            "value": "Reject"
                        },
                        {
                            "name": "rejectBy",
                            "value": rejectBy
                        },
                        {
                            "name": "reasonForReject",
                            "value": reasonForRejecting
                        }
                    ];
        requestJson = {
                          "action": "complete",
                          "variables": variables
                      };

        messages:setJsonPayload(request,requestJson);
        messages:setHeader(request,"Authorization",bpmnBasicAuthToken);
        url = "bpmn/runtime/tasks/" + taskId;
        response = httpConnectorBpmn.post(url,request);
        statusCode = http:getStatusCode(response);
        if(statusCode == 200 || statusCode == 201){
            responseJson = {"responseType":"Done","responseMessage":"done"};
            logger:info("Successfully reject repository request");
        }else{
            responseJson = {"responseType":"Error","responseMessage":"Error"};
            logger:info("Error encountered");
        }

    }catch(errors:Error err){

        responseJson = {"responseType":"Error","responseMessage":err.msg};
        logger:error(err.msg);

    }
    return;
}

@doc:Description {value:"Start 3rd party library request BPMN process instance"}
@doc:Param {value:"requestData: Data that need to start the process"}
@doc:Param {value:"response: Return JSON of BPMN"}
function bpmnRequestLibrary(json requestData)(json responseJson){
    message requestMessage = {};
    message responseFromBpmn = {};
    json requestPayloadJson;
    json responseFromBpmnJson;
    json libraryMainUsers;
    json variables;
    string url;
    string sendToList = " ";
    string libraryName;
    string libraryUseVersion;
    int taskId;
    int processInstanceId;
    int i = 0;
    int libraryMainUsersJsonLength;
    int databaseUpdateReturnValue;
    int statusCode;

    try{
        if(httpConnectorBpmn == null){
            setBpmnConnection();
        }
        libraryMainUsers = database:roleSelectLibraryMainUsers();
        libraryMainUsersJsonLength = lengthof libraryMainUsers;
        while(i < libraryMainUsersJsonLength){
            sendToList = sendToList + jsons:toString(libraryMainUsers[i].ROLE_EMAIL) + ", ";
            i = i + 1;
        }
        url = "bpmn/runtime/process-instances/";
        variables = [
                        {"name": "data","value":requestData.data},
                        {"name": "sendToList","value":sendToList},
                        {"name": "jwt","value":requestData.token},
                        {"name": "origin","value":originURL}
                    ];
        logger:info(variables);
        messages:setHeader(requestMessage,"Authorization",bpmnBasicAuthToken);
        requestPayloadJson = {
                                 "processDefinitionKey": "libraryApprovalProcess",
                                 "businessKey": "myBusinessKey",
                                 "tenantId": "-1234",
                                 "variables":variables
                             };
        messages:setJsonPayload(requestMessage,requestPayloadJson);
        responseFromBpmn = http:ClientConnector.post(httpConnectorBpmn,url,requestMessage);
        responseFromBpmnJson = messages:getJsonPayload(responseFromBpmn);
        statusCode = http:getStatusCode(responseFromBpmn);
        logger:info(statusCode);

        if(statusCode == 201 || statusCode == 200){

            processInstanceId,_ = <int>jsons:toString(responseFromBpmnJson.id);
            taskId = getTaskIdFromProcessId(processInstanceId);
            libraryName = jsons:toString(requestData.data.libName);
            libraryUseVersion = jsons:toString(requestData.data.libUseVersion);
            databaseUpdateReturnValue = database:libraryRequestUpdateTaskAndProcessIds(taskId,processInstanceId,libraryName,libraryUseVersion);

            if(databaseUpdateReturnValue > 0){
                responseJson = {"responseType":"Done","responseMessage":"Successfully submit library request"};
                logger:info("Successfully submit library request");
            }else{
                responseJson = {"responseType":"Error","responseMessage":"Task ID and Process ID update fails"};
                logger:error("Library request : Task and Process IDs update fails");
            }

        }else{
            responseJson = {"responseType":"Error","responseMessage":"BPMN Error occurs"};
            logger:error("Library request : BPMN Error occurs");
        }


    }catch(errors:Error err){
        responseJson = {"responseType":"Error","responseMessage":err.msg};
        logger:error(err.msg);

    }
    return;
}

@doc:Description {value:"Get task ID from given process ID"}
@doc:Param {value:"processId: Process ID"}
@doc:Param {value:"taskId: Task ID"}
function getTaskIdFromProcessId(int processId)(int taskId){

    message requestForBpmn = {};
    message responseFromBpmn = {};
    json responseFromBpmnJson;
    string url;

    int currentProcessId;
    int responseFromBpmnJsonLength = 0;
    int i = 0;

    try{
        if(httpConnectorBpmn == null){
            setBpmnConnection();
        }
        messages:setHeader(requestForBpmn,"Authorization",bpmnBasicAuthToken);
        url = "bpmn/runtime/tasks/";
        responseFromBpmn = httpConnectorBpmn.get(url,requestForBpmn);
        responseFromBpmnJson = messages:getJsonPayload(responseFromBpmn);
        responseFromBpmnJson = responseFromBpmnJson.data;
        responseFromBpmnJsonLength = lengthof responseFromBpmnJson;

        while(i < responseFromBpmnJsonLength){
            currentProcessId,_ = <int>jsons:toString(responseFromBpmnJson[i].processInstanceId);
            if(currentProcessId == processId){
                taskId,_ = <int>jsons:toString(responseFromBpmnJson[i].id);
                return;
            }
            i = i + 1;
        }
    }catch(errors:Error err){
        taskId = 0;
        logger:error(" getTaskIdFromProcessId :" + err.msg);

    }
    taskId = 0;
    return;
}

@doc:Description {value:"Accept particular 3rd party library request"}
@doc:Param {value:"taskId: Task ID of business process"}
@doc:Param {value:"responseJson: Return JSON of BPMN"}
function acceptLibraryRequest(string taskId, string token)(json responseJson){
    message response = {};
    message request = {};
    json requestJson;
    json variables;
    string url;
    int statusCode;
    try{
        if(httpConnectorBpmn == null){
            setBpmnConnection();
        }
        variables = [
                        {
                            "name": "outputType",
                            "value": "Done"
                        },
                        {
                            "name": "adminJwt",
                            "value": token
                        }
                    ];
        requestJson = {
                          "action": "complete",
                          "variables": variables
                      };

        messages:setJsonPayload(request,requestJson);
        messages:setHeader(request,"Authorization",bpmnBasicAuthToken);
        url = "bpmn/runtime/tasks/" + taskId;
        response = httpConnectorBpmn.post(url,request);
        logger:info(response);
        logger:info(http:getStatusCode(response));
        statusCode = http:getStatusCode(response);
        if(statusCode == 200 || statusCode == 201){
            responseJson = {"responseType":"Done","responseMessage":"Successfully accept library request"};
            logger:info("Successfully accept library request");
        }else{
            responseJson = {"responseType":"Error","responseMessage":"Accept library request fails"};
            logger:info("Accept library request fails");
        }

    }catch(errors:Error err){
        logger:error(err.msg);
        responseJson = {"responseType":"Error","responseMessage":err.msg};

    }
    return;
}

@doc:Description {value:"Reject particular 3rd party library request"}
@doc:Param {value:"taskId: Task ID of business process"}
@doc:Param {value:"responseJson: Return JSON of BPMN"}
function rejectLibraryRequest(string taskId,string rejectBy, string reasonForRejecting)(json responseJson){
    message response = {};
    message request = {};
    json requestJson;
    json variables;
    string url;
    int statusCode;
    try{
        if(httpConnectorBpmn == null){
            setBpmnConnection();
        }
        variables = [
                        {
                            "name": "outputType",
                            "value": "Reject"
                        },
                        {
                            "name": "rejectBy",
                            "value": rejectBy
                        },
                        {
                            "name": "reasonForReject",
                            "value": reasonForRejecting
                        }
                    ];
        requestJson = {
                          "action": "complete",
                          "variables": variables
                      };

        messages:setJsonPayload(request,requestJson);
        messages:setHeader(request,"Authorization",bpmnBasicAuthToken);
        url = "bpmn/runtime/tasks/" + taskId;
        response = httpConnectorBpmn.post(url,request);
        statusCode = http:getStatusCode(response);
        if(statusCode == 200 || statusCode == 201){
            responseJson = {"responseType":"Done","responseMessage":"Successfully reject library request"};
            logger:info("Successfully reject library request");
        }else{
            responseJson = {"responseType":"Error","responseMessage":"Reject library request fails"};
            logger:info("Reject library request fails");
        }
    }catch(errors:Error err){
        logger:error(err.msg);
        responseJson = {"responseType":"Error","responseMessage":err.msg};

    }
    return;
}

