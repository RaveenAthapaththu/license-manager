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
import ballerina.utils.logger;
import ballerina.doc;


string jenkinsApiUrl = conf:getConfigData("JENKINS_URL");

@doc:Description {value:"Create Jenkins job"}
@doc:Param {value:"jenkinsJobName: Jenkins job name"}
@doc:Param {value:"responseJson: Response from Jenkins REST API"}
function createJenkinsJob(string jenkinsJobName)(json responseJson){

    message responseJenkins = {};
    message requestJenkinsMessage = {};
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

        fileName = "conf/" + confFile;
        files:File issueFile = {path:fileName};
        files:open(issueFile,"r");
        var content, _ = files:read(issueFile, 100000);
        s = blobs:toString(content, "utf-8");
        authenticateToken = "Basic " + conf:getConfigData("JENKINS_TOKEN");
        jenkinsRequestXml = xmls:parse(s);


        messages:setXmlPayload(requestJenkinsMessage,jenkinsRequestXml);
        messages:setHeader(requestJenkinsMessage,"Content-Type","application/xml");
        messages:setHeader(requestJenkinsMessage,"Authorization",authenticateToken);
        requestJenkinsUrl = "/job/"+folder+"/createItem?name=" +jenkinsJobName;

        responseJenkins = jenkinsClientConnector.post(requestJenkinsUrl,requestJenkinsMessage);
        createJobStatusCode = http:getStatusCode(responseJenkins);
        logger:info(createJobStatusCode);

        if(createJobStatusCode == 200){
            responseJson = {"responseType":"Done","responseMessage":"Jenkins job successfully created"};
            logger:info("Jenkins job successfully created");
        }else{
            responseJson = {"responseType":"Error","responseMessage":"Unknown Error"};
            logger:info("Jenkins job creation fail : Unknown Error");
        }

    }catch(errors:Error err){
        responseJson = {"responseType":"Error","responseMessage":err.msg};
    }

    return;
}

