/**
 * Created by wohlgemuth on 10/16/14.
 */

(function() {
    'use strict';

    displayCompoundInfoController.$inject = ['$scope', '$log', 'dialogs', '$filter'];
    angular.module('moaClientApp')
        .directive('displayCompoundInfo', displayCompoundInfo);

    function displayCompoundInfo() {
        return {
            require: "ngModel",
            restrict: "A",
            replace: true,
            scope: {
                compound: '=compound'
            },
            templateUrl: '/views/compounds/display/template/displayCompound.html',
            controller: displayCompoundInfoController
        };
    }

    /* @ngInject */
    function displayCompoundInfoController($scope, $log, dialogs, $filter) {
        //calculate some unique id for the compound picture
        $scope.pictureId = Math.floor(Math.random() * 100000);
        $scope.chemId = Math.floor(Math.random() * 100000);


        /**
         * Emulate the downloading of a file given its contents and name
         * @param data
         * @param filetype
         * @param mimetype
         */
        $scope.downloadData = function(data, filetype, mimetype) {
            // Identify and sanitize filename
            var inchikeys = $scope.compound.metaData.filter(function(x) {
                return x.name == 'InChIKey';
            });

            var filename = (inchikeys.length > 0) ? inchikeys[0].value :
                angular.isDefined($scope.compound.inchiKey) ? $scope.compound.inchiKey : $scope.compound.names[0].name;

            filename = filename.replace(/[^a-z0-9\-]/gi, '_');

            // Emulate download
            var hiddenElement = document.createElement('a');

            hiddenElement.href = 'data:'+ mimetype +',' + encodeURI(data);
            hiddenElement.target = '_blank';
            hiddenElement.download = filename +'.'+ filetype;

            document.body.appendChild(hiddenElement);
            hiddenElement.click();
            document.body.removeChild(hiddenElement);
        };

        $scope.downloadAsMOL = function() {
            if (angular.isDefined($scope.compound.molFile)) {
                $scope.downloadData($scope.compound.molFile, 'mol', 'chemical/x-mdl-molfile');
            } else {
                dialogs.error('Error generating MOL file', 'MOL file is unavailable!', {size: 'md', backdrop: 'static'});
            }
        };

        $scope.downloadAsJSON = function() {
            $scope.downloadData($filter('json')($scope.compound), 'json', 'application/json');
        };
    }
})();
