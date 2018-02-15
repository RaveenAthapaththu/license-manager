package org.wso2.internalapps.aethermicroservice.serviceSupportClasses;

import org.wso2.internalapps.aethermicroservice.configFileReader.ConfigFilePOJO;
import org.wso2.internalapps.aethermicroservice.JSONFormats.*;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.collection.CollectResult;

import org.eclipse.aether.resolution.ArtifactDescriptorRequest;
import org.eclipse.aether.resolution.ArtifactDescriptorResult;
import org.eclipse.util.Booter;
import org.eclipse.aether.util.graph.manager.DependencyManagerUtils;
import org.eclipse.aether.util.graph.transformer.ConflictResolver;

public class DependencyHeirarchy {

    //return the dependency heirarchy of the given Artifact
    public static StringBuilder getDependencyHeirarchy(String groupID, String artifactID, String version, ConfigFilePOJO configurations){


        RepositorySystem system = Booter.newRepositorySystem();

        DefaultRepositorySystemSession session = Booter.newRepositorySystemSession( system );

        session.setConfigProperty( ConflictResolver.CONFIG_PROP_VERBOSE, true );
        session.setConfigProperty( DependencyManagerUtils.CONFIG_PROP_VERBOSE, true );


        String artifactRef = groupID+":"+artifactID+":"+version;

        Artifact artifact = new DefaultArtifact(artifactRef);//set the artifact

        ArtifactDescriptorRequest descriptorRequest = new ArtifactDescriptorRequest();
        descriptorRequest.setArtifact( artifact );
        descriptorRequest.setRepositories( Booter.newRepositories( system, session, configurations ) );

        CollectRequest collectRequest = new CollectRequest();


        CollectResult collectResult;
        StringBuilder jsonTreeString = new StringBuilder("{\"ErrorMsg\":\""+"NotFound\"}");
        StringBuilder str;
        try{

            ArtifactDescriptorResult descriptorResult = system.readArtifactDescriptor( session, descriptorRequest );

            collectRequest.setRootArtifact( descriptorResult.getArtifact() );
            collectRequest.setDependencies( descriptorResult.getDependencies() );
            collectRequest.setManagedDependencies( descriptorResult.getManagedDependencies() );
            collectRequest.setRepositories( descriptorRequest.getRepositories() );

            collectResult = system.collectDependencies( session, collectRequest );
            BuildJSONStructure JSONHeirarchy = new BuildJSONStructure();

            collectResult.getRoot().accept( JSONHeirarchy );

            str  =   JSONHeirarchy.stringJSON;

        }catch (Exception e){
            jsonTreeString = jsonTreeString.append("{\"ErrorMsg\":\""+e.getMessage()+"\"}");
            return jsonTreeString;
        }

        return str;
    }
}

