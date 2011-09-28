package org.topicquests.util

import java.text.DecimalFormat


/**
 * @author dfernandez
 * @license Apache2.0
 *
 */


object LongHelper   {

  /**
   * Validates if a string can be converted to a long
   *
   * @param str the string to be validated
   * @return Boolean with true if it could be converted, false if it could not
   */
  def validateLong(str: String): Boolean = {
    try {
      str.toLong
      true;
    }
    catch {
      case e => false;
    }
  }

  /**
   * Transforms a string to a Long
   *
   * @param str     the string to be transformed
   * @param default the default value if the string can not be transformed
   * @result returns a Long with the transformed value, if it could not transform it, it returns the default value
   */
  def getLongParam(str : String, default : Long) : Long = {
    try {
      str.toLong
    }
    catch {
      case e => default // Should log something in this case
    }
  }

}