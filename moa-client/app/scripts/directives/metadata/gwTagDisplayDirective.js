/**
 * a directive to display our tags and keep track of selections/deselections
 */

(function() {
    'use strict';

    angular.module('moaClientApp')
      .directive('gwTagDisplay', gwTagDisplay)

    function gwTagDisplay() {
        var directive = {
            restrict: 'A',
            templateUrl: '/views/templates/tagsDisplay.html',
            replace: true,
            transclude: true,
            scope: {
                selectedTags: "=selection"
            },
            link: linkFunc,
            controller: gwTagDisplayController
        };

        return directive;
    }


    function linkFunc($scope, element, attrs, ngModel) {

    }

    //controller to handle building of the queires
    gwTagDisplayController.$inject = ['$scope', '$element', '$log', 'TaggingService'];

    function gwTagDisplayController($scope, $element, $log, TaggingService) {

        /**
         * List of tags loaded from the REST api
         * @type {Array}
         */
        $scope.tags = [];

        /**
         * Number of associated spectra for each tag
         * @type {{}}
         */
        $scope.tagsCount = {};

        /**
         * associated options
         * @type {Array}
         */
        $scope.options = [];

        /**
         * calculate the tag class
         * @param tag
         * @returns {Array}
         */
        $scope.tagClass = function(tag) {
            var tagClass = [];

            //if something is selected
            if ($scope.selectedTags[tag.text] === '+') {
                tagClass.push('btn-success');
            }
            else if ($scope.selectedTags[tag.text] === '-') {
                tagClass.push('btn-danger');
            }
            else {
                tagClass.push('btn-default');
                $scope.selectedTags[tag.text] = false;
            }

            //size of our actual tagging value
            if ($scope.maxTagsCount > 0 && $scope.tagsCount.hasOwnProperty(tag.text)) {
                if ($scope.tagsCount[tag.text] / $scope.maxTagsCount < 0.25) {
                    tagClass.push('btn-xs');
                } else if ($scope.tagsCount[tag.text] / $scope.maxTagsCount < 0.5) {
                    tagClass.push('btn-sm');
                } else if ($scope.tagsCount[tag.text] / $scope.maxTagsCount > 0.75) {
                    tagClass.push('btn-lg');
                }
            }

            return tagClass;
        };

        /**
         * selects and highlights the selected tag
         * @param tag
         * @parm what + | - | undefiend
         */
        $scope.selectTag = function(tag, what) {
            $scope.selectedTags[tag.text] = what;
        };

        /**
         * load initial data
         */
        (function list() {
            $scope.options = [
                {
                    name: '+ has tag',
                    action: function(tag, status) {
                        $scope.selectTag(tag, '+');
                    }
                },
                {
                    name: '- does not have tag',
                    action: function(tag, status) {
                        $scope.selectTag(tag, '-');
                    }
                }
            ];

            TaggingService.query(
              function(data) {
                  $scope.tags = data;
              },
              function(error) {
                  $log.error('failed: ' + error);
              }
            );

            TaggingService.statistics(
              function(data) {
                  $scope.maxTagsCount = 0;
                  $scope.tagsCount = {};

                  for (var i = 0; i < data.length; i++) {
                      $scope.tagsCount[data[i].tag] = data[i].count;

                      if (data[i].count > $scope.maxTagsCount)
                          $scope.maxTagsCount = data[i].count;
                  }
              },
              function(error) {
                  $log.error('failed: ' + error);
              }
            );
        })();
    }
})();