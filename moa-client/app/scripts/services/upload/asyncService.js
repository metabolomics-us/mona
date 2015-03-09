/**
 * simple async service utilizing hte parallel api internally
 * for the heavy lifting
 * Created by wohlgemuth on 7/17/14.
 */

app.service('AsyncService', function (ApplicationError, $log, $q, $interval) {

    var parallel = new Parallel('importer');

    var runningTasks = 0;

    var maxRunningTasks = 10;

    var pool = [];

    var poolRate = 100;

    /**
     * adds a function, which takes one argument and our obect to the pool
     * @param runMe
     * @param executeFunction
     */
    this.addToPool = function (executeFunction, data) {
        //$log.debug("adding work to pool");


        pool.push({execute: executeFunction, data: data});

    };

    this.startPool = function () {


        /**
         * small wrapper since parallel api exspects a string value as result
         * @param object
         * @returns {string}
         */
        var submit = function (object) {

            runningTasks = runningTasks + 1;

            //$log.debug("calling submit function...");
            $log.debug("actually doing some work....");

            object.execute(object.data).then(function (data) {
                $log.info("done with my task...");

                runningTasks = runningTasks - 1;
            }).catch(function (error) {
                $log.info("my task failed...");
                runningTasks = runningTasks - 1;
            });

            return 'done';
        };


        //works over the pool
        var handlePool = function () {
            if (runningTasks < maxRunningTasks) {
                for (var i = 0; i < maxRunningTasks; i++) {
                    if (angular.isDefined(pool)) {
                        if (pool.length > 0) {

                            /**
                             * spawn a new process to the server
                             */
                            parallel.spawn(submit(pool.pop()))
                        }
                    }
                }
            }
        };

        //start the pull as interval

        $interval(handlePool, poolRate);
    };


    this.startPool();
});