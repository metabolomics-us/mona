/**
 * Created by wohlgemuth on 7/17/14.
 */

app.service('AsyncService', function (ApplicationError, $log, $q, $timeout, $filter) {

    /**
     * contains our pooled data
     * @type {Array}
     */
    var pool = [];

    var executionLimit = 4;

    var poolRate = 500;

    /**
     * a simple pool to ensure we are not using more than 'executionLimit' ajax calls while
     * uploading data to the server

     */
    this.startPool = function () {

        //keeps track of our pool
        var poolRunning = false;

        //how many parallel processes do we want
        var poolLimit = executionLimit;

        //works over the pool
        var handlePool = function () {
            if (poolRunning == false) {
                try {
                    poolRunning = true;
                    for (var i = 0; i < poolLimit; i++) {
                        if (pool.length > 0) {

                            var object = pool.pop();
                            object.execute(object.data);
                        }
                    }
                }
                finally {
                    poolRunning = false;
                    $timeout(handlePool, poolRate);
                }
            }
        };

        $timeout(handlePool, poolRate);
    };

    /**
     * adds a function, which takes one argument and our obect to the pool
     * @param runMe
     * @param executeFunction
     */
    this.addToPool = function (runMe, executeFunction) {
        pool.push({data: runMe, execute: executeFunction});
    };

    /**
     * contains all tasks currently in the pool
     * @returns {Array}
     */
    this.currentTaskList = function () {
        return pool;
    };

    /**
     * current tasks in the pool
     * @returns {Number}
     */
    this.currentTaskCount = function () {
        return pool.length;
    };

    /**
     * clear this pool
     */
    this.clearPool = function(){

        while(pool.length > 0){
            pool.pop();
        }
    };
    /**
     * start our pool and get some work done
     */
    this.startPool();
});