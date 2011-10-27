/**
 * 
 */
package org.topicquests.model
import net.liftweb.mapper._

/**
 * @author park
 * @license Apache2.0
 *
 */
case class ConversationLoc (id: String) {
  lazy val record: IBISConversation = {
    IBISConversation.find(By(IBISConversation.id, id.toLong)).open_!
  }
}