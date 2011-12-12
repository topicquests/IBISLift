package bootstrap.liftweb

import net.liftweb._
import util._
import Helpers._

import common._
import http._
import sitemap._
import Loc._
import mapper._
import net.liftweb.sitemap.Loc
import net.liftweb.http.TemplateFinder
import org.topicquests.db.{DBVendorMySQL}
import org.topicquests.dispatcher.ImageDispatcher
import org.topicquests._
import model._
import snippet._
import org.topicquests.util.ConversationHelper

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot extends Loggable {
  def boot {

	  RecentChangeCache.setSize(50)
	  Environment.init()
	  
    //Set the template format to XHTML and the Output to HTML5
    //LiftRules.htmlProperties.default.set((r: Req) => new XHtmlInHtml5OutProperties(r.userAgent))
    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) => new Html5Properties(r.userAgent))

    //Check db properties
    Props.requireOrDie("db.type")

    //create the database
    logger.info("STARTING "+Props.get("db.type"))

    //Property options
    //db.type = h2
    //db.type = mysql
    var dbtype = Props.get("db.type").open_! // prop existance already checked

    if (dbtype.equals("h2")) {
      val vendor =
      new StandardDBVendor(Props.get("db.driver") openOr "org.h2.Driver",
        Props.get("db.url") openOr
                "jdbc:h2:lift_proto.db;AUTO_SERVER=TRUE",
        Props.get("db.user"), Props.get("db.password"))
println("XXXX "+Props.get("db.user")+" "+Props.get("db.password"))
      LiftRules.unloadHooks.append(vendor.closeAllConnections_! _)

      DB.defineConnectionManager(DefaultConnectionIdentifier, vendor)
    } else if (dbtype.equals("mysql")) {
      //Sets the MySQL connection
      DB.defineConnectionManager(DefaultConnectionIdentifier, DBVendorMySQL);
    } else {
      logger.error("We're doomed! No database of type: "+dbtype)
    }

    //REDIS for nodes (optional)
    //db.spec = redis
    //db.spec = none  (or other nosql database)
    /*var special = Props.get("db.spec").open_! // very dangerous
    if (special.equals("redis")) {
	    //connect to Redis
	    var r: RedisClient = new RedisClient("localhost", 6379)
	    DBVendorRedis.setDatabase(r)
	    logger.info("Starting "+r)
    }*/

    // Use Lift's Mapper ORM to populate the database
    // you don't need to use Mapper to use Lift... use
    // any ORM you want
    Schemifier.schemify(true, Schemifier.infoF _,
      User,
      Node,
      Tag,
      IBISConversation,
      ChatLine,
      Image,
      Invitee)

    //create default admin user
    initUsers()

    def initUsers() {
      // using properties appears to work
      val umail: String = Props.get("admin.email").open_!
      val upwd: String = Props.get("admin.pwd").open_!
      val unm: String = Props.get("admin.uname").open_!

      if (isSuperUserExist == false) {

        val superUser = User.create
                .userName(unm)
                .firstName("Super")
                .lastName("Admin")
                .email(umail)
                .password(upwd)
                .superUser(true)
                .validated(true)
        superUser.save
      }
    }

    def isSuperUserExist() = {
      User.count(By(User.superUser, true)) match {
        case x if x > 0 => true
        case _ => false
      }
    }


    // where to search snippet
    LiftRules.addToPackages("org.topicquests")

    //Here, we punt the entire issue of rewrites and fancy Loc objects out the door
    //Instead, we pass a template to the conversation menu item which embeds
    //the conversation.html template where it belongs.
    //We can use this trick for all sorts of page loads.
    //For it to work, we are faced with this issue:
    // a conversation comes in the form of /conversation/<id>, e.g. /conversation/249
    // which means we must reference a snippet in conversation.html that knows how
    // to fetch the path (/conversation/249) and factor the id (249) out of it and
    // to then fetch the conversation with that identity and render it.
    // This method will generalize to other forms where the path is made of more
    // elements
    val conB =  Template({
      () => <lift:embed what="conversation" />  } )

    val newconTempl = Template({ () => <lift:embed what="newconversation" />})
    val wsexportTempl = Template({ () => <lift:embed what="wsexport" />})
    val recentchangesTempl = Template({ () => <lift:embed what="recentchanges" />})
    val nodeB = Template({ () => <lift:embed what="nodeview" />})
    val tagB = Template({ () => <lift:embed what="tagview" />})

    //Check authentications properties
    Props.requireOrDie("invite.required")
    Props.requireOrDie("authentication.required")

    val authRequired = if(Props.get("authentication.required").open_! == "true") true else false

    val AuthCheck = If(() => (User.loggedIn_? || !authRequired), S.?("must.be.logged.in"))

    val adminMenus: List[Menu] =
    Menu(Loc("Users", "admin" :: "user" :: "list" :: Nil, "Users", User.IfAdmin)) ::
            Menu(Loc("Add User", "admin" :: "user" :: "add" :: Nil, "Add User", User.IfAdmin)) ::
        Menu(Loc("Edit User", "admin" :: "user" :: "edit" :: Nil, "Edit User", User.IfAdmin)) ::
            Invitee.menus;

    // Build SiteMap
    def menus: List[Menu] =
      (Menu(Loc("Home", "index" :: Nil, "Home", User.AddUserMenusAfter)) ::
              Menu(Loc("Conversation", "conversation" :: Nil, "Conversation", Hidden, conB, AuthCheck)) ::
              Menu(Loc("Node", "node" :: Nil, "Node", Hidden, nodeB, AuthCheck)) ::
              Menu(Loc("Tag", "tag" :: Nil, "Tag", Hidden, tagB, AuthCheck)) ::
              Menu(Loc("Recent Changes", "recentchanges" :: Nil, "Recent Changes", recentchangesTempl)) ::
              Menu(Loc("New Conversation", "newconversation" :: Nil, "New Conversation", newconTempl, User.IfLoggedIn)) ::
              Menu(Loc("WS Export", "wsexport" :: Nil, "WS Export",  Hidden, wsexportTempl, AuthCheck)) ::
              Menu(Loc("Admin", "admin" :: "index" :: Nil, "Admin", User.IfAdmin), adminMenus : _*) ::
      Menu(Loc("Static", Link(List("static"), true, "/static/index"), 
	       "Static Content")) ::
              Nil) ::: User.menus;
    //TODO add other menus as needed

    // set the sitemap.  Note if you don't want access control for
    // each page, just comment this line out.
    LiftRules.setSiteMap(SiteMap(menus:_*))


    //Rewrites
    LiftRules.statefulRewrite.append {
      case RewriteRequest(ParsePath("conversation" :: conversationId :: Nil, _, _, _), _, _) => {
        val map = Map[String,String]("id" -> conversationId);
        RewriteResponse("conversation" :: Nil, map)
      }
      case RewriteRequest(ParsePath("node" :: conversationId :: Nil, _, _, _), _, _) => {
        val map = Map[String,String]("id" -> conversationId);
        RewriteResponse("node" :: Nil, map)
      }
      case RewriteRequest(ParsePath("tag" :: conversationId :: Nil, _, _, _), _, _) => {
        val map = Map[String,String]("id" -> conversationId);
        RewriteResponse("tag" :: Nil, map)
      }
      case RewriteRequest(ParsePath("wsexport" :: conversationId :: Nil, _, _, _), _, _) => {
        val map = Map[String,String]("id" -> conversationId);
        RewriteResponse("wsexport" :: Nil, map)
      }
      case RewriteRequest(ParsePath("admin" :: "user" :: "add" :: Nil, _, _, _), _, _) => {
        RewriteResponse("admin" :: "user" :: "edit" :: Nil)
      }
      case RewriteRequest(ParsePath("admin" :: "user" :: "edit" :: uniqueId :: Nil, _, _, _), _, _) => {
        val map = Map[String,String]("uniqueId" -> uniqueId);
        RewriteResponse("admin" :: "user" :: "edit" :: Nil, map)
      }
    }


    //Rewrites
    LiftRules.statelessDispatchTable.append {
      case Req("wsexport" :: conversationId :: Nil, _, GetRequest) =>
        () => {  IBISConversation.find(By(IBISConversation.id, conversationId.toLong)) match {
          case Full(conversation) => {
            val bytes = ConversationHelper.exportConversation(conversation).getBytes
            Full(StreamingResponse(
              new java.io.ByteArrayInputStream(bytes),
              () => {},
              bytes.length,
              ("Content-type" -> "application/json") :: ("Content-length" -> bytes.length.toString) :: ("Content-disposition" -> ("attachment; filename=conversation" + conversationId.toString + ".json")) :: Nil,
              Nil,
              200
            ))
          }
          case _ => Full(NotFoundResponse())
        }
      }
    }


    // Use jQuery 1.4
    LiftRules.jsArtifacts = net.liftweb.http.js.jquery.JQuery14Artifacts

    //Show the spinny image when an Ajax call starts
    LiftRules.ajaxStart =
            Full(() => LiftRules.jsArtifacts.show("ajax-loader").cmd)

    // Make the spinny image go away when it ends
    LiftRules.ajaxEnd =
            Full(() => LiftRules.jsArtifacts.hide("ajax-loader").cmd)

    // Force the request to be UTF-8
    LiftRules.early.append(_.setCharacterEncoding("UTF-8"))

    // What is the function to test if a user is logged in?
    LiftRules.loggedInTest = Full(() => User.loggedIn_?)

    // Make a transaction span the whole HTTP request
    //   S.addAround(DB.buildLoanWrapper)

    //Messages auto fade
    LiftRules.noticesAutoFadeOut.default.set((noticeType: NoticeType.Value) => Full((3 seconds, 2 seconds)))

    //Serve images
    LiftRules.dispatch.append(ImageDispatcher.matcher)

  }
}
