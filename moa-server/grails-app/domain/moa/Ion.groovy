package moa

class Ion extends SupportsMetaData {

    static constraints = {
    }

    static belongsTo = [spectrum:Spectrum]

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

    String toString(){
        return "${mass}:${intensity}"
    }
}
