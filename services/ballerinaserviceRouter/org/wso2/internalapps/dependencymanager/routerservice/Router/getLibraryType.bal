package org.wso2.internalapps.dependencymanager.routerservice.Router;

import ballerina.log;

public function getReqType(string libType)(string){
    if(libType.contains("jar") || libType.contains("bundle")){
        return "java";
    }else {
        return "javascript";
    }
}