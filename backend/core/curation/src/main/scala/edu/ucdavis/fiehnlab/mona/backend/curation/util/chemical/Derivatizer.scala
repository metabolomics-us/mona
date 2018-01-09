package edu.ucdavis.fiehnlab.mona.backend.curation.util.chemical

import org.openscience.cdk.interfaces.IAtomContainer

/**
  * Created by sajjan on 5/19/16.
  */
trait Derivatizer {
  def derivatize(molecule: IAtomContainer,
                 functionalGroups: Array[IAtomContainer],
                 ignoreImpossibleCompounds: Boolean = false): Array[IAtomContainer]

  def isDerivatized(molecule: IAtomContainer): Boolean

  def generateDerivatizationProduct(molecule: IAtomContainer,
                                    maxTMSCount: Integer,
                                    functionalGroups: Array[IAtomContainer] = TMSFavoredFunctionalGroups.buildFavoredGroupsInOrder()): IAtomContainer

  def getMOLFile(molecule: IAtomContainer): String
}
