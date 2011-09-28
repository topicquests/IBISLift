package org.topicquests.snippet

import net.liftweb._
import common.{Loggable, Logger, Full}
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
import org.topicquests.util.{LongHelper, DateHelper}

/**
 * @author dfernandez
 * @license Apache2.0
 *
 */

class Chat extends Loggable  {

  /**
   * Loads the last chat lines
   * 
   * @return CssBind
   */
  def loadChat() = {
    //If no conversation id param it redirects you to the homepage
    val conversationId = S.param("id") match {
     case Full(id) if(LongHelper.validateLong(id)) => id.toLong
     case _ => S.redirectTo("/")
    }
    val list = ChatLine.getLastLines(conversationId)
    if(list.size>0){
    "#message" #> (list.reverse.map(chatLine => {
      val user = chatLine.user.open_!;
      "*" #> ChatTemplate.getXHTML(new FullChatLine(chatLine.conversation.is,
        chatLine.chat.is,
        user.fullName,
        chatLine.createdDate.is.getTime.toString,
        {if(user.picture.obj.isDefined) user.picture.obj.open_!.imageUrl else "/images/userimage.png"}
      ))
    }))
    }
    else {
      "#message" #> <div class="chat-welcome-message" style="margin:8px">Welcome!!</div>
    }
  }

  /**
   * Prepares the chat input which after sumit send a chatline object to the ChatCometServer
   *
   * @return CssBind
   */
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

  /**
   * Submits chat form Javascript function
   *
   * @return JsCmd Function
   */
  def getsubmitchat(): JsCmd = {
    Function("submitchat", List(), SHtml.submitAjaxForm("chatform"))    
  }

  /**
   * Bind method that adds javascript functions
   *
   * @return CSSBind
   */
  def setscripts() = {
    val sc1 = getsubmitchat();
    "*" #> Script(sc1)
  }


}