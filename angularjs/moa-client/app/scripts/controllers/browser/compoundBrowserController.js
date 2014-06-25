/**
 * Created by sajjan on 6/11/14.
 */

'use strict';

moaControllers.CompoundBrowserController = function($scope, Compound, $modal, $location) {
    /**
     * contains all local objects
     * @type {Array}
     */
    $scope.compounds = [];

    /**
     * list all our submitters in the system
     */
    $scope.listCompounds = list();


    $scope.viewSpectra = function(inchikey) {
        console.log("#/spectra/"+ inchikey);
        $location.path("/spectra/"+ inchikey);
    };


    /**
     * helper function
     */
    function list() {
        $scope.compounds = Compound.query(function (data) {
        }, function (error) {
            alert('failed: ' + error);
        });
        console.log($scope.compounds)
    }
};