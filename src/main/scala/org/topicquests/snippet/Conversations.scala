/**
 *
 */
package org.topicquests.snippet

import net.liftweb.util._
import net.liftweb.common._
import net.liftweb.util.Helpers._
import net.liftweb.http._
import js.JE.JsRaw
import js.JsCmd
import net.liftweb.mapper._
import net.liftweb.http.SHtml.ChoiceHolder
import net.liftweb.http.S._
import org.topicquests.model._
import scala.xml._
import scala.collection.immutable.List
import java.util.Date
import org.topicquests.comet.{ConversationCometServer, AddNodeAction}
import net.liftweb.http.js.JsCmds._
import org.topicquests.model.NodeModel
/**
 * @author park
 *
 */
class Conversations  {
	//used for Node creation
	var model: NodeModel = new NodeModel()
	
  /**
   * Add a node to a given conversation
   * @param in
   * @return NodeSeq
   */
  def addresponse(in: NodeSeq) : NodeSeq = {
    var label = ""
    var details = ""
    var nodetype = ""
    // set in Conversation.show
    var x = "-1"

    var parentId: Long = -1

    def process(): JsCmd = {
      try {
        //this will fail when the user is not logged in
        //but in that case, we are not painting the response form anyway
        x = S.get("nodeid").openTheBox
        parentId = x.toLong
      } catch {
        case exc : Exception => {
          //do nothing
        }
      }
      println("STARTING PARENT ID "+x+" "+nodetype)
      User.currentUser match {
        case Full(user) =>  {
          doRespond(nodetype,label,details,parentId, user)
          S.notice("Respond saved")
          //Clears the inputs
          JsRaw("$('#resplabel').val('')").cmd & JsRaw("$('#respdetails').val('')")
        }
        case _ => Noop;
      }
    }

    val radmap = Seq[String] ("Question", "Idea", "Pro", "Con", "Reference")
    val radios: ChoiceHolder[String] = SHtml.radio(radmap,
      Full("Question"),
      nodetype = _ )

    SHtml.ajaxForm(
	    bind("entry", in,
	      "question" -> radios(0),
	      "idea" -> radios(1),
	      "pro" -> radios(2),
	      "con" -> radios(3),
	      "ref" -> radios(4),
	      "label" -> SHtml.textarea(label, label = _ , "class" -> "required","style" -> "width:auto;height:auto;","cols" -> "79","rows" -> "2","id" -> "resplabel"),
	      "details" -> SHtml.textarea(details,  details = _ , "cols" -> "79", "rows" -> "5", "id" -> "respdetails"),
	      "submit" -> SHtml.ajaxSubmit("Respond", process)
	      )
	    )
  }
	
  /**
   * Driven by a form on the front page for New Conversation
   */
  def addnewconversation(in: NodeSeq) : NodeSeq = {
    //  println("ADDING "+in)

    var title = ""
    var label = ""
    var details = ""
    var nodetype = ""

    def process() {
      println("STARTING")
      User.currentUser match {
        case Full(user) =>  doAdd(title,nodetype,label,details,user)
        case _ => Text("No user available")

      }
      S.redirectTo("/")
    }


    val radmap = Map("Idea"->"I", "Question"->"Q") //this could have been a Seq[String]
    val radios: ChoiceHolder[String] = SHtml.radio(radmap.keys.toList,
      Full("Question"),
      nodetype = _ )
    bind("entry", in,
      "title" -> SHtml.text(title, title = _, "class" -> "required"),
      "question" -> radios(1),
      "idea" -> radios(0),
      "label" -> SHtml.textarea(label,
        label = _ ,
        "class" -> "required",
        "style" -> "width:auto;height:auto;",
        "cols" -> "68",
        "rows" -> "2"),
      "details" -> SHtml.textarea(details,
        details = _ ,
        "style" -> "width:auto;",
        "cols" -> "68",
        "rows" -> "5"),
      "submit" -> SHtml.submit("Add", process)
      )

  }

  
  /**
   * Fetch a particular node
   * @param id
   */
  case class IBISNodeLoc(id: Long) {
    lazy val record: org.topicquests.model.Node = {
      org.topicquests.model.Node.find(By(org.topicquests.model.Node.id, id)).open_!
    }
  }

  /**
   * Here when a user responds to a node, called from <code>addresponse</code>
   * @param nodetype
   * @param label
   * @param details
   * @param parentId
   * @param user
   */
  def doRespond(nodetype: String, label: String, details: String, parentId: Long, user: User) = {
    println("PROCESSING "+label+" "+nodetype+" "+parentId)
    var date: Date = new Date()
    var node: org.topicquests.model.Node = model.createNode(nodetype,label,details,user)
    var parentUniqueId = ""
    if (parentId > -1) {
      var nx = IBISNodeLoc(parentId).record
      //saves the parent node uniqueId
      parentUniqueId = nx.uniqueId.is
      //set node's parent
      node.parent(nx)
      //add child to parent
      var snappers: List[org.topicquests.model.Node] = nx.children.toList
      //These printlins do, indeed, show the node has children
      //but PROCESSING-BB doesn't show the added child.
      //you see it in the next PROCESSING-AA -- when another child is added
      //NOT SURE WHY THAT HAPPENS
      //println("PROCESSING-AA "+snappers)
      snappers :+ node
      // println("PROCESSING-BB "+snappers)
      nx.save()
      //	  println("PROCESSING-XX "+nx)
    }
    //still need to know how this plays out
    node.save()

    //Updates all comets listeners
    ConversationCometServer ! new AddNodeAction(parentId, parentUniqueId, node)
  }
  /**
   * Heavy lifting of creating an IBISConversation
   */
  def doAdd(title: String, nodetype: String, label: String, details: String, user: User) = {
    println("PROCESSING "+title+" "+nodetype)
    //make the root node
    var date: Date = new Date()
    var node: org.topicquests.model.Node = model.createNode(nodetype,label,details,user)
    node.save()
    println("PROCESSING-2 "+node)
    //make the conversation itself
    //technically speaking, this is really a Map node
    //TODO upgrade from Conversation to Map nodetype
    var conversation: IBISConversation = IBISConversation.create
    conversation.label(title)
    conversation.creator(user)
    conversation.startdate(date)
    conversation.lastdate(date)
    conversation.rootnodeid(node)
    conversation.save()
    println("PROCESSED "+conversation.id)
  }

  /**
   * List all IBISConversations as HREFs for the front page
   * TODO: paging if the list gets too long
   */
  def list(in: NodeSeq) = {
    val convs: List[IBISConversation] = IBISConversation.findAll()
    //    println("XXXX "+convs)
    bindMessages(convs,in)
  }

  /**
   * Convert a list of IBISConversations into a list of HREFs to those
   * conversations
   */
  def bindMessages(messageList: List[IBISConversation],in: NodeSeq): NodeSeq = {
    messageList.flatMap{m =>
      val ix = m.id.toString()
      val lbl = m.label.toString()
      <a href={"/conversation/"+ix}>{lbl}</a><br />
    }
  }

}