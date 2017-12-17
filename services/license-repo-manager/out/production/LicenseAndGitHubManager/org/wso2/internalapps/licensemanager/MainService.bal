package org.wso2.internalapps.licensemanager;

import ballerina.net.http;
import org.wso2.internalapps.licensemanager.services;
import org.wso2.internalapps.licensemanager.database;
import ballerina.lang.messages;
import ballerina.lang.jsons;
import ballerina.lang.strings;
import ballerina.lang.system;



@http:configuration {basePath:"/",httpsPort: 9090, keyStoreFile: "${ballerina.home}/bre/security/wso2carbon.jks",
                     keyStorePass: "wso2carbon", certPass: "wso2carbon"}


service<http> MainService {

    @http:POST {}
    @http:Path {value:"/createRepositories"}

    resource createRepositories (message m) {

        message response = {};
        message responseDataFromDb;
        json responseDataFromDbJson;
        json responseGitHubJson = null;
        json finalResponseJson = {"responseType":"Done","responseMessage":" ","toSend":" "};
        json inValidUserJson = {"responseType":"Error","responseMessage":"Invalid user"};
        json requestDataJson;
        string responseType;
        int repositoryId;

        if(services:getIsValidUser()){


            requestDataJson = messages:getJsonPayload(m);
            repositoryId,_ = <int> jsons:toString(requestDataJson.repositoryId);
            responseGitHubJson = services:createGitHubRepository(repositoryId);
            responseDataFromDb = database:repositorySelectFromId(repositoryId);
            responseDataFromDbJson = messages:getJsonPayload(responseDataFromDb);
            responseType = jsons:toString(responseGitHubJson.responseType);
            if(responseType == "Done"){
                finalResponseJson = {"responseType":"Done","responseMessage":" ","responseDefault":"Done","repoUpdatedDetails":responseDataFromDbJson[0]};
            }else{
                finalResponseJson = {"responseType":"Error","responseMessage":" ","responseDefault":"Done","repoUpdatedDetails":responseDataFromDbJson[0]};
            }
            system:println(finalResponseJson);
            messages:setJsonPayload(response,finalResponseJson);


        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin","*");
        reply response;
    }

    @http:POST {}
    @http:Path {value:"/gitHub/setIssueTemplate"}
    resource gitHubSetIssueTemplateResource(message m){

        message response = {};
        json inValidUserJson = {"responseType":"Error","responseMessage":"Invalid user"};
        json requestJson;
        string organization;
        string repositoryName;

        if(services:getIsValidUser()){
            requestJson = messages:getJsonPayload(m);
            organization = jsons:toString(requestJson.organization);
            repositoryName = jsons:toString(requestJson.repositoryName);
            response = services:setIssueTemplate(organization,repositoryName);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin","*");
        reply response;
    }

    @http:POST {}
    @http:Path {value:"/gitHub/setPullRequestTemplate"}
    resource gitHubSetPullRequestTemplateResource(message m){

        message response = {};
        json requestJson;
        json inValidUserJson = {"responseType":"Error","responseMessage":"Invalid user"};
        string organization;
        string repositoryName;

        if(services:getIsValidUser()){
            requestJson = messages:getJsonPayload(m);
            organization = jsons:toString(requestJson.organization);
            repositoryName = jsons:toString(requestJson.repositoryName);
            response = services:setPullRequestTemplate(organization,repositoryName);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin","*");
        reply response;
    }

    @http:POST {}
    @http:Path {value:"/gitHub/setReadMe"}
    resource gitHubSetReadMeResource(message m){
        system:println(m);
        message response = {};
        json requestJson;
        json inValidUserJson = {"responseType":"Error","responseMessage":"Invalid user"};
        string organization;
        string repositoryName;
        string repositoryDescription;

        if(services:getIsValidUser()){
            requestJson = messages:getJsonPayload(m);
            organization = jsons:toString(requestJson.organization);
            repositoryName = jsons:toString(requestJson.repositoryName);
            repositoryDescription = jsons:toString(requestJson.repositoryDescription);
            response = services:setReadMe(organization,repositoryName,repositoryDescription);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin","*");
        reply response;
    }

    @http:GET {}
    @http:Path {value:"/gitHub/getTeams"}
    resource gitHubGetTeams(@http:QueryParam {value:"organization"} string organization){

        message response = {};
        json inValidUserJson = {"responseType":"Error","responseMessage":"Invalid user"};

        if(services:getIsValidUser()){
            response = services:getTeamsFromOrganization(organization);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin","*");
        reply response;
    }

    @http:GET {}
    @http:Path {value:"/gitHub/getTeamsFromId"}
    resource gitHubGetTeamsFromId(@http:QueryParam {value:"teamId"} string teamId){

        message response = {};
        json responseJson;
        json inValidUserJson = {"responseType":"Error","responseMessage":"Invalid user"};

        if(services:getIsValidUser()){
            responseJson = services:getTeamsFromId(teamId);
            messages:setJsonPayload(response,responseJson);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin","*");
        reply response;
    }

    @http:POST {}
    @http:Path {value:"/createNexus"}
    resource createNexus (message m) {

        message response = {};
        json inValidUserJson = {"responseType":"Error","responseMessage":"Invalid user"};
        json requestJson;
        json responseJson;
        string groupId;

        if(services:getIsValidUser()){
            requestJson = messages:getJsonPayload(m);
            groupId = jsons:toString(requestJson.id);
            responseJson = services:createNexus(groupId,groupId);
            messages:setJsonPayload(response,responseJson);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin","*");
        reply response;

    }

    @http:POST {}
    @http:Path {value:"/createNexusRepositoryTarget"}
    resource createNexusRepositoryTargetResource (message m) {

        message response = {};
        json inValidUserJson = {"responseType":"Error","responseMessage":"Invalid user"};
        json requestJson;
        json responseJson;
        string groupId;
        string contentClass;
        string pattern;

        if(services:getIsValidUser()){

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

        messages:setHeader(response,"Access-Control-Allow-Origin","*");
        reply response;

    }

    @http:POST {}
    @http:Path {value:"/createNexusStagingProfile"}
    resource createNexusStagingProfileResource (message m) {

        message response = {};
        json inValidUserJson = {"responseType":"Error","responseMessage":"Invalid user"};
        json requestJson;
        json responseJson;
        string groupId;

        if(services:getIsValidUser()){

            requestJson = messages:getJsonPayload(m);
            groupId = jsons:toString(requestJson.groupId);
            responseJson = services:createNexusStagingProfile(groupId);
            messages:setJsonPayload(response,responseJson);

        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin","*");
        reply response;

    }

    @http:POST {}
    @http:Path {value:"/createJenkins"}
    resource createJenkins (message m) {

        message response = {};
        json inValidUserJson = {"responseType":"Error","responseMessage":"Invalid user"};
        json requestJson;
        json responseJson;
        string jenkinsJobName;


        if(services:getIsValidUser()){

            requestJson = messages:getJsonPayload(m);
            jenkinsJobName = jsons:toString(requestJson.name);

            responseJson = services:createJenkinsJob(jenkinsJobName);
            messages:setJsonPayload(response,responseJson);

        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin","*");
        reply response;
    }

    @http:GET {}
    @http:Path {value:"/getAllLanguages"}
    resource getAllLanguages (message m) {

        message response = {};
        json inValidUserJson = {"responseType":"Invalid User"};

        if(services:getIsValidUser()){
            response = services:getAllLanguages(m);

        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin","*");
        reply response;
    }


    @http:POST {}
    @http:Path {value:"/databaseService/repository/insertData"}
    resource repositoryInsertDataResource(message m){
        system:println(m);
        message getInsertedDataMessage = {};
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

        if(services:getIsValidUser()){

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
                getInsertedDataMessage = database:repositorySelectFromName(name);
                getInsertedDataJson = messages:getJsonPayload(getInsertedDataMessage);
                responseJson = {"responseType":"Done","responseMessage":" ","repositoryId":jsons:toString(getInsertedDataJson[0].REPOSITORY_ID)};
            }else{
                responseJson = {"responseType":"Error","responseMessage":" "};
            }

            messages:setJsonPayload(response,responseJson);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }



        messages:setHeader(response,"Access-Control-Allow-Origin","*");
        reply response;
    }

    @http:POST {}
    @http:Path {value:"/databaseService/repository/updateBpmnAndTaskIds"}
    resource updateBpmnAndTaskIdsResource(message m){
        system:println(m);
        message response = {};
        json requestJson = messages:getJsonPayload(m);
        json responseJson;
        json inValidUserJson = {"responseType":"Error","responseMessage":"Invalid user"};
        string repositoryName;
        int taskId;
        int processId;
        int responseValue;


        if(services:getIsValidUser()){

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

        messages:setHeader(response,"Access-Control-Allow-Origin","*");
        reply response;
    }

    @http:POST {}
    @http:Path {value:"/databaseService/repository/updateRejectDetails"}
    resource updateRejectDetailsResource(message m){

        message response = {};
        json requestJson = messages:getJsonPayload(m);
        json responseJson;
        json inValidUserJson = {"responseType":"Error","responseMessage":"Invalid user"};
        string rejectBy;
        string rejectReason;
        int repositoryId;
        int responseValue;


        if(services:getIsValidUser()){

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

        messages:setHeader(response,"Access-Control-Allow-Origin","*");
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

        json inValidUserJson = {"responseType":"Error","responseMessage":"Invalid user"};
        if(services:getIsValidUser()){
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

        messages:setHeader(response,"Access-Control-Allow-Origin","*");
        reply response;
    }

    @http:GET {}
    @http:Path {value:"/databaseService/repository/selectAll"}
    resource repositorySelectAllResource(message m){

        message response = {};
        json inValidUserJson = {"responseType":"Error","responseMessage":"Invalid user"};

        if(services:getIsValidUser()){
            response = database:repositorySelectAll();
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin","*");
        reply response;
    }

    @http:GET {}
    @http:Path {value:"/databaseService/repository/selectFromName"}
    resource repositorySelectFromNameResource(@http:QueryParam {value:"name"} string name){

        message response = {};
        json inValidUserJson = {"responseType":"Error","responseMessage":"Invalid user"};

        if(services:getIsValidUser()){
            response = database:repositorySelectFromName(name);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin","*");
        reply response;
    }

    @http:GET {}
    @http:Path {value:"/databaseService/repository/selectFromId"}
    resource repositorySelectFromIdResource(@http:QueryParam {value:"id"} int id){

        message response = {};
        json inValidUserJson = {"responseType":"Error","responseMessage":"Invalid user"};

        if(services:getIsValidUser()){
            response = database:repositorySelectFromId(id);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin","*");
        reply response;
    }

    @http:GET {}
    @http:Path {value:"/databaseService/repository/selectFromRequestByAndWaiting"}
    resource repositorySelectFromRequestByResource(@http:QueryParam {value:"requestBy"} string requestBy){

        message response = {};
        json inValidUserJson = {"responseType":"Error","responseMessage":"Invalid user"};

        if(services:getIsValidUser()){
            response = database:repositorySelectFromRequestByAndWaiting(requestBy);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin","*");
        reply response;
    }

    @http:GET {}
    @http:Path {value:"/databaseService/repository/selectWaitingRequests"}
    resource repositorySelectWaitingRequests(message m){

        message response = {};
        json inValidUserJson = {"responseType":"Error","responseMessage":"Invalid user"};

        if(services:getIsValidUser()){
            response = database:repositorySelectWaitingRequests();
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin","*");
        reply response;
    }

    @http:GET {}
    @http:Path {value:"/databaseService/license/selectAll"}
    resource licenseSelectAllResource(message m){

        message response = {};
        json inValidUserJson = {"responseType":"Error","responseMessage":"Invalid user"};

        if(services:getIsValidUser()){
            response = database:licenseSelectAll();
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin","*");
        reply response;
    }

    @http:GET {}
    @http:Path {value:"/databaseService/organization/selectAll"}
    resource organizationSelectAllResource(message m){

        message response = {};
        json inValidUserJson = {"responseType":"Error","responseMessage":"Invalid user"};

        if(services:getIsValidUser()){
            response = database:organizationSelectAll();
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin","*");
        reply response;
    }

    @http:GET {}
    @http:Path {value:"/databaseService/repoType/selectAll"}
    resource typeSelectAllResource(message m){

        message response = {};
        json inValidUserJson = {"responseType":"Error","responseMessage":"Invalid user"};

        if(services:getIsValidUser()){
            response = database:repositoryTypeSelectAll();
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin","*");
        reply response;
    }

    @http:GET {}
    @http:Path {value:"/databaseService/team/selectAll"}
    resource teamSelectAllResource(message m){

        message response = {};
        json inValidUserJson = {"responseType":"Error","responseMessage":"Invalid user"};

        if(services:getIsValidUser()){
            response = database:teamSelectAll();
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin","*");
        reply response;
    }

    @http:GET {}
    @http:Path {value:"/databaseService/component/selectAll"}
    resource componentSelectAllResource(message m){

        message response = {};
        json inValidUserJson = {"responseType":"Error","responseMessage":"Invalid user"};

        if(services:getIsValidUser()){
            response = database:componentSelectAll();
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin","*");
        reply response;
    }

    @http:GET {}
    @http:Path {value:"/databaseService/user/selectMainUsers"}
    resource userSelectMainUsersResource(message m){

        message response = {};
        json inValidUserJson = {"responseType":"Error","responseMessage":"Invalid user"};

        if(services:getIsValidUser()){
            response = database:userSelectMainUsers();
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin","*");
        reply response;
    }

    @http:GET {}
    @http:Path {value:"/databaseService/user/checkAdminUsers"}
    resource userCheckAdminUsersResource(@http:QueryParam {value:"email"} string email){

        message response = {};
        json inValidUserJson = {"responseType":"Error","responseMessage":"Invalid user"};
        json responseJson;

        if(services:getIsValidUser()){
            responseJson = database:userCheckAdminUsers(email);
            messages:setJsonPayload(response,responseJson);
        }else{
            messages:setJsonPayload(response,inValidUserJson);
        }

        messages:setHeader(response,"Access-Control-Allow-Origin","*");
        reply response;
    }

    @http:POST {}
    @http:Path {value:"/authentication/isValidUser"}
    resource authenticateIsValidUsersResource(message m){
        message response = {};
        json requestJson = messages:getJsonPayload(m);
        string webToken = jsons:toString(requestJson.token);
        json responseValue = services:validateUser(webToken);
        json responseJson = {"isValid":responseValue.isValid,"userEmail":responseValue.userEmail};
        messages:setJsonPayload(response,responseJson);
        messages:setHeader(response,"Access-Control-Allow-Origin","*");
        reply response;
    }

    @http:POST{}
    @http:Path {value:"/authentication/isAdminUser"}
    resource authenticateIsAdminUsersResource(message m){

        message response = {};
        json requestJson = messages:getJsonPayload(m);
        string webToken = jsons:toString(requestJson.token);
        json responseValue = services:isAdminUser(webToken);
        json responseJson = {"isAdmin":responseValue.isAdmin,"userEmail":responseValue.userEmail};
        messages:setJsonPayload(response,responseJson);
        messages:setHeader(response,"Access-Control-Allow-Origin","*");
        messages:setHeader(response,"Content-Type","application/json");

        reply response;
    }

    @http:GET{}
    @http:Path {value:"/authentication/getUserDetails"}
    resource authenticateGetSessionDetails(message m){

        message response = {};

        json responseJson = services:getSessionDetails();

        messages:setJsonPayload(response,responseJson);
        messages:setHeader(response,"Access-Control-Allow-Origin","*");
        messages:setHeader(response,"Content-Type","application/json");

        reply response;
    }

    @http:GET {}
    @http:Path {value:"/databaseService/jenkinsFolder/selectFolders"}
    resource jenkinsSelectFromNameRegex(@http:QueryParam {value:"name"} string name){

        message response = {};
        json responseJson = database:jenkinsFolderMatchRegex(name);
        messages:setJsonPayload(response,responseJson);
        messages:setHeader(response,"Access-Control-Allow-Origin","*");
        reply response;
    }

}
