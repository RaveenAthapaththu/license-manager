package org.wso2.internalapps.licensemanager.database;

import ballerina.data.sql;
import ballerina.lang.errors;
import org.wso2.internalapps.licensemanager.conf;
import ballerina.utils.logger;
import ballerina.doc;
import ballerina.lang.jsons;



@doc:Description {value:"Set Connection"}
function setConnection(){
    sql:ClientConnector connection = null;
    if(connection == null){
        string dbURL = conf:getConfigData("DB_URL");
        string username = conf:getConfigData("DB_USERNAME");
        string password = conf:getConfigData("DB_PASSWORD");
        int poolSize;
        poolSize,_ = <int>conf:getConfigData("POOL_SIZE");
        map propertiesMap = {"jdbcUrl":dbURL, "username":username, "password":password,"maximumPoolSize":poolSize};
        connection = create sql:ClientConnector(propertiesMap);
        return;
    }
}


@doc:Description {value:"Get Connection"}
function getConnection()(sql:ClientConnector con){
    string dbURL = conf:getConfigData("DB_URL");
    string username = conf:getConfigData("DB_USERNAME");
    string password = conf:getConfigData("DB_PASSWORD");
    int poolSize;
    poolSize,_ = <int>conf:getConfigData("POOL_SIZE");
    map propertiesMap = {"jdbcUrl":dbURL, "username":username, "password":password,"maximumPoolSize":poolSize};
    con = create sql:ClientConnector(propertiesMap);
    return;

}

@doc:Description {value:"Get Props"}
function getProps()(map propertiesMap){
    string dbURL = conf:getConfigData("DB_URL");
    string username = conf:getConfigData("DB_USERNAME");
    string password = conf:getConfigData("DB_PASSWORD");
    int poolSize;
    poolSize,_ = <int>conf:getConfigData("POOL_SIZE");
    propertiesMap = {"jdbcUrl":dbURL, "username":username, "password":password,"maximumPoolSize":poolSize};
    return;

}

@doc:Description {value:"Insert data into LM_REPOSITORY table"}
@doc:Param {value:"name: Repository name"}
@doc:Param {value:"language: Repository language"}
@doc:Param {value:"buildable: Repository buildable"}
@doc:Param {value:"nexus: Does it has Nexus"}
@doc:Param {value:"private: Is repository private"}
@doc:Param {value:"description: Description for README"}
@doc:Param {value:"groupId: Group ID of the team"}
@doc:Param {value:"license: License of the repository/product/component"}
@doc:Param {value:"team: GitHub team of the repository"}
@doc:Param {value:"organization: GitHub organization of the repository"}
@doc:Param {value:"repoType: Repository Type(Carbon/Product etc)"}
@doc:Param {value:"requestedBy: E-mail of the requested person"}
@doc:Param {value:"returnValue: No. of rows affected by"}
function repositoryInsertData(string name,string language,boolean buildable,boolean nexus,boolean private,string description,string groupId,int license,int team,int organization,int repoType,string requestBy, string productArea)(int returnValue) {

    sql:ClientConnector connection = getConnection();

    try {
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
                                                    REPOSITORY_REQUEST_BY,
                                                    REPOSITORY_PRODUCT_AREA
                                                  )
                                                   VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
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
        sql:Parameter paraProductArea = {sqlType:"varchar", value:productArea};
        sql:Parameter[] parameterArray = [paraName,paraLanguage,paraBuildable,paraNexus,paraPrivate,paraDescription,paraGroupId,paraLicense,paraTeam,paraOrganization,paraRepoType,paraRequestBy,paraProductArea];
        returnValue = connection.update(query,parameterArray);
        logger:debug(returnValue);
    } catch(errors:Error err) {
        returnValue = -1;
        logger:error("DB functions : repositoryInsertData " + err.msg);
    } finally {
        connection.close();
    }
    return returnValue;
}

@doc:Description {value:"Update rejected details in LM_REPOSITORY table"}
@doc:Param {value:"rejectBy: E-mail of the person who reject the request"}
@doc:Param {value:"rejectReason: reason for reject it"}
@doc:Param {value:"repositoryId: ID of the rejected repository request in LM_REPOSITORY table"}
@doc:Param {value:"returnValue: No. of rows affected by"}
function repositoryUpdateRejectDetails(string rejectBy,string rejectReason,int repositoryId)(int returnValue){

    sql:ClientConnector connection = getConnection();

    try {
        string query = "UPDATE LM_REPOSITORY SET REPOSITORY_ACCEPT = ? , REPOSITORY_DEACTIVATED_BY = ? , REPOSITORY_DEACTIVATED_REASON = ? WHERE REPOSITORY_ID = ?";
        sql:Parameter paraAccept = {sqlType:"boolean", value:false};
        sql:Parameter paraRejectBy = {sqlType:"varchar", value:rejectBy};
        sql:Parameter paraRejectReason = {sqlType:"varchar", value:rejectReason};
        sql:Parameter paraRepositoryId = {sqlType:"integer", value:repositoryId};
        sql:Parameter[] parameterArray = [paraAccept,paraRejectBy,paraRejectReason,paraRepositoryId];
        returnValue = connection.update(query,parameterArray);
        logger:debug(returnValue);
    } catch(errors:Error err) {
        logger:error("DB functions : repositoryInsertData " + err.msg);
    } finally {
        connection.close();
    }

    return returnValue;
}

@doc:Description {value:"Update BPMN details in LM_REPOSITORY table"}
@doc:Param {value:"taskId: Task ID of the business process"}
@doc:Param {value:"processId: Process ID of the business process"}
@doc:Param {value:"repositoryName: name of the repository request in LM_REPOSITORY table"}
@doc:Param {value:"returnValue: No. of rows affected by"}
function repositoryUpdateTaskAndProcessIds(int taskId,int processId,string repositoryName)(int returnValue) {

    sql:ClientConnector connection = getConnection();

    try {
        string query = "UPDATE LM_REPOSITORY SET REPOSITORY_BPMN_TASK_ID = ? , REPOSITORY_BPMN_PROCESS_ID = ? WHERE REPOSITORY_NAME = ?";
        sql:Parameter paraTaskId = {sqlType:"integer", value:taskId};
        sql:Parameter paraProcessId = {sqlType:"integer", value:processId};
        sql:Parameter paraRepositoryName = {sqlType:"varchar", value:repositoryName};
        sql:Parameter[] parameterArray = [paraTaskId,paraProcessId,paraRepositoryName];
        returnValue = connection.update(query,parameterArray);
        logger:debug(returnValue);
    } catch(errors:Error err) {
        returnValue = -1;
        logger:error("DB functions : repositoryInsertData " + err.msg);
    } finally {
        connection.close();
    }

    return;
}

@doc:Description {value:"Update all data of LM_REPOSITORY table"}
@doc:Param {value:"name: Repository name"}
@doc:Param {value:"language: Repository language"}
@doc:Param {value:"buildable: Repository buildable"}
@doc:Param {value:"nexus: Does it has Nexus"}
@doc:Param {value:"private: Is repository private"}
@doc:Param {value:"description: Description for README"}
@doc:Param {value:"groupId: Group ID of the team"}
@doc:Param {value:"license: License of the repository/product/component"}
@doc:Param {value:"team: GitHub team of the repository"}
@doc:Param {value:"organization: GitHub organization of the repository"}
@doc:Param {value:"repoType: Repository Type(Carbon/Product etc)"}
@doc:Param {value:"accept: Request is accepted/not"}
@doc:Param {value:"acceptBy: E-mail of the accepted person"}
@doc:Param {value:"requestedBy: E-mail of the requested person"}
@doc:Param {value:"returnValue: No. of rows affected by"}
function repositoryUpdateAll(string name,string language,boolean buildable,boolean nexus,boolean private,string description,string groupId,int license,int team,int organization,int repoType,boolean accept,string acceptBy,string productArea,int id)(int returnValue) {

    sql:ClientConnector connection = getConnection();

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
                                                    REPOSITORY_ACCEPTED_BY = ?,
                                                    REPOSITORY_PRODUCT_AREA = ?

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
        sql:Parameter paraProductArea = {sqlType:"varchar", value:productArea};
        sql:Parameter paraRepositoryId = {sqlType:"integer", value:id};
        sql:Parameter[] parameterArray = [paraName, paraLanguage, paraBuildable, paraNexus, paraPrivate, paraDescription, paraGroupId, paraLicense, paraTeam, paraOrganization, paraRepoType, paraAccept, paraAcceptBy, paraProductArea, paraRepositoryId];
        returnValue = connection.update(query, parameterArray);
        logger:debug(returnValue);
    } catch(errors:Error err) {
        returnValue = -1;
        logger:error("DB functions : repositoryUpdateAll " + err.msg);
    } finally {
        connection.close();
    }
    return returnValue;
}

@doc:Description {value:"Select all data from LM_REPOSITORY table"}
@doc:Param {value:"resultJson: JSON payload of selected data"}
function repositorySelectAll()(json resultJson) {

    sql:ClientConnector connection = getConnection();

    try {
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
        resultJson,_ = <json>responseDataFromDb;
        logger:debug(resultJson);
    } catch(errors:Error err) {
        resultJson = {"responseType":"Error","responseMessage":err.msg};
        logger:error("DB functions : repositorySelectAll " + err.msg);
    } finally {
        connection.close();
    }
    return;
}

@doc:Description {value:"Select LM_REPOSITORY table data from name"}
@doc:Param {value:"name: Repository name"}
@doc:Param {value:"resultJson: JSON payload of selected data"}
function repositorySelectFromName(string name)(json resultJson) {

    sql:ClientConnector connection = getConnection();

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
        resultJson,_ = <json>responseDataFromDb;
        logger:debug(resultJson);
    } catch(errors:Error err) {
        resultJson = {"responseType":"Error","responseMessage":err.msg};
        logger:error("DB functions : repositorySelectFromName " + err.msg);
    } finally {
        connection.close();
    }
    return;
}

@doc:Description {value:"Select LM_REPOSITORY table data from ID"}
@doc:Param {value:"name: Repository ID"}
@doc:Param {value:"resultJson: JSON payload of selected data"}
function repositorySelectFromId(int id)(json resultJson){

    sql:ClientConnector connection = getConnection();

    try {

        string query = "SELECT
                            LM_REPOSITORY.*,
                            LM_LICENSE.LICENSE_NAME,
                            LM_LICENSE.LICENSE_KEY,
                            LM_LICENSE.LICENSE_GITHUB_KEY,
                            LM_ORGANIZATION.ORGANIZATION_NAME,
                            LM_ORGANIZATION.ORGANIZATION_ADMIN_TEAM,
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
        resultJson,_ = <json>responseDataFromDb;
        logger:debug(resultJson);
    } catch(errors:Error err) {
        resultJson = {"responseType":"Error","responseMessage":err.msg};
        logger:error("DB functions : repositorySelectFromId " + err.msg);
    } finally {
        connection.close();
    }
    return;
}

@doc:Description {value:"Select LM_REPOSITORY table data from request by and waiting"}
@doc:Param {value:"name: E-mail of the requested person"}
@doc:Param {value:"resultJson: JSON payload of selected data"}
function repositorySelectFromRequestByAndWaiting(string requestBy)(json resultJson) {

    sql:ClientConnector connection = getConnection();

    try {
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
                        WHERE REPOSITORY_REQUEST_BY = ? AND REPOSITORY_ACCEPT IS NULL ORDER BY LM_REPOSITORY.REPOSITORY_ID DESC";
        sql:Parameter paraName = {sqlType:"varchar", value:requestBy};
        sql:Parameter[] parameterArray = [paraName];
        datatable responseDataFromDb = connection.select(query ,parameterArray);
        resultJson,_ = <json>responseDataFromDb;
        logger:debug(resultJson);
    } catch(errors:Error err) {
        resultJson = {"responseType":"Error","responseMessage":err.msg};
        logger:error("DB functions : repositorySelectFromRequestByAndWaiting " + err.msg);
    } finally {
        connection.close();
    }
    return;
}

@doc:Description {value:"Select LM_REPOSITORY table data which are in waiting state"}
@doc:Param {value:"resultJson: JSON payload of selected data"}
function repositorySelectWaitingRequests()(json resultJson){

    sql:ClientConnector connection = getConnection();

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
                        WHERE REPOSITORY_ACCEPT IS NULL ORDER BY LM_REPOSITORY.REPOSITORY_ID DESC";
        sql:Parameter[] parameterArray = [];
        datatable responseDataFromDb = connection.select(query ,parameterArray);
        resultJson,_ = <json>responseDataFromDb;
        logger:debug(resultJson);
    } catch(errors:Error err) {
        resultJson = {"responseType":"Error","responseMessage":err.msg};
        logger:error("DB functions : repositorySelectWaitingRequests " + err.msg);
    } finally {
        connection.close();
    }
    return;
}

@doc:Description {value:"Select all from LM_ORGANIZATION table"}
@doc:Param {value:"resultJson: JSON payload of selected data"}
function organizationSelectAll()(json resultJson) {

    sql:ClientConnector connection = getConnection();

    try{
        string query = "SELECT * FROM LM_ORGANIZATION";
        sql:Parameter[] parameterArray = [];
        datatable responseDataFromDb = connection.select(query ,parameterArray);
        resultJson,_ = <json>responseDataFromDb;
        logger:debug(resultJson);
    }catch(errors:Error err){
        resultJson = {"responseType":"Error","responseMessage":err.msg};
        logger:error("DB functions : organizationSelectAll " + err.msg);
    } finally {
        connection.close();
    }
    return;

}

@doc:Description {value:"Select all from LM_LICENSE table"}
@doc:Param {value:"resultJson: JSON payload of selected data"}
function licenseSelectAll()(json resultJson) {

    sql:ClientConnector connection = getConnection();

    try{
        string query = "SELECT * FROM LM_LICENSE";
        sql:Parameter[] parameterArray = [];
        datatable responseDataFromDb = connection.select(query ,parameterArray);
        resultJson,_ = <json>responseDataFromDb;
        logger:debug(resultJson);
    }catch(errors:Error err){
        resultJson = {"responseType":"Error","responseMessage":err.msg};
        logger:error("DB functions : licenseSelectAll " + err.msg);
    } finally {
        connection.close();
    }
    return;
}

@doc:Description {value:"Select all from LM_REPOTYPE table"}
@doc:Param {value:"resultJson: JSON payload of selected data"}
function repositoryTypeSelectAll()(json resultJson){

    sql:ClientConnector connection = getConnection();

    try{
        string query = "SELECT * FROM LM_REPOSITORYTYPE";
        sql:Parameter[] parameterArray = [];
        datatable responseDataFromDb = connection.select(query ,parameterArray);
        resultJson,_ = <json>responseDataFromDb;
        logger:debug(resultJson);
    }catch(errors:Error err){
        resultJson = {"responseType":"Error","responseMessage":err.msg};
        logger:error("DB functions : repositoryTypeSelectAll " + err.msg);
    } finally {
        connection.close();
    }
    return;
}

@doc:Description {value:"Select all from LM_COMPONENT table"}
@doc:Param {value:"resultJson: JSON payload of selected data"}
function componentSelectAll()(json resultJson){

    sql:ClientConnector connection = getConnection();

    try {
        string query = "SELECT * FROM LM_COMPONENT";
        sql:Parameter[] parameterArray = [];
        datatable responseDataFromDb = connection.select(query ,parameterArray);
        resultJson,_ = <json>responseDataFromDb;
        logger:debug(resultJson);
    } catch(errors:Error err) {
        resultJson = {"responseType":"Error","responseMessage":err.msg};
        logger:error("DB functions : componentSelectAll " + err.msg);
    } finally {
        connection.close();
    }
    return;

}

@doc:Description {value:"Select repository admin users from LM_ROLE table"}
@doc:Param {value:"resultJson: JSON payload of selected data"}
function roleSelectRepositoryAdminUsers()(json resultJson) {

    sql:ClientConnector connection = getConnection();

    try {
        string query = "SELECT * FROM LM_ROLE WHERE ROLE_TYPE = 'REPOSITORY' AND ROLE_PERMISSION = 'ADMIN'";
        sql:Parameter[] parameterArray = [];
        datatable responseDataFromDb = connection.select(query ,parameterArray);
        resultJson,_ = <json>responseDataFromDb;
        logger:debug(resultJson);
    } catch(errors:Error err) {
        resultJson = {"responseType":"Error","responseMessage":err.msg};
        logger:error("DB functions : roleSelectRepositoryAdminUsers " + err.msg);
    } finally {
        connection.close();
    }
    return;

}

@doc:Description {value:"Select repository main users from LM_ROLE table"}
@doc:Param {value:"resultJson: JSON payload of selected data"}
function roleSelectRepositoryMainUsers()(json resultJson){

    sql:ClientConnector connection = getConnection();

    try {
        string query = "SELECT * FROM LM_ROLE WHERE ROLE_TYPE = 'REPOSITORY'";
        sql:Parameter[] parameterArray = [];
        datatable responseDataFromDb = connection.select(query ,parameterArray);
        resultJson,_ = <json>responseDataFromDb;
        logger:debug(resultJson);
    } catch(errors:Error err) {
        resultJson = {"responseType":"Error","responseMessage":err.msg};
        logger:error("DB functions : roleSelectRepositoryMainUsers " + err.msg);
    } finally {
        connection.close();
    }
    return;

}

@doc:Description {value:"Check whether given e-mail is a repository admin user e-mail from LM_ROLE table"}
@doc:Param {value:"email: given e-mail to check"}
@doc:Param {value:"resultJson: JSON payload of selected data"}
function roleRepositoryCheckAdminUsers(string email)(json resultJson){

    json responseDbJson;

    sql:ClientConnector connection = getConnection();

    try{
        string query = "SELECT * FROM LM_ROLE WHERE ROLE_EMAIL = ? AND ROLE_TYPE = 'REPOSITORY'";
        sql:Parameter paraEmail = {sqlType:"varchar", value:email};
        sql:Parameter[] parameterArray = [paraEmail];
        datatable responseDataFromDb = connection.select(query ,parameterArray);
        responseDbJson,_ = <json>responseDataFromDb;
        logger:debug(responseDbJson);
        int length = lengthof responseDbJson;
        if(length > 0){
            resultJson = responseDbJson;
        }else{
            resultJson = [];
        }
    }catch(errors:Error err){
        resultJson = {"responseType":"Error","responseMessage":err.msg};
        logger:error("DB functions : roleRepositoryCheckAdminUsers " + err.msg);

    } finally {
        connection.close();
    }
    return;

}

@doc:Description {value:"Select library main users from LM_ROLE table"}
@doc:Param {value:"resultJson: JSON payload of selected data"}
function roleSelectLibraryMainUsers()(json resultJson){

    sql:ClientConnector connection = getConnection();

    try {
        string query = "SELECT * FROM LM_ROLE WHERE ROLE_TYPE = 'LIBRARY'";
        sql:Parameter[] parameterArray = [];
        datatable responseDataFromDb = connection.select(query ,parameterArray);
        resultJson,_ = <json>responseDataFromDb;
        logger:debug(resultJson);
    }catch(errors:Error err){
        resultJson = {"responseType":"Error","responseMessage":err.msg};
        logger:error("DB functions : roleSelectLibraryMainUsers " + err.msg);
    } finally {
        connection.close();
    }
    return;
}

@doc:Description {value:"Select all from LM_LIBCATEGORY table"}
@doc:Param {value:"resultJson: JSON payload of selected data"}
function libCategorySelectAll()(json resultJson){

    sql:ClientConnector connection = getConnection();

    try {
        string query = "SELECT * FROM LM_LIBCATEGORY";
        sql:Parameter[] parameterArray = [];
        datatable responseDataFromDb = connection.select(query ,parameterArray);
        resultJson,_ = <json>responseDataFromDb;
        logger:debug(resultJson);
    } catch(errors:Error err) {
        resultJson = {"responseType":"Error","responseMessage":err.msg};
        logger:error("DB functions : libCategorySelectAll " + err.msg);
    } finally {
        connection.close();
    }
    return;

}

@doc:Description {value:"Get role details from LM_ROLE table for given e-mail address"}
@doc:Param {value:"email: given e-mail to select data"}
@doc:Param {value:"resultJson: JSON payload of selected data"}
function roleGetUserDetails(string email)(json resultJson) {
    json responseDbJson;

    sql:ClientConnector connection = getConnection();

    try{
        string query = "SELECT
                            LM_ROLE.*,
                            LM_LIBCATEGORY.LIBCATEGORY_NAME AS ROLE_LIB_CATEGORY_NAME,
                            LM_LIBCATEGORY.LIBCATEGORY_ID AS ROLE_LIB_CATEGORY_ID
                        FROM LM_ROLE
                        LEFT JOIN LM_LIBCATEGORY ON LM_ROLE.ROLE_LIB_CATEGORY = LM_LIBCATEGORY.LIBCATEGORY_ID
                        WHERE ROLE_EMAIL=?";
        sql:Parameter paraEmail = {sqlType:"varchar", value:email};
        sql:Parameter[] parameterArray = [paraEmail];
        datatable responseDataFromDb = connection.select(query ,parameterArray);
        responseDbJson,_ = <json>responseDataFromDb;
        logger:debug(responseDbJson);
        int length = lengthof responseDbJson;
        if(length > 0){
            resultJson = responseDbJson;
        }else{
            resultJson = [];
        }
    }catch(errors:Error err){
        resultJson = {"responseType":"Error","responseMessage":err.msg};
        logger:error("DB functions : roleGetUserDetails " + err.msg);
    } finally {
        connection.close();
    }
    return;

}

@doc:Description {value:"Select folder to add Jenkins job"}
@doc:Param {value:"jenkinsJobName: Jenkins job name"}
@doc:Param {value:"resultJson: JSON payload of selected data"}
function jenkinsFolderMatchRegex(string jenkinsJobName)(json resultJson){

    sql:ClientConnector connection = getConnection();

    try{
        string query = "CALL JENKINS_GET_FOLDER(?)";
        sql:Parameter paraJenkinsJobName = {sqlType:"varchar", value:jenkinsJobName};
        sql:Parameter[] parameterArray = [paraJenkinsJobName];
        datatable responseDataFromDb = connection.select(query ,parameterArray);
        resultJson,_ = <json>responseDataFromDb;
        logger:debug(resultJson);
    }catch(errors:Error err){
        resultJson = {"responseType":"Error","responseMessage":err.msg};
        logger:error("DB functions : jenkinsFolderMatchRegex " + err.msg);
    } finally {
        connection.close();
    }
    return resultJson;

}

@doc:Description {value:"Insert data into LM_COMPONENT table"}
@doc:Param {value:"key: Component key"}
@doc:Param {value:"url: Component URL"}
@doc:Param {value:"returnValue: No. of rows affected by"}
function componentInsertData(string key,string url)(int returnValue){

    sql:ClientConnector connection = getConnection();

    try {
        string query = "INSERT INTO LM_COMPONENT(
                                                    COMP_KEY,
                                                    COMP_NAME,
                                                    COMP_TYPE,
                                                    COMP_URL,
                                                    COMP_FILE_NAME
                                                  )
                                                   VALUES (?,?,?,?,?)";
        sql:Parameter paraKey = {sqlType:"varchar", value:key};
        sql:Parameter paraName = {sqlType:"varchar", value:key};
        sql:Parameter paraType = {sqlType:"boolean", value:"bundle"};
        sql:Parameter paraUrl = {sqlType:"boolean", value:url};
        sql:Parameter paraFileName = {sqlType:"boolean", value:key};
        sql:Parameter[] parameterArray = [paraKey,paraName,paraType,paraUrl,paraFileName];
        returnValue = connection.update(query,parameterArray);
        logger:debug(returnValue);
    }catch(errors:Error err){
        returnValue = -1;
        logger:error("DB functions : componentInsertData " + err.msg);
    } finally {
        connection.close();
    }
    return returnValue;
}

@doc:Description {value:"Check whether given library name and version is previously requested or already in LM_LIBRARY table"}
@doc:Param {value:"libraryName: Library name"}
@doc:Param {value:"libraryVersion: Library version"}
@doc:Param {value:"returnValue: No. of rows affected by"}
function libraryAndRequestSelectFromNameAndVersion(string libraryName,string libraryVersion)(json resultJson){

    sql:ClientConnector connection = getConnection();

    try {
        string query = "(SELECT LIB_ID,LIB_NAME,LIB_VERSION FROM LM_LIBRARY WHERE LIB_NAME=? AND LIB_VERSION=?)
                        UNION
                        (SELECT LIBREQUEST_ID,LIBREQUEST_NAME,LIBREQUEST_USE_VERSION FROM LM_LIBREQUEST WHERE LIBREQUEST_NAME=? AND LIBREQUEST_USE_VERSION=?);";
        sql:Parameter paraLibraryName = {sqlType:"varchar", value:libraryName};
        sql:Parameter paraLibraryVersion = {sqlType:"varchar", value:libraryVersion};
        sql:Parameter[] parameterArray = [paraLibraryName,paraLibraryVersion,paraLibraryName,paraLibraryVersion];
        datatable responseDataFromDb = connection.select(query ,parameterArray);
        resultJson,_ = <json>responseDataFromDb;
        logger:debug(resultJson);
    }catch(errors:Error err){
        resultJson = {"responseType":"Error","responseMessage":err.msg};
        logger:error("DB functions : libraryAndRequestSelectFromNameAndVersion " + err.msg);
    } finally {
        connection.close();
    }
    return;

}

@doc:Description {value:"Select default values of LM_LIBTYPE table"}
@doc:Param {value:"resultJson: JSON payload of selected data"}
function libTypeSelectDefault()(json resultJson){

    sql:ClientConnector connection = getConnection();

    try {
        string query = "SELECT
                            LM_LIBTYPE.*,
                            LM_LIBCATEGORY.LIBCATEGORY_NAME
                        FROM LM_LIBTYPE
                        INNER JOIN LM_LIBCATEGORY ON LM_LIBTYPE.LIBTYPE_CATEGORY = LM_LIBCATEGORY.LIBCATEGORY_ID
                        WHERE LIBTYPE_CATEGORY = (SELECT LIBCATEGORY_ID FROM LM_LIBCATEGORY LIMIT 1)";
        sql:Parameter[] parameterArray = [];
        datatable responseDataFromDb = connection.select(query ,parameterArray);
        resultJson,_ = <json>responseDataFromDb;
        logger:debug(resultJson);
    } catch(errors:Error err) {
        resultJson = {"responseType":"Error","responseMessage":err.msg};
        logger:error("DB functions : libTypeSelectDefault " + err.msg);
    } finally {
        connection.close();
    }
    return;

}

@doc:Description {value:"Select library types from LM_LIBTYPE table for given category"}
@doc:Param {value:"categoryId: Given category for select library types"}
@doc:Param {value:"resultJson: JSON payload of selected data"}
function libTypeSelectFromCategory(int categoryId)(json resultJson){

    sql:ClientConnector connection = getConnection();

    try {
        string query = "SELECT
                            LM_LIBTYPE.*,
                            LM_LIBCATEGORY.LIBCATEGORY_NAME
                        FROM LM_LIBTYPE
                        INNER JOIN LM_LIBCATEGORY ON LM_LIBTYPE.LIBTYPE_CATEGORY = LM_LIBCATEGORY.LIBCATEGORY_ID
                        WHERE LIBTYPE_CATEGORY = ?";
        sql:Parameter paraCategoryId = {sqlType:"integer", value:categoryId};
        sql:Parameter[] parameterArray = [paraCategoryId];
        datatable responseDataFromDb = connection.select(query ,parameterArray);
        resultJson,_ = <json>responseDataFromDb;
        logger:debug(resultJson);
    }catch(errors:Error err){
        resultJson = {"responseType":"Error","responseMessage":err.msg};
        logger:error("DB functions : libTypeSelectFromCategory " + err.msg);
    } finally {
        connection.close();
    }
    return;

}

@doc:Description {value:"Insert data into LM_LIBREQUEST table"}
@doc:Param {value:"name: Library name"}
@doc:Param {value:"libType: Library type"}
@doc:Param {value:"category: Library category"}
@doc:Param {value:"groupId: Library group ID : only for Java libraries"}
@doc:Param {value:"artifactId: Library artifact ID : only for Java libraries"}
@doc:Param {value:"useVersion: Version that we going to use"}
@doc:Param {value:"latestVersion: Latest version of the library"}
@doc:Param {value:"fileName: Library file name"}
@doc:Param {value:"company: Company of the library"}
@doc:Param {value:"sponsored: Does that company sponsored or not"}
@doc:Param {value:"purpose: Purpose of using this library"}
@doc:Param {value:"description: Description about library"}
@doc:Param {value:"alternatives: Alternative libraries"}
@doc:Param {value:"requestBy: E-mail of the requested person"}
@doc:Param {value:"returnValue: No. of rows affected by"}
function libraryRequestInsertData(string name,int libType,int category,string groupId,string artifactId,string useVersion,string latestVersion,string fileName,string company,boolean sponsored,string purpose,string description,string alternatives,string requestBy)(int returnValue){

    sql:ClientConnector connection = getConnection();

    try{
        string query = "INSERT INTO LM_LIBREQUEST(
                                                    LIBREQUEST_NAME,
                                                    LIBREQUEST_TYPE,
                                                    LIBREQUEST_CATEGORY,
                                                    LIBREQUEST_GROUP_ID,
                                                    LIBREQUEST_ARTIFACT_ID,
                                                    LIBREQUEST_USE_VERSION,
                                                    LIBREQUEST_LATEST_VERSION,
                                                    LIBREQUEST_FILE_NAME,
                                                    LIBREQUEST_COMPANY,
                                                    LIBREQUEST_SPONSORED,
                                                    LIBREQUEST_PURPOSE,
                                                    LIBREQUEST_DESCRIPTION,
                                                    LIBREQUEST_ALTERNATIVES,
                                                    LIBREQUEST_REQUESTED_BY
                                                  )
                                                   VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        sql:Parameter paraName = {sqlType:"varchar", value:name};
        sql:Parameter paraType = {sqlType:"integer", value:libType};
        sql:Parameter paraCategory = {sqlType:"integer", value:category};
        sql:Parameter paraGroupId = {sqlType:"varchar", value:groupId};
        sql:Parameter paraArtifactId = {sqlType:"varchar", value:artifactId};
        sql:Parameter paraUseVersion = {sqlType:"varchar", value:useVersion};
        sql:Parameter paraLatestVersion = {sqlType:"varchar", value:latestVersion};
        sql:Parameter paraFileName = {sqlType:"varchar", value:fileName};
        sql:Parameter paraCompany = {sqlType:"varchar", value:company};
        sql:Parameter paraSponsored = {sqlType:"boolean", value:sponsored};
        sql:Parameter paraPurpose = {sqlType:"varchar", value:purpose};
        sql:Parameter paraDescription = {sqlType:"varchar", value:description};
        sql:Parameter paraAlternatives = {sqlType:"varchar", value:alternatives};
        sql:Parameter paraRequestBy = {sqlType:"varchar", value:requestBy};
        sql:Parameter[] parameterArray = [
                                         paraName,
                                         paraType,
                                         paraCategory,
                                         paraGroupId,
                                         paraArtifactId,
                                         paraUseVersion,
                                         paraLatestVersion,
                                         paraFileName,
                                         paraCompany,
                                         paraSponsored,
                                         paraPurpose,
                                         paraDescription,
                                         paraAlternatives,
                                         paraRequestBy
                                         ];

        returnValue = connection.update(query,parameterArray);
        logger:debug(returnValue);
    }catch(errors:Error err){
        returnValue = -1;
        logger:error("DB functions : libraryRequestInsertData " + err.msg);
    } finally {
        connection.close();
    }

    return;
}

@doc:Description {value:"Insert data into LM_LIBRARY table"}
@doc:Param {value:"name: Library name"}
@doc:Param {value:"libType: Library type"}
@doc:Param {value:"useVersion: Version that we going to use"}
@doc:Param {value:"fileName: Library file name"}
@doc:Param {value:"description: Description about library"}
@doc:Param {value:"groupId: Library group ID : only for Java libraries"}
@doc:Param {value:"artifactId: Library artifact ID : only for Java libraries"}
@doc:Param {value:"returnValue: No. of rows affected by"}
function libraryInsertData(string name,string libType,string useVersion,string fileName,string description,string groupId,string artifactId)(int returnValue){

    sql:ClientConnector connection = getConnection();

    try {
        string query = "INSERT INTO LM_LIBRARY(
                                                    LIB_NAME,
                                                    LIB_TYPE,
                                                    LIB_VERSION,
                                                    LIB_FILE_NAME,
                                                    LIB_DESCRIPTION,
                                                    LIB_GROUP_ID,
                                                    LIB_ARTIFACT_ID
                                              )
                                               VALUES (?,?,?,?,?,?,?)";
        sql:Parameter paraName = {sqlType:"varchar", value:name};
        sql:Parameter paraType = {sqlType:"varchar", value:libType};
        sql:Parameter paraUseVersion = {sqlType:"varchar", value:useVersion};
        sql:Parameter paraFileName = {sqlType:"varchar", value:fileName};
        sql:Parameter paraDescription = {sqlType:"varchar", value:description};
        sql:Parameter paraGroupId = {sqlType:"varchar", value:groupId};
        sql:Parameter paraArtifactId = {sqlType:"varchar", value:artifactId};
        sql:Parameter[] parameterArray = [
                                         paraName,
                                         paraType,
                                         paraUseVersion,
                                         paraFileName,
                                         paraDescription,
                                         paraGroupId,
                                         paraArtifactId

                                         ];
        returnValue = connection.update(query,parameterArray);
        logger:debug(returnValue);
    } catch(errors:Error err) {
        returnValue = -1;
        logger:error("DB functions : libraryInsertData " + err.msg);
    } finally {
        connection.close();
    }

    return returnValue;
}

@doc:Description {value:"Select library request data from LM_LIBREQUEST table for given library name and version"}
@doc:Param {value:"libraryName: Library name"}
@doc:Param {value:"libraryVersion: Library version"}
@doc:Param {value:"resultJson: JSON payload of selected data"}
function libraryRequestSelectFromNameAndVersion(string libraryName,string libraryVersion)(json resultJson){

    sql:ClientConnector connection = getConnection();

    try {
        string query = "SELECT * FROM LM_LIBREQUEST WHERE LIBREQUEST_NAME = ? AND LIBREQUEST_USE_VERSION = ?";
        sql:Parameter paraLibraryName = {sqlType:"varchar", value:libraryName};
        sql:Parameter paraLibraryVersion = {sqlType:"varchar", value:libraryVersion};
        sql:Parameter[] parameterArray = [paraLibraryName,paraLibraryVersion];
        datatable responseDataFromDb = connection.select(query ,parameterArray);
        resultJson,_ = <json>responseDataFromDb;
        logger:debug(resultJson);
    } catch(errors:Error err) {
        resultJson = {"responseType":"Error","responseMessage":err.msg};
        logger:error("DB functions : libraryRequestSelectFromNameAndVersion " + err.msg);
    } finally {
        connection.close();
    }
    return;

}

@doc:Description {value:"Update BPMN details in LM_LIBREQUEST table"}
@doc:Param {value:"taskId: Task ID of the business process"}
@doc:Param {value:"processId: Process ID of the business process"}
@doc:Param {value:"libraryName: name of the library request in LM_LIBREQUEST table"}
@doc:Param {value:"returnValue: No. of rows affected by"}
function libraryRequestUpdateTaskAndProcessIds(int taskId,int processId,string libraryName, string useVersion)(int returnValue){

    sql:ClientConnector connection = getConnection();

    try{
        string query = "UPDATE LM_LIBREQUEST SET LIBREQUEST_BPMN_TASK_ID = ? , LIBREQUEST_BPMN_PROCESS_ID = ? WHERE LIBREQUEST_NAME = ? AND LIBREQUEST_USE_VERSION = ?";
        sql:Parameter paraTaskId = {sqlType:"integer", value:taskId};
        sql:Parameter paraProcessId = {sqlType:"integer", value:processId};
        sql:Parameter paraLibraryName = {sqlType:"varchar", value:libraryName};
        sql:Parameter paraUseVersion = {sqlType:"varchar", value:useVersion};
        sql:Parameter[] parameterArray = [paraTaskId,paraProcessId,paraLibraryName,paraUseVersion];
        returnValue = connection.update(query,parameterArray);
        logger:debug(returnValue);
    }catch(errors:Error err){
        returnValue = -1;
        logger:error("DB functions : libraryRequestUpdateTaskAndProcessIds " + err.msg);
    } finally {
        connection.close();
    }
    return;
}

@doc:Description {value:"Update accept or reject details in LM_LIBREQUEST table"}
@doc:Param {value:"accept: ACCEPT/REJECT"}
@doc:Param {value:"acceptOrRejectBy: E-mail of the person who accept or reject the request"}
@doc:Param {value:"rejectReason: Reason for rejecting : Only ask if accept == REJECT"}
@doc:Param {value:"libRequestId: ID of the library request"}
@doc:Param {value:"returnValue: No. of rows affected by"}
function libraryRequestUpdateAcceptOrRejectDetails(string accept,string acceptOrRejectBy,string rejectReason,int libRequestId)(int returnValue){

    sql:ClientConnector connection = getConnection();

    try {
        string query = "UPDATE
                            LM_LIBREQUEST SET LIBREQUEST_ACCEPTED = ? ,
                            LIBREQUEST_ACCEPT_OR_REJECT_BY = ? ,
                            LIBREQUEST_REJECT_REASON = ?
                        WHERE LIBREQUEST_ID = ?";
        sql:Parameter paraAccept = {sqlType:"varchar", value:accept};
        sql:Parameter paraAcceptOrRejectBy = {sqlType:"varchar", value:acceptOrRejectBy};
        sql:Parameter paraRejectReason = {sqlType:"varchar", value:rejectReason};
        sql:Parameter paraRequestId = {sqlType:"integer", value:libRequestId};
        sql:Parameter[] parameterArray = [paraAccept,paraAcceptOrRejectBy,paraRejectReason,paraRequestId];
        returnValue = connection.update(query,parameterArray);
        logger:debug(returnValue);
    } catch(errors:Error err) {
        returnValue = 0;
        logger:error("DB functions : libraryRequestUpdateAcceptOrRejectDetails " + err.msg);
    } finally {
        connection.close();
    }

    return returnValue;
}

@doc:Description {value:"Select library request details from LM_LIBREQUEST table for given ID"}
@doc:Param {value:"id: Library request ID"}
@doc:Param {value:"resultJson: JSON payload of selected data"}
function libraryRequestSelectFromId(int id)(json resultJson){

    sql:ClientConnector connection = getConnection();

    try {
        string query = "SELECT
                            LM_LIBREQUEST.*,
                            LM_LIBCATEGORY.LIBCATEGORY_NAME,
                            LM_LIBTYPE.LIBTYPE_NAME
                        FROM LM_LIBREQUEST
                        INNER JOIN LM_LIBCATEGORY ON LM_LIBREQUEST.LIBREQUEST_CATEGORY = LM_LIBCATEGORY.LIBCATEGORY_ID
                        INNER JOIN LM_LIBTYPE ON LM_LIBREQUEST.LIBREQUEST_TYPE = LM_LIBTYPE.LIBTYPE_ID
                        WHERE LIBREQUEST_ID = ?";
        sql:Parameter paraLibraryId = {sqlType:"integer", value:id};
        sql:Parameter[] parameterArray = [paraLibraryId];
        datatable responseDataFromDb = connection.select(query ,parameterArray);
        resultJson,_ = <json>responseDataFromDb;
        logger:debug(resultJson);
    } catch(errors:Error err) {
        resultJson = {"responseType":"Error","responseMessage":err.msg};
        logger:error("DB functions : libraryRequestSelectFromId " + err.msg);
    } finally {
        connection.close();
    }
    return resultJson;

}

@doc:Description {value:"Select library waiting requests details from LM_LIBREQUEST table"}
@doc:Param {value:"idArray: JSON array of set of library IDs"}
@doc:Param {value:"resultJson: JSON payload of selected data"}
function libraryRequestSelectWaitingRequests(json idArray)(json resultJson){
    string whereClause = "";
    int i = 0;
    int idArrayLength = lengthof idArray;
    sql:ClientConnector connection = getConnection();

    try {
        if(idArrayLength == 0){
            resultJson = [];
            return;
        }
        while(i < idArrayLength){
            if(i == 0){
                whereClause = " LIBREQUEST_CATEGORY = " + jsons:toString(idArray[i]);
                i = i + 1;
                continue;
            }
            whereClause = whereClause + " OR LIBREQUEST_CATEGORY = " + jsons:toString(idArray[i]);
            i = i + 1;
        }
        string query = "SELECT
                            LM_LIBREQUEST.*,
                            LM_LIBCATEGORY.LIBCATEGORY_NAME,
                            LM_LIBTYPE.LIBTYPE_NAME
                        FROM LM_LIBREQUEST
                        INNER JOIN LM_LIBCATEGORY ON LM_LIBREQUEST.LIBREQUEST_CATEGORY = LM_LIBCATEGORY.LIBCATEGORY_ID
                        INNER JOIN LM_LIBTYPE ON LM_LIBREQUEST.LIBREQUEST_TYPE = LM_LIBTYPE.LIBTYPE_ID
                        WHERE LIBREQUEST_ACCEPTED = 'WAITING' AND (" + whereClause + ") ORDER BY LM_LIBREQUEST.LIBREQUEST_ID DESC";

        sql:Parameter[] parameterArray = [];
        datatable responseDataFromDb = connection.select(query ,parameterArray);
        resultJson,_ = <json>responseDataFromDb;
        logger:debug(resultJson);
    } catch(errors:Error err) {
        resultJson = {"responseType":"Error","responseMessage":err.msg};
        logger:error("DB functions : libraryRequestSelectFromId " + err.msg);
    } finally {
        connection.close();
    }
    return resultJson;

}

@doc:Description {value:"Select library waiting requests details from LM_LIBREQUEST table by requested email"}
@doc:Param {value:"requestedBy: requested e-mail"}
@doc:Param {value:"resultJson: JSON payload of selected data"}
function libraryRequestSelectWaitingRequestsFromRequestBy(string requestedBy)(json resultJson){

    sql:ClientConnector connection = getConnection();

    try {
        string query = "SELECT
                            LM_LIBREQUEST.*,
                            LM_LIBCATEGORY.LIBCATEGORY_NAME,
                            LM_LIBTYPE.LIBTYPE_NAME
                        FROM LM_LIBREQUEST
                        INNER JOIN LM_LIBCATEGORY ON LM_LIBREQUEST.LIBREQUEST_CATEGORY = LM_LIBCATEGORY.LIBCATEGORY_ID
                        INNER JOIN LM_LIBTYPE ON LM_LIBREQUEST.LIBREQUEST_TYPE = LM_LIBTYPE.LIBTYPE_ID
                        WHERE LIBREQUEST_ACCEPTED = 'WAITING' AND LM_LIBREQUEST.LIBREQUEST_REQUESTED_BY = ? ORDER BY LM_LIBREQUEST.LIBREQUEST_ID DESC";
        sql:Parameter paraRequestedBy = {sqlType:"varchar", value:requestedBy};
        sql:Parameter[] parameterArray = [paraRequestedBy];
        datatable responseDataFromDb = connection.select(query ,parameterArray);
        resultJson,_ = <json>responseDataFromDb;
        logger:debug(resultJson);
    } catch(errors:Error err) {
        resultJson = {"responseType":"Error","responseMessage":err.msg};
        logger:error("DB functions : libraryRequestSelectFromId " + err.msg);
    } finally {
        connection.close();
    }
    return resultJson;

}