package org.wso2.internalapps.dependencymanager;

import ballerina.net.http;
import ballerina.data.sql;
import org.wso2.internalapps.dependencymanager.Database;
import org.wso2.internalapps.dependencymanager.ConfigFiles;

@http:configuration {
    basePath:"/LMDependencyManager",
    httpsPort:9099,
    keyStoreFile:"${ballerina.home}/bre/security/wso2carbon.jks",
    keyStorePassword:"wso2carbon",
    certPassword:"wso2carbon",
    trustStoreFile:"${ballerina.home}/bre/security/client-truststore.jks",
    trustStorePassword:"wso2carbon",
    allowCredentials : false,
    allowOrigins:["*"]
}


service<http> DatabaseLM {

    json configs = ConfigFiles:getConfigData("config/configDependencyMgr.json");
    sql:ClientConnector sqlConnection = Database:getDatabaseConfiguration(configs);


    @http:resourceConfig {
        methods:["GET"],
        path:"/library/names"
    }
    resource libraryNamesResource (http:Request request, http:Response response) {
        if(sqlConnection == null){
            sqlConnection = Database:getDatabaseConfiguration(configs);
        }
        if(sqlConnection != null){
            json jsonresponse = Database:selectLibraryDropDown(sqlConnection);
            response.setJsonPayload(jsonresponse);
        }else{
            json errorMessage = {"responseType":"Error","responseMessage":"Connection Error"};
            response.setJsonPayload(errorMessage);
        }
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/library/versions/{libraryName}"
    }
    resource fillProductVersionDropDownResource (http:Request request, http:Response response,string libraryName) {
        if(sqlConnection == null){
            sqlConnection = Database:getDatabaseConfiguration(configs);
        }

        if(sqlConnection != null){
            json jsonresponse = Database:selectLibraryVersionDropDown(sqlConnection, libraryName);
            response.setJsonPayload(jsonresponse);
        }else{
            json errorMessage = {"responseType":"Error","responseMessage":"Connection Error"};
            response.setJsonPayload(errorMessage);
        }
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/component/versions/{componentName}"
    }
    resource fillComponentVersionDropDownResource (http:Request request, http:Response response,string componentName) {
        if(sqlConnection == null){
            sqlConnection = Database:getDatabaseConfiguration(configs);
        }

        if(sqlConnection != null){
            json jsonresponse = Database:selectComponentVersionDropDown(sqlConnection, componentName);
            response.setJsonPayload(jsonresponse);
        }else{
            json errorMessage = {"responseType":"Error","responseMessage":"Connection Error"};
            response.setJsonPayload(errorMessage);
        }
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/product/versions/{productName}"
    }
    resource fillLibraryVersionDropDownResource (http:Request request, http:Response response,string productName) {
        if(sqlConnection == null){
            sqlConnection = Database:getDatabaseConfiguration(configs);
        }

        if(sqlConnection != null){
            json jsonresponse = Database:selectProductVersionDropDown(sqlConnection, productName);
            response.setJsonPayload(jsonresponse);
        }else{
            json errorMessage = {"responseType":"Error","responseMessage":"Connection Error"};
            response.setJsonPayload(errorMessage);
        }
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/library/artifactIDgroupID/{Name}"
    }
    resource getArtifactGroupID (http:Request request, http:Response response,string Name) {
        if(sqlConnection == null){
            sqlConnection = Database:getDatabaseConfiguration(configs);
        }

        if(sqlConnection != null){
            map params = request.getQueryParams();
            var givenVersion, _ = (string)params.reqVersion;
            json jsonresponse = Database:selectArtifactGroupIDsLibrary(sqlConnection, Name, givenVersion);
            response.setJsonPayload(jsonresponse);
        }else{
            json errorMessage = {"responseType":"Error","responseMessage":"Connection Error"};
            response.setJsonPayload(errorMessage);
        }
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/library/productsandcomponents/{libraryName}"
    }
    resource getViewByLibrary (http:Request request, http:Response response,string libraryName) {
        if(sqlConnection == null){
            sqlConnection = Database:getDatabaseConfiguration(configs);
        }

        if(sqlConnection != null){
            map params = request.getQueryParams();
            var libVersion, _ = (string)params.libraryVersion;
            json jsonresponse = Database:viewByLibrary(sqlConnection, libraryName, libVersion);
            response.setJsonPayload(jsonresponse);
        }else{
            json errorMessage = {"responseType":"Error","responseMessage":"Connection Error"};
            response.setJsonPayload(errorMessage);
        }
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/productsandcomponents/names"
    }
    resource getProductAndComponentList (http:Request request, http:Response response) {
        if(sqlConnection == null){
            sqlConnection = Database:getDatabaseConfiguration(configs);
        }
        if(sqlConnection != null){
            json jsonresponse = Database:selectProductsAndComponents(sqlConnection);
            response.setJsonPayload(jsonresponse);
        }else{
            json errorMessage = {"responseType":"Error","responseMessage":"Connection Error"};
            response.setJsonPayload(errorMessage);
        }
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/component/libraries/{componentName}"
    }
    resource getLibrariesForSelectedComponent (http:Request request, http:Response response, string componentName) {
        if(sqlConnection == null){
            sqlConnection = Database:getDatabaseConfiguration(configs);
        }
        if(sqlConnection != null){
            map params = request.getQueryParams();
            var componentVersion, _ = (string)params.compVersion;

            var routerURL, _ = (string)configs.LM_ROUTER_SERVICE.URL;
            http:HttpClient httpClient=create http:HttpClient (routerURL, {});

            json jsonresponse = Database:selectLibrariesForSelectedComp(sqlConnection, componentName, componentVersion, httpClient);

            response.setJsonPayload(jsonresponse);
        }else{
            json errorMessage = {"responseType":"Error","responseMessage":"Connection Error"};
            response.setJsonPayload(errorMessage);
        }
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/product/libraries/{productName}"
    }
    resource getLibrariesForSelectedProduct (http:Request request, http:Response response, string productName) {
        if(sqlConnection == null){
            sqlConnection = Database:getDatabaseConfiguration(configs);
        }
        if(sqlConnection != null){
            map params = request.getQueryParams();
            var productVersion, _ = (string)params.prodVersion;

            var routerURL, _ = (string)configs.LM_ROUTER_SERVICE.URL;
            http:HttpClient httpClient=create http:HttpClient (routerURL, {});

            json jsonresponse = Database:selectLibrariesForSelectedProd(sqlConnection, productName, productVersion, httpClient);
            response.setJsonPayload(jsonresponse);
        }else{
            json errorMessage = {"responseType":"Error","responseMessage":"Connection Error"};
            response.setJsonPayload(errorMessage);
        }
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/component/details/{componentName}"
    }
    resource getComponentDetails (http:Request request, http:Response response, string componentName) {
        if(sqlConnection == null){
            sqlConnection = Database:getDatabaseConfiguration(configs);
        }
        if(sqlConnection != null){
            map params = request.getQueryParams();
            var componentVersion, _ = (string)params.compVersion;

            json jsonresponse = Database:selectComponentDetails(sqlConnection, componentName, componentVersion);

            response.setJsonPayload(jsonresponse);
        }else{
            json errorMessage = {"responseType":"Error","responseMessage":"Connection Error"};
            response.setJsonPayload(errorMessage);
        }
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/productAreas/names"
    }
    resource productAreas (http:Request request, http:Response response) {
        if(sqlConnection == null){
            sqlConnection = Database:getDatabaseConfiguration(configs);
        }
        if(sqlConnection != null){
            json jsonresponse = Database:selectProductAreas(sqlConnection);
            response.setJsonPayload(jsonresponse);
        }else{
            json errorMessage = {"responseType":"Error","responseMessage":"Connection Error"};
            response.setJsonPayload(errorMessage);
        }
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.send();
    }

    @http:resourceConfig {
        methods:["GET"],
        path:"/products/names/{productAreaName}"
    }
    resource products (http:Request request, http:Response response, string productAreaName) {
        if(sqlConnection == null){
            sqlConnection = Database:getDatabaseConfiguration(configs);
        }
        if(sqlConnection != null){
            json jsonresponse = Database:selectProducts(sqlConnection, productAreaName);
            response.setJsonPayload(jsonresponse);
        }else{
            json errorMessage = {"responseType":"Error","responseMessage":"Connection Error"};
            response.setJsonPayload(errorMessage);
        }
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.send();
    }
}
