package moa.server.convert

import grails.transaction.Transactional
import moa.Compound
import moa.MetaDataValue
import moa.Name

import java.text.Normalizer

@Transactional
class CompoundConversionService {

    def serviceMethod() {

    }

    /**
     * converts the given compound to a mol file
     * @param compound
     * @return
     */
    String convertToMol(Compound compound) {
        return Normalizer.normalize(compound.molFile,Normalizer.Form.NFC)
    }

    /**
     * creates a sdf file
     * @param compound
     * @return
     */
    String convertToSdf(Compound compound) {

        StringBuffer buffer = new StringBuffer()

        buffer.append(convertToMol(compound))

        buffer.append("\n")
        addField(buffer, "InChI Key", compound.inchiKey)
        addField(buffer, "InChI Code", compound.inchi)

        StringBuffer names = new StringBuffer()

        Iterator<Name> nameIterator = compound.names.iterator()
        while (nameIterator.hasNext()) {

            names.append(nameIterator.next().name)
            if (nameIterator.hasNext()) {
                names.append(", ")
            }
        }

        addField(buffer, "Names", names.toString())

        for(MetaDataValue v : compound.metaData){

            addField(buffer, v.getName(), v.getValue().toString())

        }


        buffer.append("\n\$\$\$\$\n")
        return buffer.toString()
    }

    private void addField(StringBuffer buffer, String name, String value) {
        if (name != null && value != null) {
            buffer.append("\n> <")
            buffer.append(name)
            buffer.append(">")
            buffer.append("\n")
            buffer.append(value)
            buffer.append("\n")
        }
    }
}
