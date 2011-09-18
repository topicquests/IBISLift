package org.topicquests.util

import java.util.Date
import java.text.SimpleDateFormat
import net.liftweb.common.{Box, Full, Empty}

/**
 * @author dfernandez
 */


object DateHelper   {

  val chatDate = new SimpleDateFormat("MMM d yyyy, hh:mm") //Nov 4, 2003

  val noSlashDate = new SimpleDateFormat("yyyyMMdd")

  val slashDate = new SimpleDateFormat("MM/dd/yyyy hh:mm")

  val slashDateShort = new SimpleDateFormat("MM/dd/yyyy")

  type DateConverter = String => Date

  def parseDate(value : String, converter : DateConverter) : Box[Date] =
    try {
      Full(converter(value))
    } catch {
      case e => Empty
    }


}