package org.topicquests.snippet

import net.liftweb._
import common.Full
import org.topicquests.model.{SessionActiveConversation, ChatLine, User}
import http._
import js.JsCmd
import util._
import Helpers._
import net.liftweb.http.js.JsCmds.SetValById
import net.liftweb.http.SHtml
import http.js.JsCmds._
import org.topicquests.template.ChatTemplate
import org.topicquests.comet.{FullChatLine, ChatCometServer}
import org.topicquests.util.DateHelper

/**
 * @author dfernandez
 *
 */

class Chat {

  def loadChat() = {
    val y: List[String] = S.request.open_!.path.partPath
    val conversationId = y.last.toLong
    val list = ChatLine.getLastLines(conversationId)
    if(list.size>0){
    "#message" #> (list.reverse.map(chatLine => {
      val user = chatLine.user.open_!;
      "*" #> ChatTemplate.getXHTML(new FullChatLine(chatLine.conversation.is,
        chatLine.chat.is,
        user.fullName,
        DateHelper.chatDate.format(chatLine.createdDate.is).capitalize,
        {if(user.picture.obj.isDefined) user.picture.obj.open_!.imageUrl else "/images/userimage.png"}
      ))
    }))
    }
    else {
      "#message" #> <div class="chat-welcome-message" style="margin:8px">Welcome!!</div>
    }
  }

  def prepare() = {
    
    if(User.loggedIn_?){
      "#chatinput" #> SHtml.onSubmit(msg => {
        SessionActiveConversation.is match {
          case Full(conversationId) => {
            val chatLine = ChatLine.create.conversation(conversationId).user(User.currentUser.open_!).chat(msg)
            ChatCometServer ! chatLine
            SetValById("chatinput", "")
          }
          case _ =>
        }
      })
    }
    else {
      "#chatform" #> <div><b>To participate you must be <a href="/login">logged in</a></b></div>
    }

  }


  def getsubmitchat(): JsCmd = {
    Function("submitchat", List(), SHtml.submitAjaxForm("chatform"))    
  }

  def setscripts() = {
    val sc1 = getsubmitchat();
    "*" #> Script(sc1)
  }


}