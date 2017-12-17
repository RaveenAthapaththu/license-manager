package org.wso2.internalapps.licensemanager.services;


import ballerina.lang.system;
import ballerina.lang.messages;
import ballerina.lang.errors;
import ballerina.lang.jsons;
import ballerina.net.http;
import org.wso2.internalapps.licensemanager.database;
import ballerina.utils;
import ballerina.lang.files;
import ballerina.lang.blobs;
import org.wso2.internalapps.licensemanager.conf;


string gitHubApiUrl = conf:getConfigData("gitHubApiUrl");
http:ClientConnector httpConnector;

function setGithubConnection(){

    httpConnector = create http:ClientConnector(gitHubApiUrl);

}

function createGitHubRepository(int repositoryId)(json ){

    message responseDataFromDb = {};
    message requestMessageForGitHub = {};
    message responseFromGitHubApi = {};
    json response;
    json responseDataFromDbJson;
    json requestDataJsonForGitHubApi;
    json responseFromGitHubApiJson;
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

        accessToken = conf:getConfigData("gitHubToken");
        responseDataFromDb = database:repositorySelectFromId(repositoryId);
        responseDataFromDbJson = messages:getJsonPayload(responseDataFromDb);


        repositoryName = jsons:toString(responseDataFromDbJson[0].REPOSITORY_NAME);
        repositoryLanguage = jsons:toString(responseDataFromDbJson[0].REPOSITORY_LANGUAGE);
        repositoryLicense = jsons:toString(responseDataFromDbJson[0].LICENSE_KEY);
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
        system:println("github create");
        system:println(messages:getHeader(responseFromGitHubApi,"Status"));
        headerValue = messages:getHeader(responseFromGitHubApi,"Status");
        if(headerValue == "201 Created"){
            system:println("done");
            response = {"responseType":"Done","responseMessage":"done"};
        }else{
            system:println("fail");
            response = {"responseType":"Error","responseMessage":"error"};
        }






    }catch(errors:Error err){
        response = {"responseType":"Error","responseMessage":err.msg};
        system:println(err);

    }


    return response;
}

function getAllLanguages(message m)(message){

    if(httpConnector == null){
        setGithubConnection();
    }

    message response = {};
    string accessToken = conf:getConfigData("gitHubToken");
    string requestUrl = "gitignore/templates?access_token=" + accessToken;

    response = httpConnector.get(requestUrl,m);

    return response;
}

function setIssueTemplate(string organization,string repositoryName)(message){
    message response = {};

    try{

        if(httpConnector == null){
            setGithubConnection();
        }

        message getAdminUserMessage = database:userSelectAdminUsers();
        json getAdminUserJson = messages:getJsonPayload(getAdminUserMessage);
        string accessToken = conf:getConfigData("gitHubToken");
        string userName = jsons:toString(getAdminUserJson[0].USER_NAME);
        string userEmail = jsons:toString(getAdminUserJson[0].USER_EMAIL);
        string requestUrl =  "repos/" + organization + "/" + repositoryName + "/contents/issue_template.md?access_token=" + accessToken + "&content=base64&branch=master";
        string headerValue;

        files:File issueFile = {path:"./org/wso2/internalapps/licensemanager/conf/issue_template.md"};
        files:open(issueFile,"r");
        var content, _ = files:read(issueFile, 100000);
        string s = blobs:toString(content, "utf-8");
        string encodeString = utils:base64encode(s);

        message gitHubRequestMessage = {};
        json gitHubRequestJson = {"message":"Add issue template","committer":{"name": userName,"email": userEmail},"content":encodeString};
        messages:setJsonPayload(gitHubRequestMessage,gitHubRequestJson);


        response = httpConnector.put(requestUrl,gitHubRequestMessage);
        system:println("github issue");
        system:println(response);
        system:println(messages:getHeader(response,"Status"));
        headerValue = messages:getHeader(response,"Status");
        if(headerValue == "201 Created"){
            system:println("done");
        }else{
            system:println("fail");
        }

        json responseMessage = {"responseType":"Done","responseMessage":"done"};
        messages:setJsonPayload(response,responseMessage);
    }catch(errors:Error err){
        response = {"responseType":"Error","responseMessage":err.msg};
        system:println(err);

    }

    return response;


}

function setPullRequestTemplate(string organization,string repositoryName)(message){
    message response = {};

    try{

        if(httpConnector == null){
            setGithubConnection();
        }

        message getAdminUserMessage = database:userSelectAdminUsers();
        json getAdminUserJson = messages:getJsonPayload(getAdminUserMessage);
        string accessToken = conf:getConfigData("gitHubToken");
        string userName = jsons:toString(getAdminUserJson[0].USER_NAME);
        string userEmail = jsons:toString(getAdminUserJson[0].USER_EMAIL);
        string requestUrl =  "repos/" + organization + "/" + repositoryName + "/contents/pull_request_template.md?access_token=" + accessToken + "&content=base64&branch=master";
        string headerValue;

        files:File issueFile = {path:"./org/wso2/internalapps/licensemanager/conf/pull_request_template.md"};
        files:open(issueFile,"r");
        var content, _ = files:read(issueFile, 100000);
        string s = blobs:toString(content, "utf-8");
        string encodeString = utils:base64encode(s);

        message gitHubRequestMessage = {};
        json gitHubRequestJson = {"message":"Add pull reaquest template","committer":{"name": userName,"email": userEmail},"content":encodeString};
        messages:setJsonPayload(gitHubRequestMessage,gitHubRequestJson);


        response = httpConnector.put(requestUrl,gitHubRequestMessage);
        system:println("github pr");
        system:println(response);
        system:println(messages:getHeader(response,"Status"));
        headerValue = messages:getHeader(response,"Status");
        if(headerValue == "201 Created"){
            system:println("done");
        }else{
            system:println("fail");
        }

        json responseMessage = {"responseType":"Done","responseMessage":"done"};
        messages:setJsonPayload(response,responseMessage);

    }catch(errors:Error err){
        response = {"responseType":"Error","responseMessage":err.msg};
        system:println(err);

    }

    return response;


}

function setReadMe(string organization,string repositoryName,string repositoryDescription)(message){
    message response = {};
    try{

        if(httpConnector == null){
            setGithubConnection();
        }

        message getAdminUserMessage = database:userSelectAdminUsers();
        json getAdminUserJson = messages:getJsonPayload(getAdminUserMessage);
        string accessToken = conf:getConfigData("gitHubToken");
        string userName = jsons:toString(getAdminUserJson[0].USER_NAME);
        string userEmail = jsons:toString(getAdminUserJson[0].USER_EMAIL);
        string requestUrl =  "repos/" + organization + "/" + repositoryName + "/contents/README.md?access_token=" + accessToken + "&content=base64&branch=master";
        string encodeString = utils:base64encode(correctString(repositoryDescription));
        string headerValue;

        message gitHubRequestMessage = {};
        json gitHubRequestJson = {"message":"Add README.md","committer":{"name": userName,"email": userEmail},"content":encodeString};
        messages:setJsonPayload(gitHubRequestMessage,gitHubRequestJson);


        response = httpConnector.put(requestUrl,gitHubRequestMessage);
        system:println("github r");
        system:println(response);
        system:println(messages:getHeader(response,"Status"));
        headerValue = messages:getHeader(response,"Status");
        if(headerValue == "201 Created"){
            system:println("done");
        }else{
            system:println("fail");
        }

        json responseMessage = {"responseType":"Done","responseMessage":"done"};
        messages:setJsonPayload(response,responseMessage);
    }catch(errors:Error err){
        json errorMessage = {"responseType":"Error","responseMessage":err.msg};
        system:println(err);
        messages:setJsonPayload(response,errorMessage);
        return response;
    }

    return response;


}

function getTeamsFromOrganization(string organization)(message ){
    message response = {};


    try{

        if(httpConnector == null){
            setGithubConnection();
        }

        json responseDataFromDbJson;
        message responseDataFromDb = {};
        string accessToken = "";
        responseDataFromDb = database:userSelectAdminUsers();
        responseDataFromDbJson = messages:getJsonPayload(responseDataFromDb);
        accessToken = conf:getConfigData("gitHubToken");

        message requestMessageFromGitHub = {};
        string getUrl = "orgs/"+ organization + "/teams?access_token=" + accessToken;

        message responseFromGitHubApi = httpConnector.get(getUrl,requestMessageFromGitHub);

        return responseFromGitHubApi;
    }catch(errors:Error err){
        json errorMessage = {"responseType":"Error","responseMessage":err.msg};
        system:println(err);
        messages:setJsonPayload(response,errorMessage);
        return response;
    }


    return response;
}

function getTeamsFromId(string id)(json ){
    json response = {};
    message requestMessageFromGitHub = {};
    string accessToken = "";
    string getUrl;

    try{

        if(httpConnector == null){
            setGithubConnection();
        }

        accessToken = conf:getConfigData("gitHubToken");
        getUrl = "teams/"+ id + "?access_token=" + accessToken;
        message responseFromGitHubApi = httpConnector.get(getUrl,requestMessageFromGitHub);
        system:println(getUrl);
        response = messages:getJsonPayload(responseFromGitHubApi);

    }catch(errors:Error err){
        response = {"responseType":"Error","responseMessage":err.msg};
        system:println(err);


    }


    return response;
}

