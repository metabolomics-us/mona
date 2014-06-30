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
            $scope.newNameFilter = ''
        }
    };

    $scope.removeNameFilter = function(name) {
        delete $scope.nameFilters[name];
    };


    // Full inchi filter
    $scope.inchiFilters = {};

    // Add inchikey to filter if given in route
    if($routeParams.inchikey)
        $scope.inchiFilters[$routeParams.inchikey] = true;

    $scope.addInchiFilter = function() {
        if($scope.spectraQuery.newInchiFilter.$valid) {
            $scope.inchiFilters[$scope.newInchiFilter] = true;
            $scope.newInchiFilter = ''
        }
    };

    $scope.removeInchiFilter = function(inchikey) {
        delete $scope.inchiFilters[inchikey];
    };


    // Partial inchi filter
    $scope.partialInchiFilters = {};

    $scope.addPartialInchiFilter = function() {
        if($scope.spectraQuery.newInchiPartialFilter.$valid) {
            $scope.partialInchiFilters[$scope.newInchiPartialFilter] = true;
            $scope.newInchiPartialFilter = ''
        }
    };

    $scope.removePartialInchiFilter = function(inchikey) {
        delete $scope.partialInchiFilters[inchikey];
    };



    // Query methods
    function intersect(arr1, arr2) {
        var r = [], o = {}, l = arr2.length, i, v;

        for (i = 0; i < l; i++)
            o[arr2[i]] = true;

        l = arr1.length;

        for (i = 0; i < l; i++) {
            v = arr1[i];
            if (v in o)
                r.push(v);
        }
        return r;
    }

    $scope.query = function(spectrum) {
        // Name filter
        var names = Object.keys($scope.nameFilters);

        var matchedAny = names.some(function(element, index, array) {
            for(var i = 0; i < spectrum.biologicalCompound.names.length; i++)
                if(spectrum.biologicalCompound.names[i].name.indexOf(element) > -1)
                    return true;

            for(var i = 0; i < spectrum.chemicalCompound.names.length; i++)
                if(spectrum.chemicalCompound.names[i].name.indexOf(element) > -1)
                    return true;

            return false;
        });

        if(names.length > 0 && !matchedAny)
            return false;


        // Full InChIKey filter
        var inchikeys = Object.keys($scope.inchiFilters);

        if(inchikeys.length > 0 &&
            inchikeys.indexOf(spectrum.biologicalCompound.inchiKey) == -1 &&
            inchikeys.indexOf(spectrum.chemicalCompound.inchiKey) == -1)
            return false;


        // Partial InChIKey filter
        inchikeys = Object.keys($scope.partialInchiFilters);

        matchedAny = inchikeys.some(function(element, index, array) {
            if(spectrum.biologicalCompound.inchiKey.indexOf(element) > -1 ||
                spectrum.chemicalCompound.inchiKey.indexOf(element) > -1)
                return true;
            return false;
        });

        if(inchikeys.length > 0 && !matchedAny)
            return false;


        // Tags filter
        var tags = spectrum.tags.map(function(tag) { return tag.text; });
        var selected_tags = $scope.tagsSelection.map(function(tag) { return tag.text; });

        if(intersect(tags, selected_tags).length == 0)
            return false;


        return true;
    };


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


moaControllers.ViewSpectrumModalController = function ($scope, $modalInstance, spectrum) {
    $scope.spectrum = spectrum;

    $scope.cancelDialog = function() {
        $modalInstance.dismiss('cancel');
    };
};