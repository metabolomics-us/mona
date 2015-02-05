package curation.rules.compound.inchi
import curation.CurationObject
import curation.actions.AddTagAction
import curation.actions.RemoveTagAction
import curation.rules.compound.AbstractCompoundRule
import moa.Compound
import moa.server.metadata.MetaDataPersistenceService
import org.openscience.cdk.Molecule
import org.openscience.cdk.inchi.InChIGenerator
import org.openscience.cdk.inchi.InChIGeneratorFactory
/**
 * computes the InChI Key from the mol file and check's if it's identical with the provided one.
 *
 * User: wohlgemuth
 * Date: 10/14/14
 * Time: 9:42 AM
 */
class VerifyInChIKeyAndMolFileMatchRule extends AbstractCompoundRule {
    MetaDataPersistenceService metaDataPersistenceService

    VerifyInChIKeyAndMolFileMatchRule() {
        //super(new RemoveTagAction(INCHI_KEY_DOESNT_MATCH_MOLECULE),new AddTagAction(INCHI_KEY_DOESNT_MATCH_MOLECULE));
        super()
        this.successAction = new RemoveTagAction(INCHI_KEY_DOESNT_MATCH_MOLECULE)
        this.failureAction = new AddTagAction(INCHI_KEY_DOESNT_MATCH_MOLECULE)
    }

    @Override
    boolean executeRule(CurationObject toValidate) {

        Compound compound = toValidate.objectAsCompound

        Molecule mol = readMolecule(compound)

        InChIGeneratorFactory factory = InChIGeneratorFactory.getInstance()
        InChIGenerator gen = factory.getInChIGenerator(mol);

        logger.debug("provided InChI Key: ${compound.inchiKey}")
        logger.debug("generated InChI Key: ${gen.getInchiKey()}")

        boolean equals = compound.inchiKey.equals(gen.getInchiKey())

        logger.debug("\t=> match(${equals}")


        metaDataPersistenceService.generateMetaDataObject(compound, [name: "calculated InChI Code", value: gen.inchi, category: "computed", computed: true])
        metaDataPersistenceService.generateMetaDataObject(compound, [name: "calculated InChI Key", value: gen.inchiKey, category: "computed", computed: true])


        def compoundKey = compound.inchiKey.split("-")
        def computedKey = gen.inchiKey.split("-")

        if (compoundKey[0] != computedKey[0]) {
            new AddTagAction("calculated InChI Key first block doesn't match").doAction(toValidate)
        }
        if (compoundKey[1] != computedKey[1]) {
            new AddTagAction("calculated InChI Key second block doesn't match").doAction(toValidate)
        }
        if (compoundKey[2] != computedKey[2]) {
            new AddTagAction("calculated InChI Key third block doesn't match").doAction(toValidate)
        }

        return equals
    }

    @Override
    boolean ruleAppliesToObject(CurationObject toValidate) {
        return toValidate.isCompound()
    }

    @Override
    String getDescription() {
        return "this rule calculates if the InChI Key is the correct one for the provided Molfile"
    }
}
