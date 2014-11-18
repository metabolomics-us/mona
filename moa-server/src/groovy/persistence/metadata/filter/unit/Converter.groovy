package persistence.metadata.filter.unit

/**
 * converts a value to a unit and tries to discover the unit
 * User: wohlgemuth
 * Date: 11/14/14
 * Time: 12:25 PM
 */
interface Converter {

    /**
     * returns a map with the converted data as key and value
     *
     * (map.unit, map.value)
     *
     * or an empty map
     * @param name
     * @param value
     * @return
     */
    Map<String,String> convert(String name, String value)
}
