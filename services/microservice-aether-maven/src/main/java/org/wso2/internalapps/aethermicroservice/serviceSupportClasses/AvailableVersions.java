package org.wso2.internalapps.aethermicroservice.serviceSupportClasses;

import org.wso2.internalapps.aethermicroservice.configFileReader.ConfigFilePOJO;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.resolution.VersionRangeRequest;
import org.eclipse.aether.resolution.VersionRangeResult;
import org.eclipse.aether.version.Version;
import org.eclipse.util.Booter;

import java.util.List;

public class AvailableVersions {

    //return the available versions of the given Artifact
    public static StringBuilder getAvailableVersions(String groupID, String artifactID, ConfigFilePOJO configurations) {


        RepositorySystem system = Booter.newRepositorySystem();

        DefaultRepositorySystemSession session = Booter.newRepositorySystemSession(system);

        String artifactRef = groupID+":"+artifactID+":[0,)";

        Artifact artifact = new DefaultArtifact(artifactRef);//set the artifact

        VersionRangeRequest rangeRequest = new VersionRangeRequest();
        rangeRequest.setArtifact( artifact );

        rangeRequest.setRepositories(Booter.newRepositories(system, session, configurations));

        StringBuilder jsonString= new StringBuilder("{\"ErrorMsg\":\"NotFound\"}");
        StringBuilder ans =  new StringBuilder();
        try{

            VersionRangeResult rangeResult = system.resolveVersionRange( session, rangeRequest );

            List<Version> versions = rangeResult.getVersions();

            System.out.println( "Available versions " + versions );

            ans.append("{\"GroupID\":\""+groupID+"\",\"ArtifactID\":\""+artifactID+"\",");

            if(versions.size() > 0 ){
                ans.append("\"AvailableVersions\":[");
                int count = 0;
                while(count < versions.size()){
                    if(count <   versions.size()-1){
                        ans.append("\""+versions.get(count).toString()+"\",");
                    }else{
                        ans.append("\""+versions.get(count).toString()+"\"");
                    }
                    count++;
                }
                ans.append("]}");
            }else{
                ans.append("\"ErrorMsg\":\"NotFound\"}");
            }
        }catch (Exception e){
            ans.append("{\"ErrorMsg\":\""+e.getMessage()+"\"}");
            return ans;
        }

        return ans;
    }
}
