package moa

import java.text.DecimalFormat
import java.text.NumberFormat

class Ion extends SupportsMetaData implements Comparable<Ion> {

    /**
     * we are always formating to n digits
     */
    static NumberFormat formatter = new DecimalFormat("#.####");

    static constraints = {
    }

    static belongsTo = [spectrum: Spectrum]

    static mapping = {
        version false
        //spectrum fetch: 'join'
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
        return "${formatter.format(mass)}:${formatter.format(intensity*100)}"
    }

    @Override
    int compareTo(Ion o) {
        if (o != null && this.getMass() != null) {
            return Double.compare(o.getMass(), this.getMass());
        } else {
            return -1;
        }
    }
}
