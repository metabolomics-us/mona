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

        Set<Name> names = spectrum.biologicalCompound.names

        writeName(spectrum.biologicalCompound?.names, buffer, converter, "COMPOUND")
        writeMetaData(spectrum.biologicalCompound?.metaData, buffer, converter, "COMPOUND")
        writeTags(spectrum.biologicalCompound?.tags, buffer, converter, "COMPOUND")

        writeName(spectrum.chemicalCompound?.names, buffer, converter, "DERIVATIZED")
        writeMetaData(spectrum.chemicalCompound?.metaData, buffer, converter, "DERIVATIZED")
        writeTags(spectrum.chemicalCompound?.tags, buffer, converter, "DERIVATIZED")

        writeName(spectrum.predictedCompound?.names, buffer, converter, "VIRTUAL_DERIVATIZED")
        writeMetaData(spectrum.predictedCompound?.metaData, buffer, converter, "VIRTUAL_DERIVATIZED")
        writeTags(spectrum.predictedCompound?.tags, buffer, converter, "VIRTUAL_DERIVATIZED")

        writeMetaData(spectrum.metaData, buffer, converter)
        writeTags(spectrum.tags, buffer, converter)


        String ms = spectrum.spectrum

        String[] ionPairs = ms.split(" ")

        converter("NumPeaks", ionPairs.length, buffer, null)

        for (String ion : ionPairs) {
            String[] values = ion.split(":")

            massSpectraConverter(values[0], values[1], buffer)
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
            if (category) {
                writer.append(category.toUpperCase())
                writer.append("\$")
            }
            writer.append(key.toUpperCase())
            writer.append(" : ")
            writer.append(value)
            writer.append("\n")
        }

        def massSpectraConverter = { def ion, def intensity, StringBuffer writer ->

            writer.append(ion)
            writer.append(" ")
            writer.append(intensity)
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

                closure("NAME", nameIterator.next().name, writer, category)

                while (nameIterator.hasNext()) {
                    closure("SYNONYM", nameIterator.next().name, writer, category)
                }
            }
        }

    }

    private void writeTags(Collection<Tag> tags, StringBuffer writer, def closure, String category = null) {
        if (tags) {
            if (!tags.isEmpty()) {
                Iterator<Tag> nameIterator = tags.iterator()


                while (nameIterator.hasNext()) {
                    closure("TAG", nameIterator.next().text, writer, category)
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
