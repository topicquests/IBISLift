package org.topicquests.comet

import net.liftweb.actor.LiftActor
import org.topicquests.model.Node
import net.liftweb.common.{Box, Empty}
import net.liftweb.http.{SessionVar, ListenerManager}

/**
 * @author dfernandez
 *
 */

//Messages to the comet to add a Node
case class AddNodeAction(parentId: Long, parentUniqueId: String, node: Node)

object ConversationCometServer  extends LiftActor with ListenerManager {

  private var action = new Object()
  def createUpdate =  action

  //Processes the messages
   override def highPriority = {
    case addNodeAction: AddNodeAction =>{         
      action = addNodeAction;
      updateListeners();
    }
   }

}