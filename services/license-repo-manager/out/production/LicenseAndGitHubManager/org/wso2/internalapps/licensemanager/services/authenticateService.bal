package org.wso2.internalapps.licensemanager.services;

import ballerina.net.http;
import ballerina.lang.strings;
import ballerina.utils;
import ballerina.lang.jsons;
import ballerina.lang.time;
import org.wso2.internalapps.licensemanager.database;
import ballerina.lang.errors;

http:Session userSession;

function validateUser(string webToken)(json responseJson){

    string email = "";
    string epocTimeString = "";
    string[] webTokenArray;
    string dencodedString;
    int epocTime;
    int currentTimeInt;
    json decodedJson;
    message sessionMessage = {};
    boolean isValid = false;

    try{
        responseJson = {"isValid":false,"userEmail":""};
        webTokenArray= strings:split(webToken,"\\.");
        dencodedString = utils:base64decode(webTokenArray[1]);
        decodedJson = jsons:parse(dencodedString);
        email = jsons:toString(decodedJson["http://wso2.org/claims/emailaddress"]);
        epocTimeString = jsons:toString(decodedJson["exp"]);
        epocTime,_ = <int>epocTimeString;
        time:Time currentTime = time:currentTime();
        currentTimeInt = currentTime.time / 1000;

        if((strings:hasSuffix(email,"@wso2.com")) && (currentTimeInt < (epocTime + 86400))){

            userSession = http:createSessionIfAbsent(sessionMessage);
            isValid = true;
            http:setAttribute(userSession,"isValid",isValid);
            http:setAttribute(userSession,"userEmail",email);
            http:setAttribute(userSession,"loginTime",epocTime);
            responseJson = {"isValid":isValid,"userEmail":email};

        }else{
            userSession = null;
            isValid = false;
            responseJson = {"isValid":isValid,"userEmail":""};

        }
    }catch(errors:Error err){
        isValid = false;
        responseJson = {"isValid":isValid,"userEmail":""};
    }


    return;

}


function isAdminUser(string webToken)(json responseJson){

    string email = "";
    string[] webTokenArray;
    string dencodedString;
    json decodedJson;
    json returnJson;
    json isValidJson;
    boolean isAdminFromDb = false;
    boolean isValid = false;
    try{
        isValidJson = validateUser(webToken);
        isValid,_ = <boolean>jsons:toString(isValidJson.isValid);
        if(!isValid){
            responseJson = {"isAdmin":false,"userEmail":""};
            return;
        }

        webTokenArray= strings:split(webToken,"\\.");
        dencodedString = utils:base64decode(webTokenArray[1]);
        decodedJson = jsons:parse(dencodedString);
        email = jsons:toString(decodedJson["http://wso2.org/claims/emailaddress"]);

        returnJson = database:userCheckAdminUsers(email);
        isAdminFromDb,_ = <boolean>jsons:toString(returnJson.isAdmin);
        if(isAdminFromDb){
            responseJson = {"isAdmin":true,"userEmail":email};
            http:setAttribute(userSession,"isValid",true);

        }else{
            responseJson = {"isAdmin":false,"userEmail":""};


        }

    }catch(errors:Error err){
        responseJson = {"isAdmin":false,"userEmail":""};


    }

    return;
}

function getIsValidUser ()(boolean returnIsValid)  {

    boolean isValidUser;
    time:Time currentTime = time:currentTime();
    int currentTimeInt;
    int epocTimeInt;
    currentTimeInt = currentTime.time / 1000;

    if(userSession != null){

        isValidUser,_ = (boolean )http:getAttribute(userSession,"isValid");
        epocTimeInt,_ = (int)http:getAttribute(userSession,"loginTime");

        if((isValidUser == true) && (currentTimeInt < (epocTimeInt + 86400))){

            returnIsValid = true;
            return;
        }else {

            returnIsValid = false;
            return;
        }
    }else{

        returnIsValid = false;
        return;
    }

}

function getSessionDetails()(json sessionDetails){
    string email;
    try{
        if(userSession != null){
            email,_ = (string) http:getAttribute(userSession,"userEmail");
            sessionDetails = {"userEmail":email};
        }else{
            sessionDetails = {"userEmail":null};
        }
    }catch(errors:Error err){
        sessionDetails = {"userEmail":null};


    }



    return;
}