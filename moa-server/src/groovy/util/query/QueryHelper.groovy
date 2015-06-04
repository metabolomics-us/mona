package util.query

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 9/29/14
 * Time: 2:28 PM
 */
class QueryHelper {

	/**
	 * builds our query for comparison fields
	 * @param fieldName
	 * @param values
	 * @param condition
	 * @param executionParams
	 * @param index
	 * @return
	 */
	public
	static List buildComparisonField(String inputQuery, String fieldName, List values, String condition, Map executionParams, int index = 0, String qualifierTable = "") {

		if (qualifierTable != "") {
			qualifierTable = qualifierTable + "."
		}

		String query = " ("

		String conditionTranslation = ""

		switch (condition) {
			case "eq":
				conditionTranslation = "="
				break
			case "like":
				conditionTranslation = "like"
				break
			case "ilike":
				conditionTranslation = "ilike"
				break

			case "gt":
				conditionTranslation = ">"
				break
			case "lt":
				conditionTranslation = "<"
				break
			case "ge":
				conditionTranslation = ">="
				break
			case "le":
				conditionTranslation = "<="
				break
			case "ne":
				conditionTranslation = "!="
				break
			case "between":
				conditionTranslation = "between"
				break
			case "in":
				conditionTranslation = "in"
				break
			case "isNotNull":
				conditionTranslation = "isNotNull"
				break
			case "isNull":
				conditionTranslation = "isNull"
				break
			default:
				throw new RuntimeException("unknown condition specified: ${condition}, skipping!")
		}

		/**
		 * special handling for between
		 */
		if (conditionTranslation.equals("between")) {
			query += "${qualifierTable}${fieldName} ${conditionTranslation} :${fieldName}_value_${index}_min and :${fieldName}_value_${index}_max"
			executionParams.put("${fieldName}_value_${index}_min".toString(), values[0])
			executionParams.put("${fieldName}_value_${index}_max".toString(), values[1])

		} else if (conditionTranslation.equals("ilike")) {

			def value = values[0];

			// like and ilike work only on textual data
			if(!(value instanceof String)) {
				throw new RuntimeException("Can't use 'like' or 'ilike with numeric data")
			}

			query += "lower(${qualifierTable}${fieldName}) like (:${fieldName}_value_${index})"

			executionParams.put("${fieldName}_value_${index}".toString(), value.toString().toLowerCase())
		}
		else if(conditionTranslation.equals("isNotNull")){
			query += "${qualifierTable}${fieldName} is not null"
		}

		else if(conditionTranslation.equals("isNull")){
			query += "${qualifierTable}${fieldName} is null"
		}

		/**
		 * general handling for everything else
		 */
		else {

			def value = values[0];
			// like and ilike work only on textual data
			if(!(value instanceof String) && condition.equals("like")) {
				throw new RuntimeException("Can't use 'like' or 'ilike with numeric data")
			}

			if (fieldName == "id") {

				//stupid grails is not able to convert from integer to longs internally
				if (value instanceof Collection) {
					value = []

					values[0].each {
						if (it instanceof Integer) {
							value.add(it as long)
						} else {
							value.add(it)
						}
					}
				} else {
					if (value instanceof Integer) {
						value = value as long
					}
				}
			}
			query += "${qualifierTable}${fieldName} ${conditionTranslation} (:${fieldName}_value_${index})"

			executionParams.put("${fieldName}_value_${index}".toString(), value)
		}

		query += ")"

		return [inputQuery + query, executionParams];
	}

}
