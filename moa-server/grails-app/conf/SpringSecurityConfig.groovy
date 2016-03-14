def protectedFilters = 'JOINED_FILTERS,-anonymousAuthenticationFilter,-exceptionTranslationFilter,-authenticationProcessingFilter,-securityContextPersistenceFilter'

grails {
    plugin {
        springsecurity {
            filterChain {
                chainMap = [
                        // Authentication endpoints
                        '/rest/login': 'JOINED_FILTERS,-exceptionTranslationFilter,-authenticationProcessingFilter,-securityContextPersistenceFilter, -rememberMeAuthenticationFilter',
                        '/rest/login/validate': 'JOINED_FILTERS,-exceptionTranslationFilter,-authenticationProcessingFilter,-securityContextPersistenceFilter, -rememberMeAuthenticationFilter',
                        '/rest/logout': 'JOINED_FILTERS,-exceptionTranslationFilter,-authenticationProcessingFilter,-securityContextPersistenceFilter, -rememberMeAuthenticationFilter',

                        // Protected urls
                        '/rest/**/create': protectedFilters,
                        '/rest/**/edit': protectedFilters,
                        '/rest/**/delete': protectedFilters,

//                        '/rest/spectra/batch/**': protectedFilters,
                        '/rest/spectra/search/export': protectedFilters,

                        // Protect all non-authentication endpoints in certain conditions
//                        '/rest/spectra/**': protectedFilters,
//                        '/rest/compounds/**': protectedFilters,
//                        '/rest/meta/**': protectedFilters,
//                        '/rest/submitters/**': protectedFilters,
//                        '/rest/news/**': protectedFilters,
//                        '/rest/statistics/**': protectedFilters,
//                        '/rest/tags/**': protectedFilters,
//                        '/rest/queue/**': protectedFilters,
//                        '/rest/webhooks/**': protectedFilters,
//                        '/rest/stored/**': protectedFilters,
//                        '/rest/limited/**': protectedFilters,

                        // Allow anonymous access in general
                        // Should have filterInvocationInterceptor at the end, but results in blank pages
                        '/**': 'anonymousAuthenticationFilter,restTokenValidationFilter,restExceptionTranslationFilter'
                ]
            }

            controllerAnnotations.staticRules = [
                    // Protected urls
                    '/rest/**/create': ['ROLE_ADMIN'],
                    '/rest/**/edit': ['ROLE_ADMIN'],
                    '/rest/**/delete': ['ROLE_ADMIN'],

//                    '/rest/spectra/batch/**': ['ROLE_USER', 'ROLE_CURATOR', 'ROLE_ADMIN'],
                    '/rest/spectra/search/export': ['ROLE_USER', 'ROLE_CURATOR', 'ROLE_ADMIN'],

                    // Protect all non-authentication endpoints in certain conditions
//                    '/rest/spectra/**/': ['ROLE_USER', 'ROLE_CURATOR', 'ROLE_ADMIN'],
//                    '/rest/compounds/**/': ['ROLE_USER', 'ROLE_CURATOR', 'ROLE_ADMIN'],
//                    '/rest/meta/**': ['ROLE_USER', 'ROLE_CURATOR', 'ROLE_ADMIN'],
//                    '/rest/submitters/**': ['ROLE_USER', 'ROLE_CURATOR', 'ROLE_ADMIN'],
//                    '/rest/news/**/': ['ROLE_USER', 'ROLE_CURATOR', 'ROLE_ADMIN'],
//                    '/rest/statistics/**/': ['ROLE_USER', 'ROLE_CURATOR', 'ROLE_ADMIN'],
//                    '/rest/tags/**': ['ROLE_USER', 'ROLE_CURATOR', 'ROLE_ADMIN'],
//                    '/rest/queue/**': ['ROLE_USER', 'ROLE_CURATOR', 'ROLE_ADMIN'],
//                    '/rest/webhooks/**/': ['ROLE_USER', 'ROLE_CURATOR', 'ROLE_ADMIN'],
//                    '/rest/stored/**/': ['ROLE_USER', 'ROLE_CURATOR', 'ROLE_ADMIN'],
//                    '/rest/limited/**': ['ROLE_USER', 'ROLE_CURATOR', 'ROLE_ADMIN'],

                    // Allow anonymous access in general
                    // Should have filterInvocationInterceptor at the end, but results in blank pages
                    '/**': ['permitAll']
            ]

            userLookup {
                userDomainClassName = 'moa.Submitter'
                usernamePropertyName = 'emailAddress'
                passwordPropertyName = 'password'
                authoritiesPropertyName = 'authorities'
                enabledPropertyName  = 'accountEnabled'
                accountExpiredPropertyName = 'accountExpired'
                accountLockedPropertyName = 'accountLocked'
                passwordExpiredPropertyName = 'passwordExpired'
                authorityJoinClassName = 'moa.auth.SubmitterRole'
            }

            authority {
                className = 'moa.auth.Role'
                nameField = 'authority'
            }

            rest {
                login {
                    active = true
                    endpointUrl = '/rest/login'
                    failureStatusCode = 401
                    useJsonCredentials = true
                    usernamePropertyName = 'email'
                    passwordPropertyName = 'password'
                }

                logout.endpointUrl = '/rest/logout'

                token {
                    generation {
                        useSecureRandom = true
                        useUUID = false
                    }

                    storage {
                        useGorm = true

                        gorm {
                            tokenDomainClassName = 'moa.auth.AuthenticationToken'
                            tokenValuePropertyName = 'tokenValue'
                            usernamePropertyName = 'emailAddress'
                        }
                    }

                    validation {
                        active = true
                        useBearerToken = false
                        endpointUrl = '/rest/login/validate'
                        enableAnonymousAcess = true
                    }
                }
            }
        }
    }
}