package edu.ucdavis.fiehnlab.mona.backend.core.workflow.graph

import edu.ucdavis.fiehnlab.mona.backend.core.domain.Types.Spectrum
import org.springframework.batch.item.ItemProcessor
import sun.security.provider.certpath.Vertex

import scala.collection.concurrent.TrieMap
import scala.reflect.ClassTag

/**
  * defines a vertex
  */
trait AbstractVertex[ID] {
  val id: ID

}

/**
  * defines an edge
  */
trait AbstractEdge[ID] {
  val from: ID
  val to: ID
}

/**
  * the basic graph to work with
  *
  * @tparam ID
  * @tparam Vertex
  * @tparam Edge
  */
class Graph[ID, Vertex <: AbstractVertex[ID]:ClassTag, Edge <: AbstractEdge[ID]] {

  /**
    * utilized index
    */
  private val nodeIndex = new TrieMap[ID, Vertex]

  /**
    * contains all our edges
    */
  private val edges = scala.collection.mutable.Set[AbstractEdge[ID]]()

  /**
    * returns all the nodes of the graph
    * @return
    */
  def nodes:Iterable[Vertex] = nodeIndex.values
  /**
    * size of our graph
    * @return
    */
  def size:Int = nodeIndex.size
  /**
    * adding a node at the given vertex
    *
    * @param vertex
    * @return
    */
  def addNode(vertex: Vertex): Graph[ID, Vertex, Edge] = {
    nodeIndex += ((vertex.id, vertex))
    this
  }

  def addEdge(edge: AbstractEdge[ID]): Graph[ID, Vertex, Edge] = {
    edges += edge
    this
  }

  /**
    * return the node with the given id
    *
    * @param id
    * @return
    */
  def getNode(id: ID): Option[Vertex] = nodeIndex.get(id)

  /**
    * do we have a child
    *
    * @param vertex
    * @return
    */
  def hasChild(vertex: Vertex): Boolean = !getChildren(vertex).isEmpty

  /**
    * finds all the children
    *
    * @param vertex
    * @return
    */
  def getChildren(vertex: Vertex): Set[Vertex] = {

    edges.filter(_.from == vertex.id).collect {
      case x: AbstractEdge[ID] => getNode(x.to).get
    }.toArray[Vertex].toSet[Vertex]

  }

  /**
    * returns the heads of the graph, obviously
    * @return
    */
  def heads : Set[Vertex] = {
    nodeIndex.keys.filter(getParents(_).isEmpty).collect {
      case x: ID => getNode(x).get
    }.toArray[Vertex].toSet[Vertex]
  }

  /**
    * returns all the tails of the graph
    * @return
    */
  def tails : Set[Vertex] = {

    nodeIndex.keys.filter(getChildren(_).isEmpty).collect {
      case x: ID => getNode(x).get
    }.toArray[Vertex].toSet[Vertex]

  }

  /**
    * returns all the children for this id
    * @param id
    * @return
    */
  def getChildren(id:ID) : Set[Vertex] = getChildren(getNode(id).get)

  /**
    * return all the parents for this id
    * @param id
    * @return
    */
  def getParents(id:ID) : Set[Vertex] = getParents(getNode(id).get)

  /**
    * returns all this parents for this vertex
    * @param vertex
    * @return
    */
  def getParents(vertex: Vertex): Set[Vertex] = {
    edges.filter(_.to == vertex.id).collect {
      case x: AbstractEdge[ID] => getNode(x.from).get
    }.toArray.toSet
  }
}


/**
  * helper class
  *
  * @param name
  */
case class ProcessingStep(val name: String, val processor: ItemProcessor[Spectrum, Spectrum], val description:String)


/**
  * defined node
  *
  * @param id
  * @param step
  * @param description
  */
case class Node(id: String, step: ProcessingStep, description: String) extends AbstractVertex[String]

/**
  * relation between nodes
  *
  * @param from
  * @param to
  */
case class Edge(from: String, to: String) extends AbstractEdge[String]
