package org.wso2.internalapps.licensemanager.conf;

import ballerina.lang.files;
import ballerina.lang.blobs;
import ballerina.lang.jsons;

function getConfigData(string key)(string value){
    string fileName;
    string s;
    json configJson;

    fileName = "./org/wso2/internalapps/licensemanager/conf/configData.json";
    files:File issueFile = {path:fileName};
    files:open(issueFile,"r");
    var content, _ = files:read(issueFile, 100000);
    s = blobs:toString(content, "utf-8");
    configJson = jsons:parse(s);
    value = jsons:toString(configJson[key]);
    return;

}
