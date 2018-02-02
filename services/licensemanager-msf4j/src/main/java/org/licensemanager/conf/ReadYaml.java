package org.licensemanager.conf;

import com.esotericsoftware.yamlbeans.YamlReader;

import java.io.FileReader;
import java.io.IOException;

public class ReadYaml {
    public static Configuration readYaml() throws IOException{

        YamlReader reader = new YamlReader(new FileReader("./conf/configurations.yaml"));

        Configuration remoteRepoConfigurations= reader.read(Configuration.class);

        return remoteRepoConfigurations;
    }

}