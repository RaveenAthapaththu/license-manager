package org.wso2.internalapps.dependencymanager.routerservice;

import ballerina.net.http;
import ballerina.log;
import org.wso2.internalapps.dependencymanager.routerservice.ConfigFiles;
import org.wso2.internalapps.dependencymanager.routerservice.Router;

@http:configuration {
    basePath:"/dependencyManager",
    httpsPort: 9091,
    keyStoreFile: "${ballerina.home}/bre/security/wso2carbon.jks",
    keyStorePassword:"wso2carbon",
    certPassword:"wso2carbon",
    allowCredentials : false,
    allowOrigins:["*"]
}
service<http> routerService {
    json configs = ConfigFiles:getConfigData("config/configRouterService.json");

    @http:resourceConfig {
        methods:["GET"],
        path:"/test"
    }
    resource test (http:Request request, http:Response response) {
        response.setStringPayload("Hello, This is wso2 llicense manager!");
        response.send();
    }

    @http:resourceConfig {
        methods:["POST"],
        path:"/router/{libType}"
    }
    resource requestResolve (http:Request request, http:Response response,string libType) {
        endpoint<http:HttpClient> navigateToWSO2 {
        }
        endpoint<http:HttpClient> navigateToMaven {
        }
        endpoint<http:HttpClient> navigateToWSO2Releases {
        }
        http:Response clientResponse = {};

        string reqType = Router:getReqType(libType);

        map params = request.getQueryParams();
        var versionReq, _ = (string)params.VersionReq;
        json jsonMsg = request.getJsonPayload();
        log:printDebug(jsonMsg.toString());

        if(reqType == "java"){
            var GroupID, _ = (string)params.GroupID;
            if(versionReq ==  "true"){
                log:printInfo("Finding Latest version...");
                log:printInfo("Directing to Maven repo microservice...");

                var URLMaven, _ = (string)configs.LM_MICROSERVICES.MAVEN_CENTRAL;
                http:HttpClient maven_repo =   create http:HttpClient(URLMaven, {});
                bind maven_repo with navigateToMaven;
                http:Request newRequestMaven = {};
                newRequestMaven.setJsonPayload(jsonMsg);
                clientResponse, _  = navigateToMaven.post("/getLatest",newRequestMaven);

                if(clientResponse.getStatusCode() != 200){
                    log:printInfo("Response Status - Not Found");
                    log:printInfo("Directing to WSO2-Public repo microservice...");

                    var URLWSO2Public, _ = (string)configs.LM_MICROSERVICES.WSO2_PUBLIC;
                    http:HttpClient wso2_public_repo =   create http:HttpClient(URLWSO2Public, {});
                    bind wso2_public_repo with navigateToWSO2;

                    http:Request newRequestWSO2Public = {};
                    newRequestWSO2Public.setJsonPayload(jsonMsg);

                    clientResponse, _ = navigateToWSO2.post("/getLatest",newRequestWSO2Public);

                }
                log:printInfo("Response returning...");
            }else{
                log:printInfo("Finding dependency heirarchy...");
                log:printInfo("Directing to Maven repo microservice...");

                var URLMaven, _ = (string)configs.LM_MICROSERVICES.MAVEN_CENTRAL;
                http:HttpClient maven_repo =   create http:HttpClient(URLMaven, {});
                bind maven_repo with navigateToMaven;
                http:Request newRequestDMaven = {};
                newRequestDMaven.setJsonPayload(jsonMsg);

                clientResponse, _  = navigateToMaven.post("/getDHeirarchy",newRequestDMaven);

                if(clientResponse.getStatusCode() != 200){
                    log:printInfo("Response Status - Not Found");
                    log:printInfo("Directing to WSO2-Public repo microservice...");

                    var URLWSO2Public, _ = (string)configs.LM_MICROSERVICES.WSO2_PUBLIC;
                    http:HttpClient wso2_public_repo =   create http:HttpClient(URLWSO2Public, {});
                    bind wso2_public_repo with navigateToWSO2;

                    http:Request newRequestDWSO2Public = {};
                    newRequestDWSO2Public.setJsonPayload(jsonMsg);

                    clientResponse, _  = navigateToWSO2.post("/getDHeirarchy",newRequestDWSO2Public);

                }
            }
            log:printInfo("Response returning...");
        }else if(reqType == "javascript"){
            //println("javascriptPart");
        }
        response.setHeader("Content-Type","application/json");
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.forward(clientResponse);
    }


}