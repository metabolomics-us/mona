package edu.ucdavis.fiehnlab.mona.backend.core.workflow.graph

import org.junit.runner.RunWith
import org.scalatest.WordSpec
import org.springframework.test.context.junit4.SpringRunner

/**
  * Created by wohlgemuth on 3/14/16.
  */
@RunWith(classOf[SpringRunner])
class GraphTest extends WordSpec {

  case class Vertex(id: String) extends AbstractVertex[String]

  "when we build a graph" when {

    "we must be able to add nodes" must {

      val graph = new Graph[String, Vertex, Edge]

      graph.addNode(Vertex("a"))
      graph.addNode(Vertex("b"))
      graph.addNode(Vertex("c"))
      graph.addNode(Vertex("d"))
      graph.addNode(Vertex("e"))
      graph.addNode(Vertex("f"))
      graph.addNode(Vertex("g"))


      "which need to be connected" in {

        graph.addEdge(Edge("a", "b"))
        graph.addEdge(Edge("a", "c"))
        graph.addEdge(Edge("b", "c"))
        graph.addEdge(Edge("c", "d"))
        graph.addEdge(Edge("c", "e"))
        graph.addEdge(Edge("b", "d"))
        graph.addEdge(Edge("e", "f"))
        graph.addEdge(Edge("e", "g"))
        graph.addEdge(Edge("d", "f"))


      }

      "we need to be able to find 2 children for a" in {
        assert(graph.getChildren("a").size == 2)
      }
      "we need to be able to find 2 parent for c" in {
        assert(graph.getParents("c").size == 2)
      }

      "we need to be able to find 2 parents for d" in {
        assert(graph.getParents("d").size == 2)
      }

      "we need to be able to find 2 children for e" in {
        assert(graph.getChildren("e").size == 2)
      }


      "we need to be able to find 2 parents for f" in {
        assert(graph.getParents("f").size == 2)
      }


      "we need to be able to find 0 children for f" in {
        assert(graph.getChildren("f").size == 0)
      }


      "we need to be able to find 0 children for g" in {
        assert(graph.getChildren("g").size == 0)
      }

      "the head should be 'a'" in {
        assert(graph.heads.size == 1)
      }

      "there should be 2 tails" in {
        assert(graph.tails.size == 2)
      }
    }
  }
}
