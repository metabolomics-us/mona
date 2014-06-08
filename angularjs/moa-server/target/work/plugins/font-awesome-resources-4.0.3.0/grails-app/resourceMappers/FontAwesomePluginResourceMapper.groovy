import org.grails.plugin.resource.mapper.MapperPhase


// Removes the warning seen in the Chrome debugger: 
// ‘Resource interpreted as Font but transferred with MIME type application/octet-stream’ 
// http://kalikallin.tumblr.com/post/25182657116/setting-grails-content-type-for-a-given-file-extension
class FontAwesomePluginResourceMapper {

    static phase = MapperPhase.ALTERNATEREPRESENTATION
    static defaultIncludes = [ '**/*.woff' ]

    def map(resource, config) {
        resource.requestProcessors << { req, resp ->
            resp.setHeader('Content-Type', 'font/opentype')
        }
    }

}


