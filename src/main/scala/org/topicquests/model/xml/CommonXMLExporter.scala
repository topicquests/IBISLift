/**
 * 
 */
package org.topicquests.model.xml
import org.topicquests.model._
import scala._

/**
 * @author park
 * @license Apache2.0
 * <p>A class to export to the Compendium DTD</p>
 * <p>This turns out to be non-trivial</p>
 * <p>The open issue is where to do the exporting:
 * <li>to a string or gzip file to be downloaded</li>
 * <li>to a web page in the browser to be saved</li></p>
 */
class CommonXMLExporter {

  
  def exportConversation(root: org.topicquests.model.Node): StringBuilder = {
    //TODO turn this puppy into an XML string
    //this will be a recursive call down the tree
    var result = new StringBuilder()
    var views = new StringBuilder()
    var nodes = new StringBuilder()
    var links = new StringBuilder
    //TODO preliminary stuff goes into result first
    //then fill with nodes and links
    //links are non-trivial: they need to 
    exportNode(root,views,nodes,links)
    //then combine views, nodes, and links into result
    //then add closing xml
    result
  }
  
  def exportNode(node: org.topicquests.model.Node, views: StringBuilder, nodes: StringBuilder, links: StringBuilder) {
    
  }
}