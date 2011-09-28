package org.topicquests.util

import xml.NodeSeq
import org.topicquests.model.IBISConversation
import org.topicquests.snippet.IBISNodeLoc
import net.liftweb.common.Loggable


/**
 * @author dfernandez
 * @license Apache2.0
 */
object ConversationHelper extends Loggable {

   /**
   * Actually export to JSON a selected conversation based on a value in the URL, e.g.
   * http://localhost:8080/wsexport/1
   */
  def exportConversation(conversation: IBISConversation) :String  = {

     // fetch the conversation
     val nidx: Long = conversation.rootnodeid.toLong
     //AFTER ALL IS SAID AND DONE: scala.xml.XML.loadString
     // converts a Stringified XML into a NodeSeq. It doesn't get
     // any better than that!
     //the node
     val nxx = IBISNodeLoc(nidx).record
     val json: String = nxx.toJSON()
     logger.info("EXPORT JSON "+json)
     //TODO call the export routines and send that
     //NodeSeq.Empty //dummy for now
     json
  }


}