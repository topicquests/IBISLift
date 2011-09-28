/**
 * 
 */
package org.topicquests.model
import scala.xml._
import scala.collection.immutable.List

/**
 * @author park
 * @license Apache2.0
 *
 */
object UserModel {

  /**
   * List user in support of an Admin user manager function
   */
  def list(in: NodeSeq) = {
    	val users: List[User] = User.findAll()
    	bindUsers(users,in)
  }
    
  def bindUsers(userList: List[User],in: NodeSeq): NodeSeq = {
    userList.flatMap{m =>
      val ix = m.id.toString()
      val lbl = m.userName.toString()
      //TODO: this is as if we want to paint a user page.
      //Must change to a javascript call that will exist on the Admin/usermanager page
      //in order to paint the traits of the user and edit them, change
      //to/from admin roles, etc
      <a href={"/user/"+ix}>{lbl}</a><br />
    }
  }


}