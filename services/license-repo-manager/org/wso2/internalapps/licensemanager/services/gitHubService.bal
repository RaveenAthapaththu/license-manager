package org.wso2.internalapps.licensemanager.services;

import ballerina.lang.messages;
import ballerina.lang.errors;
import ballerina.lang.jsons;
import ballerina.net.http;
import org.wso2.internalapps.licensemanager.database;
import ballerina.utils;
import ballerina.lang.files;
import ballerina.lang.blobs;
import org.wso2.internalapps.licensemanager.conf;
import ballerina.utils.logger;
import ballerina.doc;

string gitHubApiUrl = conf:getConfigData("GITHUB_URL");
http:ClientConnector httpConnector;

@doc:Description {value:"Set GitHub client connector"}
function setGithubConnection(){

    httpConnector = create http:ClientConnector(gitHubApiUrl);
    return;

}

@doc:Description {value:"Create GitHub repository"}
@doc:Param {value:"repositoryId: Repository ID"}
@doc:Param {value:"responseJson: Return JSON which indicates function is successfully completed or not"}
function createGitHubRepository(int repositoryId)(json responseJson){

    message requestMessageForGitHub = {};
    message responseFromGitHubApi = {};
    json responseDataFromDbJson;
    json requestDataJsonForGitHubApi;
    string accessToken = "";
    string repositoryName;
    string repositoryLanguage;
    string repositoryDescription = " ";
    string repositoryLicense;
    string repositoryOrganization;
    string repositoryPrivateString;
    string postUrl;
    string headerValue;
    int repositoryTeam;
    boolean repositoryPrivate = false;

    try{
        if(httpConnector == null){
            setGithubConnection();
        }

        accessToken = conf:getConfigData("GITHUB_TOKEN");
        responseDataFromDbJson = database:repositorySelectFromId(repositoryId);
        repositoryName = jsons:toString(responseDataFromDbJson[0].REPOSITORY_NAME);
        repositoryLanguage = jsons:toString(responseDataFromDbJson[0].REPOSITORY_LANGUAGE);
        repositoryLicense = jsons:toString(responseDataFromDbJson[0].LICENSE_GITHUB_KEY);
        repositoryOrganization = jsons:toString(responseDataFromDbJson[0].ORGANIZATION_NAME);
        repositoryPrivateString = jsons:toString(responseDataFromDbJson[0].REPOSITORY_PRIVATE);
        repositoryTeam,_ = <int>(jsons:toString(responseDataFromDbJson[0].REPOSITORY_TEAM));

        if(repositoryPrivateString == "true"){
            repositoryPrivate = true;
        }

        postUrl = "orgs/"+repositoryOrganization + "/repos?access_token=" + accessToken;

        requestDataJsonForGitHubApi = {
                                               "name":repositoryName,
                                               "description":repositoryDescription,
                                               "private":repositoryPrivate,
                                               "gitignore_template":repositoryLanguage,
                                               "license_template":repositoryLicense,
                                               "team_id":repositoryTeam
                                           };


        messages:setJsonPayload(requestMessageForGitHub,requestDataJsonForGitHubApi);
        responseFromGitHubApi = httpConnector.post(postUrl,requestMessageForGitHub);
        headerValue = messages:getHeader(responseFromGitHubApi,"Status");
        if(headerValue == "201 Created"){
            responseJson = {"responseType":"Done","responseMessage":"done"};
            logger:info("Repository created successfully");
        }else{

            responseJson = {"responseType":"Error","responseMessage":"createGitHubRepository error"};
            logger:error("Repository creation error");
        }

    }catch(errors:Error err){
        responseJson = {"responseType":"Error","responseMessage":err.msg};
        logger:error("GitHub Functions : createGitHubRepository " + err.msg);

    }

    return;
}

@doc:Description {value:"Get all languages for GitHub REST API"}
@doc:Param {value:"response: response message"}
function getAllLanguages()(message){
    message m = {};
    message response = {};
    string accessToken;
    string requestUrl;

    if(httpConnector == null){
        setGithubConnection();
    }

    accessToken = conf:getConfigData("GITHUB_TOKEN");
    requestUrl = "gitignore/templates?access_token=" + accessToken;
    response = httpConnector.get(requestUrl,m);
    return response;
}

@doc:Description {value:"Set issue template"}
@doc:Param {value:"organization: GitHub organization"}
@doc:Param {value:"repositoryName: Repository name"}
@doc:Param {value:"userName: User name"}
@doc:Param {value:"userEmail: User e-mail"}
@doc:Param {value:"responseJson: Return JSON which indicates function is successfully completed or not"}
function setIssueTemplate(string organization,string repositoryName,string userName,string userEmail)(json responseJson){

    json gitHubRequestJson;
    message responseMessage = {};
    message gitHubRequestMessage = {};
    string accessToken;
    string requestUrl;
    string headerValue;
    string s;
    string encodeString;

    try{

        if(httpConnector == null){
            setGithubConnection();
        }


        accessToken = conf:getConfigData("GITHUB_TOKEN");
        requestUrl =  "repos/" + organization + "/" + repositoryName + "/contents/issue_template.md?access_token=" + accessToken + "&content=base64&branch=master";
        files:File issueFile = {path:"conf/issue_template.md"};
        files:open(issueFile,"r");
        var content, _ = files:read(issueFile, 100000);
        s = blobs:toString(content, "utf-8");
        encodeString = utils:base64encode(s);
        gitHubRequestJson = {"message":"Add issue template","committer":{"name": userName,"email": userEmail},"content":encodeString};
        messages:setJsonPayload(gitHubRequestMessage,gitHubRequestJson);
        responseMessage = httpConnector.put(requestUrl,gitHubRequestMessage);
        headerValue = messages:getHeader(responseMessage,"Status");

        if(headerValue == "201 Created"){
            responseJson = {"responseType":"Done","responseMessage":"Repository set issue template successfully"};
            logger:info("Repository set issue template successfully");
        }else{
            responseJson = {"responseType":"Error","responseMessage":"Unknown Error"};
            logger:error("Repository set issue template error: Unknown Error");
        }


    }catch(errors:Error err){
        responseJson = {"responseType":"Error","responseMessage":err.msg};
        logger:error("GitHub Functions : setIssueTemplate " + err.msg);

    }
    return;


}

@doc:Description {value:"Set PR template"}
@doc:Param {value:"organization: GitHub organization"}
@doc:Param {value:"repositoryName: Repository name"}
@doc:Param {value:"userName: User name"}
@doc:Param {value:"userEmail: User e-mail"}
@doc:Param {value:"responseJson: Return JSON which indicates function is successfully completed or not"}
function setPullRequestTemplate(string organization,string repositoryName,string userName,string userEmail)(json responseJson){

    message response = {};
    message gitHubRequestMessage = {};
    json gitHubRequestJson;
    string accessToken;
    string requestUrl;
    string headerValue;
    string s;
    string encodeString;
    try{

        if(httpConnector == null){
            setGithubConnection();
        }


        accessToken = conf:getConfigData("GITHUB_TOKEN");
        requestUrl =  "repos/" + organization + "/" + repositoryName + "/contents/pull_request_template.md?access_token=" + accessToken + "&content=base64&branch=master";
        files:File issueFile = {path:"conf/pull_request_template.md"};
        files:open(issueFile,"r");
        var content, _ = files:read(issueFile, 100000);
        s = blobs:toString(content, "utf-8");
        encodeString = utils:base64encode(s);
        gitHubRequestJson = {"message":"Add pull reaquest template","committer":{"name": userName,"email": userEmail},"content":encodeString};
        messages:setJsonPayload(gitHubRequestMessage,gitHubRequestJson);
        response = httpConnector.put(requestUrl,gitHubRequestMessage);
        headerValue = messages:getHeader(response,"Status");

        if(headerValue == "201 Created"){
            responseJson = {"responseType":"Done","responseMessage":"Repository set PR template successfully"};
            logger:info("Repository set PR template successfully");
        }else{
            responseJson = {"responseType":"Done","responseMessage":"Unknown Error"};
            logger:info("Repository set PR template error: Unknown Error");
        }

    }catch(errors:Error err){
        responseJson = {"responseType":"Error","responseMessage":err.msg};
        logger:error("GitHub Functions : setPullRequestTemplate " + err.msg);

    }

    return responseJson;
}

@doc:Description {value:"Set README"}
@doc:Param {value:"organization: GitHub organization"}
@doc:Param {value:"repositoryName: Repository name"}
@doc:Param {value:"repositoryDescription: Repository description"}
@doc:Param {value:"userName: User name"}
@doc:Param {value:"userEmail: User e-mail"}
@doc:Param {value:"responseJson: Return JSON which indicates function is successfully completed or not"}
function setReadMe(string organization,string repositoryName,string repositoryDescription,string userName,string userEmail)(json responseJson){

    message response = {};
    message gitHubRequestMessage = {};
    json gitHubRequestJson;
    string accessToken;
    string requestUrl;
    string encodeString;
    string headerValue;
    try{

        if(httpConnector == null){
            setGithubConnection();
        }

        accessToken = conf:getConfigData("GITHUB_TOKEN");
        requestUrl =  "repos/" + organization + "/" + repositoryName + "/contents/README.md?access_token=" + accessToken + "&content=base64&branch=master";
        encodeString = utils:base64encode(correctString(repositoryDescription));
        gitHubRequestJson = {"message":"Add README.md","committer":{"name": userName,"email": userEmail},"content":encodeString};
        messages:setJsonPayload(gitHubRequestMessage,gitHubRequestJson);
        response = httpConnector.put(requestUrl,gitHubRequestMessage);
        headerValue = messages:getHeader(response,"Status");

        if(headerValue == "201 Created"){
            responseJson = {"responseType":"Done","responseMessage":"Repository set ReadMe successfully"};
            logger:info("Repository set ReadMe successfully");
        }else{
            responseJson = {"responseType":"Error","responseMessage":"Unknown Error"};
            logger:error("Repository set ReadMe error: Unknown Error");
        }

    }catch(errors:Error err){
        responseJson = {"responseType":"Error","responseMessage":err.msg};
        logger:error("GitHub Functions : setReadMe " + err.msg);
    }

    return;
}

@doc:Description {value:"Get all teams in a given organization"}
@doc:Param {value:"response: Response message with all teams in the organization"}
function getTeamsFromOrganization(string organization)(message ){

    message response = {};
    message requestMessageFromGitHub = {};
    string accessToken;
    string getUrl;

    try{
        if(httpConnector == null){
            setGithubConnection();
        }

        accessToken = conf:getConfigData("GITHUB_TOKEN");
        getUrl = "orgs/"+ organization + "/teams?access_token=" + accessToken;
        response = httpConnector.get(getUrl,requestMessageFromGitHub);

    }catch(errors:Error err){
        json errorMessage = {"responseType":"Error","responseMessage":err.msg};
        messages:setJsonPayload(response,errorMessage);
        logger:error("GitHub Functions : getTeamsFromOrganization " + err.msg);
    }

    return response;
}

@doc:Description {value:"Get team from given ID"}
@doc:Param {value:"responseJson: Response JSON with team details"}
function getTeamsFromId(string id)(json responseJson){

    message requestMessageFromGitHub = {};
    message responseFromGitHubApi = {};
    string accessToken = "";
    string getUrl;

    try{

        if(httpConnector == null){
            setGithubConnection();
        }

        accessToken = conf:getConfigData("GITHUB_TOKEN");
        getUrl = "teams/"+ id + "?access_token=" + accessToken;
        responseFromGitHubApi = httpConnector.get(getUrl,requestMessageFromGitHub);
        responseJson = messages:getJsonPayload(responseFromGitHubApi);

    }catch(errors:Error err){
        responseJson = {"responseType":"Error","responseMessage":err.msg};
        logger:error("GitHub Functions : getTeamsFromId " + err.msg);
    }

    return;
}

@doc:Description {value:"Set default team - Infra team"}
@doc:Param {value:"organization: Organization name"}
@doc:Param {value:"repositoryName: Repository name"}
@doc:Param {value:"teamId: Team ID"}
function setDefaultTeam(string organization,string repositoryName,string teamId)(json responseJson){

    message response = {};
    message gitHubRequestMessage = {};
    json gitHubRequestJson = {};
    string accessToken;
    string requestUrl;
    int statusCode;
    try{

        if(httpConnector == null){
            setGithubConnection();
        }

        accessToken = conf:getConfigData("GITHUB_TOKEN");
        requestUrl =  "teams/" + teamId + "/repos/" + organization + "/" + repositoryName + "?access_token=" + accessToken + "&permission=admin";
        messages:setJsonPayload(gitHubRequestMessage,gitHubRequestJson);
        response = httpConnector.put(requestUrl,gitHubRequestMessage);
        statusCode = http:getStatusCode(response);
        if(statusCode == 204){
            responseJson = {"responseType":"Done","responseMessage":"Repository default team assign successfully"};
            logger:info("Repository default team assign successfully");
        }else{
            responseJson = {"responseType":"Error","responseMessage":"Unknown Error"};
            logger:info("Repository default team assign error: Unknown Error");
        }

    }catch(errors:Error err){
        responseJson = {"responseType":"Error","responseMessage":err.msg};
        logger:error("GitHub Functions : setPullRequestTemplate " + err.msg);

    }

    return responseJson;
}

