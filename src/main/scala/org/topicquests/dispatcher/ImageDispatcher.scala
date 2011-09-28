package org.topicquests.dispatcher


import net.liftweb.http._
import net.liftweb.util.Helpers._
import net.liftweb.common.{Box, Full}
import net.liftweb.mapper.By
import org.topicquests.model.Image

/**
 * @author dfernandez
 * @license Apache2.0
 */


object ImageDispatcher {

  //Loads the image with the Id
  object DBImage {
    def unapply(id: String): Option[Image] = {
      val boximg = Image.find(By(Image.uniqueId, id.trim))
      if(boximg.isDefined)
        Full(boximg.open_!);
      else
        None;
    }
  }

  //Matches the request
  def matcher: LiftRules.DispatchPF = {
    case req @ Req("image" :: DBImage(img) :: Nil, _, GetRequest) =>
      () => serveImage(img, req)
  }

  //Serves the image with a InMemoryResponse
  def serveImage(image: Image, req: Req) : Box[LiftResponse] = {
    //TODO: see how to avoid serving image twice
      Full(InMemoryResponse(
        image.image.is,
        List("Last-Modified" -> toInternetDate(image.saveTime),
          "Content-Type" -> image.mimeType.is,
          "Content-Length" -> image.image.is.length.toString),
        Nil /*cookies*/,
        200)
        )
  }

}