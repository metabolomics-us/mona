package moa.scoring

import moa.Spectrum

class Score {

    static constraints = {
    }

    static mapping = {
        impacts nullable:true
        spectrum nullable:true
        version:false
    }

    static belongsTo = [spectrum:Spectrum]

    static hasMany =[ impacts:Impact]

    /**
     * all associated impacts to this object
     */
    Set<Impact> impacts

    /**
     * owning spectrum object
     */
    Spectrum spectrum

    /**
     * calculates the actual score based on the impacts
     * @return
     */
    Double getScore(){
        if(impacts == null){
            return 0
        }
        double score = 0

        double max = 0

        impacts.each {
            score = score + it.impactValue
            max = Math.abs(it.impactValue) + max
        }

        return score
    }

    /**
     * relative score for this object
     * @return
     */
    Double getRelativeScore(){
        if(impacts == null){
            return 0
        }
        double score = 0

        double max = 0

        impacts.each {
            score = score + it.impactValue
            max = Math.abs(it.impactValue) + max
        }

        return score/max
    }

    @Override
    public String toString() {
        return "Score{" +
                "impacts=" + impacts +  " score=" + score +
                '}';
    }

}
