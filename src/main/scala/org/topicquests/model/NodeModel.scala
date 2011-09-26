/**
 * 
 */
package org.topicquests.model
import java.util.Date
import org.topicquests.dispatcher.NodeEventDispatcher
import net.liftweb.common.Loggable

/**
 * @author park
 * <p>A class to form a Node API</p>
 */
class NodeModel extends Loggable {

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
    //node.lang("en") //TODO lang is wrong
    //notify the new node (even though it's not saved yet)
    //NOTE: could move this to wherever createNode is called
    NodeEventDispatcher.dispatchNodeEvent(node.id.toString(),label)
    node
  }
  
  def updateNode(nodetype: String, label: String, details: String, user: User, node: org.topicquests.model.Node) = {
	  logger.info("UPDATING "+nodetype)
    var date: Date = new Date()
    node.label(label)
    node.details(details)
    node.date(date)
//    node.creator(user)
    node.nodetype(nodetype)
    node.smallImage(nodeTypeToSmallImage(nodetype))
    node.largeImage(nodeTypeToImage(nodetype))
    //node.lang("en") //TODO lang is wrong
    //notify the new node (even though it's not saved yet)
    //NOTE: could move this to wherever createNode is called
  //  NodeEventDispatcher.dispatchNodeEvent(node.id.toString(),label)
    node.save()
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
    logger.info("AAAAA "+nt)
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