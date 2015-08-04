package moa.server.convert

import grails.transaction.Transactional
import moa.MetaDataValue
import moa.Name
import moa.Spectrum
import moa.Tag

@Transactional
class SpectraConversionService {

    /**
     * uses the given converter and converts the spectrum to this format
     * @param spectrum
     * @param converter
     * @return
     */
    private String convert(Spectrum spectrum, def converter, def massSpectraConverter) {
        StringBuffer buffer = new StringBuffer()

        writeName(spectrum.biologicalCompound?.names, buffer, converter, "COMPOUND")

        if (spectrum.biologicalCompound.inchi)
            converter('InChI', spectrum.biologicalCompound.inchi,buffer,null)
        if (spectrum.biologicalCompound.inchiKey)
            converter('InChIKey', spectrum.biologicalCompound.inchiKey,buffer,null)

        writeMetaData(spectrum.biologicalCompound?.metaData, buffer, converter, "COMPOUND")
        writeTags(spectrum.biologicalCompound?.tags, buffer, converter, "COMPOUND")

// Ignore all but the biological compound to conform to export standard
//        writeName(spectrum.chemicalCompound?.names, buffer, converter, "DERIVATIZED")
//        writeMetaData(spectrum.chemicalCompound?.metaData, buffer, converter, "DERIVATIZED")
//        writeTags(spectrum.chemicalCompound?.tags, buffer, converter, "DERIVATIZED")
//
//        writeName(spectrum.predictedCompound?.names, buffer, converter, "VIRTUAL_DERIVATIZED")
//        writeMetaData(spectrum.predictedCompound?.metaData, buffer, converter, "VIRTUAL_DERIVATIZED")
//        writeTags(spectrum.predictedCompound?.tags, buffer, converter, "VIRTUAL_DERIVATIZED")

        // Separate annotations from metadata
        Collection<MetaDataValue> metadata = spectrum.metaData.findAll { it.category != 'annotation' };
        Collection<MetaDataValue> annotations = spectrum.metaData.findAll { it.category == 'annotation' };


        writeMetaData(metadata, buffer, converter)
        writeTags(spectrum.tags, buffer, converter)


        String[] ionPairs = spectrum.spectrum.split(" ")

        converter("Num Peaks", ionPairs.length, buffer, null)

        for (String ion : ionPairs) {
            String[] values = ion.split(":")
            massSpectraConverter(values[0], values[1], buffer, annotations)
        }

        buffer.append("\n")

        return buffer.toString()
    }


    /**
     * generates a MSP file
     * @param spectrum
     * @return
     */
    String convertToMsp(Spectrum spectrum) {
        /**
         * converts data into MSP
         */
        def mspConverter = { String key, def value, StringBuffer writer, String category ->
            writer.append(key)
            writer.append(": ")
            writer.append(value)
            writer.append("\n")
        }

        def massSpectraConverter = { def ion, def intensity, StringBuffer writer, Collection<MetaDataValue> annotations ->
            writer.append(ion)
            writer.append(" ")
            writer.append(intensity)

            if (annotations) {
                for (MetaDataValue metaDataValue in annotations) {
                    if (Math.abs(Double.parseDouble(metaDataValue.value) - Double.parseDouble(ion)) < 0.0001) {
                        writer.append(" \"")
                        writer.append(metaDataValue.name)
                        writer.append("\"")
                    }
                }
            }

            writer.append("\n")
        }

        return convert(spectrum, mspConverter, massSpectraConverter)
    }

    /**
     * writes out the names for us
     * @param names
     * @param writer
     * @param closure
     */
    private void writeName(Collection<Name> names, StringBuffer writer, def closure, String category = null) {
        if (names) {
            if (!names.isEmpty()) {
                Iterator<Name> nameIterator = names.iterator()

                closure("Name", nameIterator.next().name, writer, category)

                while (nameIterator.hasNext()) {
                    closure("Synon", nameIterator.next().name, writer, category)
                }
            }
        }

    }

    private void writeTags(Collection<Tag> tags, StringBuffer writer, def closure, String category = null) {
        if (tags) {
            if (!tags.isEmpty()) {
                Iterator<Tag> nameIterator = tags.iterator()


                while (nameIterator.hasNext()) {
                    closure("Tag", nameIterator.next().text, writer, category)
                }
            }
        }

    }

    private void writeMetaData(Collection<MetaDataValue> values, StringBuffer writer, def closure, String category = null) {
        if (values) {
            if (!values.isEmpty()) {
                Iterator<MetaDataValue> nameIterator = values.iterator()

                while (nameIterator.hasNext()) {
                    MetaDataValue value = nameIterator.next()

                    if(!value.hidden && !value.deleted) {
                        String myCat = category
                        if (myCat != null) {
                            if (myCat != value.category) {
                                myCat = myCat + "_" + value.category
                            }
                        } else {
                            myCat = value.category
                        }
                        closure(value.name, value.value, writer, myCat)
                    }
                }
            }
        }
    }
}
