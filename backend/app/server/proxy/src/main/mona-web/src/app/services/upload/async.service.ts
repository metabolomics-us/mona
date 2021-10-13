/**
 * simple async service utilizing hte parallel api internally
 * for the heavy lifting
 * Created by wohlgemuth on 7/17/14.
 */

import {NGXLogger} from 'ngx-logger';
import {Injectable} from '@angular/core';

@Injectable()
export class AsyncService{
    runningTasks;
    maxRunningTasks;
    pool;
    poolRate;
    timeout;

    constructor(public logger: NGXLogger) {
        this.runningTasks = 0;

        // firefox allows max 6,
        this.maxRunningTasks = 8;

        this.pool = [];

        this.poolRate = 100;

        this.timeout = null;
    }

    /**
     * adds a function, which takes one argument and our obect to the pool
     * @param executeFunction function to run in job
     */
    addToPool(executeFunction, data) {
        this.pool.push({execute: executeFunction, data});

        if (this.timeout === null) {
            this.logger.info('starting pool and waiting for jobs');
            this.startPool();
            this.timeout = setInterval(() => this.startPool(), this.poolRate);
        }
    }

    startPool() {
        // works over the pool
      if (this.runningTasks < this.maxRunningTasks) {
        for (let i = 0; i < this.maxRunningTasks; i++) {
          if (typeof this.pool !== 'undefined') {
            if (this.pool.length > 0) {
              this.runningTasks = this.runningTasks + 1;
              const object = this.pool.pop();
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
        // stop the interval to save resources
        clearInterval(this.timeout);
        // this.$interval.cancel(this.timeout);
        this.timeout = null;
      }
      else {
        this.logger.debug('waiting for running tasks to finish (' + this.runningTasks + ')');
      }
    }

    hasPooledTasks() {
        return this.pool.length > 0;
    }

    resetPool() {
        this.pool = [];
        this.timeout = null;
    }

}
