package org.wso2.internalapps.aethermicroservice.JSONFormats;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class gaJSON {
    public  String groupID;
    public  String artifactID;

    public gaJSON(){}

    public gaJSON(String groupID, String artifactID){
        this.groupID = groupID;
        this.artifactID = artifactID;
    }


}