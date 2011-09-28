/**
 * 
 */
package org.topicquests.model
import net.liftweb.mapper._
import net.liftweb.common._
/**
 * @author park
 * @license Apache2.0
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

	def toJSON(): String = {
	    var buf: StringBuilder = new StringBuilder("{ \"id\" : \""+this.id+"\", ")
	    buf.append("\"creator\" : \""+this.creator+"\", ")
	    buf.append("\"startdate\" : \""+this.startdate+"\", ")
	    buf.append("\"lastdate\" : \""+this.lastdate+"\", ")
	    buf.append("\"label\" : \""+this.label+"\", ")
	    buf.append("\"details\" : \""+this.details+"\", ")
	    var rx: Box[Node] = Node.findById(rootnodeid)
	    if (rx != null) {
	    	val root = rx.open_!
	    	buf.append("\tree\" : "+root.toJSON())
	    }
	    buf.append("}")
	    buf.toString()
	}

}

object IBISConversation extends IBISConversation with LongKeyedMetaMapper[IBISConversation] {
 	override def dbTableName = "conversations" // define the DB table name 

}


