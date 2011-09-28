package org.topicquests.util

import java.util.regex.Pattern
import xml.{Text, XML, NodeSeq}

/**
 * @author dfernandez
 */
object StringHelper {

  /**Processes the a text to find internal url and transform the to HTML anchors
    *
   * @param text the text to be processed
   * @result a NodeSeq HTML with the urls in the text replaced for HTML anchors
   */
  def processText(text:String): NodeSeq = {

    val regex = """(\A|\s)((https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])"""; // matches <http://google.com>
    val mUrls = Pattern.compile(regex).matcher(text)
    val result0 = new StringBuffer() ;
    while (mUrls.find) {
      mUrls.appendReplacement(result0, mUrls.group(1) + "<a target='_blank' href='" + mUrls.group(2) + "'>" + mUrls.group(2) + "</a>")
    }
    mUrls.appendTail(result0)

    try {
      val xml = XML.loadString("<span>" + result0.toString + "</span>");
      val newXml = xml.head.child.flatMap(node => {
        if(node.isAtom)
          Text(node.text)
        else
          node
      })
      newXml
    }
    catch {
      case e:Exception => <span></span>
    }

  }


}