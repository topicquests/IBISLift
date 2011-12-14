/**
 * 
 */
package org.topicquests.model
import net.liftweb.mapper._
import net.liftweb.mapper.ManyToMany$MappedManyToMany
import net.liftweb.common.Box
import scala._
import scala.collection.mutable.StringBuilder
import org.topicquests.model.{User, Node => tNode}

/**
 * @author park
 *
 */
class Tag extends LongKeyedMapper[Tag] with IdPK  with ManyToMany {
	def getSingleton = Tag 
	def nodeType : String = "Tag"
	  
	object details extends MappedText(this)
	object label extends MappedString(this,200)
	object smallImage extends MappedString(this,32)
	object largeImage extends MappedString(this,32)
	object lang extends MappedString(this,3)
	object date extends MappedDateTime(this)
	object creator extends MappedLongForeignKey(this,User)
	object parent extends MappedLongForeignKey(this, Node)

	//Mapping borrowed from ESME
	//This means that you have to search for tags that go with nodes
	object nodes extends MappedManyToMany(TagNodes, TagNodes.tag, TagNodes.node, tNode)
	
	object users extends MappedManyToMany(TagUsers, TagUsers.tag, TagUsers.user, User)
 

}

object Tag extends Tag with LongKeyedMetaMapper[Tag] {
  	override def dbTableName = "tags" // define the DB table name
  
	def findById (id: Long) : Box[Tag] =
    	Tag.find(By(Tag.id, id))   
}

//Join tables

class TagNodes extends LongKeyedMapper[TagNodes] with IdPK {
  def getSingleton = TagNodes
  object node extends LongMappedMapper(this, tNode)
  object tag extends LongMappedMapper(this, Tag)
}
object TagNodes extends TagNodes with LongKeyedMetaMapper[TagNodes]
class TagUsers extends LongKeyedMapper[TagUsers] with IdPK {
  def getSingleton = TagUsers
  object user extends LongMappedMapper(this, User)
  object tag extends LongMappedMapper(this, Tag)
}
object TagUsers extends TagUsers with LongKeyedMetaMapper[TagUsers]

