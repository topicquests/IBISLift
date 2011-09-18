package org.topicquests.template


import net.liftweb.util._
import Helpers._
import net.liftweb.http.Templates
import xml.{NodeSeq, Text}
import org.topicquests.model.Node

/**
 * @author dfernandez
 *
 */

object NodeTemplate {

  val nodeXHTML = Templates(List("/templates-hidden/nodetemplate")).open_!

  def getXHTML(node: Node): NodeSeq = {
     (
      "#node [id]" #> ("node_" + node.uniqueId.is.toString)  &
      ".nodehref [href]" #> node.id.toString() &
      "#nodeimg [src]" #> ("/images/ibis/" + node.smallImage) &
      "#nodeimg [id]" #> (None: Option[String]) &
      "#nodetitle *" #> node.label.toString()  &
      "#nodetitle [id]" #> (None: Option[String])
     )(nodeXHTML)
  }


}