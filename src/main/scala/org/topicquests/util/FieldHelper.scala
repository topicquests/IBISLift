package org.topicquests.util

import net.liftweb.util.{FieldIdentifier, FieldError}
import net.liftweb.util.Helpers._
import net.liftweb.http.{SHtml, FileParamHolder, S}
import xml.{Elem, Text}
import net.liftweb.mapper.{MappedLongForeignKey, Mapper, MappedString}
import org.topicquests.model.Image
import net.liftweb.common.{Box, Empty, Full}

/**
 * @author dfernandez
 * @license Apache2.0
 */


/**
 * Trait for MapperFields that want to have a single clean value, eg. username
 */
trait CleanField extends FieldIdentifier{
  def valClean(msg: => String)(value: String): List[FieldError] =
    if(value == clean(value))
      Nil
    else
      List(FieldError(this, Text(msg)))
}


