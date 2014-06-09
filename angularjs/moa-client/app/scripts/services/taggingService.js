/**
 * Created by wohlgemuth on 6/9/14.
 */

'use strict';

/**
 * simple service to help with available tags
 */
app.service('TaggingService',function($http, $q, REST_BACKEND_SERVER){

    /**
     * returns all possible tags
     */
    this.getTags = function(){

        /**
         * later will be populated by a rest backend
         * @type {{text: string}[]}
         */
        var tags = [
            {"text":"dirty"},
            {"text":"clean"},
            {"text":"mixed"},
            {"text":"standard"},
            {"text":"injected"},
            {"text":"experimental"}
        ];

        var deferred = $q.defer();
        deferred.resolve(tags);
        return deferred.promise;
    }
});