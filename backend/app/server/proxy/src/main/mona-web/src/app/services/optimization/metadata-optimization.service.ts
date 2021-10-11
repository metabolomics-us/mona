/**
 * Created by wohlgemuth on 7/18/14.
 */


/**
 *
 * TODO move to rule system on the server side
 *
 * general service to optimize metadata and take care of some formating issues
 */
import {NGXLogger} from 'ngx-logger';
import {Injectable} from '@angular/core';

@Injectable()
export class MetadataOptimization {
    private regexNumeric;

    constructor(public logger: NGXLogger) {
        /**
         * numeric value
         * @RegExp regex string
         */
        this.regexNumeric = /([0-9]+\.?[0-9]+)/;
    }

    /**
     * converts the name and category
     * @param metadata string
     */
     convertName(metadata) {
        if (metadata == null || metadata.name == null || metadata.name === '') {
          return null;
        }

        metadata.name = metadata.name.replace(/_/g, ' ').toLowerCase();

        if (typeof metadata.category !== 'undefined') {
            metadata.category = metadata.category.replace(/_/g, ' ').toLowerCase();

        }
        return metadata;
    }

    /**
     * works on the provided metadata array and returns a promise
     * @param metaData string
     * @*
     */
    optimizeMetaData(metaData) {
        const myPromise = new Promise((resolve, reject) => {
            // build our result object
            const result = [];

            // build the list of values we want to ignore
            for (let i = 0; i < metaData.length; i++) {
                let object = metaData[i];

                object = this.convertName(object);

                if (object !== null) {
                    result.push(object);
                }
            }
            resolve(result);
        });

        return myPromise;
    }
}
