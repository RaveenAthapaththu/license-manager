package org.wso2.internalapps.licensemanager.services;


import ballerina.lang.messages;
import ballerina.net.http;
import ballerina.lang.errors;
import ballerina.lang.files;
import ballerina.lang.blobs;
import ballerina.lang.xmls;
import org.wso2.internalapps.licensemanager.database;
import ballerina.lang.jsons;
import org.wso2.internalapps.licensemanager.conf;


string jenkinsApiUrl = conf:getConfigData("jenkinsApiUrl");

function createJenkinsJob(string jenkinsJobName)(json ){
    message responseJenkins = {};
    message requestJenkinsMessage = {};
    json response;
    json responseDb;
    xml jenkinsRequestXml;
    int createJobStatusCode;

    string view;
    string folder;
    string confFile;
    string requestJenkinsUrl;
    string fileName;
    string s;
    string authenticateToken;
    http:ClientConnector jenkinsClientConnector = create http:ClientConnector(jenkinsApiUrl);

    try{
        responseDb = database:jenkinsFolderMatchRegex(jenkinsJobName);
        view = jsons:toString(responseDb[0].JF_VIEW);
        folder = jsons:toString(responseDb[0].JF_FOLDER);
        confFile = jsons:toString(responseDb[0].JF_CONF);

        fileName = "./org/wso2/internalapps/licensemanager/conf/" + confFile;
        files:File issueFile = {path:fileName};
        files:open(issueFile,"r");
        var content, _ = files:read(issueFile, 100000);
        s = blobs:toString(content, "utf-8");
        authenticateToken = "Basic " + conf:getConfigData("jenkinsToken");
        jenkinsRequestXml = xmls:parse(s);


        messages:setXmlPayload(requestJenkinsMessage,jenkinsRequestXml);
        messages:setHeader(requestJenkinsMessage,"Content-Type","application/xml");
        messages:setHeader(requestJenkinsMessage,"Authorization",authenticateToken);
        requestJenkinsUrl = "view/"+view+"/job/"+folder+"/createItem?name=" +jenkinsJobName;

        responseJenkins = jenkinsClientConnector.post(requestJenkinsUrl,requestJenkinsMessage);
        createJobStatusCode = http:getStatusCode(responseJenkins);


        if(createJobStatusCode == 200){

            response = {"responseType":"Done","responseMessage":"done"};
        }else{

            response = {"responseType":"Error","responseMessage":"Jenkins Error"};
        }

        return response;
    }catch(errors:Error err){
        response = {"responseType":"Error","responseMessage":err.msg};
        return response;
    }

    return response;




}

