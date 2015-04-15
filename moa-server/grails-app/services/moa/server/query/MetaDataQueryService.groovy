package moa.server.query
import grails.transaction.Transactional
import moa.MetaDataValue

import static util.query.QueryHelper.buildComparisonField

@Transactional
class MetaDataQueryService {

    static transactional = false

    @Transactional
    def query(long id) {
        return MetaDataValue.get(id)
    }

    @Transactional
    def query(def json, def params) {

        if (!params.max) {
            params.max = -1
        }

        if (!params.offset) {
            params.offset = -1
        }

        if (json == null) {
            throw new Exception("your query needs to contain some parameters!")
        }


        return query(json, params.max as int, params.offset as int)
    }

    /**
     * queries metadata and returns the result as json array of metadata types
     * @param json
     */
    @Transactional
    def query(def json, int limit = -1, int offset = -1) {

        log.info("received query: ${json}")

        def params = [:]

        if (limit != -1) {
            params.max = limit
        }

        if (offset != -1) {
            params.offset = offset
        }

        String field = "m"
        /**
         * if only one field is specifed
         */
	    if (json.property != null && json.property != []) {
            log.debug("add property limitation to query: ${json.property}")
            field = "m.${json.property}"
        }

        String queryOfDoom = "select distinct ${field} from MetaDataValue m left join m.metaData as md left join md.category as mdc "

        String queryOfDoomJoins = ""

        String queryOfDoomWhere = ""


        if (json.isEmpty() == false) {
            queryOfDoomWhere += " where "
        }

        def executionParams = [:]


        queryOfDoomWhere = buildMetadataQueryString(queryOfDoomWhere, json, executionParams, "md", "m", "mdc", 0)

        queryOfDoom = queryOfDoom + queryOfDoomJoins + queryOfDoomWhere

        log.info("generated query: ${queryOfDoom}")
        def result = MetaDataValue.executeQuery(queryOfDoom, executionParams, params)

        println ("\n\nt query: \n\n ${queryOfDoom}\n\n")
        log.debug("result size: ${result.size()}")
        return result

    }

    /**
     * helper method to figure out the exactly required expressions
     *
     * @param whereQuery query we are building
     * @param current query json object
     * @param executionParams list of exectution parameters
     * @param metaDataTableName name of our metadata table name
     * @param valueTable name of our metadata value table name
     * @param index current join in case we have more than 1
     * @return
     */
    protected String buildMetadataQueryString(String whereQuery, Map current, Map executionParams, String metaDataTableName, String valueTable, String categoryTable, int index = 0) {

        whereQuery = addRequiredAnd(whereQuery)

        whereQuery += " ("

        //support for categories
        if (current.category) {
            whereQuery = addRequiredAnd(whereQuery)

            //long form
            if (current.category instanceof Map) {

                //query by id
                if (current.category.id) {
                    (whereQuery, executionParams) = buildComparisonField(whereQuery, "id", [current.category.id], "eq", executionParams, index, categoryTable)

                }

                //query by name
                else {
                    current.category.keySet().each { String key ->
                        if (current.category."${key}") {
                            (whereQuery, executionParams) = buildComparisonField(whereQuery, "name", [current.category."${key}"], key, executionParams, index, categoryTable)
                        }
                    }
                }
            }
            //short form
            else {
                (whereQuery, executionParams) = buildComparisonField(whereQuery, "name", [current.category], "eq", executionParams, index, categoryTable)
            }
        }
        //we have a name specified
        if (current.name) {
            whereQuery = addRequiredAnd(whereQuery)

            //long form
            if (current.name instanceof Map) {
                current.name.keySet().each { String key ->
                    if (current.name."${key}") {
                        (whereQuery, executionParams) = buildComparisonField(whereQuery, "name", [current.name."${key}"], key, executionParams, index, metaDataTableName)
                    }
                }
            }
            //short form
            else {
                (whereQuery, executionParams) = buildComparisonField(whereQuery, "name", [current.name], "eq", executionParams, index, metaDataTableName)
            }
        }

        if (current.id) {
            whereQuery = addRequiredAnd(whereQuery)

            //long form
            if (current.id instanceof Map) {
                current.id.keySet().each { String key ->
                    if (current.id."${key}") {
                        (whereQuery, executionParams) = buildComparisonField(whereQuery, "id", [current.id."${key}"], key, executionParams, index, metaDataTableName)

                    }
                }
            }
            //short form
            else {
                (whereQuery, executionParams) = buildComparisonField(whereQuery, "id", [current.id], "eq", executionParams, index, metaDataTableName)
            }
        }

        //we have a value specified
        if (current.value) {
            whereQuery = addRequiredAnd(whereQuery)

            //complex specification as map
            if (current.value instanceof Map) {
                //find out the keys in our current object and try to do something with it
                current.value.keySet().each { String key ->

                    if (current.value."${key}" != null) {

                        //special treatment for between
                        if (key.equals("between")) {

                            def impl = estimateMetaDataValueImpl(current.value.between[0].toString())

                            (whereQuery, executionParams) = buildComparisonField(whereQuery, impl.name.toString(), [estimateMetaDataValueImpl(current.value.between[0]).value, estimateMetaDataValueImpl(current.value.between[1]).value], key, executionParams, index, valueTable)
                        }
                        // and unit inside value
                        else if (key.equals("unit")) {
	                        // implemented below -- code duplication warning!!!
                        } else {
                            def impl = estimateMetaDataValueImpl(current.value."${key}".toString())

                            (whereQuery, executionParams) = buildComparisonField(whereQuery, impl.name.toString(), [impl.value], key, executionParams, index, valueTable)
                        }
                    }
                }
            }
            //short form specified
            else {
                def impl = estimateMetaDataValueImpl(current.value.toString())

                (whereQuery, executionParams) = buildComparisonField(whereQuery, impl.name, [impl.value], "eq", executionParams, index, valueTable)

            }
        }

        //we have an unit specified
        if (current.value instanceof Map && current.value.unit != null) {
            whereQuery = addRequiredAnd(whereQuery)

            (whereQuery, executionParams) = buildComparisonField(whereQuery, "unit", [current.value.unit], "eq", executionParams, index, valueTable)
        }

        //support for units in long form
        if (current.unit != null) {
            whereQuery = addRequiredAnd(whereQuery)

	        //long form
            if (current.unit instanceof Map) {
                current.unit.keySet().each { String key ->
                    if (current.unit."${key}") {
                        (whereQuery, executionParams) = buildComparisonField(whereQuery, "unit", [current.unit."${key}"], key, executionParams, index, valueTable)
                    }
                }
            }
            //short form
            else {
                (whereQuery, executionParams) = buildComparisonField(whereQuery, "unit", [current.unit], "eq", executionParams, index, valueTable)
            }
        }

        whereQuery += ")"

        return whereQuery
    }

    /**
     * checks terms and adds an and if required
     * @param whereQuery
     * @return
     */
    private String addRequiredAnd(String whereQuery) {
        if (!whereQuery.trim().endsWith("where")) {
            if (!whereQuery.trim().endsWith(" and")) {
                if (!whereQuery.trim().endsWith("(")) {
                    whereQuery += " and "
                }
            }
        }

        whereQuery
    }

    /**
     * returns a map with exactly two keys
     * @param content
     * @return
     */
    protected Map estimateMetaDataValueImpl(String content) {

        def result = [:];

        result.name = "stringValue"
        result.value = content

        //temporary while we are diagnonsing issues, we only support string storage
        /*
        MetaDataValue value = MetaDataValueHelper.getValueObject(content)

        if (value instanceof BooleanMetaDataValue) {

            result.name = "booleanValue"
            result.value = value.booleanValue
        } else if (value instanceof DoubleMetaDataValue) {

            result.name = "doubleValue"
            result.value = value.doubleValue
        } else if (value instanceof StringMetaDataValue) {
            result.name = "stringValue"
            result.value = value.getStringValue()
        }
          */
        return result;
    }
}
