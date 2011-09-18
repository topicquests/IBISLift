/**
 * 
 */
package org.topicquests.model
import net.liftweb.mapper._

/**
 * @author park
 *
 */

class IBISConversation extends LongKeyedMapper[IBISConversation] with IdPK {
	def getSingleton = IBISConversation
	

	/**
	 * Maps the conversation tree root node
	 */
	object rootnodeid extends MappedLongForeignKey(this,Node)
	object startdate extends MappedDateTime(this)
	object lastdate extends MappedDateTime(this)
	object details extends MappedText(this)
	object label extends MappedString(this,200)
	object creator extends MappedLongForeignKey(this,User)

}

object IBISConversation extends IBISConversation with LongKeyedMetaMapper[IBISConversation] {
 	override def dbTableName = "conversations" // define the DB table name 

}


