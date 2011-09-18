package org.topicquests.template

import net.liftweb.util._
import Helpers._
import net.liftweb.http.Templates
import xml.NodeSeq
import org.topicquests.model.{ChatLine, Node}
import org.topicquests.comet.FullChatLine

/**
 * @author dfernandez
 *
 */

object ChatTemplate {

  val nodeXHTML = Templates(List("/templates-hidden/chattemplate")).open_!

  def getXHTML(fullChatLine: FullChatLine): NodeSeq = {
     (      
      ".message-profile-name *" #> fullChatLine.userName &
      ".message-timestamp *" #> fullChatLine.time &
      ".message-text *" #> fullChatLine.chat &
      ".profile-icon-image [src]" #> fullChatLine.imageSrc
     )(nodeXHTML)
  }


}