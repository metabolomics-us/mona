package moa.scoring

class Impact {

    static constraints = {
        reason nullable: false, unique: false
        score nullable: false, unique: false
        scoringClass nullable: false, unique: false
        impactValue nullable: false, unique: false
    }

    static mapping = {
        version: false
        score fetch:'join'
    }

    static belongsTo = [score:Score]

    /***
     * owning score
     */
    Score score

    /**
     * reason for this impact
     */
    String reason

    /**
     * which class did the scoring
     */
    String scoringClass

    /**
     * what is the impact on the scoring
     */
    Double impactValue

    @Override
    public String toString() {
        return "Impact{" +
                "impactValue=" + impactValue +
                ", reason='" + reason + '\'' +
                '}';
    }
}
