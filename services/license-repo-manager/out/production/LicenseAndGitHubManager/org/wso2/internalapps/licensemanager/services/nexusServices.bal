package org.wso2.internalapps.licensemanager.services;

import ballerina.lang.messages;
import ballerina.net.http;
import ballerina.lang.errors;
import ballerina.lang.system;
import ballerina.lang.xmls;
import ballerina.lang.files;
import ballerina.lang.blobs;
import ballerina.lang.jsons;
import org.wso2.internalapps.licensemanager.conf;



string nexusApiUrl = conf:getConfigData("nexusApiUrl");

function createNexus(string nexusRepositoryName,string nexusRepositoryId)(json ){

    json returnJson;
    try{

        string requestNexusUrl =  "nexus/service/local/repositories";
        message requestNexusMessage = {};
        json requestJsonPayload = {
                                      "data": {
                                                  "repoType": "proxy",
                                                  "id": nexusRepositoryId,
                                                  "name": nexusRepositoryName,
                                                  "browseable": true,
                                                  "indexable": true,
                                                  "notFoundCacheTTL": 1440,
                                                  "artifactMaxAge": -1,
                                                  "metadataMaxAge": 1440,
                                                  "itemMaxAge": 1440,
                                                  "repoPolicy": "RELEASE",
                                                  "provider": "maven2",
                                                  "providerRole": "org.sonatype.nexus.proxy.repository.Repository",
                                                  "downloadRemoteIndexes": true,
                                                  "autoBlockActive": true,
                                                  "fileTypeValidation": true,
                                                  "exposed": true,
                                                  "checksumPolicy": "WARN",
                                                  "remoteStorage": {
                                                                       "remoteStorageUrl": "http://someplace.com/repo",
                                                                       "authentication": null,
                                                                       "connectionSettings": null
                                                                   }
                                              }
                                  };
        messages:setJsonPayload(requestNexusMessage,requestJsonPayload);
        messages:setHeader(requestNexusMessage,"Content-Type","application/json");
        string authenticateToken = "Basic " + conf:getConfigData("nexusToken");
        messages:setHeader(requestNexusMessage,"Authorization",authenticateToken);
        http:ClientConnector nexusClientConnector = create http:ClientConnector(nexusApiUrl);
        message responseNexus = nexusClientConnector.post(requestNexusUrl,requestNexusMessage);

        returnJson = {"responseType":"Done","responseMessage":""};

    }catch(errors:Error err){
        returnJson = {"responseType":"Error","responseMessage":err.msg};
        system:println(err);


    }
    return returnJson;




}

function createNexusRepositoryTarget(string id,string name,string contentClass,string pattern)(json){
    int statusCode;
    string idString = "<id>" + id + "</id>";
    string contentClassString = "<contentClass>"+ contentClass + "</contentClass>";
    string nameString = "<name>" + name + "</name>";
    string patternString = "<patterns><pattern>" + pattern + "</pattern></patterns>";
    string xmlString = "<repo-target><data>" + idString + contentClassString + nameString + patternString + "</data></repo-target>";
    string requestNexusUrl = "nexus/service/local/repo_targets";
    string authenticateToken = "Basic " + conf:getConfigData("nexusToken");
    json returnJson;
    try{
        xml requestXml = xmls:parse(xmlString);
        message requestNexusMessage = {};
        messages:setXmlPayload(requestNexusMessage,requestXml);
        messages:setHeader(requestNexusMessage,"Content-Type","application/xml");
        messages:setHeader(requestNexusMessage,"Authorization",authenticateToken);
        http:ClientConnector nexusClientConnector = create http:ClientConnector(nexusApiUrl);
        message responseNexus = nexusClientConnector.post(requestNexusUrl,requestNexusMessage);
        statusCode = http:getStatusCode(responseNexus);
        if(statusCode >=200 && statusCode < 300){

            returnJson = {"responseType":"Done","responseMessage":""};
        }else{

            returnJson = {"responseType":"Error","responseMessage":""};
        }
    }catch(errors:Error err){
        returnJson = {"responseType":"Error","responseMessage":err.msg};
        system:println(err);


    }

    return returnJson;

}

function createNexusStagingProfile(string groupId)(json){
    string fileName;
    string xmlString;
    string requestNexusUrl;
    string authenticateToken;
    files:File issueFile;
    xmls:Options xmlOption = {};
    jsons:Options jsonOptions = {};
    xml readXml;
    json readJson;
    json returnJson;
    message requestNexusMessage = {};
    int statusCode;

    try{

        fileName = "./org/wso2/internalapps/licensemanager/conf/nexusStagingProfileConf.xml";
        issueFile = {path:fileName};
        files:open(issueFile,"r");
        var content, _ = files:read(issueFile, 100000);

        xmlString = blobs:toString(content, "utf-8");
        readXml = xmls:parse(xmlString);
        readJson = xmls:toJSON(readXml,xmlOption);
        readJson.profileRequest.data.name = groupId;
        readJson.profileRequest.data.repositoryTargetId = groupId;
        readXml = jsons:toXML(readJson,jsonOptions);
        authenticateToken = "Basic " + conf:getConfigData("nexusToken");

        messages:setXmlPayload(requestNexusMessage,readXml);
        messages:setHeader(requestNexusMessage,"Content-Type","application/xml");
        messages:setHeader(requestNexusMessage,"Authorization",authenticateToken);
        http:ClientConnector nexusClientConnector = create http:ClientConnector(nexusApiUrl);
        requestNexusUrl = "nexus/service/local/staging/profiles";
        message responseNexus = nexusClientConnector.post(requestNexusUrl,requestNexusMessage);
        statusCode = http:getStatusCode(responseNexus);
        if(statusCode >=200 && statusCode < 300){

            returnJson = {"responseType":"Done","responseMessage":""};
        }else{

            returnJson = {"responseType":"Error","responseMessage":""};
        }


    }catch(errors:Error err){
        returnJson = {"responseType":"Error","responseMessage":err.msg};
        system:println(err);


    }

    return returnJson;
}