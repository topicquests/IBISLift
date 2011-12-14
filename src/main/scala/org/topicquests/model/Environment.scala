/**
 * 
 */
package org.topicquests.model
import net.liftweb._
import util._
import Helpers._

/**
 * @author park
 *
 */
object Environment {
  var _landingTitle:String = ""
  var _subTitle:String = "Public, Open signup"
  var _staticPath = ""
    
  def init() {
    _landingTitle = Props.get("landing.title") openOr "Conversations that matter"
    var pvt:Boolean = if(Props.get("authentication.required").open_! == "true") true else false
    var invite = if(Props.get("invite.required").open_! == "true") true else false
    
    if (pvt && invite)
      _subTitle = "Private, Invitation only"
    else if (!pvt && invite)
      _subTitle = "Public, Invitation only"
    else if (pvt && !invite)
      _subTitle = "Private, Open signup"
        
    _staticPath = Props.get("static.data.path").open_!
  }

  def landingTitle = _landingTitle
  def subTitle = _subTitle
  def staticFolderPath = _staticPath
}