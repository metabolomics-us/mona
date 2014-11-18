package persistence.metadata.filter.unit
/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 11/14/14
 * Time: 12:41 PM
 */
class BasicUnitConverter implements Converter {

    @Override
    Map<String, String> convert(String name, String value) {

        def metadata = [:]

        def regexEv = /^\+?(-?[0-9]+\.?[0-9]+).*ev$/;
        def regexPercent = /^\+?(-?[0-9]+\.?[0-9]*)\s*\%(?:\s\(nominal\)$)?/;
        def regexV = /^\+?(-?[0-9]+\.?[0-9]*).*v$/;
        def regexC = /^\+?(-?[0-9]+\.?[0-9]*).*c$/;
        def regexKpa = /^\+?(-?[0-9]+\.?[0-9]*).*kpa$/;
        def regexMa = /^\+?(-?[0-9]+\.?[0-9]*).*mA$/;
        def regexKv = /^\+?(-?[0-9]+\.?[0-9]*).*kv$/;
        def regexFlowMinutes = /^(?:add +)?\+?(-?[0-9]+\.?[0-9]*).*ml\/min$/;
        def regexFlowMicroMinutes = /^(?:add +)?\+?(-?[0-9]+\.?[0-9]*).*ul\/min$/;
        def regexScanBysec = /^\+?(-?[0-9]+\.?[0-9]*).*sec\/scan.*$/;


        if (value.matches(regexEv)) {
            metadata.value = getValue(value, regexEv)
            metadata.unit = "eV";
        } else if (value.matches(regexPercent)) {
            metadata.value = getValue(value, regexPercent)
            metadata.unit = "%";
        } else if (value.matches(regexV)) {
            metadata.value = getValue(value, regexV)
            metadata.unit = "V";
        } else if (value.matches(regexC)) {
            metadata.value = getValue(value, regexC)
            metadata.unit = "C";
        } else if (value.matches(regexKpa)) {
            metadata.value = getValue(value, regexKpa)
            metadata.unit = "kPa";
        } else if (value.matches(regexMa)) {
            metadata.value = getValue(value, regexMa)
            metadata.unit = "mA";
        } else if (value.matches(regexKv)) {
            metadata.value = getValue(value, regexKv).toDouble() * 1000
            metadata.unit = "V";
        } else if (value.matches(regexFlowMinutes)) {
            metadata.value = getValue(value, regexFlowMinutes)
            metadata.unit = "ml/min";
        } else if (value.matches(regexFlowMicroMinutes)) {

            metadata.value = getValue(value, regexFlowMicroMinutes).toDouble() / 1000
            metadata.unit = "ml/min";
        } else if (value.matches(regexScanBysec)) {
            metadata.value = getValue(value, regexScanBysec).toDouble() / 1000
            metadata.unit = "sec/scan";
        }

        return metadata;

    }

    private String getValue(String value, def regex) {
        def matcher = (value =~ regex)

        matcher.matches()

        return matcher[0][1]

    }

}
