package edu.ucdavis.fiehnlab.mona.backend.core.workflow.graph
import scala.collection.concurrent.TrieMap

/*
 * trait to be mixed in by user code for making their nodes graph friendly
 */
trait AbstractVertex[ID] {
  val id: ID
}

/*
 * trait to be mixed in by user code for making their nodes graph friendly
*/
trait AbstractEdge[ID] {
  val id1: ID
  val id2: ID
}

class Graph[ID, Vertex <: AbstractVertex[ID], Edge <: AbstractEdge[ID]] {

  private val nodeIndex = new TrieMap[ID, Vertex]
  private val edgeIndex = new TrieMap[ID, List[Edge]]

  def addNode(vertex: Vertex): Graph[ID, Vertex, Edge] = {
    nodeIndex += ((vertex.id, vertex))
    this
  }

  def getNode(id: ID): Option[Vertex] = nodeIndex.get(id)

}

object Test extends App {

  case class Node(id: Int,
                  name: String,
                  kind: String) extends AbstractVertex[Int]

  case class Relation(id1: Int,
                      Kind: String,
                      id2: Int) extends AbstractEdge[Int]

  new Graph[Int, Node, Relation].addNode(Node(3, "foo", "bar")).getNode(3).get

}