/**
 * Created by wohlgemuth on 7/18/14.
 */

/**
 * a simple service to validate metadata and filter objects we don't really want to persist
 */
app.service('MetaDataOptimizationService', function (ApplicationError, $log, $q, $timeout, $filter) {

    /**
     * we want to ignore all of these
     * @type {Array}
     */
    var ignore = [
        'SCIENTIFIC_NAME',
        'LINEAGE',
        'ACCESSION',
        'SAMPLE',
        'COMPOUND_CLASS',
        'taxonomy',
        'COMMENT',
        'pubchem',
        'chemspider',
        'cas',
        'kegg',
        'knapsack',
        'lipidbank',
        'date',
        'cayman',
        'chebi',
        'hmdb',
        'nikkaji',
        'chempdb'
    ];

    /**
     * works on the provided metadata array and returns a promise
     * @param metaData
     * @returns {*}
     */
    this.optimizeMetaData = function (metaData) {
//        $log.debug("optimizing metaData");
        var deferred = $q.defer();

        //build our result object
        var result = [];

        //build the list of values we want to ignore
        for (i in metaData) {
            var object = metaData[i];
            var ignored = false;
            for (x in ignore) {

                if (object.name === ignore[x]) {
                    ignored = true;
                }
            }

            if(ignored === false){
                result.push(object);
            }
            else{
  //              $log.debug("ignored object: " + $filter("json")(object));
            }
        }

//        $log.debug("result object: " + $filter('json')(result));
        //right now we do nothing, maybe later we do something with this stuff
        deferred.resolve(result);


        return deferred.promise;
    }
});