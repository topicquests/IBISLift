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
import org.topicquests.db.DBVendorMySQL
import org.topicquests.dispatcher.ImageDispatcher

//import code.model._
import org.topicquests._
import model._
import snippet._

/**
 * A class that's instantiated early and run.  It allows the application
 * to modify lift's environment
 */
class Boot extends Loggable {
  def boot {

    //Set the template format to XHTML and the Output to HTML5
    //LiftRules.htmlProperties.default.set((r: Req) => new XHtmlInHtml5OutProperties(r.userAgent))
    // Use HTML5 for rendering
    LiftRules.htmlProperties.default.set((r: Req) => new Html5Properties(r.userAgent))

    //create the database
    println("STARTING "+Props.get("db.type"))
    //Property options
    //db.type = h2
    //db.type = mysql
    var dbtype = Props.get("db.type").open_!

    if (dbtype.equals("h2")) {
      val vendor =
      new StandardDBVendor(Props.get("db.driver") openOr "org.h2.Driver",
        Props.get("db.url") openOr
                "jdbc:h2:lift_proto.db;AUTO_SERVER=TRUE",
        Props.get("db.user"), Props.get("db.password"))

      LiftRules.unloadHooks.append(vendor.closeAllConnections_! _)

      DB.defineConnectionManager(DefaultConnectionIdentifier, vendor)
    } else if (dbtype.equals("mysql")) {
      //Sets the MySQL connection
      DB.defineConnectionManager(DefaultConnectionIdentifier, DBVendorMySQL);
    } else {
      println("We're doomed! No database of type: "+dbtype)
    }

    // Use Lift's Mapper ORM to populate the database
    // you don't need to use Mapper to use Lift... use
    // any ORM you want
    Schemifier.schemify(true, Schemifier.infoF _,
      User,
      Node,
      IBISConversation,
      ChatLine,
      Image)

    //create admin user
    initUsers()

    def initUsers() {
      // using properties appears to work
      val umail: String = Props.get("admin.email").open_!
      val upwd: String = Props.get("admin.pwd").open_!
      //println("BOOTING "+umail+" "+upwd)
      if (isSuperUserExist == false) {

        val superUser = User.create
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

    // Build SiteMap
    def sitemap = SiteMap(
      Menu.i("Home") / "index" >> User.AddUserMenusAfter, // Menu.i the simple way to declare a menu
      Menu.i("conversation") / "conversation" / **   >> Hidden >> conB, //If(() => User.loggedIn_?, conA , conB)
      //this paints a New Conversation menu item if and only if a user is logged in
      Menu.i("New Conversation") / "newconversation" >> newconTempl >> Unless(() => !User.loggedIn_?, () => RedirectResponse("index"))
      //TODO add other menus as needed
      )

    //   println("XXXX "+LiftRules.ajaxPath) // defaults to "ajax_request"
    def sitemapMutators = User.sitemapMutator

    // set the sitemap.  Note if you don't want access control for
    // each page, just comment this line out.
    LiftRules.setSiteMapFunc(() => sitemapMutators(sitemap))

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
    S.addAround(DB.buildLoanWrapper)

    //Messages auto fade
    LiftRules.noticesAutoFadeOut.default.set((noticeType: NoticeType.Value) => Full((3 seconds, 2 seconds)))

    //Serve images
    LiftRules.dispatch.append(ImageDispatcher.matcher)

  }
}
 