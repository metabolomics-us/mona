/*
 * Copyright 2007-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.grails.plugins.jquery

/**
 * @author Sergey Nebolsin (nebolsin@prophotos.ru)
 */
class JQueryConfig {
    def defaultPlugins
    def plugins = [:]

    static SHIPPED_VERSION = '1.10.2'

    def init() {

        def application = org.codehaus.groovy.grails.commons.ApplicationHolder.application
        application.metadata.findAll { key, value ->
            key.startsWith('jquery.plugins')
        }.each {key, value ->
            // wtf?
            def pluginName = (key.length() >= 16)? key[15..-1] : "(ungrouped)"
            plugins."$pluginName" = value.split(",") as List
        }

        defaultPlugins = application.config.jquery?.defaultPlugins
    }
}
