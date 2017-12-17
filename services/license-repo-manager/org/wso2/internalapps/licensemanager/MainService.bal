package org.wso2.internalapps.licensemanager;

import ballerina.net.http;
import org.wso2.internalapps.licensemanager.services;
import org.wso2.internalapps.licensemanager.database;
import ballerina.lang.messages;
import ballerina.lang.jsons;
import ballerina.lang.strings;
import ballerina.utils.logger;
import org.wso2.internalapps.licensemanager.conf;



@http:configuration {basePath:"/",httpsPort: 9090, keyStoreFile: "${ballerina.home}/bre/security/wso2carbon.jks",
                     keyStorePass: "wso2carbon", certPass: "wso2carbon"}


service<http> MainService {

    string ORIGIN = conf:getConfigData("ORIGIN");
    string BPMN_ORIGIN = conf:getConfigData("BPMN_ORIGIN");

    @http:POST {}
    @http:Path {value:"/createRepositories"}
    resource createRepositories (message m) {

        message response = {};

        json responseDataFromDbJson;
        json responseGitHubJson = null;
        json finalResponseJson = {"responseType":"Done","responseMessage":" ","toSend":" "};
        json inValidUserJson = {"data":[],"responseType":"Error","responseMessage":"Invalid user"};
        json requestDataJson;
        json teamJson;
        string responseType;
        string teamName;
        string teamId;
        int repositoryId;

        if(services:validateUserTokenRepository(m)){


            requestDataJson = messages:getJsonPayload(m);
            repositoryId,_ = <int> jsons:toString(requestDataJson.repositoryId);
            responseGitHubJson = services:createGitHubRepository(repositoryId);
            responseDataFromDbJson = database:repositorySelectFromId(repositoryId);

            teamId = jsons:toString(responseDataFromDbJson[0].REPOSITORY_TEAM);
            teamJson = services:getTeamsFromId(teamId);
            teamName = jsons:toString(teamJson.name);

            responseType = jsons:toString(responseGitHubJson.responseType);
            if(responseType == "Done"){
                finalResponseJson = {"responseType":"Done","responseMessage":"Done","responseDefault":"Done","repoUpdatedDetails":responseDataFromDbJson[0],"teamName":teamName};
            }else{
                finalResponseJson = {"responseType":"Error","responseMessage":"Error","responseDefault":"Done","repoUpdatedDetails":responseDataFromDbJson[0],"teamName":teamName};
            }
            logger:info(finalResponseJson);
            messages:setJsonPayload(response,finalResponseJson);


        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        reply response;
    }

    @http:POST {}
    @http:Path {value:"/gitHub/setIssueTemplate"}
    resource gitHubSetIssueTemplateResource(message m){

        message response = {};
        json inValidUserJson = {"data":[],"responseType":"Error","responseMessage":"Invalid user"};
        json requestJson;
        json userJson;
        json responseJson;
        string organization;
        string repositoryName;
        string acceptedByEmail;
        string acceptedByName;

        if(services:validateUserTokenRepository(m)){
            requestJson = messages:getJsonPayload(m);
            organization = jsons:toString(requestJson.organization);
            repositoryName = jsons:toString(requestJson.repositoryName);
            acceptedByEmail = jsons:toString(requestJson.acceptedByEmail);
            userJson = database:roleRepositoryCheckAdminUsers(acceptedByEmail);
            acceptedByName = jsons:toString(userJson[0].ROLE_NAME);
            responseJson = services:setIssueTemplate(organization,repositoryName,acceptedByEmail,acceptedByName);
            messages:setJsonPayload(response,responseJson);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        reply response;
    }

    @http:POST {}
    @http:Path {value:"/gitHub/setPullRequestTemplate"}
    resource gitHubSetPullRequestTemplateResource(message m){

        message response = {};
        json requestJson;
        json inValidUserJson = {"data":[],"responseType":"Error","responseMessage":"Invalid user"};
        json userJson;
        json responseJson;
        string organization;
        string repositoryName;
        string acceptedByEmail;
        string acceptedByName;

        if(services:validateUserTokenRepository(m)){
            requestJson = messages:getJsonPayload(m);
            organization = jsons:toString(requestJson.organization);
            repositoryName = jsons:toString(requestJson.repositoryName);
            acceptedByEmail = jsons:toString(requestJson.acceptedByEmail);
            userJson = database:roleRepositoryCheckAdminUsers(acceptedByEmail);
            acceptedByName = jsons:toString(userJson[0].ROLE_NAME);
            responseJson = services:setPullRequestTemplate(organization,repositoryName,acceptedByEmail,acceptedByName);
            messages:setJsonPayload(response,responseJson);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        reply response;
    }

    @http:POST {}
    @http:Path {value:"/gitHub/setDefaultTeam"}
    resource gitHubSetDefaultTeamResource(message m){

        message response = {};
        json requestJson;
        json inValidUserJson = {"data":[],"responseType":"Error","responseMessage":"Invalid user"};
        json userJson;
        json responseJson;
        string organization;
        string repositoryName;
        string teamId;

        if(services:validateUserTokenRepository(m)){
            requestJson = messages:getJsonPayload(m);
            organization = jsons:toString(requestJson.organization);
            repositoryName = jsons:toString(requestJson.repositoryName);
            teamId = jsons:toString(requestJson.teamId);


            responseJson = services:setDefaultTeam(organization,repositoryName,teamId);
            messages:setJsonPayload(response,responseJson);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        reply response;
    }

    @http:POST {}
    @http:Path {value:"/gitHub/setReadMe"}
    resource gitHubSetReadMeResource(message m){

        message response = {};
        json requestJson;
        json responseJson;
        json inValidUserJson = {"data":[],"responseType":"Error","responseMessage":"Invalid user"};
        json userJson;
        string organization;
        string repositoryName;
        string repositoryDescription;
        string acceptedByEmail;
        string acceptedByName;

        if(services:validateUserTokenRepository(m)){
            requestJson = messages:getJsonPayload(m);
            organization = jsons:toString(requestJson.organization);
            repositoryName = jsons:toString(requestJson.repositoryName);
            repositoryDescription = jsons:toString(requestJson.repositoryDescription);
            acceptedByEmail = jsons:toString(requestJson.acceptedByEmail);
            userJson = database:roleRepositoryCheckAdminUsers(acceptedByEmail);
            acceptedByName = jsons:toString(userJson[0].ROLE_NAME);
            responseJson = services:setReadMe(organization,repositoryName,repositoryDescription,acceptedByEmail,acceptedByName);
            messages:setJsonPayload(response,responseJson);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response, "Access-Control-Allow-Methods", "GET, OPTIONS");
        messages:setHeader(response,"Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        reply response;
    }

    @http:GET {}
    @http:Path {value:"/bpmn/getTasks"}
    resource bpmnGetTask(message m){

        message response = {};
        int no = 0;
        no = services:getTaskIdFromProcessId(1);
        logger:info(no);

        messages:setHeader(response,"Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        reply response;
    }

    @http:GET {}
    @http:Path {value:"/gitHub/getTeams"}
    resource gitHubGetTeams(message m,@http:QueryParam {value:"organization"} string organization){

        message response = {};
        json inValidUserJson = {"data":[],"responseType":"Error","responseMessage":"Invalid user"};

        if(services:getIsValidUser(m)){
            response = services:getTeamsFromOrganization(organization);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        reply response;
    }

    @http:GET {}
    @http:Path {value:"/gitHub/getTeamsFromId"}
    resource gitHubGetTeamsFromId(message m,@http:QueryParam {value:"teamId"} string teamId){

        message response = {};
        json responseJson;
        json inValidUserJson = {"data":[],"responseType":"Error","responseMessage":"Invalid user"};

        if(services:getIsValidUser(m)){
            responseJson = services:getTeamsFromId(teamId);
            messages:setJsonPayload(response,responseJson);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        reply response;
    }

    @http:POST {}
    @http:Path {value:"/createNexus"}
    resource createNexus (message m) {

        message response = {};
        json inValidUserJson = {"data":[],"responseType":"Error","responseMessage":"Invalid user"};
        json requestJson;
        json responseJson;
        string groupId;

        if(services:validateUserTokenRepository(m)){
            requestJson = messages:getJsonPayload(m);
            groupId = jsons:toString(requestJson.id);
            responseJson = services:createNexus(groupId,groupId);
            messages:setJsonPayload(response,responseJson);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");

        reply response;

    }

    @http:POST {}
    @http:Path {value:"/createNexusRepositoryTarget"}
    resource createNexusRepositoryTargetResource (message m) {

        message response = {};
        json inValidUserJson = {"data":[],"responseType":"Error","responseMessage":"Invalid user"};
        json requestJson;
        json responseJson;
        string groupId;
        string contentClass;
        string pattern;

        if(services:validateUserTokenRepository(m)){

            requestJson = messages:getJsonPayload(m);
            groupId = jsons:toString(requestJson.groupId);
            contentClass = jsons:toString(requestJson.contentClass);
            pattern = strings:replace(groupId,".","/");
            pattern = " .*/" + pattern + "/.* ";
            responseJson = services:createNexusRepositoryTarget(groupId,groupId,contentClass,pattern);
            messages:setJsonPayload(response,responseJson);

        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        reply response;

    }

    @http:POST {}
    @http:Path {value:"/createNexusStagingProfile"}
    resource createNexusStagingProfileResource (message m) {

        message response = {};
        json inValidUserJson = {"data":[],"responseType":"Error","responseMessage":"Invalid user"};
        json requestJson;
        json responseJson;
        string groupId;

        if(services:validateUserTokenRepository(m)){

            requestJson = messages:getJsonPayload(m);
            groupId = jsons:toString(requestJson.groupId);
            responseJson = services:createNexusStagingProfile(groupId);
            messages:setJsonPayload(response,responseJson);

        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        reply response;

    }

    @http:POST {}
    @http:Path {value:"/createJenkins"}
    resource createJenkins (message m) {

        message response = {};
        json inValidUserJson = {"data":[],"responseType":"Error","responseMessage":"Invalid user"};
        json requestJson;
        json responseJson;
        string jenkinsJobName;


        if(services:validateUserTokenRepository(m)){

            requestJson = messages:getJsonPayload(m);
            jenkinsJobName = jsons:toString(requestJson.name);

            responseJson = services:createJenkinsJob(jenkinsJobName);
            messages:setJsonPayload(response,responseJson);

        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        reply response;
    }

    @http:GET {}
    @http:Path {value:"/getAllLanguages"}
    resource getAllLanguages (message m) {

        message response = {};
        json inValidUserJson = {"responseType":"Invalid User"};

        if(services:getIsValidUser(m)){
            response = services:getAllLanguages();

        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        reply response;
    }

    @http:POST {}
    @http:Path {value:"/databaseService/repository/insertData"}
    resource repositoryInsertDataResource(message m){

        message response = {};
        json requestJson = messages:getJsonPayload(m);
        json inValidUserJson = {"responseType":"Invalid User"};
        json responseJson;
        json getInsertedDataJson;
        string name;
        string language;
        string requestBy;
        string description;
        string groupId;
        int license;
        int team;
        int organization;
        int repoType;
        int responseValue;
        boolean buildable;
        boolean nexus;
        boolean private;

        if(services:validateUserToken(m)){

            name = jsons:toString(requestJson.data[0]);
            language = jsons:toString(requestJson.data[1]);
            buildable,_ =  <boolean>(jsons:toString(requestJson.data[2]));
            nexus,_ = <boolean>(jsons:toString(requestJson.data[3]));
            private,_ = <boolean>(jsons:toString(requestJson.data[4]));
            description = jsons:toString(requestJson.data[5]);
            groupId = jsons:toString(requestJson.data[6]);
            license,_ = <int>(jsons:toString(requestJson.data[7]));
            team,_ = <int>(jsons:toString(requestJson.data[8]));
            organization,_ = <int>(jsons:toString(requestJson.data[9]));
            repoType,_ = <int>(jsons:toString(requestJson.data[10]));
            requestBy = jsons:toString(requestJson.data[11]);
            responseValue = database:repositoryInsertData(name,language,buildable,nexus,private,description,groupId,license,team,organization,repoType,requestBy);

            if(responseValue > 0){
                getInsertedDataJson = database:repositorySelectFromName(name);
                responseJson = {"responseType":"Done","responseMessage":" ","repositoryId":jsons:toString(getInsertedDataJson[0].REPOSITORY_ID)};
            }else{
                responseJson = {"responseType":"Error","responseMessage":" "};
            }

            messages:setJsonPayload(response,responseJson);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }



        messages:setHeader(response,"Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        reply response;
    }

    @http:POST {}
    @http:Path {value:"/databaseService/repository/updateBpmnAndTaskIds"}
    resource updateBpmnAndTaskIdsResource(message m){

        message response = {};
        json requestJson = messages:getJsonPayload(m);
        json responseJson;
        json inValidUserJson = {"data":[],"responseType":"Error","responseMessage":"Invalid user"};
        string repositoryName;
        int taskId;
        int processId;
        int responseValue;


        if(services:getIsValidUser(m)){

            taskId,_ = <int>(jsons:toString(requestJson.data[0]));
            processId,_ = <int>(jsons:toString(requestJson.data[1]));
            repositoryName = jsons:toString(requestJson.data[2]);
            responseValue = database:repositoryUpdateTaskAndProcessIds(taskId,processId,repositoryName);

            if(responseValue > 0){
                responseJson = {"responseType":"Done","responseMessage":" "};
            }else{
                responseJson = {"responseType":"Error","responseMessage":" "};
            }

            messages:setJsonPayload(response,responseJson);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        reply response;
    }

    @http:POST {}
    @http:Path {value:"/databaseService/repository/updateRejectDetails"}
    resource updateRejectDetailsResource(message m){

        message response = {};
        json requestJson = messages:getJsonPayload(m);
        json responseJson;
        json inValidUserJson = {"data":[],"responseType":"Error","responseMessage":"Invalid user"};
        string rejectBy;
        string rejectReason;
        int repositoryId;
        int responseValue;


        if(services:getIsValidUser(m)){

            rejectBy = jsons:toString(requestJson.data[0]);
            rejectReason = jsons:toString(requestJson.data[1]);
            repositoryId,_ = <int>(jsons:toString(requestJson.data[2]));
            responseValue = database:repositoryUpdateRejectDetails(rejectBy,rejectReason,repositoryId);

            if(responseValue > 0){
                responseJson = {"responseType":"Done","responseMessage":" "};
            }else{
                responseJson = {"responseType":"Error","responseMessage":" "};
            }

            messages:setJsonPayload(response,responseJson);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        reply response;
    }

    @http:POST {}
    @http:Path {value:"/databaseService/repository/updateAll"}
    resource repositoryUpdateAllResource(message m){

        message response = {};
        json requestJson = messages:getJsonPayload(m);
        json responseJson;
        string description;
        string groupId;
        string name;
        string language;
        string acceptBy;
        int license;
        int team;
        int organization;
        int repoType;
        int repositoryId;
        int responseValue;
        boolean buildable;
        boolean nexus;
        boolean private;
        boolean accept;

        json inValidUserJson = {"data":[],"responseType":"Error","responseMessage":"Invalid user"};
        if(services:getIsValidUser(m)){
            name = jsons:toString(requestJson.data[0]);
            language = jsons:toString(requestJson.data[1]);
            buildable,_ =  <boolean>(jsons:toString(requestJson.data[2]));
            nexus,_ = <boolean>(jsons:toString(requestJson.data[3]));
            private,_ = <boolean>(jsons:toString(requestJson.data[4]));
            description = jsons:toString(requestJson.data[5]);
            groupId = jsons:toString(requestJson.data[6]);
            license,_ = <int>(jsons:toString(requestJson.data[7]));
            team,_ = <int>(jsons:toString(requestJson.data[8]));
            organization,_ = <int>(jsons:toString(requestJson.data[9]));
            repoType,_ = <int>(jsons:toString(requestJson.data[10]));
            accept,_ = <boolean>(jsons:toString(requestJson.data[11]));
            acceptBy = jsons:toString(requestJson.data[12]);
            repositoryId,_ = <int>(jsons:toString(requestJson.repoId));

            responseValue = database:repositoryUpdateAll(name,language,buildable,nexus,private,description,groupId,license,team,organization,repoType,accept,acceptBy,repositoryId);



            if(responseValue > 0){
                responseJson = {"responseType":"Done","responseMessage":" "};
            }else{
                responseJson = {"responseType":"Error","responseMessage":" "};
            }

            messages:setJsonPayload(response,responseJson);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        reply response;
    }

    @http:POST {}
    @http:Path {value:"/databaseService/component/insertData"}
    resource componentInsertDataResource(message m){

        message response = {};
        json requestJson = messages:getJsonPayload(m);
        json responseJson;
        json inValidUserJson = {"data":[],"responseType":"Error","responseMessage":"Invalid user"};
        string componentKey;
        string componentUrl;
        int responseValue;


        if(services:getIsValidUser(m)){

            componentKey = jsons:toString(requestJson.componentKey);
            componentUrl = jsons:toString(requestJson.componentUrl);

            responseValue = database:componentInsertData(componentKey,componentUrl);

            if(responseValue > 0){
                responseJson = {"responseType":"Done","responseMessage":" "};
            }else{
                responseJson = {"responseType":"Error","responseMessage":" "};
            }

            messages:setJsonPayload(response,responseJson);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        reply response;
    }

    @http:GET {}
    @http:Path {value:"/databaseService/repository/selectAll"}
    resource repositorySelectAllResource(message m){

        message response = {};
        json responseJson;
        json inValidUserJson = {"data":[],"responseType":"Error","responseMessage":"Invalid user"};

        if(services:getIsValidUser(m)){
            responseJson = database:repositorySelectAll();
            messages:setJsonPayload(response,responseJson);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        reply response;
    }

    @http:GET {}
    @http:Path {value:"/databaseService/repository/selectFromName"}
    resource repositorySelectFromNameResource(message m,@http:QueryParam {value:"name"} string name){

        message response = {};
        json responseJson;
        json inValidUserJson = {"data":[],"responseType":"Error","responseMessage":"Invalid user"};

        if(services:getIsValidUser(m)){
            responseJson = database:repositorySelectFromName(name);
            messages:setJsonPayload(response,responseJson);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        reply response;
    }

    @http:GET {}
    @http:Path {value:"/databaseService/repository/selectFromId"}
    resource repositorySelectFromIdResource(message m,@http:QueryParam {value:"id"} int id){

        message response = {};
        json responseJson = {"responseType":"Error","responseMessage":"Invalid user"};


        if(services:getIsValidUser(m)){
            responseJson = database:repositorySelectFromId(id);
        }else{
            responseJson = {"responseType":"Error","responseMessage":"Invalid user"};
        }
        messages:setJsonPayload(response,responseJson);
        messages:setHeader(response,"Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        reply response;
    }

    @http:GET {}
    @http:Path {value:"/databaseService/repository/selectFromRequestByAndWaiting"}
    resource repositorySelectFromRequestByResource(message m,@http:QueryParam {value:"requestBy"} string requestBy){

        message response = {};
        json responseJson;
        json inValidUserJson = {"data":[],"responseType":"Error","responseMessage":"Invalid user"};

        if(services:getIsValidUser(m)){
            responseJson = database:repositorySelectFromRequestByAndWaiting(requestBy);
            messages:setJsonPayload(response,responseJson);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        reply response;
    }

    @http:GET {}
    @http:Path {value:"/databaseService/libraryRequest/selectFromRequestByAndWaiting"}
    resource libRequestSelectFromRequestByResource(message m,@http:QueryParam {value:"requestBy"} string requestBy){

        message response = {};
        json responseJson;
        json inValidUserJson = {"data":[],"responseType":"Error","responseMessage":"Invalid user"};

        if(services:getIsValidUser(m)){
            responseJson = database:libraryRequestSelectWaitingRequestsFromRequestBy(requestBy);
            messages:setJsonPayload(response,responseJson);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        reply response;
    }

    @http:GET {}
    @http:Path {value:"/databaseService/repository/selectWaitingRequests"}
    resource repositorySelectWaitingRequests(message m){

        message response = {};
        json responseJson;
        json inValidUserJson = {"data":[],"responseType":"Error","responseMessage":"Invalid user"};

        if(services:getIsValidUser(m)){
            responseJson = database:repositorySelectWaitingRequests();
            messages:setJsonPayload(response,responseJson);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        reply response;
    }

    @http:GET {}
    @http:Path {value:"/databaseService/license/selectAll"}
    resource licenseSelectAllResource(message m){

        message response = {};
        json responseJson;
        json inValidUserJson = {"data":[],"responseType":"Error","responseMessage":"Invalid user"};

        if(services:getIsValidUser(m)){
            responseJson = database:licenseSelectAll();
            messages:setJsonPayload(response,responseJson);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        reply response;
    }

    @http:GET {}
    @http:Path {value:"/databaseService/organization/selectAll"}
    resource organizationSelectAllResource(message m){

        message response = {};
        json responseJson;
        json inValidUserJson = {"data":[],"responseType":"Error","responseMessage":"Invalid user"};

        if(services:getIsValidUser(m)){
            responseJson = database:organizationSelectAll();
            messages:setJsonPayload(response,responseJson);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        reply response;
    }

    @http:GET {}
    @http:Path {value:"/databaseService/repoType/selectAll"}
    resource typeSelectAllResource(message m){

        message response = {};
        json responseJson;
        json inValidUserJson = {"data":[],"responseType":"Error","responseMessage":"Invalid user"};

        if(services:getIsValidUser(m)){
            responseJson = database:repositoryTypeSelectAll();
            messages:setJsonPayload(response,responseJson);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        reply response;
    }

    @http:GET {}
    @http:Path {value:"/databaseService/component/selectAll"}
    resource componentSelectAllResource(message m){

        message response = {};
        json responseJson;
        json inValidUserJson = {"data":[],"responseType":"Error","responseMessage":"Invalid user"};

        if(services:getIsValidUser(m)){
            responseJson = database:componentSelectAll();
            messages:setJsonPayload(response,responseJson);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        reply response;
    }

    @http:GET {}
    @http:Path {value:"/databaseService/role/selectRepositoryAdminUsers"}
    resource roleSelectRepositoryAdminUsersResource(message m){

        message response = {};
        json responseJson;
        json inValidUserJson = {"data":[],"responseType":"Error","responseMessage":"Invalid user"};

        if(services:getIsValidUser(m)){
            responseJson = database:roleSelectRepositoryAdminUsers();
            messages:setJsonPayload(response,responseJson);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        reply response;
    }

    @http:GET {}
    @http:Path {value:"/databaseService/role/selectRepositoryMainUsers"}
    resource roleSelectRepositoryMainUsersResource(message m){

        message response = {};
        json responseJson;
        json inValidUserJson = {"data":[],"responseType":"Error","responseMessage":"Invalid user"};
        if(services:getIsValidUser(m)){
            responseJson = database:roleSelectRepositoryMainUsers();
            messages:setJsonPayload(response,responseJson);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        reply response;
    }

    @http:GET {}
    @http:Path {value:"/databaseService/role/checkRepositoryAdminUser"}
    resource userCheckAdminUsersResource(message m,@http:QueryParam {value:"email"} string email){

        message response = {};
        json inValidUserJson = {"data":[],"responseType":"Error","responseMessage":"Invalid user"};
        json responseJson;

        if(services:getIsValidUser(m)){
            responseJson = database:roleRepositoryCheckAdminUsers(email);
            messages:setJsonPayload(response,responseJson);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        reply response;
    }

    @http:GET {}
    @http:Path {value:"/databaseService/role/selectLibraryMainUsers"}
    resource roleSelectLibraryMainUsersResource(message m){

        message response = {};
        json responseJson;
        json inValidUserJson = {"data":[],"responseType":"Error","responseMessage":"Invalid user"};

        if(services:getIsValidUser(m)){
            responseJson = database:roleSelectLibraryMainUsers();
            messages:setJsonPayload(response,responseJson);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        reply response;
    }

    @http:GET {}
    @http:Path {value:"/databaseService/libCategory/selectAll"}
    resource roleSelectLibraryCategoriesResource(message m){

        message response = {};
        json responseJson;
        json inValidUserJson = {"data":[],"responseType":"Error","responseMessage":"Invalid user"};
        if(services:getIsValidUser(m)){

            logger:info("valid");
            responseJson = database:libCategorySelectAll();
            logger:info(responseJson);
            messages:setJsonPayload(response,responseJson);
        }else{
            logger:info("invalid");
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        reply response;
    }

    @http:POST {}
    @http:Path {value:"/authentication/isBpmnValidUser"}
    resource authenticateIsBpmnValidUsersResource(message m){

        message response = {};
        message requestForService = {};
        string sessionId;
        json responseJson;

        requestForService = m;
        logger:info(messages:getHeader(m,"Origin"));
        response, sessionId = services:validateUser(requestForService);
        sessionId = "BSESSIONID=" + sessionId;
        responseJson = {"sessionId":sessionId};
        messages:setJsonPayload(response,responseJson);
        messages:setHeader(response, "Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        logger:info(response);
        reply response;
    }

    @http:POST {}
    @http:Path {value:"/authentication/isValidUser"}
    resource authenticateIsValidUsersResource(message m){

        message response = {};
        string sessionId;


        response, sessionId = services:validateUser(m);
        messages:setHeader(response, "Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        logger:info(response);
        reply response;
    }

    @http:GET{}
    @http:Path {value:"/authentication/getUserDetails"}
    resource authenticateGetSessionDetails(message m){

        message response = {};
        json responseJson = services:getSessionDetails(m);
        messages:setJsonPayload(response,responseJson);
        messages:setHeader(response, "Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        reply response;
    }

    @http:GET {}
    @http:Path {value:"/databaseService/jenkinsFolder/selectFolders"}
    resource jenkinsSelectFromNameRegex(message m,@http:QueryParam {value:"name"} string name){

        message response = {};
        json responseJson = database:jenkinsFolderMatchRegex(name);
        messages:setJsonPayload(response,responseJson);
        messages:setHeader(response,"Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        reply response;
    }

    @http:POST {}
    @http:Path {value:"/bpmn/library/request"}
    resource requestLibraryResource(message m){

        message response = {};
        json requestJson = messages:getJsonPayload(m);
        json responseJson;
        json inValidUserJson = {"data":[],"responseType":"Error","responseMessage":"Invalid user"};


        if(services:getIsValidUser(m)){
            responseJson = services:bpmnRequestLibrary(requestJson);
            messages:setJsonPayload(response,responseJson);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        reply response;
    }

    @http:POST {}
    @http:Path {value:"/bpmn/library/accept"}
    resource acceptLibraryRequestResource(message m){

        message response = {};
        int id;
        int i = 0;
        int userDetailsLength;
        int dbResponseValue;
        boolean valid = false;
        json requestJson = messages:getJsonPayload(m);
        json responseJson;
        json inValidUserJson = {"data":[],"responseType":"Error","responseMessage":"Invalid user"};
        json userDetails;
        json libraryRequestDetails;
        string taskId;
        string accept;
        string acceptBy;
        string jwt;

        id,_ = <int>jsons:toString(requestJson.libraryRequestId);
        jwt = jsons:toString(requestJson.token);
        userDetails = services:getSessionDetails(m);
        acceptBy = jsons:toString(userDetails.userEmail);
        userDetails = userDetails.libraryUserDetails;
        userDetailsLength = lengthof userDetails;
        libraryRequestDetails = database:libraryRequestSelectFromId(id);
        while(i < userDetailsLength){
            if((jsons:toString(userDetails[i].roleLibType) == jsons:toString(libraryRequestDetails[0].LIBCATEGORY_NAME)) && jsons:toString(userDetails[i].rolePermission) == "ADMIN" ){
                valid = true;
                taskId = jsons:toString(libraryRequestDetails[0].LIBREQUEST_BPMN_TASK_ID);

                break;
            }else{
                valid = false;
            }
            i = i + 1;
        }


        if(valid){
            accept = "ACCEPT";
            dbResponseValue = database:libraryRequestUpdateAcceptOrRejectDetails(accept,acceptBy,"",id);
            responseJson = services:acceptLibraryRequest(taskId, jwt);
            messages:setJsonPayload(response,responseJson);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        reply response;
    }

    @http:POST {}
    @http:Path {value:"/bpmn/library/reject"}
    resource rejectLibraryRequestResource(message m){

        message response = {};
        int id;
        int i = 0;
        int userDetailsLength;
        int dbResponseValue;
        boolean valid = false;
        json requestJson = messages:getJsonPayload(m);
        json responseJson;
        json inValidUserJson = {"data":[],"responseType":"Error","responseMessage":"Invalid user"};
        json userDetails;
        json libraryRequestDetails;
        string taskId;
        string accept;
        string acceptBy;
        string rejectReason;

        id,_ = <int>jsons:toString(requestJson.libraryRequestId);
        rejectReason = jsons:toString(requestJson.libraryRejectReason);
        userDetails = services:getSessionDetails(m);
        acceptBy = jsons:toString(userDetails.userEmail);
        userDetails = userDetails.libraryUserDetails;
        userDetailsLength = lengthof userDetails;
        libraryRequestDetails = database:libraryRequestSelectFromId(id);
        while(i < userDetailsLength){
            if((jsons:toString(userDetails[i].roleLibType) == jsons:toString(libraryRequestDetails[0].LIBCATEGORY_NAME)) && jsons:toString(userDetails[i].rolePermission) == "ADMIN" ){
                valid = true;
                taskId = jsons:toString(libraryRequestDetails[0].LIBREQUEST_BPMN_TASK_ID);

                break;
            }else{
                valid = false;
            }
            i = i + 1;
        }


        if(valid){
            accept = "REJECT";
            dbResponseValue = database:libraryRequestUpdateAcceptOrRejectDetails(accept,acceptBy,rejectReason,id);
            responseJson = services:rejectLibraryRequest(taskId,acceptBy,rejectReason);
            messages:setJsonPayload(response,responseJson);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        reply response;
    }

    @http:POST {}
    @http:Path {value:"/bpmn/repository/request"}
    resource requestRepositoryResource(message m){

        message response = {};
        logger:info(m);
        json requestJson = messages:getJsonPayload(m);
        json data;
        json mailData;
        json responseJson;
        json inValidUserJson = {"data":[],"responseType":"Error","responseMessage":"Invalid user"};
        string jwToken;

        if(services:getIsValidUser(m)){
            jwToken = jsons:toString(requestJson.token);
            data = requestJson.repositoryData;
            mailData = requestJson.repositoryMailData;
            responseJson = services:bpmnRequestRepository(data,mailData,jwToken);
            messages:setJsonPayload(response,responseJson);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        reply response;
    }

    @http:POST {}
    @http:Path {value:"/bpmn/repository/accept"}
    resource acceptRepositoryRequestResource(message m){

        message response = {};
        json requestJson = messages:getJsonPayload(m);
        json responseJson;
        json inValidUserJson = {"data":[],"responseType":"Error","responseMessage":"Invalid user"};
        json userDetails;
        string acceptBy;
        string description;
        string groupId;
        string name;
        string language;
        string repoIdString;
        string taskIdString;
        string jwToken;
        int taskId;
        int license;
        int team;
        int organization;
        int repoType;
        int repositoryId;
        int id;
        int dbResponseValue;
        boolean buildable;
        boolean nexus;
        boolean private;
        boolean accept;
        boolean valid = false;


        id,_ = <int>jsons:toString(requestJson.repositoryId);
        userDetails = services:getSessionDetails(m);
        valid, _ = <boolean>jsons:toString(userDetails.isRepositoryAdmin);
        acceptBy = jsons:toString(userDetails.userEmail);

        if(valid){
            jwToken = jsons:toString(requestJson.token);
            name = jsons:toString(requestJson.repositoryData[0]);
            language = jsons:toString(requestJson.repositoryData[1]);
            buildable,_ =  <boolean>(jsons:toString(requestJson.repositoryData[2]));
            nexus,_ = <boolean>(jsons:toString(requestJson.repositoryData[3]));
            private,_ = <boolean>(jsons:toString(requestJson.repositoryData[4]));
            description = jsons:toString(requestJson.repositoryData[5]);
            groupId = jsons:toString(requestJson.repositoryData[6]);
            license,_ = <int>(jsons:toString(requestJson.repositoryData[7]));
            team,_ = <int>(jsons:toString(requestJson.repositoryData[8]));
            organization,_ = <int>(jsons:toString(requestJson.repositoryData[9]));
            repoType,_ = <int>(jsons:toString(requestJson.repositoryData[10]));
            accept,_ = <boolean>(jsons:toString(requestJson.repositoryData[11]));
            repositoryId,_ = <int>(jsons:toString(requestJson.repositoryId));
            taskId,_ = <int>(jsons:toString(requestJson.repositoryTaskId));
            repoIdString = jsons:toString(requestJson.repositoryId);
            taskIdString = jsons:toString(requestJson.repositoryTaskId);

            dbResponseValue = database:repositoryUpdateAll(name,language,buildable,nexus,private,description,groupId,license,team,organization,repoType,accept,acceptBy,repositoryId);

            responseJson = services:acceptRepositoryRequest(repoIdString,taskIdString,jwToken);
            messages:setJsonPayload(response,responseJson);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        reply response;
    }

    @http:POST {}
    @http:Path {value:"/bpmn/repository/reject"}
    resource rejectRepositoryRequestResource(message m){

        message response = {};
        json requestJson = messages:getJsonPayload(m);
        json responseJson;
        json inValidUserJson = {"data":[],"responseType":"Error","responseMessage":"Invalid user"};
        json userDetails;
        string repoIdString;
        string taskIdString;
        string rejectReason;
        string rejectBy;
        int id;
        int dbResponseValue;
        boolean valid = false;


        id,_ = <int>jsons:toString(requestJson.repositoryId);
        userDetails = services:getSessionDetails(m);
        valid, _ = <boolean>jsons:toString(userDetails.isRepositoryAdmin);
        rejectBy = jsons:toString(userDetails.userEmail);

        if(valid){

            repoIdString = jsons:toString(requestJson.repositoryId);
            taskIdString = jsons:toString(requestJson.repositoryTaskId);
            rejectReason = jsons:toString(requestJson.repositoryRejectReason);


            dbResponseValue = database:repositoryUpdateRejectDetails(rejectBy,rejectReason,id);

            responseJson = services:rejectRepositoryRequest(taskIdString,rejectBy,rejectReason);
            messages:setJsonPayload(response,responseJson);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        reply response;
    }

    @http:GET {}
    @http:Path {value:"/databaseService/libraryAndRequest/selectFromNameAndVersion"}
    resource librarySelectFromNameResource(message m,@http:QueryParam {value:"name"} string name,@http:QueryParam {value:"version"} string version){

        message response = {};
        json responseJson;
        json inValidUserJson = {"data":[],"responseType":"Error","responseMessage":"Invalid user"};

        if(services:getIsValidUser(m)){
            responseJson = database:libraryAndRequestSelectFromNameAndVersion(name,version);
            messages:setJsonPayload(response,responseJson);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        reply response;
    }

    @http:GET {}
    @http:Path {value:"/databaseService/libraryRequest/selectFromNameAndVersion"}
    resource libraryRequestSelectFromNameAndVersionResource(message m,@http:QueryParam {value:"name"} string name,@http:QueryParam {value:"version"} string version){

        message response = {};
        json responseJson;
        json inValidUserJson = {"data":[],"responseType":"Error","responseMessage":"Invalid user"};

        if(services:getIsValidUser(m)){
            responseJson = database:libraryRequestSelectFromNameAndVersion(name,version);
            messages:setJsonPayload(response,responseJson);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        reply response;
    }

    @http:GET {}
    @http:Path {value:"/databaseService/libType/selectDefault"}
    resource libraryTypeSelectDefaultResource(message m){

        message response = {};
        json responseJson;
        json inValidUserJson = {"data":[],"responseType":"Error","responseMessage":"Invalid user"};

        if(services:getIsValidUser(m)){
            responseJson = database:libTypeSelectDefault();
            messages:setJsonPayload(response,responseJson);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        reply response;
    }

    @http:GET {}
    @http:Path {value:"/databaseService/libType/selectFromCategory"}
    resource libraryTypeSelectFromCategory(message m,@http:QueryParam {value:"id"} int id){

        message response = {};
        json responseJson;
        json inValidUserJson = {"data":[],"responseType":"Error","responseMessage":"Invalid user"};

        if(services:getIsValidUser(m)){
            responseJson = database:libTypeSelectFromCategory(id);
            messages:setJsonPayload(response,responseJson);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        reply response;
    }

    @http:POST {}
    @http:Path {value:"/databaseService/libraryRequest/insertData"}
    resource libraryRequestInsertDataResource(message m){

        message response = {};
        json requestJson = messages:getJsonPayload(m);
        json responseJson;
        json inValidUserJson = {"data":[],"responseType":"Error","responseMessage":"Invalid user"};
        json requestDetails;
        string name;
        int libType;
        int libCategory;
        string groupId;
        string artifactId;
        string useVersion;
        string latestVersion;
        string fileName;
        string company;
        boolean sponsored;
        string purpose;
        string description;
        string alternatives;
        string requestBy;
        string id;
        int responseValue;


        if(services:validateUserToken(m)){

            name = jsons:toString(requestJson.libName);
            libType,_ = <int>jsons:toString(requestJson.libTypeId);
            libCategory,_ = <int>jsons:toString(requestJson.libCategoryId);
            groupId = jsons:toString(requestJson.libGroupId);
            artifactId = jsons:toString(requestJson.libArtifactId);
            useVersion = jsons:toString(requestJson.libUseVersion);
            latestVersion = jsons:toString(requestJson.libLatestVersion);
            fileName = jsons:toString(requestJson.libFileName);
            company = jsons:toString(requestJson.libCompany);
            sponsored,_ = <boolean>jsons:toString(requestJson.libSponsored);
            purpose = jsons:toString(requestJson.libPurpose);
            description = jsons:toString(requestJson.libDescription);
            alternatives = jsons:toString(requestJson.libAlternatives);
            requestBy = jsons:toString(requestJson.libRequestBy);
            responseValue = database:libraryRequestInsertData(name,libType,libCategory,groupId,artifactId,useVersion,latestVersion,fileName,company,sponsored,purpose,description,alternatives,requestBy);


            if(responseValue > 0){
                requestDetails = database:libraryRequestSelectFromNameAndVersion(name,useVersion);
                id = jsons:toString(requestDetails[0].LIBREQUEST_ID);
                responseJson = {"responseType":"Done","responseMessage":" ","libRequestId":id};
            }else{
                responseJson = {"responseType":"Error","responseMessage":" ","libRequestId":" "};
            }

            messages:setJsonPayload(response,responseJson);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        reply response;
    }

    @http:POST {}
    @http:Path {value:"/databaseService/library/insertData"}
    resource libraryInsertDataResource(message m){

        message response = {};
        json requestJson = messages:getJsonPayload(m);
        json responseJson;
        json inValidUserJson = {"data":[],"responseType":"Error","responseMessage":"Invalid user"};
        string name;
        string libType;
        string version;
        string fileName;
        string description;
        string groupId;
        string artifactId;
        int responseValue;
        int libCategory;

        libCategory,_ = <int>jsons:toString(requestJson.libCategoryId);

        if(services:validateUserTokenLibrary(m,libCategory)){

            name = jsons:toString(requestJson.libName);
            libType = jsons:toString(requestJson.libType);
            version = jsons:toString(requestJson.libVersion);
            fileName = jsons:toString(requestJson.libFileName);
            description = jsons:toString(requestJson.libDescription);
            groupId = jsons:toString(requestJson.libGroupId);
            artifactId = jsons:toString(requestJson.libArtifactId);
            responseValue = database:libraryInsertData(name,libType,version,fileName,description,groupId,artifactId);

            if(responseValue > 0){
                responseJson = {"responseType":"Done","responseMessage":" "};
            }else{
                responseJson = {"responseType":"Error","responseMessage":" "};
            }

            messages:setJsonPayload(response,responseJson);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        reply response;
    }

    @http:GET {}
    @http:Path {value:"/databaseService/libraryRequest/selectFromId"}
    resource libraryRequestSelectFromId(message m,@http:QueryParam {value:"id"} int id){

        message response = {};
        json responseJson;
        json inValidUserJson = {"data":[],"responseType":"Error","responseMessage":"Invalid user"};

        if(services:getIsValidUser(m)){
            responseJson = database:libraryRequestSelectFromId(id);
            messages:setJsonPayload(response,responseJson);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        reply response;
    }

    @http:GET {}
    @http:Path {value:"/databaseService/libraryRequest/selectWaitingRequests"}
    resource libraryRequestSelectWaitingRequests(message m){

        message response = {};
        message tempRequest = m;
        json responseJson;
        json inValidUserJson = {"data":[],"responseType":"Error","responseMessage":"Invalid user"};
        json librarySessionDetails;
        json libraryIdArray = [];
        int i = 0;
        int librarySessionDetailsLength;
        boolean isValid = false;

        librarySessionDetails = services:getSessionDetails(tempRequest);
        isValid, _ = <boolean>jsons:toString(librarySessionDetails.isValid);
        if(isValid){
            librarySessionDetails = librarySessionDetails.libraryUserDetails;
            librarySessionDetailsLength = lengthof librarySessionDetails;
            while(i < librarySessionDetailsLength){
                libraryIdArray[i] = librarySessionDetails[i].roleLibId;
                logger:info(libraryIdArray[i]);
                i = i + 1;
            }
            responseJson = database:libraryRequestSelectWaitingRequests(libraryIdArray);
            messages:setJsonPayload(response,responseJson);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        reply response;
    }

    @http:GET {}
    @http:Path {value:"/test"}
    resource test(message m){

        message response = {};
        json responseJson;



        responseJson = {"valid":"HelloB"};
        messages:setJsonPayload(response,responseJson);
        logger:info("Call");


        messages:setHeader(response,"Access-Control-Allow-Origin",ORIGIN);
        messages:setHeader(response, "Access-Control-Allow-Credentials", "true");
        reply response;
    }

}
