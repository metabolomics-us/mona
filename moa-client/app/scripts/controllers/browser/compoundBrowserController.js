/**
 * Created by sajjan on 6/11/14.
 */

'use strict';

moaControllers.CompoundBrowserController = function($scope, Compound, $location, SpectraQueryBuilderService) {
    /**
     * contains all local objects
     * @type {Array}
     */
    $scope.compounds = [];


    /**
     * show the currently selected spectra based on inchikey
     * @param inchikey
     */
    $scope.viewSpectra = function(inchikey) {
        SpectraQueryBuilderService.compileQuery({ inchiFilter: inchikey });
        $location.path('/spectra/browse/');
    };


    /**
     * loads more compounds into the view using our query object
     */
    $scope.compoundLoadLength = -1;

    $scope.loadingMore = false;

    $scope.loadMoreCompounds = function () {
        if ($scope.compoundLoadLength != $scope.compounds.length) {
            $scope.compoundLoadLength = $scope.compounds.length;
            $scope.loadingMore = true;

            Compound.query(
                {offset: '&offset='+ $scope.compounds.length},
                function (data) {
                    $scope.compounds.push.apply($scope.compounds, data);
                    $scope.loadingMore = false;
                }
            );
        }
    };
};