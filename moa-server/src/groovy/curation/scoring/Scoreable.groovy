package curation.scoring

import moa.scoring.Score

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 3/26/15
 * Time: 12:20 PM
 */
public interface Scoreable {

    /**
     * returns the score of our object
     * @return
     */
    Score getScore();

    /**
     * assigns a score
     * @param score
     */
    void setScore(Score score);
}