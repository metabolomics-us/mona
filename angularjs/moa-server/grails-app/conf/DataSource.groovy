// environment specific settings
environments {
	development {
		mongo {
			host = "localhost"
			port = 27017
			username = ""
			password = ""
			databaseName = "moa-server"
			connectionString = "mongodb://localhost/moa-server"

			options {
				autoConnectRetry = true
				connectTimeout = 300
			}
		}
	}
	test {
		mongo {
			host = "localhost"
			port = 27017
			username = ""
			password = ""
			databaseName = "moa-server"
			connectionString = "mongodb://localhost/moa-server"

			options {
				autoConnectRetry = true
				connectTimeout = 300
			}
		}
	}
	production {
		mongo {
			host = "localhost"
			port = 27017
			username = ""
			password = ""
			databaseName = "moa-server"
			connectionString = "mongodb://localhost/moa-server"

			options {
				autoConnectRetry = true
				connectTimeout = 300
			}
		}
	}
}
