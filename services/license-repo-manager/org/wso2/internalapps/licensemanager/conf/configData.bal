package org.wso2.internalapps.licensemanager.conf;

import ballerina.lang.files;
import ballerina.lang.blobs;
import ballerina.lang.jsons;
import ballerina.doc;

@doc:Description {value:" Gives configuration data according to given key"}
@doc:Param {value:"key : key for config JSON"}
@doc:Param {value:"value : return congiguration value"}
function getConfigData(string key)(string value) {
    string fileName;
    string s;
    json configJson;

    fileName = "conf/configData.json";
    files:File issueFile = {path:fileName};
    files:open(issueFile,"r");
    var content, _ = files:read(issueFile, 100000);
    s = blobs:toString(content, "utf-8");
    configJson = jsons:parse(s);
    value = jsons:toString(configJson[key]);
    return;
}
