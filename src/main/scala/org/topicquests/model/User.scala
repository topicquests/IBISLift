
package org.topicquests.model

import net.liftweb.util.Helpers._
import net.liftweb.util._
import net.liftweb.common._
import net.liftweb.http._
import net.liftweb.mapper._
import net.liftweb.sitemap.Loc._
import collection.mutable.{HashSet, ListBuffer}
import net.liftweb.http.S
import S._
import scala.xml.Text
import scala.xml.NodeSeq
import js._
import JsCmds._
import org.topicquests.util.{ImageHelper, CleanField}
//The session object SessionActiveConversation keeps the users active conversations.
object SessionActiveConversation extends SessionVar[Box[Long]](Empty)

//The session object sessionActiveNodes keeps the users active nodes. It is clear when the user changes the conversation.
object SessionActiveNodes extends SessionVar[Box[HashSet[Long]]](Empty)


class User extends MegaProtoUser[User] {
  def getSingleton = User // what's the "meta" server

  object userName extends MappedString(this, 32) with CleanField {
    override def dbIndexed_? = true
    override def writePermission_?  = true
    override def validations = valUnique(S.?("username.in.use")) _ :: valClean(S.?("username.must.be.clean")) _ :: super.validations
    override def displayName = S.?("username") //TODO:changeText  !!!!!!!!!!!!!!!!
    override val fieldId = Some(Text("txtUserName"))
  }
  override lazy val password = new MyPassword(this) {
    override def _toForm: Box[NodeSeq] = {
      S.fmapFunc({s: List[String] => this.setFromAny(s)}){funcName =>
        Full({appendFieldId(<input type={formInputType} name={funcName} value={is.toString}  size="30" maxlength="256" />)})
      }
    }
    override def setFromAny(f: Any): String = {
      f match {
        case a : Array[String] if (a.length == 1) => {this.set(a(0))}
        case l : List[String] if (l.length == 1) => {this.set(l.head)}
        case a : Array[String] if (a.length == 2 && a(0) == a(1)) => {this.set(a(0))}
        case l : List[String] if (l.length == 2 && l.head == l(1)) => {this.set(l.head)}
        case _ => {S.error("passwords.do.not.match")}
      }
      is
    }
  }
  object picture extends MappedLongForeignKey(this, Image)
  object createdDate extends MappedLong(this) {
    override def defaultValue = Helpers.millis
  }

  def fullName: String = (firstName.is, lastName.is, userName.is) match {
    case (f, l, u) if f.length > 1 && l.length > 1 => f+" "+l+" ("+u+")"
    case (f, _, u) if f.length > 1 => f+" ("+u+")"
    case (_, l, u) if l.length > 1 => l+" ("+u+")"
    case (_, _, u) => u
  }
}

/**
 * The singleton that has methods for accessing the database
 */
object User extends User with MetaMegaProtoUser[User] {

  val IfLoggedIn = If(() => loggedIn_?, S.?("must.be.logged.in"))
  val IfAdmin = If(() => loggedIn_? && superUser_?, "must.be.admin")

  override def homePage = "/"

  override def thePath(end: String): List[String] = List(end)
  override def signUpSuffix = "signup"
  override def loginSuffix = "login"
  override def lostPasswordSuffix = "lostpassword"
  override def passwordResetSuffix = "resetpassword"
  override def changePasswordSuffix = "changepassword"
  override def logoutSuffix = "logout"
  override def editSuffix = "edituser"
  override def validateUserSuffix = "validateuser"

  override def dbTableName = "users" // define the DB table name
  override def screenWrap = Full(<lift:surround with="default" at="content">
      <lift:bind /></lift:surround>)
  // define the order fields will appear in forms and output
  override def fieldOrder = List(id, userName, email, password)

  override def signupFields = List(userName, email, password)

  override def editFields = List(email)

  // comment this line out to require email validations
  override def skipEmailValidation = true

  def findByUserName (userName: String) : Box[User] =
    User.find(By(User.userName, userName))

  override def userNameFieldString: String = S.??("username").capitalize
  def EmailFieldString: String = S.??("email.address")

  override def loginXhtml = {
    (<div class="cont">
      <div class="toptitle">
        {S.??("log.in")}
      </div>
      <div style="text-align:left;color:#000;">
        <form method="post" action={S.uri}>
          <div class="watertext" style="margin-top:5px;" >
            <label  class="watermark" for="username" >{userNameFieldString}</label>
              <input type="text" size="30" name="username" id="username" maxlength="256"/>
          </div>
          <div class="watertext" style="margin-top:5px;" >
            <label  class="watermark" for="password" >{S.??("password")}</label>
              <input type="password" size="30" name="password" id="password" maxlength="256"/>
          </div>
          <a style="margin-top:10px;text-align:right;" href={lostPasswordPath.mkString("/", "/", "")}><small>{S.??("recover.password")}</small></a>
          <div style="margin-top: 40px;"><input type="submit" id="submit" value="Send" class="button save" /></div>
        </form>
      </div>
    </div>)
  }

  override def login = {

    val redirAfterLogin = homePage

    def internalLogin() = {
      S.param("username").
              flatMap(username => findByUserName(username)) match {
        case Full(user) if user.validated_? &&
                user.testPassword(S.param("password")) => {
          val preLoginState = capturePreLoginState()
          logUserIn(user, () => {
            S.notice(S.??("logged.in"))
            preLoginState()
            S.redirectTo(redirAfterLogin)
          })
        }

        case Full(user) if !user.validated_? =>
          S.error(S.??("account.validation.error"))

        case _ => S.error(S.??("invalid.credentials"))
      }
    }

    ("#submit" #> SHtml.submit(S.??("log.in"), internalLogin, "class" -> "button save"))(loginXhtml)
  }

  override def lostPasswordXhtml = {
    (<div class="cont">
      <div class="toptitle">
        {S.??("lost.password")}
      </div>
      <p>{S.??("enter.email")}</p>
      <div style="text-align:left;color:#000;">
        <form method="post" action={S.uri}>
          <div class="watertext" style="margin-top:5px;" >
            <label  class="watermark" for="email" >{EmailFieldString}</label>
              <input type="text" size="30" name="email" id="email" maxlength="256"/>
          </div>
          <div style="margin-top: 40px;"><input type="submit" id="submit" value="Send" class="button save" /></div>
        </form>
      </div>
    </div>)
  }

  override  def lostPassword = {
    ("#email" #> SHtml.text("", sendPasswordReset _, "size" -> "30", "maxlength" -> "256") &
            "#submit" #> <input type="submit" value={S.??("send.it")} class="button save" />)(lostPasswordXhtml)
  }

  def signupXhtml = {
    (<div class="cont">
      <div class="toptitle">
        { S.??("sign.up") }
      </div>
      <div style="text-align:left;color:#000;">
        <form method="post" enctype="multipart/form-data" action={S.uri}>
          <div class="watertext" style="margin-top:5px;" >
            <label  class="watermark" for="username" >{userNameFieldString}</label>
              <input type="text" size="30" name="username" id="username" maxlength="256"/>
          </div>
          <div class="watertext" style="margin-top:5px;" >
            <label  class="watermark" for="email" >{EmailFieldString}</label>
              <input type="text" size="30" name="email" id="email" maxlength="256"/>
          </div>
          <div class="watertext" style="margin-top:5px;" >
            <label  class="watermark" for="picture" >Picture</label>
              <input name="picture" id="picture" />
          </div>
          <div class="watertext" style="margin-top:5px;" >
            <label  class="watermark" for="password" >{S.??("password")}</label>
              <input type="password" size="30" name="password" id="password" maxlength="256"/>
          </div>
          <div style="margin-top: 40px;"><input type="submit" id="submit" value="Send" class="button save" /></div>
        </form>
        <p style="margin-top: 40px;" class="small">By signing up you agree to our <a href="/tos">terms &amp; condiitions</a> and <a href="/privacy">privacy policy</a>.</p>
      </div>
    </div>)
  }



  override def signup = {
    val theUser: TheUserType = mutateUserOnSignup(createNewUserInstance())
    val theName = signUpPath.mkString("")

    var photoFileHolder : Box[FileParamHolder] = Empty

    def testSignup() {
      val fileOk = photoFileHolder match {
        case Full(FileParamHolder(_, null, _, _)) => true
        case Full(FileParamHolder(_, mime, _, data))  if mime.startsWith("image/") => {
          val imageWithMetaData = ImageHelper.getImageFromByteArray(data);
          val bufferedImageThumb = ImageHelper.square(None, imageWithMetaData.buffImageimage, 38);
          val imgBytes = ImageHelper.imageToByteArray(imageWithMetaData.format, bufferedImageThumb);
          val img = Image.create.image(imgBytes).mimeType(mime);
          img.save();
          theUser.picture(img);
          true
        }
        case Full(_) => {
          S.error("Invalid receipt attachment")
          false
        }
        case _ => true
      }


      (fileOk, validateSignup(theUser)) match {
        case (true,Nil) =>
          actionsAfterSignup(theUser, () => S.redirectTo(homePage))

        case (_,xs) => S.error(xs) ; signupFunc(Full(innerSignup _))
      }
    }

    def innerSignup = (
            "#username" #> SHtml.text(theUser.userName.is, theUser.userName(_) , "size" -> "30", "maxlength" -> "256" ) &
                    "#email" #> SHtml.text(theUser.email.is, theUser.email(_) , "size" -> "30", "maxlength" -> "256" ) &
                    "#password" #> theUser.password._toForm &
                    "#picture" #> SHtml.fileUpload(fh => photoFileHolder = Full(fh)) &
                    "#submit" #> SHtml.submit(S.??("sign.up"), testSignup _, "class" -> "button save")
            )(signupXhtml)

    innerSignup

  }

  override def passwordResetXhtml = {
    (<div class="cont">
      <div class="toptitle">
        {S.??("reset.your.password")}
      </div>
      <div style="text-align:left;color:#000;">
        <form method="post" action={S.uri}>
          <div class="watertext" style="margin-top:5px;" >
            <label  class="watermark" for="password" >{S.??("enter.your.new.password")}</label>
              <input type="password" size="30" id="password" class="password" maxlength="256"/>
          </div>
          <div class="watertext" style="margin-top:5px;" >
            <label  class="watermark" for="reppassword" >{S.??("repeat.your.new.password")}</label>
              <input type="password" size="30" id="reppassword" class="password" maxlength="256"/>
          </div>
          <div style="margin-top: 40px;"><input type="submit" id="submit" value="Send" class="button save" /></div>
        </form>
      </div>
    </div>)
  }

  override def passwordReset(id: String) =
    findUserByUniqueId(id) match {
      case Full(user) =>
        def finishSet() {
          user.validate match {
            case Nil =>
              user.save
              logUserIn(user, () => {S.notice(S.??("password.changed")); S.redirectTo(homePage)})
            case xs => S.error(xs)
          }
        }
        user.resetUniqueId().save

        (".password" #> SHtml.password_*("",(p: List[String]) => user.setPasswordFromListString(p)) &
                "#submit" #> SHtml.submit(S.??("set.password"), finishSet _,  "class" -> "button save")
                )(passwordResetXhtml)

      case _ => S.error(S.??("password.link.invalid")); S.redirectTo(homePage)
    }

  override def changePasswordXhtml = {
    (<div class="cont">
      <div class="toptitle">
        {S.??("change.password")}
      </div>
      <div style="text-align:left;color:#000;">
        <form method="post" action={S.uri}>
          <div class="watertext" style="margin-top:5px;" >
            <label  class="watermark" for="old_pwd" >{S.??("old.password")}</label>
              <input type="password" size="30" id="old_pwd" maxlength="256"/>
          </div>
          <div class="watertext" style="margin-top:5px;" >
            <label  class="watermark" for="new_pwd" >{S.??("new.password")}</label>
              <input type="password" size="30" id="new_pwd" class="new_pwd" maxlength="256"/>
          </div>
          <div class="watertext" style="margin-top:5px;" >
            <label  class="watermark" for="repnew_pwd" >{S.??("repeat.password")}</label>
              <input type="password" size="30" id="repnew_pwd" class="new_pwd" maxlength="256"/>
          </div>
          <div style="margin-top: 40px;"><input type="submit" id="submit" value="Send" class="button save" /></div>
        </form>
      </div>
    </div>)
  }

  override def changePassword = {
    val user = currentUser.open_! // we can do this because the logged in test has happened
    var oldPassword = ""
    var newPassword: List[String] = Nil

    def testAndSet() {
      if (!user.testPassword(Full(oldPassword))) S.error(S.??("wrong.old.password"))
      else {
        user.setPasswordFromListString(newPassword)
        user.validate match {
          case Nil => user.save; S.notice(S.??("password.changed")); S.redirectTo(homePage)
          case xs => S.error(xs)
        }
      }
    }

    ("#old_pwd" #> SHtml.password("", s => oldPassword = s) &
            ".new_pwd" #> SHtml.password_*("", LFuncHolder(s => newPassword = s)) &
            "#submit" #> SHtml.submit(S.??("change"), testAndSet _)
            )(changePasswordXhtml)
  }

  def editXhtml = {
    (<div class="cont" >
      <div class="toptitle">
        {S.??("edit")}
      </div>
      <div style="text-align:left;color:#000;">
        <form method="post" enctype="multipart/form-data" action={S.uri}>
          <div class="watertext" style="margin-top:5px;" >
            <label  class="watermark" for="email" >{EmailFieldString}</label>
              <input type="password" size="30" id="email" name="email" maxlength="256"/>
          </div>
          <div class="watertext" style="margin-top:5px;" >
            <label  class="watermark" for="picture" >Picture</label>
              <input name="picture" id="picture" />
          </div>
          <div style="margin-top: 40px;"><input type="submit" id="submit" value="Save" class="button save" /></div>
        </form>
        <div style="margin-top: 40px;"><a href="/changepassword" >Change password</a></div>
      </div>
    </div>)
  }

  override def edit = {
    val theUser: TheUserType =
    mutateUserOnEdit(currentUser.open_!) // we know we're logged in

    val theName = editPath.mkString("")

    var photoFileHolder : Box[FileParamHolder] = Empty

    def testEdit() {
      val fileOk = photoFileHolder match {
        case Full(FileParamHolder(_, null, _, _)) => true
        case Full(FileParamHolder(_, mime, _, data))  if mime.startsWith("image/") => {
          val imageWithMetaData = ImageHelper.getImageFromByteArray(data);
          val bufferedImageThumb = ImageHelper.square(None, imageWithMetaData.buffImageimage, 38);
          val imgBytes = ImageHelper.imageToByteArray(imageWithMetaData.format, bufferedImageThumb);
          val img = Image.create.image(imgBytes).mimeType(mime);
          img.save();
          if(theUser.picture.obj.isDefined)
            theUser.picture.obj.open_!.delete_!
          theUser.picture(img);
          true
        }
        case Full(_) => {
          S.error("Invalid receipt attachment")
          false
        }
        case _ => true
      }

      (fileOk, theUser.validate) match {
        case (true,Nil) =>
          theUser.save
          S.notice(S.??("profile.updated"))
          S.redirectTo(homePage)

        case (_,xs) => S.error(xs) ; editFunc(Full(innerEdit _))
      }
    }

    def innerEdit = (
            "#email" #> SHtml.text(theUser.email.is, theUser.email(_) , "size" -> "30", "maxlength" -> "256" ) &
                    "#picture" #> SHtml.fileUpload(fh => photoFileHolder = Full(fh)) &
                    "#submit" #> SHtml.submit("Change email", testEdit _, "class" -> "button save")
            )(editXhtml)

    innerEdit

  }


}

