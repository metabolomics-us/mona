package edu.ucdavis.fiehnlab.mona.backend.core.workflow.graph

import edu.ucdavis.fiehnlab.mona.backend.core.workflow.exception.NameAlreadyRegisteredException
import org.springframework.batch.item.ItemProcessor

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
class Graph[ID, Vertex <: AbstractVertex[ID] : ClassTag, Edge <: AbstractEdge[ID]] {

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
    *
    * @return
    */
  def nodes: Iterable[Vertex] = nodeIndex.values

  /**
    * access to all our edges
    *
    * @return
    */
  def getEdges: List[AbstractEdge[ID]] = edges.toList

  /**
    * size of our graph
    *
    * @return
    */
  def size: Int = nodeIndex.size

  /**
    * adding a node at the given vertex
    *
    * @param vertex
    * @return
    */
  def addNode(vertex: Vertex): Graph[ID, Vertex, Edge] = {
    getNode(vertex.id) match {
      case None =>
        nodeIndex += ((vertex.id, vertex))
        this
      case _ =>
        throw new NameAlreadyRegisteredException(s"a node with the id '${vertex.id}' was already registered, please use unique names")
    }
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
  def hasChild(vertex: Vertex): Boolean = getChildren(vertex).nonEmpty

  /**
    * finds all the children
    *
    * @param vertex
    * @return
    */
  def getChildren(vertex: Vertex): Set[Vertex] = {
    edges.filter(_.from == vertex.id).map(x => getNode(x.to).get).toArray[Vertex].toSet[Vertex]
  }

  /**
    * returns the heads of the graph, obviously
    *
    * @return
    */
  def heads: Set[Vertex] = {
    nodeIndex.keys.filter(getParents(_).isEmpty).map(x => getNode(x).get).toArray[Vertex].toSet[Vertex]
  }

  /**
    * returns all the tails of the graph
    *
    * @return
    */
  def tails: Set[Vertex] = {
    nodeIndex.keys.filter(getChildren(_).isEmpty).map(x => getNode(x).get).toArray[Vertex].toSet[Vertex]
  }

  /**
    * returns all the children for this id
    *
    * @param id
    * @return
    */
  def getChildren(id: ID): Set[Vertex] = getChildren(getNode(id).get)

  /**
    * return all the parents for this id
    *
    * @param id
    * @return
    */
  def getParents(id: ID): Set[Vertex] = getParents(getNode(id).get)

  /**
    * returns all this parents for this vertex
    *
    * @param vertex
    * @return
    */
  def getParents(vertex: Vertex): Set[Vertex] = {
    edges.filter(_.to == vertex.id).map(x => getNode(x.from).get).toArray.toSet
  }
}


/**
  * helper class
  *
  * @param name the name must be unique across all steps!
  */
case class ProcessingStep[INPUT, OUTPUT](name: String, processor: ItemProcessor[INPUT, OUTPUT], description: String)


/**
  * defined node
  *
  * @param id
  * @param step
  * @param description
  */
case class Node[INPUT, OUTPUT](id: String, step: ProcessingStep[INPUT, OUTPUT], description: String) extends AbstractVertex[String]

/**
  * relation between nodes
  *
  * @param from
  * @param to
  */
case class Edge(from: String, to: String) extends AbstractEdge[String]
