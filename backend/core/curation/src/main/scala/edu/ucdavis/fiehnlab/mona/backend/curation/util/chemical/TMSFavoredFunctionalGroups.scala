package edu.ucdavis.fiehnlab.mona.backend.curation.util.chemical

import org.openscience.cdk.interfaces.IAtomContainer

/**
  * Created by sajjan on 4/25/16.
  */
object TMSFavoredFunctionalGroups {
  /**
    * Builds a list of favored groups in there order of likelihood
    *
    * @return
    */
  def buildFavoredGroupsInOrder(): List[IAtomContainer] = {
    List(
      FunctionalGroupBuilder.makeHydroxyGroup(),
      FunctionalGroupBuilder.makePhosphate(),
      FunctionalGroupBuilder.makeThiol(),
      FunctionalGroupBuilder.makePrimaryAmine(),
      FunctionalGroupBuilder.makeSecondaryAmine()
    )
  }
}