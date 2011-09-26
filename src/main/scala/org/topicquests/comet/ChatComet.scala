package org.topicquests.comet

import net.liftweb._
import common.Full
import http._
import js.JE.JsRaw
import js.jquery.JqJE
import util._
import Helpers._
import net.liftweb.http.{CometActor, CometListener}
import org.topicquests.model.{SessionActiveConversation}
import org.topicquests.template.ChatTemplate
import net.liftweb.http.js.JsCmds.OnLoad

/**
 * @author dfernandez
 *
 */

class ChatComet extends CometActor with CometListener {

  //Register as a listener with the ChatCometServer
  def registerWith = ChatCometServer

  //No pre render needed
  def render = <br/>

  //Proccess messages
  override def lowPriority = {
    case fullChatLine: FullChatLine => {
      SessionActiveConversation.is match {
        case Full(conversationId) if(conversationId == fullChatLine.conversastionId) => {
          this.partialUpdate(OnLoad((JqJE.Jq("#chat") ~> JqJE.JqAppend(ChatTemplate.getXHTML(fullChatLine))).cmd &
                  JsRaw("$('.time-formatted').each(function (i) {toLocalDate($(this)); $(this).removeClass('time-formatted');});scrollBottom();").cmd)
            )
        }
        case _ =>
      }
    }
  }


}