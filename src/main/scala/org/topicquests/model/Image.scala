package org.topicquests.model

import net.liftweb.util.Helpers
import net.liftweb.mapper._

/**
 * @author dfernandez
 */

class Image  extends LongKeyedMapper[Image] with IdPK {

 def getSingleton = Image

 object image extends MappedBinary(this)

 object uniqueId extends MappedUniqueId(this, 16) {
   override def dbIndexed_? = true
 }
 object saveTime extends MappedLong(this) {
   override def defaultValue = Helpers.millis
 }
 object mimeType extends MappedString(this, 256)

 def imageUrl : String = {
   "/image/"+uniqueId
 }

}


object Image extends Image with LongKeyedMetaMapper[Image] {
 override def dbTableName = "images"
}
