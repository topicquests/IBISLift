package org.topicquests.template


import net.liftweb.util._
import Helpers._
import net.liftweb.http.Templates
import xml.{NodeSeq, Text}
import org.topicquests.model.Node
import net.liftweb.common.Loggable

/**
 * @author dfernandez
 *
 */

object NodeTemplate extends Loggable {

  //The XHTML that contains a node template
  val nodeXHTML = Templates(List("/templates-hidden/nodetemplate")).open_!

  /**
   * Loads the template with the node object and returns the resulting XHTML
   *
   * @param node the object with the info of the node
   * @reutn a NodeSeq with the resulting XHTML
   */
  def getXHTML(node: Node): NodeSeq = {
     (
      "#node [id]" #> ("node_" + node.uniqueId.is.toString)  &
      ".nodehref [href]" #> node.id.toString() &
      ".nodeimg [src]" #> ("/images/ibis/" + node.smallImage) &
      ".nodetitle *" #> node.label.toString()
     )(nodeXHTML)
  }


}