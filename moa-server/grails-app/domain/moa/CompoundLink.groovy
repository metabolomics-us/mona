package moa

import util.chemical.CompoundType

class CompoundLink {

    static constraints = {
    }

    static mapping = {
        version false
    }

    Compound compound

    Spectrum spectrum

    CompoundType type

    boolean equals(o) {
        if (this.is(o)) return true
        if (!(o instanceof CompoundLink)) return false

        CompoundLink that = (CompoundLink) o

        if (compound != that.compound) return false
        if (spectrum != that.spectrum) return false
        if (type != that.type) return false

        return true
    }

    int hashCode() {
        int result
        result = (compound != null ? compound.hashCode() : 0)
        result = 31 * result + (spectrum != null ? spectrum.hashCode() : 0)
        result = 31 * result + (type != null ? type.hashCode() : 0)
        return result
    }
}
