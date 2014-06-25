/**
 * Created by sajjan on 6/11/14.
 */

'use strict';

moaControllers.SpectraBrowserController = function($scope, Spectrum, TaggingService, $modal, $routeParams) {
    /**
     * contains all local objects
     * @type {Array}
     */
    $scope.spectra = [];
    $scope.tags = [];
    $scope.tagsSelection = [];



    // Inchi Filter
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

    $scope.removeInchiFilder = function(inchikey) {
        delete $scope.inchiFilters[inchikey];
    }



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
        // InChIKey filter
        var inchikeys = Object.keys($scope.inchiFilters);

        if(inchikeys.length > 0 &&
                inchikeys.indexOf(spectrum.biologicalCompound.inchiKey) == -1 &&
                inchikeys.indexOf(spectrum.chemicalCompound.inchiKey) == -1)
            return false;

        // Tags filter
        var tags = spectrum.tags.map(function(tag) { return tag.text; });
        var selected_tags = $scope.tagsSelection.map(function(tag) { return tag.text; });

        if(intersect(tags, selected_tags).length == 0)
            return false;

        return true;
    };


    $scope.viewSpectrum = function(index) {
        var modalInstance = $modal.open({
            templateUrl: '/views/browser/viewSpectrum.html',
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
        $scope.spectra = Spectrum.query(function (data) {
        }, function (error) {
            alert('failed: ' + error);
        });

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