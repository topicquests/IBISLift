package org.topicquests.template

import net.liftweb.util._
import Helpers._
import net.liftweb.http.Templates
import org.topicquests.model.{ChatLine, Node}
import org.topicquests.comet.FullChatLine
import java.util.regex.Pattern
import xml.{Text, XML, NodeSeq}
import net.liftweb.common.Loggable
import org.topicquests.util.StringHelper

/**
 * @author dfernandez
 * @license Apache2.0
 *
 */

object ChatTemplate extends Loggable {

  //The XHTML that contains a chat line template
  val nodeXHTML = Templates(List("/templates-hidden/chattemplate")).open_!

  /**
   * Loads the template with the FullChatLine object and returns the resulting XHTML
   *
   * @param FullChatLine the object with the info of the chat line
   * @reutn a NodeSeq with the resulting XHTML
   */
  def getXHTML(fullChatLine: FullChatLine): NodeSeq = {
     (      
      ".message-profile-name *" #> fullChatLine.userName &
      ".message-timestamp *" #> fullChatLine.time &
      ".message-text *" #> StringHelper.processText(fullChatLine.chat) &
      ".profile-icon-image [src]" #> fullChatLine.imageSrc
     )(nodeXHTML)
  }




}