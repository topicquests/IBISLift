/**
 * 
 */
package org.topicquests.dispatcher

/**
 * @author park
 * <p> In which we create a dispatcher to serve notices of new node events</p>
 */
object NodeEventDispatcher {

  /**
   * <p>This must send event to a Comet server that runs a not-persisted
   * ring-buffer of node events displayed on the portal's homepage</p>
   * <p>This basically requires a REST URL for each node</p>
   * 
   */
  def dispatchNodeEvent(nodeId: String, nodeLabel: String) {
    val HREF = "" //TODO make an href out of nodeId and label
      //TODO send that to a Comet server
  }
}