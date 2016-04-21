package edu.ucdavis.fiehnlab.mona.backend.core.workflow.viz

import java.io.FileOutputStream

import edu.ucdavis.fiehnlab.mona.backend.core.workflow.graph.{AbstractVertex, Edge, Graph}
import org.scalatest.WordSpec

/**
  * just tests if the file can be written, no guarantee for correctness
  */
class DotVisualizerTest extends WordSpec {

  case class Vertex(id: String) extends AbstractVertex[String]

  "DotVisualizerTest" should {

    "visualize" in {

      val graph = new Graph[String, Vertex, Edge]

      graph.addNode(Vertex("a"))
      graph.addNode(Vertex("b"))
      graph.addNode(Vertex("c"))
      graph.addNode(Vertex("d"))
      graph.addNode(Vertex("e"))
      graph.addNode(Vertex("f"))
      graph.addNode(Vertex("g"))

      graph.addEdge(Edge("a","b"))
      graph.addEdge(Edge("a","c"))
      graph.addEdge(Edge("b","c"))
      graph.addEdge(Edge("c","d"))
      graph.addEdge(Edge("c","e"))
      graph.addEdge(Edge("b","d"))
      graph.addEdge(Edge("e","f"))
      graph.addEdge(Edge("e","g"))
      graph.addEdge(Edge("d","f"))


      val viz = new DotVisualizer

      val out = new FileOutputStream("graph.txt")
      viz.visualize[String](graph,out)
      out.close()
    }

  }
}
