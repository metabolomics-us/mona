/**
 * Created by sajjan on 6/11/14.
 */

'use strict';

moaControllers.CompoundsController = function ($scope, SpectrumService, $modal) {
    /**
     * contains all local objects
     * @type {Array}
     */
    $scope.spectra = [];

    /**
     * list all our submitters in the system
     */
    $scope.listSpectra = list();

    /**
     * helper function
     */
    function list() {
        $scope.spectra = SpectrumService.getSpectra()
        console.log($scope.spectra)
    }
}