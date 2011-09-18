/**
 * 
 */
package org.topicquests.model
import java.util.Date

/**
 * @author park
 * <p>A class to form a Node API</p>
 */
class NodeModel {

  /**
   * <p>Create a node; does not deal with parent or child nodes</p>
   * <p>NOTE: the returned node is <em>NOT</em> persisted</p>
   * @param nodeType
   * @param label 		not <code>null</code>, not empty string
   * @param details 	not <code>null</code>, can be empty string
   * @param user
   * @return
   */
  def createNode(nodetype: String, label: String, details: String, user: User): Node = {
    var node : org.topicquests.model.Node = org.topicquests.model.Node.create 
    var date: Date = new Date()
    node.label(label)
    node.details(details)
    node.date(date)
    node.creator(user)
    node.nodetype(nodetype)
    node.smallImage(nodeTypeToSmallImage(nodetype))
    node.largeImage(nodeTypeToImage(nodetype))
    
    node
  }
  
  
  def nodeTypeToSmallImage(nt: String): String = {
    try {
      nt match {
        case "Question" => "issue_sm.png"
        case "Idea" => "position_sm.png"
        case "Pro" => "plus_sm.png"
        case "Con" => "minus_sm.png"
        case "Map" => "map_sm.png"
        case "Reference" => "reference_sm.png"
        case "Tag" => "tag_sm.png"
        case "Connection" => "link_sm.png"
        case _ => ""
      }
    } catch {
      case e: Exception  => e.getMessage()
    }
  }
  def nodeTypeToImage(nt: String): String = {
    println("AAAAA "+nt)
    try {
      nt match {
        case "Question" => "issue.png"
        case "Idea" => "position.png"
        case "Pro" => "plus.png"
        case "Con" => "minus.png"
        case "Map" => "map.png"
        case "Reference" => "reference.png"
        case "Tag" => "tag.png"
        case "Connection" => "link.png"
        case _ => ""
      }
    } catch {
      case e: Exception  => e.getMessage()
    }
  }

}