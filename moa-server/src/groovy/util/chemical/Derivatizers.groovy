package util.chemical

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 10/27/14
 * Time: 1:38 PM
 */
class Derivatizers {

    /**
     * provides us with access to all registered derivatizers
     * @return
     */
    static List<Derivatizer> getDerivatizers() {
        [new Derivatizer()]
    }
}
