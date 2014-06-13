/**
 * Created by sajjan on 6/11/14.
 */

'use strict';

moaControllers.CompoundsController = function ($scope, Compound, $modal) {
    /**
     * contains all local objects
     * @type {Array}
     */
    $scope.compounds = [];

    /**
     * list all our submitters in the system
     */
    $scope.listCompounds = list();


    $scope.viewSpectrum = function(id) {
        var modalInstance = $modal.open({
            templateUrl: '/views/compounds/viewCompound.html',
            controller: moaControllers.ViewCompoundModalController,
            size: 'lg',
            backdrop: 'true',
            resolve: {
                compound: function () {
                    return $scope.compounds[id];
                }
            }
        });
    }


    /**
     * helper function
     */
    function list() {
        $scope.compounds = Compound.query(function (data) {
        }, function (error) {
            alert('failed: ' + error);
        })
    }

//        $scope.spectra = [
//            // http://gmd.mpimp-golm.mpg.de/Metabolites/38660976-a573-4a36-b283-4ea09d1e22ec.aspx
//            // http://gmd.mpimp-golm.mpg.de/Analytes/7fc9eee3-ed3e-4480-bb5e-04c0afbbbf58.aspx
//            // http://gmd.mpimp-golm.mpg.de/webservices/SpectrumJcampDx.ashx?id=ab6314e7-3962-411f-aa47-b631f9bc86ad
//            {
//                "id": 1,
//                "biologicalCompound": { "inchi": "VKOBVWXKNCXXDE-UHFFFAOYSA-N", "name": "arachic acid" },
//                "chemicalCompound": { "inchi": "AYSGFBVOWSXJCN-UHFFFAOYSA-N", "name": "arachidic acid (1TMS)" },
//                "tags": [
//                    { "class": "moa.Tag", "id": 24, "text": "experimental" },
//                    { "class": "moa.Tag", "id": 20, "text": "clean" }
//                ],
//                "metadata": [],
//                "submitter": { "class": "moa.Submitter", "id": 1, "emailAddress": "wohlgemuth@ucdavis.edu", "firstName": "Gert", "lastName": "Wohlgemuth", "password": "dasdsa" },
//                "spectrum": "1:1"
//            },
//
//            // http://gmd.mpimp-golm.mpg.de/Metabolites/0c9a2dc0-fea2-4864-b98b-0597cdd0ad06.aspx
//            // http://gmd.mpimp-golm.mpg.de/Analytes/607DBF7C-9714-402B-B03A-2CC96DC5CE02.aspx
//            {
//                "id": 2,
//                "biologicalCompound": { "inchi": "BJEPYKJPYRNKOW-UHFFFAOYSA-N", "name": "malic acid" },
//                "chemicalCompound": { "inchi": "QCOBWZRPFJZHGU-UHFFFAOYSA-N", "name": "malic acid (3TMS)" },
//                "tags": [
//                    { "class": "moa.Tag", "id": 24, "text": "experimental" },
//                    { "class": "moa.Tag", "id": 20, "text": "clean" }
//                ],
//                "metadata": [],
//                "submitter": { "class": "moa.Submitter", "id": 1, "emailAddress": "wohlgemuth@ucdavis.edu", "firstName": "Gert", "lastName": "Wohlgemuth", "password": "dasdsa" },
//                "spectrum": "1:1"
//            }
//        ];
}