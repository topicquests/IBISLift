package org.topicquests.comet

import org.topicquests.template.NodeTemplate
import net.liftweb.http.js.JsCmds.OnLoad
import net.liftweb.http.js.jquery.JqJE
import net.liftweb.http.{S, CometActor, CometListener}
import net.liftweb.common.Full
import collection.mutable.HashSet
import org.topicquests.model.SessionActiveNodes

/**
 * @author dfernandez
 *
 */

class ConversationComet extends CometActor with CometListener {

  //Register asa listener with the ConversationCometServer
  def registerWith = ConversationCometServer

  //Render method: not used
  def render = <br/>

  //Processes messages sent by ConversationCometServer
  override def lowPriority = {
    
    //If the user is viewing the node's conversation and the parent node has been loaded, it adds the new node.
    case AddNodeAction(parentId, parentUniqueId, node) =>{

      //Adds loaded node to the active nodes in the session
      if(!SessionActiveNodes.isDefined){
       SessionActiveNodes.set(Full(new HashSet[Long]()))
      }
      SessionActiveNodes.open_!.add(node.id.is);

      if(parentId > -1){
        //Checks if the object is defined (just in case) and if contains the parent node as an active node
        if(SessionActiveNodes.isDefined && SessionActiveNodes.open_!.contains(parentId)){           
           this.partialUpdate(OnLoad((JqJE.Jq("#node_" + parentUniqueId + " > ul > div") ~> JqJE.JqAppend(NodeTemplate.getXHTML(node))).cmd))
        }
      }
    }
  }


}