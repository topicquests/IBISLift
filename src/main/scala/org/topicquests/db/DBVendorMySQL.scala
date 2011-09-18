package org.topicquests.db

import net.liftweb.mapper.{ConnectionManager, ConnectionIdentifier}
import java.sql.{Connection, DriverManager}
import net.liftweb.common.{Full, Empty}
import net.liftweb.util.Props

/**
 * @author dfernandez
 *
 */

object DBVendorMySQL extends ConnectionManager {

  //Loads the connextion properties
  val server = Props.get("db.server").openOr("");
  val port = Props.get("db.port").openOr("");
  val database = Props.get("db.database").openOr("");
  val charset = Props.get("db.charset").openOr("");
  val user = Props.get("db.user").openOr("");
  val pass = Props.get("db.pass").openOr("");

  //Force to load the driver
  Class.forName("com.mysql.jdbc.Driver");

  //New connection method
  def newConnection(name: ConnectionIdentifier) = {
    try{
      Full(DriverManager.getConnection("jdbc:mysql://" + this.server + ":" + this.port + "/" + this.database + "?useEncoding=true&amp;characterEncoding=UTF-8", this.user, this.pass))
    }
    catch {
      case e : Exception => e.printStackTrace; Empty
    }
  }

  //Release connection method
  def releaseConnection(conn: Connection){ conn.close }
}