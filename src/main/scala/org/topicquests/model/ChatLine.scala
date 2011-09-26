package org.topicquests.model

import java.util.Date
import net.liftweb.mapper._
import net.liftweb.util.{Props, Helpers}

/**
 * @author dfernandez
 */

class ChatLine extends LongKeyedMapper[ChatLine] with IdPK {

  def getSingleton = ChatLine

  object conversation extends MappedLongForeignKey(this, User)
  object user extends MappedLongForeignKey(this, User)
  object chat extends MappedText(this)  
  object createdDate extends MappedDateTime(this){
    override def defaultValue = new Date()
    override def dbIndexed_? = true
  }

}

object ChatLine extends ChatLine with LongKeyedMetaMapper[ChatLine] {

  def getLastLines(conversationId: Long) : List[ChatLine] = 
    ChatLine.findAll(By(ChatLine.conversation, conversationId), OrderBy(ChatLine.createdDate, Descending), MaxRows(Props.get("max.chat.rows").openOr("15").toLong))

}