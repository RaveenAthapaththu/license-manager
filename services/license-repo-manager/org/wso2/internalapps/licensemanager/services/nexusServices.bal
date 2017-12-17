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
import ballerina.doc;
import ballerina.utils.logger;

string nexusApiUrl = conf:getConfigData("NEXUS_URL");

@doc:Description {value:"Create Nexus repository target"}
@doc:Param {value:"id: Nexus repository target ID"}
@doc:Param {value:"name: Nexus repository target name"}
@doc:Param {value:"contentClass: Nexus repository content class"}
@doc:Param {value:"pattern: Nexus repository target pattern : extend from group ID"}
@doc:Param {value:"returnJson: Return JSON from Nexus REST API"}
function createNexusRepositoryTarget(string id,string name,string contentClass,string pattern)(json returnJson){

    int statusCode;
    string idString = "<id>" + id + "</id>";
    string contentClassString = "<contentClass>"+ contentClass + "</contentClass>";
    string nameString = "<name>" + name + "</name>";
    string patternString = "<patterns><pattern>" + pattern + "</pattern></patterns>";
    string xmlString = "<repo-target><data>" + idString + contentClassString + nameString + patternString + "</data></repo-target>";
    string requestNexusUrl = "nexus/service/local/repo_targets";
    string authenticateToken = "Basic " + conf:getConfigData("NEXUS_TOKEN");

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
            logger:info("Nexus repository target created successfully");
        }else{
            logger:error("Error encountered when creating Nexus repository target");
            returnJson = {"responseType":"Error","responseMessage":""};
        }
    }catch(errors:Error err){
        returnJson = {"responseType":"Error","responseMessage":err.msg};
        logger:error("Error encountered when creation Nexus repository target");
        logger:error(err.msg);
    }

    return;
}

@doc:Description {value:"Create Nexus staging profile"}
@doc:Param {value:"groupId: Group ID"}
@doc:Param {value:"returnJson: Return JSON from Nexus REST API"}
function createNexusStagingProfile(string groupId)(json returnJson){

    string fileName;
    string xmlString;
    string requestNexusUrl;
    string authenticateToken;
    files:File issueFile;
    xmls:Options xmlOption = {};
    jsons:Options jsonOptions = {};
    xml readXml;
    json readJson;
    message requestNexusMessage = {};
    int statusCode;

    try{

        fileName = "conf/nexusStagingProfileConf.xml";
        issueFile = {path:fileName};
        files:open(issueFile,"r");
        var content, _ = files:read(issueFile, 100000);

        xmlString = blobs:toString(content, "utf-8");
        readXml = xmls:parse(xmlString);
        readJson = xmls:toJSON(readXml,xmlOption);
        readJson.profileRequest.data.name = groupId;
        readJson.profileRequest.data.repositoryTargetId = groupId;
        readXml = jsons:toXML(readJson,jsonOptions);
        authenticateToken = "Basic " + conf:getConfigData("NEXUS_TOKEN");

        messages:setXmlPayload(requestNexusMessage,readXml);
        messages:setHeader(requestNexusMessage,"Content-Type","application/xml");
        messages:setHeader(requestNexusMessage,"Authorization",authenticateToken);
        http:ClientConnector nexusClientConnector = create http:ClientConnector(nexusApiUrl);
        requestNexusUrl = "nexus/service/local/staging/profiles";
        message responseNexus = nexusClientConnector.post(requestNexusUrl,requestNexusMessage);
        statusCode = http:getStatusCode(responseNexus);
        if(statusCode >=200 && statusCode < 300){
            returnJson = {"responseType":"Done","responseMessage":""};
            logger:info("Nexus staging profile created successfully");
        }else{
            logger:error("Error encountered when creating Nexus staging profile");
            logger:error(responseNexus);
            returnJson = {"responseType":"Error","responseMessage":"Error encountered when creating Nexus staging profile"};
        }


    }catch(errors:Error err){
        returnJson = {"responseType":"Error","responseMessage":err.msg};
        logger:error("Error encountered when creating Nexus staging profile");
        logger:error(err.msg);
    }

    return;
}

@doc:Description {value:"Create Nexus repository"}
@doc:Param {value:"nexusRepositoryName: Nexus repository name"}
@doc:Param {value:"nexusRepositoryId: Group ID"}
@doc:Param {value:"returnJson: Return JSON from Nexus REST API"}
function createNexus(string nexusRepositoryName,string nexusRepositoryId)(json returnJson){

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
        string authenticateToken = "Basic " + conf:getConfigData("NEXUS_TOKEN");
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