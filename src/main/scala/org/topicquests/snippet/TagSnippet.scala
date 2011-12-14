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
import scala.xml._
import org.topicquests.util.{XHTMLHelper, LongHelper, StringHelper}

/**
 * @author park
 *
 */
class TagSnippet extends Loggable {

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

        var taglink: NodeSeq = null
        
        var nodelinks: NodeSeq = null
        bind("v",in,
	      "selectedtag" -> taglink,
	      "relatednodes" -> nodelinks
        )

       }
}