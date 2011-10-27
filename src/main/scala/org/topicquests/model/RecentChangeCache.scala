/**
 * 
 */
package org.topicquests.model
import scala._
import org.topicquests.model.{Node => tNode}
/**
 * @author park
 * <p>A RingBuffer of Node objects for tracking recent changes.</p>
 * <p>Based loosely on RingBuffer.scala</p>
 * @see https://github.com/twitter/util/blob/master/util-core/src/main/scala/com/twitter/util/RingBuffer.scala
 */
object RecentChangeCache  {
  private var maxSize:Int = 0
	private var array: Array[tNode] = null
	private var read = 0
	private var write = 0
	private var count_ = 0
    def length = count_
    def size = count_
    
    def setSize(max_size: Int) {
    	if (maxSize == 0) {
    		maxSize = max_size
    		array = new Array[tNode](maxSize)
    	}
   }

    var lx = System.currentTimeMillis()
    /**
     * Clear the buffer
     */
    def clear() {
		read = 0
		write = 0
		count_ = 0
	}
	
	/**
	 * Add a Node to the buffer; using a synchronized block
	 * to allow concurrent things to go on here
	 */
	def add(elem: tNode) {
	  println("ADDING "+elem)
	  array.synchronized {
	    array(write) = elem
	    write = (write + 1) % maxSize
	    if (count_ == maxSize) read = (read + 1) % maxSize
	    else count_ += 1
	    println("ADDED "+count_ +" "+lx)
	  }
	}

	/**
	 * Get an iterator to walk through the buffer
	 */
	def iterator = new Iterator[tNode] {
	  println("ITERATOR "+count_ +" "+lx)
	  //ideally, this is where you would place a synchronized block
	  //but it mucks up the compiler so the internal definitions 
	  //cannot be seen
		var idx = 0
		override def hasNext = idx != count_
		override def next = {
			val res = apply(idx)
			idx += 1
			println("NEXT "+res)
			res
		}
	}
	
	def apply(i: Int): tNode = {
	  //this is not really where we should have a synchronized block.
	  //with it here, it's possible to have modifications to arrah
	  //while the iterator is running.
	  //don't try this at home.
	  array.synchronized {
		if (i >= count_) throw new IndexOutOfBoundsException(i.toString)
		else array((read + i) % maxSize)
	  }
	}

}