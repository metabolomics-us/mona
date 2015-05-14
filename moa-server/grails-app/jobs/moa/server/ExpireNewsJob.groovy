package moa.server

import moa.Spectrum

/**
 * Created with IntelliJ IDEA.
 * User: wohlgemuth
 * Date: 2/27/15
 * Time: 1:01 PM
 */
class ExpireNewsJob {

    NewsService newsService

    def concurrent = false

    def group = "delete"

    def description = "removes outdated news"
    static triggers = {
            cron name: 'news', startDelay: 60, cronExpression: '0 */1 * * * ?', priority: 10
    }

    def execute() {
        newsService.removeOutDatedNews()
    }
}
