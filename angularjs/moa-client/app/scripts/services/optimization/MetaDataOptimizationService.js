/**
 * Created by wohlgemuth on 7/18/14.
 */

/**
 * a simple service to validate metadata and filter objects we don't really want to persist
 */
app.service('MetaDataOptimizationService', function (ApplicationError, $log, $q, $timeout, $filter) {

    /**
     * works on the provided metadata array and returns a promise
     * @param metaData
     * @returns {*}
     */
    this.optimizeMetaData = function(metaData){
        var deferred = $q.defer();

        //right now we do nothing, maybe later we do something with this stuff
        deferred.resolve(metaData);

        return deferred.promise;
    }
});