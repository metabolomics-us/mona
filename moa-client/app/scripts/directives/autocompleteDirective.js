/**
 * Created by Gert on 6/7/2014.
 *
 * based on: https://github.com/darylrowland/angucomplete
 * and slightly modified.
 *
 * added features:
 *
 * we are taking our given query string as succesful result, if the user presses enter.
 * possible to define a name for the generated field, so that we can watch it
 */

(function() {
    'use strict';

    angular.module('moaClientApp')
      .directive('flAngucomplete', flAngucomplete);

    flAngucomplete.$inject = ['$parse', '$http', '$sce', '$timeout'];

    function flAngucomplete($parse, $http, $sce, $timeout) {
        var directive = {
            restrict: 'A',
            replace: false,
            /**
             * generates our field
             */
            templateUrl: '/views/autocomplete.html',
            scope: {
                "id": "@inputid",
                "name": "&inputname",
                "placeholder": "@",
                "selectedObject": "=selectedobject",
                "url": "@",
                "dataField": "@datafield",
                "titleField": "@titlefield",
                "descriptionField": "@descriptionfield",
                "imageField": "@imagefield",
                "inputClass": "@inputclass",
                "userPause": "@pause",
                "localData": "=localdata",
                "searchFields": "@searchfields",
                "minLengthUser": "@minlength",
                "matchClass": "@matchclass",
                "required": "@isrequired"
            },
            /**
             * does the actual linking
             * @param $scope
             * @param elem
             * @param attrs
             */
            link: function($scope, elem, attrs) {
                $scope.lastSearchTerm = null;
                $scope.currentIndex = null;
                $scope.justChanged = false;
                $scope.searchTimer = null;
                $scope.hideTimer = null;
                $scope.searching = false;
                $scope.pause = 500;
                $scope.minLength = 1;
                $scope.searchStr = null;

                if ($scope.minLengthUser && $scope.minLengthUser !== "") {
                    $scope.minLength = $scope.minLengthUser;
                }

                if ($scope.userPause) {
                    $scope.pause = $scope.userPause;
                }

                /**
                 * are we requiring to update our search
                 * @param newTerm
                 * @param oldTerm
                 * @returns {boolean}
                 */
                var isNewSearchNeeded = function(newTerm, oldTerm) {
                    return newTerm.length >= $scope.minLength && newTerm !== oldTerm
                };

                /**
                 * processes the actual results of the search
                 * @param responseData
                 * @param str
                 */
                $scope.processResults = function(responseData, str) {
                    if (responseData && responseData.length > 0) {
                        $scope.results = [];

                        var titleFields = [];
                        if ($scope.titleField && $scope.titleField !== "") {
                            titleFields = $scope.titleField.split(",");
                        }

                        for (var i = 0; i < responseData.length; i++) {
                            // Get title variables
                            var titleCode = [];

                            for (var t = 0; t < titleFields.length; t++) {
                                titleCode.push(responseData[i][titleFields[t]]);
                            }

                            var description = "";
                            if ($scope.descriptionField) {
                                description = responseData[i][$scope.descriptionField];
                            }

                            var image = "";
                            if ($scope.imageField) {
                                image = responseData[i][$scope.imageField];
                            }

                            var text = titleCode.join(' ');
                            if ($scope.matchClass) {
                                var re = new RegExp(str, 'i');
                                var strPart = text.match(re)[0];
                                text = $sce.trustAsHtml(text.replace(re, '<span class="' + $scope.matchClass + '">' + strPart + '</span>'));
                            }

                            //assign our result object
                            $scope.results[$scope.results.length] = {
                                title: text,
                                description: description,
                                image: image,
                                originalObject: responseData[i]
                            };
                        }


                    } else {
                        $scope.results = [];
                    }
                };

                /**
                 * executes the search ones the timer completes
                 * @param str
                 */
                $scope.searchTimerComplete = function(str) {
                    // Begin the search

                    if (str.length >= $scope.minLength) {
                        if ($scope.localData) {
                            var searchFields = $scope.searchFields.split(",");

                            var matches = [];

                            for (var i = 0; i < $scope.localData.length; i++) {
                                var match = false;

                                for (var s = 0; s < searchFields.length; s++) {
                                    match = match || (typeof $scope.localData[i][searchFields[s]] === 'string' && typeof str === 'string' && $scope.localData[i][searchFields[s]].toLowerCase().indexOf(str.toLowerCase()) >= 0);
                                }

                                if (match) {
                                    matches[matches.length] = $scope.localData[i];
                                }
                            }

                            $scope.searching = false;
                            $scope.processResults(matches, str);

                        } else {
                            $http.get($scope.url + str, {}).
                              success(function(responseData, status, headers, config) {
                                  $scope.searching = false;
                                  $scope.processResults((($scope.dataField) ? responseData[$scope.dataField] : responseData ), str);
                              }).
                              error(function(data, status, headers, config) {
                                  console.log("error");
                              });
                        }
                    }
                };

                $scope.hideResults = function() {

                    $scope.hideTimer = $timeout(function() {
                        $scope.showDropdown = false;
                    }, $scope.pause);
                };

                $scope.resetHideResults = function() {
                    if ($scope.hideTimer) {
                        $timeout.cancel($scope.hideTimer);
                    }
                };

                /**
                 * if we hover over a row. we need to execute this action
                 * @param index
                 */
                $scope.hoverRow = function(index) {
                    $scope.currentIndex = index;

                };

                /**
                 * if we press an enter key or so
                 * @param event
                 */
                $scope.keyPressed = function(event) {
                    if (!(event.which === 38 || event.which === 40 || event.which === 13)) {
                        if (!$scope.searchStr || $scope.searchStr === "") {
                            $scope.showDropdown = false;
                            $scope.lastSearchTerm = null
                        } else if (isNewSearchNeeded($scope.searchStr, $scope.lastSearchTerm)) {
                            $scope.lastSearchTerm = $scope.searchStr;
                            $scope.showDropdown = true;
                            $scope.currentIndex = -1;
                            $scope.results = [];

                            if ($scope.searchTimer) {
                                $timeout.cancel($scope.searchTimer);
                            }

                            $scope.searching = true;

                            $scope.searchTimer = $timeout(function() {
                                $scope.searchTimerComplete($scope.searchStr);
                            }, $scope.pause);
                        }
                    } else {
                        event.preventDefault();
                    }
                };

                /**
                 * actually assign the result
                 * @param result
                 */
                $scope.selectResult = function(result) {
                    if ($scope.matchClass) {
                        result.title = result.title.toString().replace(/(<([^>]+)>)/ig, '');
                    }
                    $scope.searchStr = result.title;
                    $scope.selectedObject = result.title;
                    $scope.showDropdown = false;
                    $scope.results = [];
                };

                var inputField = elem.find('input');

                inputField.on('keyup', $scope.keyPressed);

                elem.on("click", function(event) {
                    $scope.showDropdown = true;

                    if (!$scope.searchStr || $scope.searchStr === "") {
                        var max = $scope.localData.length <= 10 ? $scope.localData.length : 10;
                        var results = [];

                        for (var i = 0; i < max; i++)
                            results.push($scope.localData[i]);

                        $scope.processResults(results, "");
                    }

                    $scope.$apply();
                    event.preventDefault;
                    event.stopPropagation();
                });

                elem.on("keyup", function(event) {
                    if (event.which === 40) {
                        if ($scope.results && ($scope.currentIndex + 1) < $scope.results.length) {
                            $scope.currentIndex++;
                            $scope.$apply();
                            event.preventDefault;
                            event.stopPropagation();
                        }

                        $scope.$apply();
                    } else if (event.which === 38) {
                        if ($scope.currentIndex >= 1) {
                            $scope.currentIndex--;
                            $scope.$apply();
                            event.preventDefault;
                            event.stopPropagation();
                        }

                    } else if (event.which === 13) {
                        if ($scope.results && $scope.currentIndex >= 0 && $scope.currentIndex < $scope.results.length) {
                            $scope.selectResult($scope.results[$scope.currentIndex]);
                            $scope.$apply();
                            event.preventDefault;
                            event.stopPropagation();
                        } else {

                            //object is needed to look like if we actual return from auto complete instead of utilizing a new object
                            var originalResponse = {};
                            originalResponse[$scope.titleField] = $scope.searchStr;

                            $scope.results = [];

                            $scope.selectResult({
                                title: $scope.searchStr,
                                description: "",
                                image: "",
                                originalObject: originalResponse
                            });

                            //no dropdown needed since we decided to add our own object
                            $scope.showDropdown = false;

                            $scope.$apply();
                            event.preventDefault;
                            event.stopPropagation();
                        }

                    } else if (event.which === 27) {
                        $scope.results = [];
                        $scope.showDropdown = false;
                        $scope.$apply();
                    } else if (event.which === 8) {
                        $scope.selectedObject = null;
                        $scope.$apply();
                    }
                });

            }
        };

        return directive;
    }
})();