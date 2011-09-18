/**
 * 
 */
package org.topicquests.model.xml
import scala.xml._ 
import scala.xml.pull._
import scala.collection.mutable._

import scala.io.Source
import net.liftweb.util._
import net.liftweb.common._
import net.liftweb.util.Helpers._

import org.topicquests.model.{NodeModel, Node}

/**
 * @author park
 * <p>A class to import from the Compendium DTD</p>
 * <p>Sources of XML files:
 * <li>from an uploaded file</li>
 * <li>from a text area where the document is pasted into the browser</li>
 * </p>
 * <p>Pasting in XML only works for very small files. Uploaded XML files are preferred</p>
 * <p>Code adapted from http://www.monomorphic.org/wordpress/first-steps-with-scala-xml-pull-parsing/</p>
 */
class CommonXMLImporter {
  var model: NodeModel = new NodeModel()
  val interLink = """\[\[(.*)\]\]""".r
	var readingText = false
	var nodes: Map[String, Map[String,Object]]= new HashMap[String, Map[String,Object]]()
	var rootId = ""
    /**
     * <p>In a properly exported map, there is IBISNode with the label = "Home Window"
     * which has noderef = rootid. When that view is imported, where viewid=rootid,
     * that node is the rootNode--the Map of this issue map. It's noderef is
     * then the viewref for its children nodes.</p>
     */
	var rootNodeId = ""
	  //a working node being crafted during parsing
	val theNode: Node = null
  
  def parse(xml: String)  {
    
    nodes.empty
	 val src = Source.fromString(xml)
     val p = new XMLEventReader(src)
     p.foreach(matchEvent)
  }
	
  //TODO write the parser here to take apart Compendium Nodes and attributes
  // EvElemStart(pre,label,attrs,scope)
  def matchEvent(ev: XMLEvent) = {
    ev match {
      //reading text
      case EvElemStart(_, "text", _, _) => { 
        readingText = true
        print(backToXml(ev))
      }
      case EvElemStart(_, "model", attrs, _) => { 
         //there is just one node, a Map, that exists
         //inside rootView. We don't care about this
         //rootId; we only care about the one that came in,
         //the canvas into which this map is imported.
    	  rootId = getAttribute(attrs,"rootview")
    	  //this will be the identity of a Map node, which means we must have a
    	  //node api that can archive values other than what is in an ordinary node
      }
      case EvElemStart(_, _, _, _) => { print(backToXml(ev)) }
      case EvText(text) => {
        if (readingText) print(filterText(text)) else print(text) 
      } 
      case EvElemEnd(_, "text") => {
        readingText = false
        print(backToXml(ev))
      }
      case EvElemEnd(_, _) => { print(backToXml(ev)) }
      case _ => {}
    }
  }
  
  def getAttribute(attrs: MetaData, key: String) :String = {
    val x : scala.collection.Seq[scala.xml.Node] = attrs.apply(key)
    var result:String = ""
    if (x != null) {
      result = x.headOption.toString()
    }
    result
  }
  
   def backToXml(ev: XMLEvent) = {
    ev match {
      case EvElemStart(pre, label, attrs, scope) => {
        "<" + label + attrsToString(attrs) + ">"
      }
      case EvElemEnd(pre, label) => {
        "</" + label + ">"
      }
      case _ => ""
    }
  }
 
  def attrsToString(attrs:MetaData) = {
    attrs.length match {
      case 0 => ""
      case _ => attrs.map( (m:MetaData) => " " + m.key + "='" + m.value +"'" ).reduceLeft(_+_)
    }
  }
 
  def filterText(text: String) = {
    val matches = interLink.findAllIn(text)
    if (matches.hasNext) matches.reduceLeft(_+_) else ""
  }
}