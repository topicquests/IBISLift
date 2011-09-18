/**
 * 
 */
package org.topicquests.snippet
import net.liftweb.util._
import net.liftweb.common._
import Helpers._
import net.liftweb.http._
import net.liftweb.mapper._
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc.{Template}
import net.liftweb.http.js._
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE._
import net.liftweb.http.js.JsCmds.{Script, Function}

import net.liftweb.http.SHtml._
import org.topicquests.model._
import scala._
import collection.mutable.HashSet
import scala.xml._


/**
 * @author park
 *
 */
class Conversation extends Loggable {


  /**
   * called by show()
   * A tree is of the form:
   * <li>
   *   <a href="<nodeId>"><img src="<imagepath>"/><nodeLabel</a></li>
   * or
   * <li>
   *   <a href="<nodeId>"><img src="<imagepath>"/><nodeLabel</a>
   *   <ul>
    * <li>  -- for each child, which can similarly nest
   *   <a href="<nodeId>"><img src="<imagepath>"/><nodeLabel</a></li></ul>
   */
  def makeTree(node: org.topicquests.model.Node, buf: StringBuilder) {

    //Adds loaded node to the active nodes in the session
    if(!SessionActiveNodes.isDefined){
     SessionActiveNodes.set(Full(new HashSet[Long]()))
    }
    SessionActiveNodes.open_!.add(node.id.is);

    buf.append("<li id='node_");
    buf.append(node.uniqueId.is)
    buf.append("'><a class='nodehref' href='")
    buf.append(node.id.toString())
    buf.append("'><img src='/images/ibis/")
    buf.append(node.smallImage)
    buf.append("'/>")
    buf.append(node.label.toString())
    buf.append("</a>");
    var snappers: List[org.topicquests.model.Node] = node.children.toList
    if (snappers.length > 0) {
      //start the nested children list
      buf.append("<ul><div style='height: 100%; overflow: visible;'>")
      //recurse on snappers
      for (nx <- snappers)
        makeTree(nx,buf) //recurse
      //end the nested children list
      buf.append("</div></ul>")
      //close out the current node
      buf.append("</li>")
    } else {
      buf.append("<ul><div style='height: 100%; overflow: visible;'></div></ul></li>")
    }
  }
  	
  	/**
  	 * 	load a conversation from database
  	 * @param id
  	 */
	case class ConversationLoc(id: String) {
		lazy val record: IBISConversation = {
		  	IBISConversation.find(By(IBISConversation.id, id.toLong)).open_!
		}
	}
	
	/**
	 * load a conversation node from database
	 * @param id
	 */
	case class IBISNodeLoc(id: Long) {
		lazy val record: org.topicquests.model.Node = {
			org.topicquests.model.Node.find(By(org.topicquests.model.Node.id, id)).open_!
    	}
	}

  /**
   * This method has the dual purpose of launching a Comet server
   * for the conversation based on the conversation's id. We must
   * keep Conversation comet servers around but probably need some
   * way to kill them, e.g. timer that gets refreshed on each hit
   */
   def show(in: NodeSeq) :NodeSeq  = {

     //Cleans the active session nodes
     if(SessionActiveNodes.isDefined){
       SessionActiveNodes.open_!.clear();
     }

     //e.g. /conversation/34 where 34 is the id of the conversation
     val y: List[String] = S.request.open_!.path.partPath
     val id = y.last
     
     // fetch the conversation
     val rec = ConversationLoc(id).record

     //Sets the session object with the active conversation
     SessionActiveConversation.set(Full(id.toLong))

     // fetch the root node of this conversation
     val nidx: Long = rec.rootnodeid.toLong
     //AFTER ALL IS SAID AND DONE: scala.xml.XML.loadString
     // converts a Stringified XML into a NodeSeq. It doesn't get
     // any better than that!
     //the node
     val nxx = IBISNodeLoc(nidx).record
     //conversation tree representation
     val tree: StringBuilder = new StringBuilder()
     makeTree(nxx,tree)
     //conversation tree
     val tx:NodeSeq = XML.loadString(tree.toString())
     
     //bind some stuff into the view       
     bind("v",in,
    		"title" -> rec.label.toString() ,
     		"root" -> tx
     )     
  }

   /**
    * called from conversation.html
    */
    def preparejavascript() = {    
       val fun=  Function("loadnodes", List("param"), SHtml.ajaxCall(JsRaw("param"), getnodes)._2)
       "#myscript" #> Script(fun)
   	}
    
    /**
     * Determine if current user can participate in a conversation
     * @param con
     * @return Boolean
     */
   def userCanParticipate(con: org.topicquests.model.Node): Boolean = {
     //simple test for now
     User.loggedIn_?
     //later, we want to see if the user is on a list of participants
     //if the conversation (not the node itself) is public, no worries
     //This means we need a reference to the conversation persisted in the node
     //and must then fetch the conversation.
   }
   /**
    * creates the ajax necessary to display a selected node in the conversation tree
    */
   	def getnodes(param: String): JsCmd = {
       val con = IBISNodeLoc(param.toLong).record
              println("XXXX "+con)
       //  save parentId in case a child node is added
       S.set("nodeid", param)

       val il = <span><img src={"/images/ibis/" + con.largeImage}/> <b>{con.label}</b></span>
       val det:String = con.details
       //note: here, we add th  e "response" form when appropriate
       if (userCanParticipate(con)) {
    	   SetHtml("tab3", myform) &
    	   SetHtml("imglabel", il.child) &  SetHtml("tabs-1", Text(det))
       } else {
          SetHtml("imglabel", il.child) &  SetHtml("tabs-1", Text(det))
       }
   	}  


    


   	/**
   	 * Returns a NodeSeq that represents the "Respond" form in the
   	 * "Respond" tab. This form should only be visible if both of two conditions
   	 * occur:<br>
   	 * <li>The user is logged in</li>
   	 * <li>A node has been selected (double-clicked) for viewing</li>
   	 * An eventual third condition is this:
   	 * <li>The user is <em>qualified</em> to participate in the conversation</li>
   	 */
    val myform = XML.loadString("""
    <lift:Conversations.addresponse>
     <fieldset>
      <div>
       <fieldset>
        <legend>Create a Response to the Selected Node</legend>
        <div class="radio">
         <fieldset>
          <legend><span>Step1: Select a Node type (REQUIRED):<span>*</span> </span></legend>
          <div>
           Question<entry:question></entry:question><img src="/images/ibis/issue.png" />
           Idea<entry:idea></entry:idea><img src="/images/ibis/position.png" />
           Pro<entry:pro></entry:pro><img src="/images/ibis/plus.png" />
           Con<entry:con></entry:con><img src="/images/ibis/minus.png" />
           Reference<entry:ref></entry:ref><img src="/images/ibis/reference.png" />
		  </div>
         </fieldset>
        </div>
       <div>
        <fieldset>
         <legend><span>Step2: State the Idea, Question Argument, or Resource (REQUIRED)<span>*</span> </span></legend>
         <label for="label">Statement:<span>*</span></label>
         <entry:label></entry:label>
        </fieldset>
       </div>
       <div>
        <fieldset>
  		 <legend>Step3: Explain the Details (Optional)</legend>
  		 <entry:details></entry:details>
  		</fieldset>
  	   </div>
  	  </fieldset>
     </div>
     <entry:submit></entry:submit>
     <div style="color:red" class="lift:Msgs?showAll=true"></div>
    </fieldset>
   </lift:Conversations.addresponse>""")
   
}