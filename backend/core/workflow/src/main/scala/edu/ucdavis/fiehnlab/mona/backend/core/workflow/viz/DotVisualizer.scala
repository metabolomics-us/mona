package edu.ucdavis.fiehnlab.mona.backend.core.workflow.viz

import java.io.{OutputStream, PrintStream}

import edu.ucdavis.fiehnlab.mona.backend.core.workflow.graph.{AbstractEdge, AbstractVertex, Graph}

/**
  * this tool is visualizing the given graph as a dot file to the given output stream
  */
class DotVisualizer {

  def visualize[ID](graph:Graph[ ID, _ <: AbstractVertex[ID], _ <: AbstractEdge[ID]], outputStream: OutputStream): Unit = {
   val writer = new PrintStream(outputStream)

    writer.println("digraph graphname {")

    graph.nodes.foreach{ x:AbstractVertex[ID] =>
      writer.print(x.id)
      writer.println(s""" [ label = "${x.toString}" ]""")
    }

    writer.println()

    graph.getEdges.foreach{x:AbstractEdge[ID] =>
      writer.print(x.from)
      writer.print(" -> ")
      writer.println(x.to)
    }

    writer.println("}")

    writer.flush()
  }
}

