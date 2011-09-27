package org.topicquests.snippet

import net.liftweb.util.Helpers._
import net.liftweb.util._
import xml.{NodeSeq, Text}
import net.liftweb.http.{FileParamHolder, S, SHtml}
import org.topicquests.util.ImageHelper
import org.topicquests.model.{Image, User}
import net.liftweb.common.{Box, Empty, Full}

/**
 * @author dfernandez
 */
class Account {

  def listUsers = {

    def delete (user : User) {
      user.delete_!
    }

    ".user" #> User.findAll().map({user =>
        ".firstName *" #> <img src={if(user.picture.obj.isDefined) user.picture.obj.open_!.imageUrl else "/images/userimage.png"} alt="User Picture"/> &
        ".firstName *" #> user.firstName.is &
        ".lastName *" #> user.lastName.is &
        ".email *" #> user.email.is &
        ".username *" #> user.userName.is &
        ".validated *" #> user.validated.is &
        ".superuser *" #> user.superUser.is &
        ".edit *" #> SHtml.link("/admin/user/edit/" + user.uniqueId.is, () => {}, Text("Edit")) &
        ".delete *" #> SHtml.link("/admin/user", () => {}, Text("Delete"))
    });

  }

  def editUser = {

    val user = S.param("uniqueId") match {
      case Full(uniqueId) => {
        User.findByUniqueId(uniqueId) match {
          case Full(user) => { //Logged int checked in boot
            user
          }
          case _ => {
            S.error(S.?("invalid.url"))
            S.redirectTo("/")
          }
        }
      }
      case _ => {
        User.create;
      }
    }

    var photoFileHolder : Box[FileParamHolder] = Empty

    def saveuser() {
      val fileOk = photoFileHolder match {
        case Full(FileParamHolder(_, null, _, _)) => true
        case Full(FileParamHolder(_, mime, _, data))  if mime.startsWith("image/") => {
          val imageWithMetaData = ImageHelper.getImageFromByteArray(data);
          val bufferedImageThumb = ImageHelper.square(None, imageWithMetaData.buffImageimage, 38);
          val imgBytes = ImageHelper.imageToByteArray(imageWithMetaData.format, bufferedImageThumb);
          val img = Image.create.image(imgBytes).mimeType(mime);
          img.save();
          if(user.picture.obj.isDefined)
            user.picture.obj.open_!.delete_!
          user.picture(img);
          true
        }
        case Full(_) => {
          S.error("Invalid receipt attachment")
          false
        }
        case _ => true
      }

      (fileOk, user.validate) match {
        case (true,Nil) =>
          user.save
          S.notice("User saved")
          S.redirectTo("/admin/user/list")

        case (_,xs) => S.error(xs) ;
      }
    }

    "#firstname" #> SHtml.text(user.firstName.is, user.firstName(_) , "size" -> "30", "maxlength" -> "256" ) &
    "#lastname" #> SHtml.text(user.lastName.is, user.lastName(_) , "size" -> "30", "maxlength" -> "256" ) &
    "#email" #> SHtml.text(user.email.is, user.email(_) , "size" -> "30", "maxlength" -> "256" ) &
    "#username" #> SHtml.text(user.userName.is, user.userName(_) , "size" -> "30", "maxlength" -> "256" ) &
    "#passworddiv" #> (if(user.saved_?) ("*" #> (Empty: Box[String])) else ("#password" #> user.password._toForm)) &
    "#validated" #> SHtml.checkbox(user.validated.is, user.validated(_) ) &
    "#admin" #> SHtml.checkbox(user.superUser.is, user.superUser(_) ) &
                    "#picture" #> SHtml.fileUpload(fh => photoFileHolder = Full(fh)) &
                    "#submit" #> SHtml.submit("Save", saveuser _, "class" -> "button save")
  }

}