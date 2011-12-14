/**
 * 
 */
package org.topicquests.snippet
import net.liftweb._
import common._
import util._
import Helpers._
import http._
import org.topicquests.model.Environment
import java.io.{File}
import scala.xml._

/**
 * @author park
 *
 */
class StaticContentSnippet {

  def show(in:NodeSeq):NodeSeq = {
    var files: Array[File] = new File(Environment.staticFolderPath).listFiles()
    println("##### "+files)
    var buf = new StringBuilder()
    buf.append("<span>");
    if (files != null && files.length > 0) {
      var where:Int = 0
      var px:String = ""
      var fix:File = null
      var fx:String = ""
      for (px <- files) {
        fx = px.getPath()
        where = fx.indexOf("index") // don't show the index file
	        if (where == -1) {
		        where = fx.indexOf("static") // grab everything including "static"
		        //that's because people will use the entire path in hrefs and image links
		        if (where > -1) {
		          fx = fx.substring(where-1)
		           buf.append("<a href=\""+fx+"\">"+fx+"</a><br />");
		        }
		    }
      }
    }
    buf.append("</span>");
    var filelist:NodeSeq = new Text("no files");
    try { // always dangerous
      filelist = XML.loadString(buf.toString())
    } catch {
      case e:Exception =>println(e.getMessage())
    }
    //bind the value into html
    bind("v",in,
        "files" -> filelist
    )
    
  }
}