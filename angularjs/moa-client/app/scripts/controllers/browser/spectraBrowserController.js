/**
 * Created by sajjan on 6/11/14.
 */

'use strict';

moaControllers.SpectraBrowserController = function($scope, Spectrum, Compound, TaggingService, $modal, $routeParams) {
    /**
     * contains all local objects
     * @type {Array}
     */
    $scope.spectra = [];
    $scope.tags = [];
    $scope.tagsSelection = [];



    // Name filter
    $scope.nameFilters = {};

    // Add name to filter if given in route
    if($routeParams.name)
        $scope.nameFilters[$routeParams.name] = true;

    $scope.addNameFilter = function() {
        if($scope.spectraQuery.newNameFilter.$valid) {
            $scope.nameFilters[$scope.newNameFilter] = true;
            $scope.newNameFilter = '';
        }
    };

    $scope.removeNameFilter = function(name) { delete $scope.nameFilters[name]; };


    // Full inchi filter
    $scope.inchiFilters = {};

    // Add inchikey to filter if given in route
    if($routeParams.inchikey)
        $scope.inchiFilters[$routeParams.inchikey] = true;

    $scope.addInchiFilter = function() {
        if($scope.spectraQuery.newInchiFilter.$valid) {
            $scope.inchiFilters[$scope.newInchiFilter] = true;
            $scope.newInchiFilter = '';
        }
    };

    $scope.removeInchiFilter = function(inchikey) { delete $scope.inchiFilters[inchikey]; };


    // Partial inchi filter
    $scope.partialInchiFilters = {};

    $scope.addPartialInchiFilter = function() {
        if($scope.spectraQuery.newInchiPartialFilter.$valid) {
            $scope.partialInchiFilters[$scope.newInchiPartialFilter] = true;
            $scope.newInchiPartialFilter = '';
        }
    };

    $scope.removePartialInchiFilter = function(inchikey) { delete $scope.partialInchiFilters[inchikey]; };



    /**
     * displays the spectrum for the given index
     * @param index
     */
    $scope.viewSpectrum = function(index) {
        var modalInstance = $modal.open({
            templateUrl: '/views/spectra/display/viewSpectrum.html',
            controller: moaControllers.ViewSpectrumModalController,
            size: 'lg',
            backdrop: 'true',
            resolve: {
                spectrum: function () {
                    return $scope.spectra[index];
                }
            }
        });
    };



    /**
     * list all our submitters in the system
     */
    $scope.listCompounds = list();

    /**
     * helper function
     */
    function list() {
        if($routeParams.inchikey) {
            $scope.spectra = Spectrum.query(function (data) {
            }, function (error) {
                alert('failed: ' + error);
            });
        } else {
            $scope.spectra = Spectrum.query(function (data) {
            }, function (error) {
                alert('failed: ' + error);
            });
        }

        $scope.tagsSelection = $scope.tags = TaggingService.query(function (data) {
        }, function (error) {
            alert('failed: ' + error);
        });

    }
};