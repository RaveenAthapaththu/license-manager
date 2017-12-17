package org.wso2.internalapps.licensemanager.database;

import ballerina.data.sql;
import ballerina.lang.messages;
import ballerina.lang.errors;
import ballerina.lang.system;
import org.wso2.internalapps.licensemanager.conf;

sql:ClientConnector connection = null;

function setConnection(){
    if(connection == null){

        string dbURL = conf:getConfigData("databaseUrl");
        string username = conf:getConfigData("databaseUserName");
        string password = conf:getConfigData("databasePassword");
        map propertiesMap = {"jdbcUrl":dbURL, "username":username, "password":password,"maximumPoolSize":100};
        connection = create sql:ClientConnector(propertiesMap);

    }

}

function repositoryInsertData(string name,string language,boolean buildable,boolean nexus,boolean private,string description,string groupId,int license,int team,int organization,int repoType,string requestBy)(int){

    message response = {};
    int returnValue;

    if(connection == null){
        setConnection();
    }


    try{

        string query = "INSERT INTO LM_REPOSITORY(
                                                    REPOSITORY_NAME,
                                                    REPOSITORY_LANGUAGE,
                                                    REPOSITORY_BUILDABLE,
                                                    REPOSITORY_NEXUS,
                                                    REPOSITORY_PRIVATE,
                                                    REPOSITORY_DESCRIPTION,
                                                    REPOSITORY_GROUPID,
                                                    REPOSITORY_LICENSE,
                                                    REPOSITORY_TEAM,
                                                    REPOSITORY_ORGANIZATION,
                                                    REPOSITORY_TYPE,
                                                    REPOSITORY_REQUEST_BY
                                                  )
                                                   VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";

        sql:Parameter paraName = {sqlType:"varchar", value:name};
        sql:Parameter paraLanguage = {sqlType:"varchar", value:language};
        sql:Parameter paraBuildable = {sqlType:"boolean", value:buildable};
        sql:Parameter paraNexus = {sqlType:"boolean", value:nexus};
        sql:Parameter paraPrivate = {sqlType:"boolean", value:private};
        sql:Parameter paraDescription = {sqlType:"varchar", value:description};
        sql:Parameter paraGroupId = {sqlType:"varchar", value:groupId};
        sql:Parameter paraLicense = {sqlType:"integer", value:license};
        sql:Parameter paraTeam = {sqlType:"integer", value:team};
        sql:Parameter paraOrganization = {sqlType:"integer", value:organization};
        sql:Parameter paraRepoType = {sqlType:"integer", value:repoType};
        sql:Parameter paraRequestBy = {sqlType:"varchar", value:requestBy};

        sql:Parameter[] parameterArray = [paraName,paraLanguage,paraBuildable,paraNexus,paraPrivate,paraDescription,paraGroupId,paraLicense,paraTeam,paraOrganization,paraRepoType,paraRequestBy];

        returnValue = connection.update(query,parameterArray);
    }catch(errors:Error err){
        json errorMessage = {"responseType":"Error","responseMessage":err.msg};
        messages:setJsonPayload(response,errorMessage);

    }

    return returnValue;


}

function repositoryUpdateRejectDetails(string rejectBy,string rejectReason,int repositoryId)(int){
    message response = {};
    int returnValue;

    if(connection == null){

        setConnection();
    }
    try{


        string query = "UPDATE LM_REPOSITORY SET REPOSITORY_ACCEPT = ? , REPOSITORY_DEACTIVATED_BY = ? , REPOSITORY_DEACTIVATED_REASON = ? WHERE REPOSITORY_ID = ?";

        sql:Parameter paraAccept = {sqlType:"boolean", value:false};
        sql:Parameter paraRejectBy = {sqlType:"varchar", value:rejectBy};
        sql:Parameter paraRejectReason = {sqlType:"varchar", value:rejectReason};
        sql:Parameter paraRepositoryId = {sqlType:"integer", value:repositoryId};
        sql:Parameter[] parameterArray = [paraAccept,paraRejectBy,paraRejectReason,paraRepositoryId];

        returnValue = connection.update(query,parameterArray);
    }catch(errors:Error err){
        json errorMessage = {"responseType":"Error","responseMessage":err.msg};
        messages:setJsonPayload(response,errorMessage);

    }

    return returnValue;
}

function repositoryUpdateTaskAndProcessIds(int taskId,int processId,string repositoryName)(int){
    message response = {};
    int returnValue;

    if(connection == null){

        setConnection();
    }
    try{


        string query = "UPDATE LM_REPOSITORY SET REPOSITORY_BPMN_TASK_ID = ? , REPOSITORY_BPMN_PROCESS_ID = ? WHERE REPOSITORY_NAME = ?";

        sql:Parameter paraTaskId = {sqlType:"integer", value:taskId};
        sql:Parameter paraProcessId = {sqlType:"integer", value:processId};
        sql:Parameter paraRepositoryName = {sqlType:"varchar", value:repositoryName};
        sql:Parameter[] parameterArray = [paraTaskId,paraProcessId,paraRepositoryName];

        returnValue = connection.update(query,parameterArray);
    }catch(errors:Error err){
        json errorMessage = {"responseType":"Error","responseMessage":err.msg};
        messages:setJsonPayload(response,errorMessage);

    }

    return returnValue;
}

function repositoryUpdateAll(string name,string language,boolean buildable,boolean nexus,boolean private,string description,string groupId,int license,int team,int organization,int repoType,boolean accept,string acceptBy,int id)(int){

    message response = {};
    int returnValue;

    if(connection == null){

        setConnection();
    }

    try {
        string query = "UPDATE LM_REPOSITORY SET
                                                    REPOSITORY_NAME = ?,
                                                    REPOSITORY_LANGUAGE = ?,
                                                    REPOSITORY_BUILDABLE = ?,
                                                    REPOSITORY_NEXUS = ?,
                                                    REPOSITORY_PRIVATE = ?,
                                                    REPOSITORY_DESCRIPTION = ?,
                                                    REPOSITORY_GROUPID = ?,
                                                    REPOSITORY_LICENSE = ?,
                                                    REPOSITORY_TEAM = ?,
                                                    REPOSITORY_ORGANIZATION = ?,
                                                    REPOSITORY_TYPE = ?,
                                                    REPOSITORY_ACCEPT = ?,
                                                    REPOSITORY_ACCEPTED_BY = ?

                                                    WHERE REPOSITORY_ID = ?";

        sql:Parameter paraName = {sqlType:"varchar", value:name};
        sql:Parameter paraLanguage = {sqlType:"varchar", value:language};
        sql:Parameter paraBuildable = {sqlType:"boolean", value:buildable};
        sql:Parameter paraNexus = {sqlType:"boolean", value:nexus};
        sql:Parameter paraPrivate = {sqlType:"boolean", value:private};
        sql:Parameter paraDescription = {sqlType:"varchar", value:description};
        sql:Parameter paraGroupId = {sqlType:"varchar", value:groupId};
        sql:Parameter paraLicense = {sqlType:"integer", value:license};
        sql:Parameter paraTeam = {sqlType:"integer", value:team};
        sql:Parameter paraOrganization = {sqlType:"integer", value:organization};
        sql:Parameter paraRepoType = {sqlType:"integer", value:repoType};
        sql:Parameter paraAccept = {sqlType:"boolean", value:accept};
        sql:Parameter paraAcceptBy = {sqlType:"varchar", value:acceptBy};
        sql:Parameter paraRepositoryId = {sqlType:"integer", value:id};

        sql:Parameter[] parameterArray = [paraName, paraLanguage, paraBuildable, paraNexus, paraPrivate, paraDescription, paraGroupId, paraLicense, paraTeam, paraOrganization, paraRepoType, paraAccept, paraAcceptBy, paraRepositoryId];

        returnValue = connection.update(query, parameterArray);

    }catch(errors:Error err){
        json errorMessage = {"responseType":"Error","responseMessage":err.msg};
        messages:setJsonPayload(response,errorMessage);

    }
    return returnValue;
}

function repositorySelectAll()(message){
    message response = {};

    if(connection == null){

        setConnection();
    }
    try{

        string query = "SELECT
                        LM_REPOSITORY.*,
                        LM_LICENSE.LICENSE_NAME,
                        LM_LICENSE.LICENSE_KEY,
                        LM_ORGANIZATION.ORGANIZATION_NAME,
                        LM_REPOSITORYTYPE.REPOSITORYTYPE_KEY,
                        LM_REPOSITORYTYPE.REPOSITORYTYPE_NAME
                        FROM LM_REPOSITORY
                        INNER JOIN LM_LICENSE ON LM_REPOSITORY.REPOSITORY_LICENSE = LM_LICENSE.LICENSE_ID
                        INNER JOIN LM_ORGANIZATION ON LM_REPOSITORY.REPOSITORY_ORGANIZATION = LM_ORGANIZATION.ORGANIZATION_ID
                        INNER JOIN LM_REPOSITORYTYPE ON LM_REPOSITORY.REPOSITORY_TYPE = LM_REPOSITORYTYPE.REPOSITORYTYPE_ID
                        ORDER BY LM_REPOSITORY.REPOSITORY_NAME";
        sql:Parameter[] parameterArray = [];
        datatable responseDataFromDb = connection.select(query ,parameterArray);
        var resultJSON,_ = <json>responseDataFromDb;
        messages:setJsonPayload(response,resultJSON);


    }catch(errors:Error err){
        json errorMessage = {"responseType":"Error","responseMessage":err.msg};
        messages:setJsonPayload(response,errorMessage);

    }
    return response;

}

function repositorySelectFromName(string name)(message){
    message response = {};

    if(connection == null){

        setConnection();
    }
    try{

        string query = "SELECT
        REPOSITORY_ID,
        REPOSITORY_NAME,
        REPOSITORY_LANGUAGE,
        REPOSITORY_BUILDABLE,
        REPOSITORY_NEXUS,
        REPOSITORY_PRIVATE ,
        REPOSITORY_DESCRIPTION,
        REPOSITORY_GROUPID,
        REPOSITORY_LICENSE,
        REPOSITORY_TEAM,
        REPOSITORY_ORGANIZATION,
        REPOSITORY_TYPE,
        REPOSITORY_ACTIVED,
        REPOSITORY_ACCEPT,
        REPOSITORY_REQUEST_BY,
        REPOSITORY_ACCEPTED_BY,
        REPOSITORY_DEACTIVATED_BY,
        REPOSITORY_DEACTIVATED_REASON,
        REPOSITORY_BPMN_TASK_ID,
        REPOSITORY_BPMN_PROCESS_ID
        FROM LM_REPOSITORY WHERE REPOSITORY_NAME = ? ";

        sql:Parameter paraName = {sqlType:"varchar", value:name};
        sql:Parameter[] parameterArray = [paraName];

        datatable responseDataFromDb = connection.select(query ,parameterArray);

        var resultJSON,_ = <json>responseDataFromDb;
        messages:setJsonPayload(response,resultJSON);


    }catch(errors:Error err){
        json errorMessage = {"responseType":"Error","responseMessage":err.msg};
        messages:setJsonPayload(response,errorMessage);

    }
    return response;

}

function repositorySelectFromId(int id)(message){
    message response = {};

    if(connection == null){

        setConnection();
    }
    try{

        string query = "SELECT
                        LM_REPOSITORY.*,
                        LM_LICENSE.LICENSE_NAME,
                        LM_LICENSE.LICENSE_KEY,
                        LM_ORGANIZATION.ORGANIZATION_NAME,
                        LM_REPOSITORYTYPE.REPOSITORYTYPE_KEY,
                        LM_REPOSITORYTYPE.REPOSITORYTYPE_NAME
                        FROM LM_REPOSITORY
                        INNER JOIN LM_LICENSE ON LM_REPOSITORY.REPOSITORY_LICENSE = LM_LICENSE.LICENSE_ID
                        INNER JOIN LM_ORGANIZATION ON LM_REPOSITORY.REPOSITORY_ORGANIZATION = LM_ORGANIZATION.ORGANIZATION_ID
                        INNER JOIN LM_REPOSITORYTYPE ON LM_REPOSITORY.REPOSITORY_TYPE = LM_REPOSITORYTYPE.REPOSITORYTYPE_ID
                        WHERE REPOSITORY_ID=?;";

        sql:Parameter paraName = {sqlType:"integer", value:id};
        sql:Parameter[] parameterArray = [paraName];
        datatable responseDataFromDb = connection.select(query ,parameterArray);
        var resultJSON,_ = <json>responseDataFromDb;
        messages:setJsonPayload(response,resultJSON);


    }catch(errors:Error err){
        json errorMessage = {"responseType":"Error","responseMessage":err.msg};
        messages:setJsonPayload(response,errorMessage);

    }
    return response;

}

function repositorySelectFromRequestByAndWaiting(string requestBy)(message){
    message response = {};

    if(connection == null){

        setConnection();
    }

    try{

        string query = "SELECT
                        LM_REPOSITORY.*,
                        LM_LICENSE.LICENSE_NAME,
                        LM_LICENSE.LICENSE_KEY,
                        LM_ORGANIZATION.ORGANIZATION_NAME,
                        LM_REPOSITORYTYPE.REPOSITORYTYPE_KEY,
                        LM_REPOSITORYTYPE.REPOSITORYTYPE_NAME
                        FROM LM_REPOSITORY
                        INNER JOIN LM_LICENSE ON LM_REPOSITORY.REPOSITORY_LICENSE = LM_LICENSE.LICENSE_ID
                        INNER JOIN LM_ORGANIZATION ON LM_REPOSITORY.REPOSITORY_ORGANIZATION = LM_ORGANIZATION.ORGANIZATION_ID
                        INNER JOIN LM_REPOSITORYTYPE ON LM_REPOSITORY.REPOSITORY_TYPE = LM_REPOSITORYTYPE.REPOSITORYTYPE_ID
                        WHERE REPOSITORY_REQUEST_BY = ? AND REPOSITORY_ACCEPT IS NULL";

        sql:Parameter paraName = {sqlType:"varchar", value:requestBy};


        sql:Parameter[] parameterArray = [paraName];

        datatable responseDataFromDb = connection.select(query ,parameterArray);
        var resultJSON,_ = <json>responseDataFromDb;

        messages:setJsonPayload(response,resultJSON);


    }catch(errors:Error err){
        json errorMessage = {"responseType":"Error","responseMessage":err.msg};
        messages:setJsonPayload(response,errorMessage);

    }
    return response;

}

function repositorySelectWaitingRequests()(message){
    message response = {};

    if(connection == null){

        setConnection();
    }

    try{

        string query = "SELECT
                        LM_REPOSITORY.*,
                        LM_LICENSE.LICENSE_NAME,
                        LM_LICENSE.LICENSE_KEY,
                        LM_ORGANIZATION.ORGANIZATION_NAME,
                        LM_REPOSITORYTYPE.REPOSITORYTYPE_KEY,
                        LM_REPOSITORYTYPE.REPOSITORYTYPE_NAME
                        FROM LM_REPOSITORY
                        INNER JOIN LM_LICENSE ON LM_REPOSITORY.REPOSITORY_LICENSE = LM_LICENSE.LICENSE_ID
                        INNER JOIN LM_ORGANIZATION ON LM_REPOSITORY.REPOSITORY_ORGANIZATION = LM_ORGANIZATION.ORGANIZATION_ID
                        INNER JOIN LM_REPOSITORYTYPE ON LM_REPOSITORY.REPOSITORY_TYPE = LM_REPOSITORYTYPE.REPOSITORYTYPE_ID
                        WHERE REPOSITORY_ACCEPT IS NULL";


        sql:Parameter[] parameterArray = [];

        datatable responseDataFromDb = connection.select(query ,parameterArray);
        var resultJSON,_ = <json>responseDataFromDb;
        messages:setJsonPayload(response,resultJSON);


    }catch(errors:Error err){
        json errorMessage = {"responseType":"Error","responseMessage":err.msg};
        messages:setJsonPayload(response,errorMessage);

    }
    return response;

}

function organizationSelectAll()(message){
    message response = {};

    if(connection == null){

        setConnection();
    }
    try{



        string query = "SELECT * FROM LM_ORGANIZATION";
        sql:Parameter[] parameterArray = [];
        datatable responseDataFromDb = connection.select(query ,parameterArray);
        var resultJSON,_ = <json>responseDataFromDb;
        messages:setJsonPayload(response,resultJSON);

    }catch(errors:Error err){
        json errorMessage = {"responseType":"Error","responseMessage":err.msg};
        messages:setJsonPayload(response,errorMessage);

    }
    return response;

}

function licenseSelectAll()(message){
    message response = {};

    if(connection == null){

        setConnection();
    }
    try{

        string query = "SELECT * FROM LM_LICENSE";
        sql:Parameter[] parameterArray = [];
        datatable responseDataFromDb = connection.select(query ,parameterArray);
        json resultJSON;
        resultJSON,_ = <json>responseDataFromDb;
        messages:setJsonPayload(response,resultJSON);

    }catch(errors:Error err){
        json errorMessage = {"responseType":"Error","responseMessage":err.msg};
        messages:setJsonPayload(response,errorMessage);
        system:println(errorMessage);

    }
    return response;

}

function repositoryTypeSelectAll()(message){
    message response = {};

    if(connection == null){

        setConnection();
    }
    try{



        string query = "SELECT * FROM LM_REPOSITORYTYPE";
        sql:Parameter[] parameterArray = [];
        datatable responseDataFromDb = connection.select(query ,parameterArray);
        json resultJSON;
        resultJSON,_ = <json>responseDataFromDb;
        messages:setJsonPayload(response,resultJSON);


    }catch(errors:Error err){
        json errorMessage = {"responseType":"Error","responseMessage":err.msg};
        messages:setJsonPayload(response,errorMessage);
        system:println(errorMessage);

    }
    return response;

}

function teamSelectAll()(message){
    message response = {};

    if(connection == null){

        setConnection();
    }
    try{



        string query = "SELECT * FROM LM_TEAM";
        sql:Parameter[] parameterArray = [];

        datatable responseDataFromDb = connection.select(query ,parameterArray);

        json resultJSON;
        resultJSON,_ = <json>responseDataFromDb;
        messages:setJsonPayload(response,resultJSON);


    }catch(errors:Error err){
        json errorMessage = {"responseType":"Error","responseMessage":err.msg};
        messages:setJsonPayload(response,errorMessage);
        system:println(errorMessage);

    }
    return response;

}

function componentSelectAll()(message){
    message response = {};

    if(connection == null){

        setConnection();
    }
    try{

        string query = "SELECT * FROM LM_COMPONENT";
        sql:Parameter[] parameterArray = [];
        datatable responseDataFromDb = connection.select(query ,parameterArray);
        json resultJSON;
        resultJSON,_ = <json>responseDataFromDb;
        messages:setJsonPayload(response,resultJSON);


    }catch(errors:Error err){
        json errorMessage = {"responseType":"Error","responseMessage":err.msg};
        messages:setJsonPayload(response,errorMessage);
        system:println(errorMessage);

    }
    return response;

}

function userSelectMainUsers()(message){
    message response = {};

    if(connection == null){

        setConnection();
    }
    try{

        string query = "SELECT * FROM LM_USER WHERE USER_PERMISSION = 'ALL' OR USER_PERMISSION = 'ACCEPT'";
        sql:Parameter[] parameterArray = [];
        datatable responseDataFromDb = connection.select(query ,parameterArray);

        json resultJSON;
        resultJSON,_ = <json>responseDataFromDb;
        messages:setJsonPayload(response,resultJSON);


    }catch(errors:Error err){
        json errorMessage = {"responseType":"Error","responseMessage":err.msg};
        messages:setJsonPayload(response,errorMessage);
        system:println(errorMessage);

    }
    return response;

}

function userSelectAdminUsers()(message){
    message response = {};

    if(connection == null){

        setConnection();
    }
    try{

        string query = "SELECT * FROM LM_USER WHERE USER_PERMISSION = 'ALL'";
        sql:Parameter[] parameterArray = [];
        datatable responseDataFromDb = connection.select(query ,parameterArray);
        json resultJSON;
        resultJSON,_ = <json>responseDataFromDb;
        messages:setJsonPayload(response,resultJSON);


    }catch(errors:Error err){
        json errorMessage = {"responseType":"Error","responseMessage":err.msg};
        messages:setJsonPayload(response,errorMessage);
        system:println(errorMessage);

    }
    return response;

}

function userCheckAdminUsers(string email)(json ){
    json responseDbJson;
    json response;

    if(connection == null){

        setConnection();
    }
    try{



        string query = "SELECT * FROM LM_USER WHERE USER_EMAIL = ?";
        sql:Parameter paraEmail = {sqlType:"varchar", value:email};
        sql:Parameter[] parameterArray = [paraEmail];
        datatable responseDataFromDb = connection.select(query ,parameterArray);
        responseDbJson,_ = <json>responseDataFromDb;
        int length = lengthof responseDbJson;
        if(length > 0){
            response = {"responseType":"Done","isAdmin":true,"userDetails":responseDbJson[0]};
        }else{
            response = {"responseType":"Done","isAdmin":false,"userDetails":""};
        }



    }catch(errors:Error err){
        response = {"responseType":"Error","responseMessage":err.msg};

        system:println(response);

    }
    return response;

}

function jenkinsFolderMatchRegex(string jenkinsJobName)(json ){
    json responseDbJson;
    json response;

    if(connection == null){

        setConnection();
    }
    try{



        string query = "CALL JENKINS_GET_FOLDER(?)";
        sql:Parameter paraJenkinsJobName = {sqlType:"varchar", value:jenkinsJobName};
        sql:Parameter[] parameterArray = [paraJenkinsJobName];
        datatable responseDataFromDb = connection.select(query ,parameterArray);
        responseDbJson,_ = <json>responseDataFromDb;

        response = responseDbJson;




    }catch(errors:Error err){
        response = {"responseType":"Error","responseMessage":err.msg};

        system:println(response);

    }
    return response;

}