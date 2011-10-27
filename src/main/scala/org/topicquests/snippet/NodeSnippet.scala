/**
 * 
 */
package org.topicquests.snippet
import net.liftweb._
import common._
import util._
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
import org.jsoup.Jsoup
import org.topicquests.util.{XHTMLHelper, LongHelper, StringHelper}
import org.topicquests.model.{Node => tNode, User}
import net.liftweb.sitemap._
import net.liftweb.sitemap.Loc.{Template}
import net.liftweb.http.js._
import net.liftweb.http.js.JsCmds._
import net.liftweb.http.js.JE._
import net.liftweb.http.js.JsCmds.{Script, Function}

import net.liftweb.http.SHtml._

/**
 * @author park
 *
 */
class NodeSnippet extends Loggable {
	var convHelper: Conversation = new Conversation()
	
    def show(in: NodeSeq) :NodeSeq  = {
      
	          //Cleans the active session nodes
	    if(SessionActiveNodes.isDefined){
	      SessionActiveNodes.open_!.clear();
	    }
	    //FIRST, fetch the desired node
	    //If no node id param it redirects you to the homepage
	    val id = S.param("id") match {
	      case Full(id) if(LongHelper.validateLong(id)) => id
	      case _ => S.redirectTo("/")
	    }
	
	// println("$$$$ "+id)
	    val theNode: tNode = NodeLoc(id).record
	//println("#### "+theNode)
	    
	    //NEXT, fetch the conversation
	    
	    val cid: Long = theNode.conversation.toLong
	//println("%%%% "+cid)
	    // fetch the conversation
	    val rec = ConLoc(cid).record
	
	    val conlink = <b><a href={"/conversation/"+cid.toString()}><img src="/images/ibis/map.png"/>{StringHelper.stripCdata(rec.label.toString())}</a></b>
	    //Sets the session object with the active conversation
	    SessionActiveConversation.set(Full(id.toLong))
	    //GET ready to paint the conversation tree
	    // fetch the root node of this conversation
	    val nidx: Long = rec.rootnodeid.toLong
	    //AFTER ALL IS SAID AND DONE: scala.xml.XML.loadString
	    // converts a Stringified XML into a NodeSeq. It doesn't get
	    // any better than that!
	    //the node
	//    val nxx = IBISNodeLoc(nidx).record
	    //parent node
	    val pid: Long = theNode.parent.toLong
logger.debug("ABABA "+pid+" "+theNode)
	    var nodelink:NodeSeq = new Text("None")
	    //appears that if parent == null, toLong returns 0
	    if (pid > 0) {
	    	val pxx = IBISNodeLoc(pid).record
	        nodelink = <b><a href={"/node/"+pid.toString()}><img src={"/images/ibis/"+pxx.largeImage.toString()}/>{StringHelper.stripCdata(pxx.label.toString())}</a></b>
	    }
	    val ux: User = User.find(rec.creator.toString()).openTheBox
	    val autname = convHelper.getAuthorName(ux)
	    val dat = rec.startdate.toString
	    //bind some stuff into the view
	 println("XXXX "+autname+" "+dat)
		//Actually Paint the node itself
		S.set("nodeid", id)
	    val uxn: User = User.find(theNode.creator.toString()).openTheBox
	    
	    val il = <span><img src={"/images/ibis/" + theNode.largeImage}/> <b>{StringHelper.stripCdata(theNode.label)}</b></span>
	    val autnamen = convHelper.getAuthorName(uxn)
	    val datn = convHelper.getNodeDate(theNode)
	    val nx = <span>{autnamen}</span>
	    val dx = <span>{datn}</span>
	    val det = <div>{Unparsed(Jsoup.clean(theNode.details.is, XHTMLHelper.whiteList))}</div>
	    //here, we are in a /node/ URL so we don't need to say node/
	    val permalink = <a href={id}>Permalink</a>
	    val TAB1:NodeSeq = det
	    var TAB2:NodeSeq = new Text("Not available")
	    var TAB3:NodeSeq = new Text("Not available")
	    var TAB4:NodeSeq = new Text("Not available yet")
	    //note: here, we add the "response" form when appropriate
	    if (convHelper.userCanEdit(theNode)) {
	      TAB2 =  convHelper.myeditform
	      TAB3 =  convHelper.mycreateform
 	    } else if (convHelper.userCanParticipate(theNode)) {
	      TAB3 = convHelper.mycreateform
	    }                                                                        
	    val prepareCkEditorJCmds = OnLoad(JsRaw(
	    "delete CKEDITOR.instances['respdetails'];" +
	    "delete CKEDITOR.instances['editdetails'];" +
	    "$('#respdetails').ckeditor();" +
	    "$('#editdetails').ckeditor();"));
	    
	    //Do the node painting
	    prepareCkEditorJCmds
	    //child nodes
	    var kids: NodeSeq = new Text("None")
	    var snappers: List[org.topicquests.model.Node] = theNode.children.toList
	    if (snappers.length > 0) {
	      kids = null
	       for (nx <- snappers) {
	         if (kids == null)
	           kids = <b><a href={"/node/"+nx.id.toString()}><img src={"/images/ibis/"+ nx.smallImage}/>{StringHelper.stripCdata(nx.label.toString())}</a></b><br />
	         else
	           kids = (kids).++(<b><a href={"/node/"+nx.id.toString()}><img src={"/images/ibis/"+ nx.smallImage}/>{StringHelper.stripCdata(nx.label.toString())}</a></b><br />)
	       }
	    }

	    //Now paint the tree
	    bind("v",in,
	      "parentconversation" -> conlink,
	      "parentnode" -> nodelink ,
	      "author" -> new Text(autname),
	      "datetime" -> new Text(dat),
	      "permalink" -> permalink,
     	  "imglabel" -> il.child,
	      "authorname"-> nx.child,
	      "datetime"-> dx.child,
	      "tabs-1" -> TAB1,
	      "tabs-2" -> TAB2,
	      "tabs-3" -> TAB3,
	      "tabs-4" -> TAB4,
	       "tabs-5" -> kids
	      )
	}

	def getnodes(param: String): JsCmd = {
	    val con = IBISNodeLoc(param.toLong).record
	    logger.info("XXXX "+con)
	    //  save parentId in case a child node is added
	    S.set("nodeid", param)
	    val ux: User = User.find(con.creator.toString()).openTheBox
	    val il = <span><img src={"/images/ibis/" + con.largeImage}/> <b>{StringHelper.stripCdata(con.label)}</b></span>
	    val autname = convHelper.getAuthorName(ux)
	    val dat = convHelper.getNodeDate(con)
	    val nx = <span>{autname}</span>
	    val dx = <span>{dat}</span>
	    val det = <div>{Unparsed(Jsoup.clean(con.details.is, XHTMLHelper.whiteList))}</div>
	    val permalink = <a href={"/node/"+param}>Permalink</a>
	    //note: here, we add the "response" form when appropriate
	    val setHtmlJCmds = if (convHelper.userCanEdit(con)) {
	      SetHtml("tab2", convHelper.myeditform) &
	      SetHtml("tab3", convHelper.mycreateform) &
	      SetHtml("imglabel", il.child) &
	      SetHtml("tabs-1", det) &
	      SetHtml("authorname", nx.child) &
	      SetHtml("datetime", dx.child) &
	      SetHtml("permalink",permalink)
	    } else if (convHelper.userCanParticipate(con)) {
	      SetHtml("tab2", new Text("Not available")) &
	      SetHtml("tab3", convHelper.mycreateform) &
	      SetHtml("imglabel", il.child) &
	      SetHtml("tabs-1", det) &
	      SetHtml("authorname", nx.child) &
	      SetHtml("datetime", dx.child) &
	      SetHtml("permalink",permalink)
	    } else {
	      SetHtml("imglabel", il.child) &
	      SetHtml("tabs-1", det) &
	      SetHtml("tab2", new Text("Not available")) &
	      SetHtml("tab3", new Text("Not available")) &
	      SetHtml("authorname", nx.child) &
	      SetHtml("datetime", dx.child) &
	      SetHtml("permalink",permalink)
	    }                                                                         
	    val prepareCkEditorJCmds = OnLoad(JsRaw(
	    "delete CKEDITOR.instances['respdetails'];" +
	    "delete CKEDITOR.instances['editdetails'];" +
	    "$('#respdetails').ckeditor();" +
	    "$('#editdetails').ckeditor();"));
	
	    setHtmlJCmds & prepareCkEditorJCmds
	}

	def preparejavascript() = {
     val fun=  Function("loadnodes", List("param"), SHtml.ajaxCall(JsRaw("param"), getnodes)._2)
      "#myscript" #> Script(fun)
    }

}

case class NodeLoc(id: String) {
  lazy val record: tNode = {
    tNode.find(By(tNode.id, id.toLong)).open_!
  }
}

case class ConLoc (id: Long) {
  lazy val record: IBISConversation = {
    IBISConversation.find(By(IBISConversation.id, id)).open_!
  }
}
