package org.wso2.internalapps.lm.thirdpartylibrary.common.filereader;

import java.io.IOException;

public class ConfigurationReader {

    public POJO_Config_File getConfigurations() throws IOException{

        POJO_Config_File configs = ReaderYaml.readYaml();
        return configs;
    }

}
