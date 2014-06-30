/**
 * Created by sajjan on 6/11/14.
 */

'use strict';

moaControllers.CompoundBrowserController = function($scope, Compound, $modal, $location, $routeParams, $log) {
    /**
     * contains all local objects
     * @type {Array}
     */
    $scope.compounds = [];



    // Name filter
    $scope.nameFilters = {};

    // Add name to filter if given in route
    if($routeParams.name)
        $scope.nameFilters[$routeParams.name] = true;

    $scope.addNameFilter = function() {
        if($scope.compoundsQuery.newNameFilter.$valid) {
            $scope.nameFilters[$scope.newNameFilter] = true;
            $scope.newNameFilter = ''
        }
    };

    $scope.removeNameFilter = function(name) { delete $scope.nameFilters[name]; };


    // Full inchi filter
    $scope.inchiFilters = {};

    // Add inchikey to filter if given in route
    if($routeParams.inchikey)
        $scope.inchiFilters[$routeParams.inchikey] = true;

    $scope.addInchiFilter = function() {
        if($scope.compoundsQuery.newInchiFilter.$valid) {
            $scope.inchiFilters[$scope.newInchiFilter] = true;
            $scope.newInchiFilter = ''
        }
    };

    $scope.removeInchiFilter = function(inchikey) { delete $scope.inchiFilters[inchikey]; };


    // Partial inchi filter
    $scope.partialInchiFilters = {};

    $scope.addPartialInchiFilter = function() {
        if($scope.compoundsQuery.newInchiPartialFilter.$valid) {
            $scope.partialInchiFilters[$scope.newInchiPartialFilter] = true;
            $scope.newInchiPartialFilter = ''
        }
    };

    $scope.removePartialInchiFilter = function(inchikey) { delete $scope.partialInchiFilters[inchikey]; };



    /**
     * show the currently selected sprectra
     * @param inchikey
     */
    $scope.viewSpectra = function(inchikey) {
        $log.debug("#/spectra/"+ inchikey);
        $location.path("/spectra/browse/"+ inchikey);
    };


    /**
     * display all our compounds
     */
    $scope.listCompounds = list();

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