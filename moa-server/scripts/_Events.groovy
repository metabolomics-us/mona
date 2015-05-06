//includeTargets << grailsScript("_GrailsInit")
//
//target(events: "The description of the script goes here!") {
//}
//
//setDefaultTarget(events)

// adding code to enable functional testing
eventAllTestsStart = {
	if (getBinding().variables.containsKey("functionalTests")) {
		functionalTests << "functional"
	}
}

//eventTestSuiteStart = { String type ->
//	if (type == "functional") {
//		testingBaseURL = argsMap["baseUrl"] ?: "http://localhost:$serverPort$serverContextPath"
//		if (!testingBaseURL.endsWith('/')) testingBaseURL += '/'
//		System.setProperty("grails.functional.test.baseURL", testingBaseURL)
//	}
//}
