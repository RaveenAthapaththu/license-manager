package org.wso2.internalapps.aethermicroservice.JSONFormats;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class gavJSON {
    public  String groupID;
    public  String artifactID;
    public String version;


    public gavJSON(){}

    public gavJSON(String groupID, String artifactId, String version){
        this.groupID = groupID;
        this.artifactID = artifactId;
        this.version = version;
    }

}
