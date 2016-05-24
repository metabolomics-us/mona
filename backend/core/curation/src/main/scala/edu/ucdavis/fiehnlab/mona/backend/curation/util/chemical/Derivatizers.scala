package edu.ucdavis.fiehnlab.mona.backend.curation.util.chemical

/**
  * Created by sajjan on 5/01/16.
  */
class Derivatizers {
  def getDerivatizers: Array[Derivatizer] = Array(new TMSDerivatizer)
}
