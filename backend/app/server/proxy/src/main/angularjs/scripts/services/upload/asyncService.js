/**
 * simple async service utilizing hte parallel api internally
 * for the heavy lifting
 * Created by wohlgemuth on 7/17/14.
 */

(function() {
    'use strict';
    asyncService.$inject = ['ApplicationError', '$log', '$q', '$interval'];
    angular.module('moaClientApp')
      .service('AsyncService', asyncService);

    /* @ngInject */
    function asyncService(ApplicationError, $log, $q, $interval) {

        var runningTasks = 0;

        //firefox allows max 6,
        var maxRunningTasks = 4;

        var pool = [];

        var poolRate = 200;

        var timeout = null;

        /**
         * adds a function, which takes one argument and our obect to the pool
         * @param runMe
         * @param executeFunction
         */
        this.addToPool = function(executeFunction, data) {
            pool.push({execute: executeFunction, data: data});

            if (timeout === null) {
                this.startPool();
            }
        };

        this.startPool = function() {

            $log.info("starting pool and waiting for jobs");

            //works over the pool
            var handlePool = function() {
                if (runningTasks < maxRunningTasks) {
                    for (var i = 0; i < maxRunningTasks; i++) {
                        if (angular.isDefined(pool)) {
                            if (pool.length > 0) {
                                runningTasks = runningTasks + 1;

                                var object = pool.pop();

                                object.execute(object.data).then(function(data) {
                                    runningTasks--;
                                }).catch(function(error) {
                                    runningTasks--;
                                });

                            }
                        }
                    }
                }
                else if (pool.length === 0) {
                    //stop the interval to save resources
                    $interval.cancel(timeout);
                    timeout = null;
                }
                else {
                    $log.debug("waiting for running tasks to finish (" + runningTasks + ")");
                }
            };

            //start the pull as interval

            timeout = $interval(handlePool, poolRate);
        };

        this.hasPooledTasks = function() {
            return pool.length > 0;
        };

        this.resetPool = function() {
            pool = [];
            timeout = null;
        }
    }
})();