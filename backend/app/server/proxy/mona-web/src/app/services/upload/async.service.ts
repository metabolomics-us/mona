/**
 * simple async service utilizing hte parallel api internally
 * for the heavy lifting
 * Created by wohlgemuth on 7/17/14.
 */

import {NGXLogger} from "ngx-logger";
import {Injectable} from "@angular/core";

@Injectable()
export class AsyncService{
    public runningTasks;
    public maxRunningTasks;
    public pool;
    public poolRate;
    public timeout;

    constructor(public logger: NGXLogger) {
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

        this.logger.info("starting pool and waiting for jobs");

        //works over the pool
        let handlePool = () => {
            if (this.runningTasks < this.maxRunningTasks) {
                for (let i = 0; i < this.maxRunningTasks; i++) {
                    if (typeof this.pool !== 'undefined') {
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
                clearInterval(this.timeout);
                //this.$interval.cancel(this.timeout);
                this.timeout = null;
            }
            else {
                this.logger.debug("waiting for running tasks to finish (" + this.runningTasks + ")");
            }
        };

        //start the pull as interval
        this.timeout = setInterval(handlePool, this.poolRate);
        //this.timeout = this.$interval(handlePool, this.poolRate);
    };

    hasPooledTasks = () => {
        return this.pool.length > 0;
    };

    resetPool = () => {
        this.pool = [];
        this.timeout = null;
    }

}
