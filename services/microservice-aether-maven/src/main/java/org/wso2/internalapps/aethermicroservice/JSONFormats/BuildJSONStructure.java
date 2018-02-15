package org.wso2.internalapps.aethermicroservice.JSONFormats;

import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.graph.DependencyVisitor;

/**
 * A dependency visitor that dumps the graph JSON.
 */
public class BuildJSONStructure implements DependencyVisitor
{
    public StringBuilder stringJSON = new StringBuilder();
    private Boolean rootNodePrinted  = false;

    public BuildJSONStructure() {

    }

    public boolean visitEnter( DependencyNode node )
    {
        if(!rootNodePrinted) {
            stringJSON.append("{\"title\":\""+node.getArtifact().getArtifactId()+" ("+node.getArtifact().getVersion()+")\",");
            stringJSON.append("\"subtitle\":\"GroupID:"+node.getArtifact().getGroupId()+", ArtifactID:"+node.getArtifact().getArtifactId()+"\"");
            if(node.getChildren().isEmpty()){
                stringJSON.append("}");
            }else{
                stringJSON.append(",\"expanded\": true");
                stringJSON.append(",\"children\":[");
            }
            rootNodePrinted = true;
            return true;
        } else {
            stringJSON.append("{\"title\":\""+node.getArtifact().getArtifactId()+" (Version: "+node.getArtifact().getVersion()+", Scope: "+node.getDependency().getScope()+")\",");
            stringJSON.append("\"subtitle\":\"GroupID:"+node.getArtifact().getGroupId()+", ArtifactID:"+node.getArtifact().getArtifactId()+"\"");
            if(node.getChildren().isEmpty()){
                stringJSON.append("}");
            }else{
                stringJSON.append(",\"expanded\": false");
                stringJSON.append(",\"children\":[");
            }
            return true;
        }
    }

    public boolean visitLeave( DependencyNode node )
    {
        if(node.getChildren().isEmpty()){
            stringJSON.append(",");
        }else{
            stringJSON.deleteCharAt(stringJSON.length()-1);
            stringJSON.append("]},");
        }
        return true;
    }


}
