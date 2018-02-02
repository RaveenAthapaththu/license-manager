package org.wso2.internalapps.licensemanager.services;

import ballerina.net.http;
import ballerina.lang.strings;
import ballerina.utils;
import ballerina.lang.jsons;
import ballerina.lang.time;
import ballerina.lang.errors;
import ballerina.lang.messages;
import org.wso2.internalapps.licensemanager.database;
import ballerina.utils.logger;
import org.wso2.internalapps.licensemanager.conf;
import ballerina.doc;



@doc:Description {value:"Validate the JWT and create session according to that JWT"}
@doc:Param {value:"request: requested message with JWT, sometimes with Session ID as well"}
@doc:Param {value:"request: same requested message return to them with Session ID"}
function validateUser(message request)(message,string){

    http:Session userSession = null;
    message response = {};
    string finalSession;
    string sessionId = "";
    string email = "";
    string epocTimeString = "";
    string[] webTokenArray;
    string decodedString;
    string decodedStringHeader;
    string webToken;
    string roleLibCategory;
    string rolePermission;
    string publicKey;
    int epocTime;
    int currentTimeInt;
    int roleLibCategoryId;
    json decodedJson;
    json requestJson;
    json returnJson;
    json responseJson;
    json userLibraryPermissionJson;
    json userLibraryPermissionJsonArray = [];
    json decodedJsonHeader;
    boolean isValid = false;
    boolean isRepositoryAdmin = false;
    boolean isLibraryAdmin = false;
    boolean verified = false;
    boolean isAnyAdmin = false;
    int i = 0;
    int returnJsonLength = 0;
    int userLibraryPermissionJsonArrayLength;

    try{
        request = getRealRequest(request);
        requestJson = messages:getJsonPayload(request);
        webToken = jsons:toString(requestJson.token);
        responseJson = {"isValid":false,"userEmail":""};
        webTokenArray= strings:split(webToken,"\\.");
        decodedStringHeader = utils:base64decode(webTokenArray[0]);
        decodedJsonHeader = jsons:parse(decodedStringHeader);
        decodedString = utils:base64decode(webTokenArray[1]);
        decodedJson = jsons:parse(decodedString);
        publicKey = conf:getConfigData("PUBLIC_KEY");
        verified = utils:getShaWithRsa(webToken,publicKey);
        email = jsons:toString(decodedJson["http://wso2.org/claims/emailaddress"]);
        logger:info("Login: " + email);
        epocTimeString = jsons:toString(decodedJson["exp"]);
        epocTime,_ = <int>epocTimeString;
        time:Time currentTime = time:currentTime();
        currentTimeInt = currentTime.time / 1000;
        if((strings:hasSuffix(email,"@wso2.com")) && (currentTimeInt < (epocTime + 86400)) && verified ){
            isValid = true;
            userSession = http:createSessionIfAbsent(request);
            returnJson = database:roleGetUserDetails(email);
            returnJsonLength = lengthof returnJson;
            while(i < returnJsonLength){
                if(jsons:toString(returnJson[i].ROLE_TYPE) == "REPOSITORY" && jsons:toString(returnJson[i].ROLE_PERMISSION) == "ADMIN"){
                    isRepositoryAdmin = true;
                    isAnyAdmin = true;
                }
                if(jsons:toString(returnJson[i].ROLE_TYPE) == "LIBRARY"){
                    roleLibCategory = jsons:toString(returnJson[i].ROLE_LIB_CATEGORY_NAME);
                    roleLibCategoryId,_ = <int>jsons:toString(returnJson[i].ROLE_LIB_CATEGORY_ID);
                    rolePermission = jsons:toString(returnJson[i].ROLE_PERMISSION);
                    if(rolePermission == "ADMIN"){
                        isLibraryAdmin = true;
                        isAnyAdmin = true;
                    }
                    userLibraryPermissionJson = {
                                                    "roleType":"LIBRARY",
                                                    "roleLibType":roleLibCategory,
                                                    "rolePermission":rolePermission,
                                                    "roleLibId":roleLibCategoryId
                                                };
                    userLibraryPermissionJsonArrayLength = lengthof userLibraryPermissionJsonArray;
                    userLibraryPermissionJsonArray[userLibraryPermissionJsonArrayLength] = userLibraryPermissionJson;
                }
                i = i + 1;
            }
            http:setAttribute(userSession,"isValid",isValid);
            http:setAttribute(userSession,"userEmail",email);
            http:setAttribute(userSession,"loginTime",epocTime);
            http:setAttribute(userSession,"isRepositoryAdmin",isRepositoryAdmin);
            http:setAttribute(userSession,"isLibraryAdmin",isLibraryAdmin);
            http:setAttribute(userSession,"isAnyAdmin",isAnyAdmin);
            http:setAttribute(userSession,"libraryUserDetails",userLibraryPermissionJsonArray);
            sessionId = http:getId(userSession);
            responseJson = {"isValid":isValid,"isAnyAdmin":isAnyAdmin,"isRepositoryAdmin":isRepositoryAdmin,"isLibraryAdmin":isLibraryAdmin,"libraryUserDetails":userLibraryPermissionJsonArray,"userEmail":email};
        } else {
            logger:info("Invalid User");
            userSession = null;
            isValid = false;
            responseJson = {"isValid":isValid,"isAnyAdmin":isAnyAdmin,"isRepositoryAdmin":isRepositoryAdmin,"isLibraryAdmin":isLibraryAdmin,"libraryUserDetails":userLibraryPermissionJsonArray,"userEmail":""};
        }
        messages:removeHeader(request,"Origin");
        messages:removeHeader(request,"Referer");
        messages:removeHeader(request,"Host");
    } catch(errors:Error err) {
        isValid = false;
        responseJson = {"isValid":isValid,"isAnyAdmin":isAnyAdmin,"isRepositoryAdmin":isRepositoryAdmin,"isLibraryAdmin":isLibraryAdmin,"libraryUserDetails":userLibraryPermissionJsonArray,"userEmail":""};
        logger:error("Authenticate functions : validate user " + err.msg);
    }
    finalSession = "BSESSIONID=" + sessionId;
    messages:setHeader(response,"Set-Cookie",finalSession);
    messages:setJsonPayload(response,responseJson);

    return response,sessionId;
}

@doc:Description {value:"Return this current user is valid user or not"}
@doc:Param {value:"returnIsValid: Valid/Not"}
function getIsValidUser (message request)(boolean returnIsValid)  {

    http:Session userSession = null;
    boolean isValidUser;
    time:Time currentTime = time:currentTime();
    int currentTimeInt;
    int epocTimeInt;
    currentTimeInt = currentTime.time / 1000;

    try {
        request = getRealRequest(request);
        userSession = http:getSession(request);
        if (userSession != null){
            isValidUser,_ = (boolean )http:getAttribute(userSession,"isValid");
            epocTimeInt,_ = (int)http:getAttribute(userSession,"loginTime");
            if ((isValidUser == true) && (currentTimeInt < (epocTimeInt + 86400))) {
                returnIsValid = true;
                return;
            } else {

                returnIsValid = false;

            }
        } else {
            returnIsValid = false;
        }
    } catch (errors:Error err) {
        logger:error("Authenticate functions : getIsValidUser " + err.msg);
        returnIsValid = false;
    }
    return;
}

@doc:Description {value:"Return this current user is repository admin or not"}
@doc:Param {value:"returnIsAdmin: Admin/Not"}
function getIsRepositoryAdminUser ()(boolean returnIsAdmin)  {

    http:Session userSession = null;
    string sessionId;
    boolean isValidUser;
    boolean isRepositoryAdminUser;
    time:Time currentTime = time:currentTime();
    int currentTimeInt;
    int epocTimeInt;
    currentTimeInt = currentTime.time / 1000;

    try {

        if (userSession != null) {
            isRepositoryAdminUser, _ = (boolean )http:getAttribute(userSession,"isRepositoryAdmin");
            isValidUser,_ = (boolean )http:getAttribute(userSession,"isValid");
            epocTimeInt,_ = (int)http:getAttribute(userSession,"loginTime");
            if ((isRepositoryAdminUser == true) && (isValidUser == true) && (currentTimeInt < (epocTimeInt + 86400))) {
                returnIsAdmin = true;
                return;
            } else {
                returnIsAdmin = false;
            }
        } else {
            returnIsAdmin = false;
        }
    } catch(errors:Error err) {
        logger:error("Authenticate functions : getIsRepositoryAdminUser " + err.msg);
        returnIsAdmin = false;
    }
    return;
}

@doc:Description {value:"Return Session details of current user"}
@doc:Param {value:"sessionDetails: JSON object which contain all session details"}
function getSessionDetails(message request)(json sessionDetails) {

    http:Session userSession = null;
    json userLibraryPermissionJsonArray = [];
    string email;
    boolean isValid = false;
    boolean isRepositoryAdmin = false;
    boolean isLibraryAdmin = false;
    boolean isAnyAdmin = false;

    try{
        request = getRealRequest(request);
        userSession = http:getSession(request);

        if (userSession != null) {
            email,_ = (string) http:getAttribute(userSession,"userEmail");
            isValid,_ = (boolean)http:getAttribute(userSession,"isValid");
            isRepositoryAdmin,_ = (boolean)http:getAttribute(userSession,"isRepositoryAdmin");
            isLibraryAdmin,_ = (boolean)http:getAttribute(userSession,"isLibraryAdmin");
            isAnyAdmin,_ = (boolean)http:getAttribute(userSession,"isAnyAdmin");
            userLibraryPermissionJsonArray,_ = (json)http:getAttribute(userSession,"libraryUserDetails");
            sessionDetails = {"isValid":isValid,"isAnyAdmin":isAnyAdmin,"isRepositoryAdmin":isRepositoryAdmin,"isLibraryAdmin":isLibraryAdmin,"libraryUserDetails":userLibraryPermissionJsonArray,"userEmail":email};
        } else {
            sessionDetails = {"isValid":false,"isAnyAdmin":false,"isRepositoryAdmin":false,"isLibraryAdmin":false,"libraryUserDetails":[],"userEmail":null};
        }
    } catch(errors:Error err) {
        logger:error("Authenticate functions : getSessionDetails " + err.msg);
        sessionDetails = {"isValid":false,"isAnyAdmin":false,"isRepositoryAdmin":false,"isLibraryAdmin":false,"libraryUserDetails":[],"userEmail":null};
    }
    return;
}

@doc:Description {value:"Verify request send from valid user or nor"}
@doc:Param {value:"request: request"}
@doc:Param {value:"returnIsValid: true or false"}
function validateUserToken(message request)(boolean returnIsValid){

    string email = "";
    string epocTimeString = "";
    string[] webTokenArray;
    string decodedString;
    string decodedStringHeader;
    string webToken;
    string publicKey;
    int epocTime;
    int currentTimeInt;
    json decodedJson;
    json decodedJsonHeader;
    boolean verified = false;

    try{
        returnIsValid = false;
        webToken = messages:getHeader(request,"ClientAuth");
        webTokenArray= strings:split(webToken,"\\.");
        decodedStringHeader = utils:base64decode(webTokenArray[0]);
        decodedJsonHeader = jsons:parse(decodedStringHeader);
        decodedString = utils:base64decode(webTokenArray[1]);
        decodedJson = jsons:parse(decodedString);
        publicKey = conf:getConfigData("PUBLIC_KEY");
        verified = utils:getShaWithRsa(webToken,publicKey);
        email = jsons:toString(decodedJson["http://wso2.org/claims/emailaddress"]);
        epocTimeString = jsons:toString(decodedJson["exp"]);
        epocTime,_ = <int>epocTimeString;
        time:Time currentTime = time:currentTime();
        currentTimeInt = currentTime.time / 1000;
        if((strings:hasSuffix(email,"@wso2.com")) && (currentTimeInt < (epocTime + 86400)) && verified ){
            returnIsValid = true;
        } else {
            returnIsValid = false;
        }

    } catch(errors:Error err) {
        returnIsValid = false;
        logger:error("Authenticate functions : validate user " + err.msg);
    }
    return;
}

@doc:Description {value:"Verify request send from library admin or not"}
@doc:Param {value:"request: request"}
@doc:Param {value:"returnIsAdmin: true or false"}
function validateUserTokenLibrary(message request ,int libraryCategotyId)(boolean returnIsAdmin){

    string email = "";
    string epocTimeString = "";
    string[] webTokenArray;
    string decodedString;
    string decodedStringHeader;
    string webToken;
    string publicKey;
    string rolePermission;
    int epocTime;
    int currentTimeInt;
    int returnJsonLength;
    int roleLibCategoryId;
    int i = 0;
    json decodedJson;
    json decodedJsonHeader;
    json returnJson;
    boolean verified = false;

    try{
        returnIsAdmin = false;
        webToken = messages:getHeader(request,"ClientAuth");
        webTokenArray= strings:split(webToken,"\\.");
        decodedStringHeader = utils:base64decode(webTokenArray[0]);
        decodedJsonHeader = jsons:parse(decodedStringHeader);
        decodedString = utils:base64decode(webTokenArray[1]);
        decodedJson = jsons:parse(decodedString);
        publicKey = conf:getConfigData("PUBLIC_KEY");
        verified = utils:getShaWithRsa(webToken,publicKey);
        email = jsons:toString(decodedJson["http://wso2.org/claims/emailaddress"]);
        epocTimeString = jsons:toString(decodedJson["exp"]);
        epocTime,_ = <int>epocTimeString;
        time:Time currentTime = time:currentTime();
        currentTimeInt = currentTime.time / 1000;
        if((strings:hasSuffix(email,"@wso2.com")) && (currentTimeInt < (epocTime + 86400)) && verified ){

            returnJson = database:roleGetUserDetails(email);
            returnJsonLength = lengthof returnJson;
            while(i < returnJsonLength){
                if(jsons:toString(returnJson[i].ROLE_TYPE) == "LIBRARY"){
                    roleLibCategoryId,_ = <int>jsons:toString(returnJson[i].ROLE_LIB_CATEGORY_ID);
                    rolePermission = jsons:toString(returnJson[i].ROLE_PERMISSION);
                    if(roleLibCategoryId == libraryCategotyId  && rolePermission == "ADMIN"){
                        returnIsAdmin = true;
                        return;
                    }

                }
                i = i + 1;
            }
        } else {
            returnIsAdmin = false;
        }

    } catch(errors:Error err) {
        returnIsAdmin = false;
        logger:error("Authenticate functions : validate user " + err.msg);
    }
    return;
}

@doc:Description {value:"Verify request send from repository admin or not"}
@doc:Param {value:"request: request"}
@doc:Param {value:"returnIsAdmin: true or false"}
function validateUserTokenRepository(message request)(boolean returnIsAdmin){

    string email = "";
    string epocTimeString = "";
    string[] webTokenArray;
    string decodedString;
    string decodedStringHeader;
    string webToken;
    string publicKey;
    int epocTime;
    int currentTimeInt;
    int returnJsonLength;
    int i = 0;
    json decodedJson;
    json decodedJsonHeader;
    json returnJson;
    boolean verified = false;

    try{
        returnIsAdmin = false;
        webToken = messages:getHeader(request,"ClientAuth");
        webTokenArray= strings:split(webToken,"\\.");
        decodedStringHeader = utils:base64decode(webTokenArray[0]);
        decodedJsonHeader = jsons:parse(decodedStringHeader);
        decodedString = utils:base64decode(webTokenArray[1]);
        decodedJson = jsons:parse(decodedString);
        publicKey = conf:getConfigData("PUBLIC_KEY");
        verified = utils:getShaWithRsa(webToken,publicKey);
        email = jsons:toString(decodedJson["http://wso2.org/claims/emailaddress"]);
        epocTimeString = jsons:toString(decodedJson["exp"]);
        epocTime,_ = <int>epocTimeString;
        time:Time currentTime = time:currentTime();
        currentTimeInt = currentTime.time / 1000;
        if((strings:hasSuffix(email,"@wso2.com")) && (currentTimeInt < (epocTime + 86400)) && verified ){

            returnJson = database:roleGetUserDetails(email);
            returnJsonLength = lengthof returnJson;
            while(i < returnJsonLength){
                if(jsons:toString(returnJson[i].ROLE_TYPE) == "REPOSITORY" && jsons:toString(returnJson[i].ROLE_PERMISSION) == "ADMIN"){
                    returnIsAdmin = true;
                    return;
                }
                i = i + 1;
            }
        } else {
            returnIsAdmin = false;
        }

    } catch(errors:Error err) {
        returnIsAdmin = false;
        logger:error("Authenticate functions : validate user " + err.msg);
    }
    return;
}

@doc:Description {value:"Set BSESSIONID"}
@doc:Param {value:"request: request"}
@doc:Param {value:"response: message"}
function getRealRequest(message request)(message){
    string cookie;
    string realCookie;
    string[] cookieHeaderParts;
    string[] cookieArray;
    int cookieArrayLength;
    int i = 0;
    try{
        cookie = messages:getHeader(request,"Cookie");
        cookieArray = strings:split(cookie,";");
        cookieArrayLength = lengthof cookieArray;
        while(i < cookieArrayLength){
            if(strings:contains(cookieArray[i],"BSESSIONID")){
                cookieHeaderParts = strings:split(cookieArray[i],"=");
                if(lengthof cookieHeaderParts == 2){
                    realCookie = strings:trim(cookieArray[i]);
                }


            }
            i = i + 1;
        }
        messages:removeHeader(request,"Cookie");
        messages:setHeader(request,"Cookie",realCookie);
    }catch(errors:Error err) {
        logger:error("Authenticate functions : getRealRequest " + err.msg);
    }
    return request;
}

