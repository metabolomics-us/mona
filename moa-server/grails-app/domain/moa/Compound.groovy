package moa

import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClassProperty
import org.codehaus.groovy.grails.commons.GrailsClass

class Compound extends SupportsMetaData {

    Date dateCreated
    Date lastUpdated

    static constraints = {
        inchiKey unique: true, nullable: false
        molFile nullable: true
        inchi nullable: true
    }

    static mapping = {
        inchiKey index: true, indexAttributes: [unique: true]
        molFile sqlType: "text"
        version false
        names batchSize: 20
        inchi sqlType: "text"
    }
    /**
     * this compound belongs to one spectrum
     */
    static hasMany = [names: Name, tags:Tag, comments:Comment]

    /**
     * inchiKey of this compound
     */
    String inchiKey

    /**
     * associated mol file for this compound to avoid excessive requests
     */
    String molFile

    /**
     * names associated to this compound
     */
    Set<Name> names

    /**
     * assoicated comments
     */
    Set<Comment> comments

    /**
     * associated inchi code
     */
    String inchi


    static notCloneable = []


    static Object deepClone(domainInstanceToClone) {

        //TODO: PRECISA ENTENDER ISSO! MB-249 no youtrack
        //Algumas classes chegam aqui com nome da classe + _$$_javassist_XX
        if (domainInstanceToClone.getClass().name.contains("_javassist"))
            return null

        //Our target instance for the instance we want to clone
        // recursion
        def newDomainInstance = domainInstanceToClone.getClass().newInstance()

        //Returns a DefaultGrailsDomainClass (as interface GrailsDomainClass) for inspecting properties
        GrailsClass domainClass = domainInstanceToClone.domainClass.grailsApplication.getDomainClass(newDomainInstance.getClass().name)

        def notCloneable = domainClass.getPropertyValue("notCloneable")

        for(DefaultGrailsDomainClassProperty prop in domainClass?.getPersistentProperties()) {
            if (notCloneable && prop.name in notCloneable)
                continue

            if (prop.association) {

                if (prop.owningSide) {
                    //we have to deep clone owned associations
                    if (prop.oneToOne) {
                        def newAssociationInstance = deepClone(domainInstanceToClone?."${prop.name}")
                        newDomainInstance."${prop.name}" = newAssociationInstance
                    } else {

                        domainInstanceToClone."${prop.name}".each { associationInstance ->
                            def newAssociationInstance = deepClone(associationInstance)

                            if (newAssociationInstance)
                                newDomainInstance."addTo${prop.name.capitalize()}"(newAssociationInstance)
                        }
                    }
                } else {

                    if (!prop.bidirectional) {

                        //If the association isn't owned or the owner, then we can just do a  shallow copy of the reference.
                        newDomainInstance."${prop.name}" = domainInstanceToClone."${prop.name}"
                    }
                    // @@JR
                    // Yes bidirectional and not owning. E.g. clone Report, belongsTo Organisation which hasMany
                    // manyToOne. Just add to the owning objects collection.
                    else {
                        //println "${prop.owningSide} - ${prop.name} - ${prop.oneToMany}"
                        //return
                        if (prop.manyToOne) {

                            newDomainInstance."${prop.name}" = domainInstanceToClone."${prop.name}"
                            def owningInstance = domainInstanceToClone."${prop.name}"
                            // Need to find the collection.
                            String otherSide = prop.otherSide.name.capitalize()
                            //println otherSide
                            //owningInstance."addTo${otherSide}"(newDomainInstance)
                        }
                        else if (prop.manyToMany) {
                            //newDomainInstance."${prop.name}" = [] as Set

                            domainInstanceToClone."${prop.name}".each {

                                //newDomainInstance."${prop.name}".add(it)
                            }
                        }

                        else if (prop.oneToMany) {
                            domainInstanceToClone."${prop.name}".each { associationInstance ->
                                def newAssociationInstance = deepClone(associationInstance)
                                newDomainInstance."addTo${prop.name.capitalize()}"(newAssociationInstance)
                            }
                        }
                    }
                }
            } else {
                //If the property isn't an association then simply copy the value
                newDomainInstance."${prop.name}" = domainInstanceToClone."${prop.name}"

                if (prop.name == "dateCreated" || prop.name == "lastUpdated") {
                    newDomainInstance."${prop.name}" = null
                }
            }
        }

        return newDomainInstance
    }
}
