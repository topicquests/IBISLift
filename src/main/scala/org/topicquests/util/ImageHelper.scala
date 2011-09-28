package org.topicquests.util


import java.awt.geom.AffineTransform
import org.apache.sanselan.ImageReadException
import org.apache.sanselan.Sanselan
import org.apache.sanselan.ImageFormat
import org.apache.sanselan.common.IImageMetadata
import org.apache.sanselan.common.RationalNumber
import org.apache.sanselan.formats.jpeg.JpegImageMetadata
import org.apache.sanselan.formats.tiff.TiffField
import org.apache.sanselan.formats.tiff.TiffImageMetadata
import org.apache.sanselan.formats.tiff.constants.TagInfo
import org.apache.sanselan.formats.tiff.constants.TiffConstants
import org.apache.sanselan.formats.tiff.constants.TiffTagConstants
import java.io.{InputStream,ByteArrayOutputStream,ByteArrayInputStream}
import net.liftweb.util.IoHelpers
import java.awt._
import java.awt.image._
import collection.mutable.ListBuffer
import javax.imageio.{ImageReader, ImageIO}
import java.net.{URLConnection, URL}
import net.liftweb.common.{Empty, Full, Box}

/**
 * @author dfernandez
 * @license Apache2.0
 */

 object ImageOutFormat extends Enumeration("png", "jpg", "ico"){
  val png,jpeg,ico = Value
}

case class ImageWithMetaData(buffImageimage:BufferedImage, orientation:Option[Int], format:ImageOutFormat.Value)

object ImageHelper {

  //Just png/gif/jpg
  def getImageFromURL(urlStr: String): Box[BufferedImage] = {
    var in : InputStream = null;
    try {
      val url = new URL(urlStr);
      val img  = ImageIO.read(url);
      Full(img);
    }
    catch{
      case e:Exception => {Empty}
    }
    finally {
      if (in != null) in.close();
    }
  }

  def getBytesFromURL(urlStr: String): Box[Array[Byte]] = {
    val str = new StringBuffer();
    var in : InputStream = null;
    try {
      val url = new URL(urlStr);
      val in = url.openStream();
      Full(Stream.continually(in.read).takeWhile(-1 !=).map(_.toByte).toArray)
    }
    catch{
      case e:Exception => {Empty}
    }
    finally {
      if (in != null) in.close();
    }
  }

  def getImageSizeFromURL(urlStr: String): Box[Dimension] = {
    val imgbox = getImageFromURL(urlStr);
    if(imgbox.isDefined){
      val img = imgbox.open_!;
      Full(new Dimension(img.getWidth, img.getHeight));
    }
    else
      Empty;
  }


  def deepCopy(bi: BufferedImage): BufferedImage = {
    val cm = bi.getColorModel();
    val isAlphaPremultiplied = cm.isAlphaPremultiplied();
    val raster = bi.copyData(null);
    return new BufferedImage(cm, raster, isAlphaPremultiplied, null);
  }


  def createBresenhamLine(g: Graphics, beforeProcessPoint: (Int, Int, Int) => Unit, afterProcessPoint: (Int, Int, Int) => Unit, x0: Int, y0: Int, x2: Int, y2: Int, size: Int, erase: Boolean){
    var x = x0;
    var y = y0;
    var w = x2 - x ;
    var h = y2 - y ;
    var dx1 = 0;
    var dy1 = 0;
    var dx2 = 0;
    var dy2 = 0 ;
    if (w<0) dx1 = -1 ; else if (w>0) dx1 = 1 ;
    if (h<0) dy1 = -1 ; else if (h>0) dy1 = 1 ;
    if (w<0) dx2 = -1 ; else if (w>0) dx2 = 1 ;
    var longest = scala.math.abs(w) ;
    var shortest = scala.math.abs(h) ;
    if (!(longest>shortest)) {
      longest = scala.math.abs(h) ;
      shortest = scala.math.abs(w) ;
      if (h<0) dy2 = -1 ; else if (h>0) dy2 = 1 ;
      dx2 = 0 ;
    }
    var numerator = longest >> 1 ;
    for (i <- 0 to longest - 1){
      beforeProcessPoint(x,y, size);
      if(erase)
        g.clearRect(x,y,size,size);
      else
        g.fillRect(x,y,size,size);
      afterProcessPoint(x,y, size);
      numerator += shortest ;
      if (!(numerator<longest)) {
        numerator -= longest ;
        x += dx1 ;
        y += dy1 ;
      } else {
        x += dx2 ;
        y += dy2 ;
      }
    }
  }

  def imageToBufferedImage(image:Image, width:Int, height:Int):BufferedImage = {
    val dest = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    val g2 = dest.createGraphics();
    g2.drawImage(image, 0, 0, null);
    g2.dispose();
    dest;
  }

  def transformGrayToTransparency(image: BufferedImage): Image = {
    val filter = new RGBImageFilter(){
      override def filterRGB(x:Int, y:Int, rgb:Int): Int = {
        ((rgb << 8) & 0xFF000000);
      }
    };
    val ip = new FilteredImageSource(image.getSource(), filter);
    Toolkit.getDefaultToolkit().createImage(ip);
  }

  def getOrientation(imageBytes:Array[Byte]):Option[Int] = Sanselan.getMetadata(imageBytes) match {
    case metaJpg: JpegImageMetadata =>
      val exifValue = metaJpg.findEXIFValue(TiffTagConstants.TIFF_TAG_ORIENTATION)
      if (exifValue != null) Some(exifValue.getIntValue) else None
    case _ => None
  }

  def getImageFromStream(is:java.io.InputStream):ImageWithMetaData = {
    val imageBytes = IoHelpers.readWholeStream(is)
    val orientation = getOrientation(imageBytes)
    val format = Sanselan.guessFormat(imageBytes) match {
      case ImageFormat.IMAGE_FORMAT_JPEG => ImageOutFormat.jpeg
      case _ => ImageOutFormat.png
    }
    ImageWithMetaData(ImageIO.read(new java.io.ByteArrayInputStream(imageBytes)), orientation, format)
  }

  def getImageFromByteArray(imageBytes:Array[Byte]):ImageWithMetaData = {
    val orientation = getOrientation(imageBytes)
    val format = Sanselan.guessFormat(imageBytes) match {
      case ImageFormat.IMAGE_FORMAT_JPEG => ImageOutFormat.jpeg
      case _ => ImageOutFormat.png
    }
    ImageWithMetaData(ImageIO.read(new java.io.ByteArrayInputStream(imageBytes)), orientation, format)
  }

  def imageToStream(format:ImageOutFormat.Value, image:BufferedImage):InputStream = {
    val outputStream = new ByteArrayOutputStream()
    ImageIO.write(image, format.toString, outputStream)
    new ByteArrayInputStream(outputStream.toByteArray)
  }

  def imageToByteArray(format:ImageOutFormat.Value, image:BufferedImage):Array[Byte] = {
    val outputStream = new ByteArrayOutputStream()
    ImageIO.write(image, format.toString, outputStream)
    outputStream.toByteArray
  }

  /**
   * Resize to a square
   * Will preserve the aspect ratio of the original and than center crop the larger dimension.
   * A image of (200w,240h) squared to (100) will first resize to (100w,120h) and then take then crop
   * 10 pixels from the top and bottom of the image to produce (100w,100h)
   */
  def square(orientation:Option[Int], originalImage:BufferedImage, max:Int):BufferedImage = {
    val image = {
      val height = originalImage.getHeight
      val width = originalImage.getWidth
      val ratio:Double = width.doubleValue/height

      //set smaller dimension to the max
      val (scaledWidth, scaledHeight) = if (width < height) {
        (max,(max.doubleValue/ratio).intValue)
      } else {
        ((max.doubleValue*ratio).intValue, max)
      }
      resize(orientation, originalImage, scaledWidth, scaledHeight)
    }

    def halfDiff(dim:Int):Int = (dim-max)/2

    if (image.getHeight > max) {
      image.getSubimage(0,halfDiff(image.getHeight), image.getWidth, max)
    } else if (image.getWidth > max) {
      image.getSubimage(halfDiff(image.getWidth),0, max, image.getHeight)
    } else image
  }


  def scaledMaxDim(width:Int, height:Int , maxWidth:Int, maxHeight:Int):(Int,Int) = {
    val ratio:Double = width.doubleValue/height

    val scaleW = (maxWidth, (maxWidth.doubleValue/ratio).intValue)
    val scaleH = ((maxHeight.doubleValue*ratio).intValue,maxHeight)

    if (width > height && scaleW._2 <= maxHeight)
      scaleW
    else if (scaleH._1 <= maxWidth)
      scaleH
    else scaleW
  }

  /**
   * Resize to maximum dimension preserving the aspect ratio. This is basically equivalent to what you would expect by setting
   * "max-width" and "max-height" CSS attributes but will scale up an image if necessary
   */
  def max(orientation:Option[Int],originalImage:BufferedImage, maxWidth:Int, maxHeight:Int):BufferedImage = {
    val (scaledWidth, scaledHeight) = scaledMaxDim(originalImage.getWidth, originalImage.getHeight, maxWidth, maxHeight)
    resize(orientation, originalImage, scaledWidth, scaledHeight)
  }

  /**
   * Algorithm adapted from example in Filthy Rich Clients http://filthyrichclients.org/
   * Resize an image and account of its orientation. This will not preserve aspect ratio.
   */
  def resize(orientation:Option[Int], img:BufferedImage, targetWidth:Int, targetHeight:Int): BufferedImage = {
    val imgType = if (img.getTransparency() == Transparency.OPAQUE) BufferedImage.TYPE_INT_RGB else BufferedImage.TYPE_INT_ARGB
    var ret = img
    var scratchImage:BufferedImage = null
    var g2:Graphics2D = null
    var w = img.getWidth
    var h = img.getHeight
    var prevW = ret.getWidth
    var prevH = ret.getHeight

    val isTranslucent:Boolean = img.getTransparency != Transparency.OPAQUE

    //If we're resizing down by more than a factor of two, resize in multiple steps to preserve image quality
    do {
      if (w > targetWidth) {
        w /= 2
        if (w < targetWidth) {
          w = targetWidth
        }
      } else w = targetWidth

      if (h > targetHeight) {
        h /= 2
        if (h < targetHeight) {
          h = targetHeight
        }
      } else h = targetHeight

      if (scratchImage == null || isTranslucent) {
        scratchImage = new BufferedImage(w, h, imgType);
        g2 = scratchImage.createGraphics
      }
      g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR)
      g2.drawImage(ret, 0, 0, w, h, 0, 0, prevW, prevH, null)
      prevW = w
      prevH = h

      ret = scratchImage
    } while (w != targetWidth || h != targetHeight)

    if (g2 != null) {
      g2.dispose
    }

    // If we used a scratch buffer that is larger than our target size,
    // create an image of the right size and copy the results into it
    // If there is an orientation value other than the default, rotate the image appropriately
    if (targetWidth != ret.getWidth || targetHeight != ret.getHeight || orientation.map(_ != 1).getOrElse(false)) {

      val (tW, tH, rotFunc) = orientation match {
        case Some(3) => // 3 => 180 (upside down)
          (targetWidth, targetHeight, (g2:Graphics2D) => {
            g2.rotate(scala.math.Pi)
            g2.translate(-targetWidth, -targetHeight)
          })
        case Some(6) => // 6 => -90 (counter clockwise)
          (targetHeight, targetWidth, (g2:Graphics2D) => {
            g2.rotate(scala.math.Pi/2)
            g2.translate(0, -targetHeight)
          })
        case Some(8) => // 8 => 90 (clockwise)
          (targetHeight, targetWidth, (g2:Graphics2D) => {
            g2.rotate(-scala.math.Pi/2)
            g2.translate(-targetWidth, 0)
          })
        case _ => (targetWidth, targetHeight, (g2:Graphics2D) => {})
      }
      scratchImage = new BufferedImage(tW, tH, imgType)
      g2 = scratchImage.createGraphics
      rotFunc(g2)
      g2.drawImage(ret, 0, 0, null)
      g2.dispose
      ret = scratchImage
    }

    ret
  }

} //ImageHelper