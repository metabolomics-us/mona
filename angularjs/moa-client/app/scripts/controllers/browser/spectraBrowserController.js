/**
 * Created by sajjan on 6/11/14.
 */

'use strict';

moaControllers.SpectraBrowserController = function($scope, Spectrum, TaggingService, $modal) {
    /**
     * contains all local objects
     * @type {Array}
     */
    $scope.spectra = [];


    $scope.tags = [];
    $scope.tagsSelection = [];

    /**
     * list all our submitters in the system
     */
    $scope.listCompounds = list();


    $scope.viewSpectrum = function(id) {
        var modalInstance = $modal.open({
            templateUrl: '/views/browser/viewSpectrum.html',
            controller: moaControllers.ViewSpectrumModalController,
            size: 'lg',
            backdrop: 'true',
            resolve: {
                spectrum: function () {
                    return $scope.spectra[id];
                }
            }
        });
    }


    /**
     * helper function
     */
    function list() {
        $scope.spectra = Spectrum.query(function (data) {
        }, function (error) {
            alert('failed: ' + error);
        })


        $scope.tagsSelection = $scope.tags = TaggingService.query(function (data) {
        }, function (error) {
            alert('failed: ' + error);
        })
    }
}


moaControllers.ViewSpectrumModalController = function ($scope, $modalInstance, spectrum) {
    $scope.spectrum = spectrum;

    $scope.data = $scope.spectrum.spectrum;

    $scope.cancelDialog = function() {
        $modalInstance.dismiss('cancel');
    };
};


app.filter('spectraFilter', function() {

});