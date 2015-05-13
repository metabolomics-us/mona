package moa.scoring

class Score {

    static constraints = {
    }

    static mapping = {
        impacts nullable: true, fetch: 'join'
        version: false
    }

    static hasMany = [impacts: Impact]

    /**
     * all associated impacts to this object
     */
    Set<Impact> impacts

    /**
     * absolute score of all the impacts
     */
    Double score

    /**
     * relative score of all the impacts
     */
    Double relativeScore

    /**
     * our scaled score between 0 and 10
     */
    Double scaledScore

    @Override
    public String toString() {
        return "Score{" +
                "impacts=" + impacts + " score=" + score +
                '}';
    }

    /**
     * always update our score to relfect on the database level the quality of our spectra
     * @return
     */
    def beforeValidate() {

        //max possible score
        double max = 0

        score = 0
        relativeScore = 0
        scaledScore = 0

        if (impacts) {
            impacts.each {
                score = score + it.impactValue

                max = Math.abs(it.impactValue) + max
            }

            relativeScore = score / max

            //should scaled to 0 - 10
            scaledScore = (relativeScore + 1) * 5;


        }

    }

}
