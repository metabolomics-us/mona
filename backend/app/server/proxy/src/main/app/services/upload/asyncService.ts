/**
 * simple async service utilizing hte parallel api internally
 * for the heavy lifting
 * Created by wohlgemuth on 7/17/14.
 */

import * as angular from 'angular';

class AsyncService{
    private static $inject = ['ApplicationError', '$log', '$http', '$interval'];
    private ApplicationError;
    private $log;
    private $http;
    private $interval;
    private runningTasks;
    private maxRunningTasks;
    private pool;
    private poolRate;
    private timeout;

    constructor(ApplicationError, $log, $http, $interval) {
        this.ApplicationError = ApplicationError;
        this.$log = $log;
        this.$http = $http;
        this.$interval = $interval;
    }

    $onInit = () => {
        this.runningTasks = 0;

        //firefox allows max 6,
        this.maxRunningTasks = 4;

        this.pool = [];

        this.poolRate = 200;

        this.timeout = null;
    }

    /**
     * adds a function, which takes one argument and our obect to the pool
     * @param runMe
     * @param executeFunction
     */
    addToPool = (executeFunction, data) => {
        this.pool.push({execute: executeFunction, data: data});

        if (this.timeout === null) {
            this.startPool();
        }
    };

    startPool = () => {

        this.$log.info("starting pool and waiting for jobs");

        //works over the pool
        let handlePool = () => {
            if (this.runningTasks < this.maxRunningTasks) {
                for (let i = 0; i < this.maxRunningTasks; i++) {
                    if (angular.isDefined(this.pool)) {
                        if (this.pool.length > 0) {
                            this.runningTasks = this.runningTasks + 1;

                            let object = this.pool.pop();

                            object.execute(object.data).then((data) => {
                                this.runningTasks--;
                            }).catch((error) => {
                                this.runningTasks--;
                            });

                        }
                    }
                }
            }
            else if (this.pool.length === 0) {
                //stop the interval to save resources
                this.$interval.cancel(this.timeout);
                this.timeout = null;
            }
            else {
                this.$log.debug("waiting for running tasks to finish (" + this.runningTasks + ")");
            }
        };

        //start the pull as interval

        this.timeout = this.$interval(handlePool, this.poolRate);
    };

    hasPooledTasks = () => {
        return this.pool.length > 0;
    };

    resetPool = () => {
        this.pool = [];
        this.timeout = null;
    }

}
angular.module('moaClientApp')
    .service('AsyncService', AsyncService);
