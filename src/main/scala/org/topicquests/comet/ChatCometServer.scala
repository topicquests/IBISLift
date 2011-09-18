package org.topicquests.comet

import net.liftweb.actor.LiftActor
import net.liftweb.http.ListenerManager
import org.topicquests.model.ChatLine
import org.topicquests.util.DateHelper

/**
 * @author dfernandez
 *
 */


case class FullChatLine(conversastionId: Long, chat: String, userName: String, time: String, imageSrc: String)

object ChatCometServer extends LiftActor with ListenerManager {

  private var action = new Object()
  def createUpdate =  action

  override def lowPriority = {
    case chatLine: ChatLine => {
      chatLine.save;
      val user = chatLine.user.open_!;
      action = new FullChatLine(chatLine.conversation.is, 
        chatLine.chat.is,
        user.fullName,
        DateHelper.chatDate.format(chatLine.createdDate.is).capitalize,
        {if(user.picture.obj.isDefined) user.picture.obj.open_!.imageUrl else "/images/userimage.png"});
      updateListeners()
    }
  }
  
}