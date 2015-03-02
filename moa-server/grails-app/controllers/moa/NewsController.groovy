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
        render News.findAllByType(News.ANNOUNCEMENT,[sort: "id", order: "desc"]) as JSON
        
    }
    
    def listUploads(){
        render News.findAllByType(News.UPLOAD,[sort: "id", order: "desc"]) as JSON
        
    }

    def listNotifications(){
        render News.findAllByType(News.NOTIFICATION,[sort: "id", order: "desc"]) as JSON
    }

    def listMilestones(){
        render News.findAllByType(News.MILESTONE,[sort: "id", order: "desc"]) as JSON
    }

}


