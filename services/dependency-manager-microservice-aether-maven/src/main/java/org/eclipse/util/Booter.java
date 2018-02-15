package org.eclipse.util;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.RemoteRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.wso2.internalapps.aethermicroservice.configFileReader.ConfigFilePOJO;


/**
 * A helper to boot the repository system and a repository system session.
 */
public class Booter
{

    public static RepositorySystem newRepositorySystem()
    {
        return org.eclipse.manual.ManualRepositorySystemFactory.newRepositorySystem();
    }

    public static DefaultRepositorySystemSession newRepositorySystemSession(RepositorySystem system )
    {
        DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();

        LocalRepository localRepo = new LocalRepository( "target/local-repo" );
        session.setLocalRepositoryManager( system.newLocalRepositoryManager( session, localRepo ) );

        session.setTransferListener( new ConsoleTransferListener() );
        session.setRepositoryListener( new ConsoleRepositoryListener() );

        // uncomment to generate dirty trees
        // session.setDependencyGraphTransformer( null );

        return session;
    }

    public static List<RemoteRepository> newRepositories(RepositorySystem system, RepositorySystemSession session, ConfigFilePOJO remoteRepoInfo)
    {
        return new ArrayList<RemoteRepository>( Arrays.asList( newCentralRepository(remoteRepoInfo) ) );
    }

    private static RemoteRepository newCentralRepository(ConfigFilePOJO remoteRepoInfo)
    {
        String url;
        String id;
        String type;

        url = remoteRepoInfo.getUrl();
        id = remoteRepoInfo.getId();
        type = remoteRepoInfo.getType();

        return new RemoteRepository.Builder( id, type, url ).build();
    }

}