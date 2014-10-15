package util.chemical

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 10/14/14
 * Time: 2:44 PM
 */
public class TMSFavoredFunctionalGroups {

    /**
     * returns a list of favored groups
     * @return
     */
    static def buildFavoredGroupsInOrder() {
        return [
                FunctionalGroupBuilder.makeHydroxyGroup(),
                FunctionalGroupBuilder.makePhosphate(),
                FunctionalGroupBuilder.makeThiol(),
                FunctionalGroupBuilder.makePrimaryAmine(),
                FunctionalGroupBuilder.makeSecondaeryAmine()
        ]
    }
}