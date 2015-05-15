/**
 * Created by wohlgemuth
 */

/**
 * simple service to help with available tags
 */

app.factory('StatisticsService', function ($resource, REST_BACKEND_SERVER, $http) {

    return $resource(
        REST_BACKEND_SERVER + '/rest/statistics/:time',
        {time: "@time", method: "@method", max: "@max"},
        {
            'executionTime': {
                url: REST_BACKEND_SERVER + '/rest/statistics/category/:method/:time?max=:max',
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                },
                isArray: true
            },
            'pendingJobs': {
                url: REST_BACKEND_SERVER + '/rest/statistics/jobs/pending',
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json'
                },
                isArray: true
            }
        }
    );
});