package org.topicquests.util

import java.util.Date
import java.text.SimpleDateFormat
import net.liftweb.common.{Box, Full, Empty}

/**
 * @author dfernandez
 * @license Apache2.0
 */


object DateHelper   {

  //Date formats
  val chatDate = new SimpleDateFormat("MMM d yyyy, hh:mm") //Nov 4, 2003
  val noSlashDate = new SimpleDateFormat("yyyyMMdd")
  val slashDate = new SimpleDateFormat("MM/dd/yyyy hh:mm")
  val slashDateShort = new SimpleDateFormat("MM/dd/yyyy")

  type DateConverter = String => Date

  /**
   * converts a String to a Date according to the DateConverted received
   *
   * @param value the string to be converted
   * @param converter the converter that knows how to parse the string
   * @return a Box object with a Full(date) object if it could be converted or an Empty object if it could not.
   */
  def parseDate(value : String, converter : DateConverter) : Box[Date] =
    try {
      Full(converter(value))
    } catch {
      case e => Empty
    }


}