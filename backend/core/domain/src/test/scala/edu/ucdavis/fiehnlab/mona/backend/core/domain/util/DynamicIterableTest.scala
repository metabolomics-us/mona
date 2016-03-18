package edu.ucdavis.fiehnlab.mona.backend.core.domain.util

import com.typesafe.scalalogging.LazyLogging
import org.scalatest.WordSpec
import org.springframework.data.domain.{PageImpl, Page, Pageable}
import scala.collection.JavaConverters._
/**
  * Created by wohlgemuth on 3/18/16.
  */
class DynamicIterableTest extends WordSpec with LazyLogging{

  "a dynamice iteratable when given a list " when {

    val data = List(1,2,3,4,5,6,7,8,9,10)


    "should be able to fetch all it's content" should {

      val dynamic = new DynamicIterable[Int,String]("",1) {

        val temp = data.iterator

        /**
        * loads more data from the server for the given query
        */
      override def fetchMoreData(query: String, pageable: Pageable): Page[Int] = {
          if(temp.hasNext) {
            new PageImpl[Int](List(temp.next()).asJava, pageable, 10)
          }
          else{
            new PageImpl[Int](List.empty[Int].asJava,pageable,10)
          }
      }
    }

      "transparently from the external implementation" in {

        val it = dynamic.iterator

        var count = 0

        while(it.hasNext){
          logger.debug(s"fetched: ${it.next()}")
          count = count + 1

        }

        assert(count == 10)
      }
    }
  }
}
