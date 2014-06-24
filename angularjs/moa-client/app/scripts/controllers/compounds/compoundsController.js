/**
 * Created by sajjan on 6/11/14.
 */

'use strict';

moaControllers.CompoundsController = function ($scope, Spectrum, $modal) {
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
        $scope.compounds = Spectrum.query(function (data) {
        }, function (error) {
            alert('failed: ' + error);
        })
    }
}