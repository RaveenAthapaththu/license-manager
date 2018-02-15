package org.configFileReader;

import java.io.IOException;

public class ConfigurationReader {

    public ConfigFilePOJO getConfigurations() throws IOException{

        ConfigFilePOJO repoConfigs = ReaderYaml.readYaml();
        return repoConfigs;
    }

}
