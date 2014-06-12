/**
 * Created by sajjan on 6/10/14.
 */
'use strict';

app.service('SpectrumService',function($http, $q, REST_BACKEND_SERVER) {

    /**
     * returns all possible tags
     */
    this.getSpectra = function(){

        /**
         * later will be populated by a rest backend
         * @type {{text: string}[]}
         */
        var spectra = [
            // http://gmd.mpimp-golm.mpg.de/webservices/SpectrumJcampDx.ashx?id=ab6314e7-3962-411f-aa47-b631f9bc86ad
            {
                "id": 1,
                "biologicalCompound": { "inchi": "VKOBVWXKNCXXDE-UHFFFAOYSA-N", "name": "arachic acid" },
                "chemicalCompound": { "inchi": "AYSGFBVOWSXJCN-UHFFFAOYSA-N", "name": "arachidic acid trimethylsilyl ester" },
                "tags": [
                    { "class": "moa.Tag", "id": 24, "text": "experimental" },
                    { "class": "moa.Tag", "id": 20, "text": "clean" }
                ],
                "metadata": [],
                "submitter": { "class": "moa.Submitter", "id": 1, "emailAddress": "wohlgemuth@ucdavis.edu", "firstName": "Gert", "lastName": "Wohlgemuth", "password": "dasdsa" }
            },

            {
                "id": 2,
                "biologicalCompound": { "inchi": "VKOBVWXKNCXXDE-UHFFFAOYSA-N", "name": "arachic acid" },
                "chemicalCompound": { "inchi": "AYSGFBVOWSXJCN-UHFFFAOYSA-N", "name": "arachidic acid trimethylsilyl ester" },
                "tags": [
                    { "class": "moa.Tag", "id": 24, "text": "experimental" },
                    { "class": "moa.Tag", "id": 20, "text": "clean" }
                ],
                "metadata": [],
                "submitter": { "class": "moa.Submitter", "id": 1, "emailAddress": "wohlgemuth@ucdavis.edu", "firstName": "Gert", "lastName": "Wohlgemuth", "password": "dasdsa" }
            }
        ];

        var deferred = $q.defer();
        deferred.resolve(spectra);
        return deferred.promise;
    }
});