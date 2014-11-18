package persistence.metadata.filter.unit
/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 11/14/14
 * Time: 12:11 PM
 */
class Converters implements Converter {

    List<Converter> converters
    /**
     * converts
     * @param name
     * @param value
     * @return
     */
    Map<String, String> convert(String name, String value) {

        for (Converter converter : converters) {

            Map result = converter.convert(name, value)

            if (!result.isEmpty()) {
                return result
            }
        }

        return [:]
    }
}
