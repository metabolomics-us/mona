package moa

import grails.converters.JSON
import grails.rest.RestfulController
import moa.server.NewsService

class NewsController extends RestfulController<News> {
    static responseFormats = ['json']

    NewsService newsService

    public NewsController() {
        super(News)
    }

    protected Map getParametersToBind() {

        if (request.JSON) {
            params.putAll(
                    request.JSON)
        }

        params
    }

    
    def listAnnouncements(){
        params.sort = "id"
        params.order = "desc"
        render News.findAllByType(News.ANNOUNCEMENT,params) as JSON
        
    }
    
    def listUploads(){
        params.sort = "id"
        params.order = "desc"
        render News.findAllByType(News.UPLOAD,params) as JSON
        
    }

    def listNotifications(){
        params.sort = "id"
        params.order = "desc"
        render News.findAllByType(News.NOTIFICATION,params) as JSON
    }

    def listMilestones(){
        params.sort = "id"
        params.order = "desc"
        render News.findAllByType(News.MILESTONE,params) as JSON
    }

}


