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
import net.liftweb.http.js.JsCmds._
import org.topicquests.model._
import js.jquery.JqJE
import org.topicquests.comet.{EditNodeAction, ConversationCometServer, AddNodeAction}
import org.topicquests.util.StringHelper

/**
 * @author park
 * @license Apache2.0
 *
 */


/**
 * 	load a conversation from database
 * @param id
 */
//case class ConversationLoc(id: String) {
//  lazy val record: IBISConversation = {
//    IBISConversation.find(By(IBISConversation.id, id.toLong)).open_!
//  }
//}

/**
 * Fetch a particular node
 * @param id
 */
case class IBISNodeLoc(id: Long) {
  lazy val record: org.topicquests.model.Node = {
    org.topicquests.model.Node.find(By(org.topicquests.model.Node.id, id)).open_!
  }
}

class Conversations extends Loggable {
  //TODO: make the cache size a config property
//  var ringBuffer: RecentChangeCache =  RecentChangeCache;

  //used for Node creation
  var model: NodeModel = new NodeModel()
  
  
  /**Creates the bindings for the edit node form
    * @param in  the xml to be transformed
   * @return the xml transformed
   *
   */
  def updatenode() = {
    var label = ""
    var details = ""
    var nodetype = ""
    var tags = ""
    // set in Conversation.show
    var x = "-1"

    var nodeId: Long = -1
    try {
      //this will fail when the user is not logged in
      //but in that case, we are not painting the response form anyway
      x = S.get("nodeid").openTheBox
      nodeId = x.toLong
    } catch {
      case exc : Exception => {
        //do nothing
      }
    }
    // grab the node
    var thenode: org.topicquests.model.Node = IBISNodeLoc(nodeId).record
    nodetype = thenode.nodetype.toString()
    label = StringHelper.stripCdata(thenode.label.toString())
    details = thenode.details.toString()

    def process(): JsCmd = {
      logger.info("STARTING PARENT ID "+x+" "+nodetype)
      User.currentUser match {
        case Full(user) =>  {
          model.updateNode(nodetype,label,details, tags,user, thenode)
          S.notice("Edit saved")
          //Updates all comets listeners
          ConversationCometServer ! new EditNodeAction(thenode)
          Noop;
        }
        case _ => Noop;
      }
    }

    val radmap = Seq[String] ("Question", "Idea", "Pro", "Con", "Reference")
    val radios: ChoiceHolder[String] = SHtml.radio(radmap,
      Full(nodetype),
      nodetype = _ )
                 
     "#editform" #> { in:NodeSeq => SHtml.ajaxForm((
        ".question" #> radios(0) &
        ".idea" #> radios(1) &
        ".pro" #> radios(2) &
        ".con" #> radios(3) &
        ".ref" #> radios(4) &
        ".label" #> SHtml.textarea(label, label = _ , "class" -> "required","style" -> "width:auto;height:auto;","cols" -> "79","rows" -> "2","id" -> "editlabel", "maxlength" -> "200", "onkeyup" -> "return imposeMaxLength(this)") &
        ".details"#> SHtml.textarea(details,  details = _ ,"cols" -> "79", "rows" -> "5", "id" -> "editdetails") &
        ".nodetags" #> SHtml.textarea(tags, tags = _ , "style" -> "width:auto;height:auto;","cols" -> "79","rows" -> "2","id" -> "resptags" ) &
        ".submit [onclick]" #> SHtml.ajaxCall(JsRaw("$('#editdetails').ckeditorGet().getData()"), { x => details = x;}) &
        ".submit" #> SHtml.ajaxSubmit("Save", process))(in)
      )}


/*    SHtml.ajaxForm(
      bind("editentry", in,
        "question" -> radios(0),
        "idea" -> radios(1),
        "pro" -> radios(2),
        "con" -> radios(3),
        "ref" -> radios(4),
        "label" -> SHtml.textarea(label, label = _ , "class" -> "required","style" -> "width:auto;height:auto;","cols" -> "79","rows" -> "2","id" -> "editlabel"),
        "details" -> SHtml.textarea(details,  details = _ , "cols" -> "79", "rows" -> "5", "id" -> "editdetails"),
        "submit [onclick]" -> SHtml.ajaxCall(JsRaw("$('#editdetails').ckeditorGet().getData()"), { x => details = x;}),
        "submit" -> SHtml.ajaxSubmit("Save", process)
        )
      )*/
  }


  /**Creates the bindings for the response node form
    * @param in  the xml to be transformed
   * @return the xml transformed
   *
   */
  def addresponse() = {
    var label = ""
    var details = ""
    var nodetype = ""
    var tags = ""
    var language = "en"  // default -- must add language codes to form

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
      logger.info("STARTING PARENT ID "+x+" "+nodetype)
      User.currentUser match {
        case Full(user) =>  {
          doRespond(nodetype,label,details,parentId, language, tags, user)
          S.notice("Response saved")
          //Clears the inputs
          JsRaw("$('#resp_step1').show();$('#resp_step2').hide();$('#resplabel').val('');$('#respdetails').val('');")
        }
        case _ => Noop;
      }
    }

    val radmap = Seq[String] ("Question", "Idea", "Pro", "Con", "Reference")
    val radios: ChoiceHolder[String] = SHtml.radio(radmap,
      Full("Question"),
      nodetype = _ )

     "#createform" #> { in:NodeSeq => SHtml.ajaxForm((
        ".question" #> radios(0) &
        ".idea" #> radios(1) &
        ".pro" #> radios(2) &
        ".con" #> radios(3) &
        ".ref" #> radios(4) &
        ".label" #> SHtml.textarea(label, label = _ , "class" -> "required","style" -> "width:auto;height:auto;","cols" -> "79","rows" -> "2","id" -> "resplabel", "maxlength" -> "200", "onkeyup" -> "return imposeMaxLength(this)") &
        ".details"#> SHtml.textarea(details,  details = _ , "cols" -> "79", "rows" -> "5", "id" -> "respdetails") &
        ".nodetags" #> SHtml.textarea(tags, tags = _ , "style" -> "width:auto;height:auto;","cols" -> "79","rows" -> "2","id" -> "resptags" ) &
        ".submit [onclick]" #> SHtml.ajaxCall(JsRaw("$('#respdetails').ckeditorGet().getData()"), { x => details = x;}) &
        ".submit" #> SHtml.ajaxSubmit("Save", process))(in)
      )}

    /*SHtml.ajaxForm(
      bind("entry", in,
        "question" -> radios(0),
        "idea" -> radios(1),
        "pro" -> radios(2),
        "con" -> radios(3),
        "ref" -> radios(4),
        "label" -> SHtml.textarea(label, label = _ , "class" -> "required","style" -> "width:auto;height:auto;","cols" -> "79","rows" -> "2","id" -> "resplabel"),
        "details" -> SHtml.textarea(details,  details = _ , "cols" -> "79", "rows" -> "5", "id" -> "respdetails"),
        /*"submit [onclick]" -> SHtml.ajaxCall(JsRaw("$('#respdetails').ckeditorGet().getData()"), { x => details = x;}),*/
        "submit" -> SHtml.ajaxSubmit("Respond", process)
        )
      )*/
  }

  /**
   * Driven by a form on the front page for New Conversation
   *
   * @param in  the xml to be transformed
   * @return the xml transformed
   */
  def addnewconversation() = {

    var title = ""
    var label = ""
    var details = ""
    var nodetype = ""
    var tags = ""
    var language = "en"  // default -- must add language codes to form

    def process() {
      logger.info("STARTING")
      User.currentUser match {
        case Full(user) =>  doAdd(title,nodetype,label,details,language, tags,user)
        case _ => Text("No user available")

      }
      S.redirectTo("/")
    }


    val radmap = Map("Question"->"Q", "Idea"->"I") //this could have been a Seq[String]
    val radios: ChoiceHolder[String] = SHtml.radio(radmap.keys.toList,
      Full("Question"),
      nodetype = _ )

        ".title" #> SHtml.text(title, title = _, "class" -> "required", "maxlength" -> "200") &
        ".question" #> radios(0) &
        ".idea" #> radios(1) &
        ".label" #> SHtml.textarea(label, label = _ , "class" -> "required","style" -> "width:auto;height:auto;","cols" -> "79","rows" -> "2", "maxlength" -> "200", "onkeyup" -> "return imposeMaxLength(this)") &
        ".details"#> SHtml.textarea(details,  details = _ , "cols" -> "79", "rows" -> "5", "id" -> "details") &        
        ".nodetags" #> SHtml.textarea(tags, tags = _ , "style" -> "width:auto;height:auto;","cols" -> "79","rows" -> "2","id" -> "resptags" ) &
        ".submit" #> SHtml.submit("Save", process)
      
  }

  /**
   * Here when a user responds to a node, called from <code>addresponse</code>
   * @param nodetype  the type of the node
   * @param label     the label of the node
   * @param details   the details of the node
   * @param parentId  the id of the parent node
   * @param user      the user who created it
   */
  def doRespond(nodetype: String, label: String, details: String, parentId: Long, language: String, tags: String, user: User) = {
    logger.info("PROCESSING "+label+" "+nodetype+" "+parentId)
 //   println("PROCESSING "+tags)
    var date: Date = new Date()
    var node: org.topicquests.model.Node = model.createNode(nodetype,label,details,language,tags, user)
    var parentUniqueId = ""
    if (parentId > -1) {
      var nx = IBISNodeLoc(parentId).record
      //saves the parent node uniqueId
      parentUniqueId = nx.uniqueId.is
      //set node's parent
      node.parent(nx)
      //set's the conversation
      node.conversation(nx.conversation.obj)
      //add child to parent
      var snappers: List[org.topicquests.model.Node] = nx.children.toList
      //These printlins do, indeed, show the node has children
      //but PROCESSING-BB doesn't show the added child.
      //you see it in the next PROCESSING-AA -- when another child is added
      //NOT SURE WHY THAT HAPPENS
      //logger.info("PROCESSING-AA "+snappers)
      snappers :+ node
      // logger.info("PROCESSING-BB "+snappers)
      nx.save
      //	  logger.info("PROCESSING-XX "+nx)
    }
    //still need to know how this plays out
    node.save

    //Updates all comets listeners
    ConversationCometServer ! new AddNodeAction(parentId, parentUniqueId, node)
  }

  /**
   * Heavy lifting of creating an IBISConversation
   *
   * @param title the title of the node
   * @param nodetype the type of the node
   * @param label the label of the node
   * @param details the details of the node
   * @param language
   * @param tags comma-delimited list of tags
   * @param user  the user who created it
   *
   */
  def doAdd(title: String, nodetype: String, label: String, details: String, language: String, tags: String, user: User) = {
    logger.info("PROCESSING "+title+" "+nodetype)
    //make the root node
    var date: Date = new Date()
    var node: org.topicquests.model.Node = model.createNode(nodetype,label,details,language,tags,user)
    node.save
    logger.info("PROCESSING-2 "+node)
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
    logger.info("PROCESSED "+conversation.id)

    //Adds the conversation to the node object
    node.conversation(conversation).save
  }

  def header(in:NodeSeq) = {
    <span><h2>{Environment.landingTitle}</h2><h3>{Environment.subTitle}</h3></span>
  }
  
  /**
   * List all IBISConversations as HREFs for the front page
   * TODO: paging if the list gets too long
   */
  def list(in: NodeSeq) = {
    val convs: List[IBISConversation] = IBISConversation.findAll()
    //    logger.info("XXXX "+convs)
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
      <a href={"/conversation/"+ix}><img src={"/images/ibis/map_sm.png"}/><b>{lbl}</b></a><br />
    }
  }
  
  def listRecentChanges(in: NodeSeq) :NodeSeq = {
    var nx: org.topicquests.model.Node = null
    var itr: Iterator[org.topicquests.model.Node] = RecentChangeCache.iterator
    var theList: NodeSeq    = null
    while (itr.hasNext) {
      nx = itr.next()
      if (theList == null)
    	  theList =  <b><a href={"node/"+nx.id.toString()}><img src={"/images/ibis/"+ nx.smallImage}/>{StringHelper.stripCdata(nx.label.toString())}</a></b><br />
      else
    	  theList = (<b><a href={"node/"+nx.id.toString()}><img src={"/images/ibis/"+ nx.smallImage}/>{StringHelper.stripCdata(nx.label.toString())}</a></b><br />).++(theList)
    }
    if (theList == null)
    	theList = new Text("No changes yet")
    logger.debug("LISTRECENTCHANGES-3 "+theList)
    bind ("v", in, 
      "list" -> theList
    )
  }

}