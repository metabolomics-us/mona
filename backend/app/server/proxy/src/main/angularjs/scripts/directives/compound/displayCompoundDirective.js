/**
 * Created by wohlgemuth on 10/16/14.
 */

(function() {
    'use strict';

    displayCompoundInfoController.$inject = ['$scope', '$log', 'dialogs', '$filter', '$timeout'];
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
            templateUrl: '/views/compounds/displayCompound.html',
            controller: displayCompoundInfoController
        };
    }

    /* @ngInject */
    function displayCompoundInfoController($scope, $log, dialogs, $filter, $timeout) {
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

        // Build compound classification tree
        $timeout(function() {
            // Get high order classifications
            var classes = ['kingdom', 'superclass', 'class', 'subclass']
                .map(function(value) {
                    var filteredData = $scope.compound.classification.filter(function(x) { return x.name == value; });
                    return filteredData.length > 0? filteredData[0] : null;
                }).filter(function(x) { return x != null; });

            // Get intermediate classifications
            var intermediate_parents = $scope.compound.classification
                .filter(function(x) { return x.name.indexOf('direct parent level') == 0; })
                .map(function(x, i) {
                    x.name = 'intermediate parent '+ (i + 1);
                    return x;
                });

            classes = classes.concat(intermediate_parents);

            // Get parent classes
            var direct_parent = $scope.compound.classification.filter(function(x) { return x.name == 'direct parent'; });
            var alternate_parents = $scope.compound.classification.filter(function(x) { return x.name == 'alternative parent'; });

            var parents = direct_parent;//.concat(alternate_parents);

            if (parents) {
                $scope.directParent = parents[0];
            }

            // Build tree
            if (classes) {
                var node = null;

                for (var i = classes.length - 1; i >= 0; i--) {
                    if (i == classes.length - 1) {
                        classes[i].nodes = parents;
                    } else {
                        classes[i].nodes = [classes[i + 1]];
                    }
                }

                $scope.classifications = [classes[0]];
            }
        })
    }
})();
