/**
 * Created by sajjan on 6/11/14.
 */

'use strict';

moaControllers.CompoundBrowserController = function($scope, Compound, $modal, $location,$log) {
    /**
     * contains all local objects
     * @type {Array}
     */
    $scope.compounds = [];

    /**
     * display all our compounds
     */
    $scope.listCompounds = list();

    /**
     * show the currently selected sprectra
     * @param inchikey
     */
    $scope.viewSpectra = function(inchikey) {
        $log.debug("#/spectra/"+ inchikey);
        $location.path("/spectra/browse/"+ inchikey);
    };


    /**
     * helper function
     */
    function list() {
        $scope.compounds = Compound.query(function (data) {
        }, function (error) {
            alert('failed: ' + error);
        });
    }
};