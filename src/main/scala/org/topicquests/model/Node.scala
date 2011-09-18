/**
 * 
 */
package org.topicquests.model
import net.liftweb.mapper._
import net.liftweb.mapper.OneToMany$MappedOneToMany
import net.liftweb.common.Box

/**
 * @author park
 * @see http://www.assembla.com/spaces/liftweb/wiki/Mapper for information on
 * linking parent and child nodes
 * <p><em>Node</em> is intended to be used for all sorts of nodes in
 * a graph-based knowledge representation system. Each node represents a
 * <em>Topic</em>, no matter whether it is a Tag, Connection, or Conversation node.
 * </p>
 */
class Node  extends LongKeyedMapper[Node] with IdPK  with OneToMany[Long, Node]{
	def getSingleton = Node

  object uniqueId extends MappedUniqueId(this, 32){
    override def dbIndexed_? = true
    override def writePermission_?  = true
  }
	object details extends MappedText(this)
	object label extends MappedString(this,200)
	object smallImage extends MappedString(this,32)
	object largeImage extends MappedString(this,32)
	object lang extends MappedInt(this) //default "en"
	object date extends MappedDateTime(this)
	object creator extends MappedLongForeignKey(this,User)
	object nodetype extends MappedString(this,32)
	object parent extends MappedLongForeignKey(this, Node)
	
	object children extends MappedOneToMany(Node, Node.parent, OrderBy(Node.id, Ascending))
	//TODO add Tags
}

object Node extends Node with LongKeyedMetaMapper[Node] {
 	//TODO: now that we are just calling this a Node, 
  // do we really want to use the table "conversation_nodes"?
  //That's because Node will be used for tags, conversations, etc
  override def dbTableName = "conversation_nodes" // define the DB table name

  def findById (id: Long) : Box[Node] =
    Node.find(By(Node.id, id))

  def findByUniqueId (uniqueId: String) : Box[Node] =
    Node.find(By(Node.uniqueId, uniqueId))
}