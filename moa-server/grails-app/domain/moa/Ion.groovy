package moa

import java.text.DecimalFormat
import java.text.NumberFormat

class Ion extends SupportsMetaData {

    /**
     * we are always formating to n digits
     */
    static NumberFormat formatter = new DecimalFormat("#.####");

    static constraints = {
    }

    static belongsTo = [spectrum: Spectrum]

    static mapping = {
        version false
    }

    /**
     * intensity
     */
    double intensity

    /**
     * mass
     */
    double mass

    /**
     * related spectrum object
     */
    Spectrum spectrum

    String toString() {
        return "${formatter.format(mass)}:${formatter.format(intensity)}"
    }
}
