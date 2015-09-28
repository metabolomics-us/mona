package util


import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass

/**
 * This class provides the inclusion and exclusion policy
 * for Marshallers.
 *
 * Usage for exclusion policy:
 * <code>
 * JSON.registerObjectMarshaller(MyDomainClass, DomainClassMarshaller.createExcludeMarshaller(MyDomainClass, ["excluedField1", "excluedField2"]))
 * </code>
 *
 * Usage for inclusion policy:
 * <code>
 * JSON.registerObjectMarshaller(MyDomainClass, DomainClassMarshaller.createIncludeMarshaller(MyDomainClass, ["id", "name", "description"]))
 * </code>
 *
 * Usage in controller:
 * <code>
 * def sampleAction(Long id) {*   def item = Product.getById(id)
 *   response item as JSON
 *}* </code>
 *
 * Create custom configuration:
 * <code>
 * JSON.createNamedConfig("forAdmin") {*   JSON.registerObjectMarshaller(MyDomainClass, DomainClassMarshaller.createIncludeMarshaller(MyDomainClass, ["id", "name", "description", "stats"]))
 *}* </code>
 * And controller:
 * <code>
 * def sampleAction(Long id) {* 	 def item = Product.getById(id)
 *   JSON.use("forAdmin") {*     response item as JSON
 *}*}* </code>
 *
 * @author Piotr 'Athlan' Pelczar
 * @author  Gert 'berlinguyinca' Wohlgemuth
 *
 */
class DomainClassMarshaller {

    public static List<String> globalRestrictedFields = ['class','version']

    public static Closure createIncludeMarshaller(Class clazz, List<String> fieldsToInclude) {
        return { domainItem ->
            DefaultGrailsDomainClass domain = new DefaultGrailsDomainClass(clazz)
            def results = [:]
            domain.properties.each { field ->
                if (!(field.name in globalRestrictedFields) && (field.name in fieldsToInclude) && (!field.name.endsWith("Service")))
                    results[field.name] = domainItem[field.name]
            }

            return results
        }
    }

    public static Closure createExcludeMarshaller(Class clazz, List<String> fieldsToExclude = []) {
        return { domainItem ->
            DefaultGrailsDomainClass domain = new DefaultGrailsDomainClass(clazz)


            def results = [:]
            domain.properties.each { field ->
                if (!(field.name in globalRestrictedFields) && !(field.name in fieldsToExclude) && (!field.name.endsWith("Service"))){

                    results[field.name] = domainItem[field.name]
                }
            }

            return results
        }
    }
}