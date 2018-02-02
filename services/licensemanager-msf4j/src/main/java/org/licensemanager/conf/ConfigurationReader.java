package org.licensemanager.conf;

import java.io.IOException;

public class ConfigurationReader {

    public Configuration getConfigurations() throws IOException{

        Configuration repoConfigs = ReadYaml.readYaml();
        return repoConfigs;
    }

}