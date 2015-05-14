/**
 * Created by wohlgemuth on 6/12/14.
 */

/**
 * disables automatic form submission when you press enter in an input element
 */
app.directive('gwTag', function () {
    return {
        //must be an attribute
        restrict: 'A',

        //we want to replace the element
        replace: true,

        //replace the fields text and add it in the internal span
        //transclude: true,

        //our template to use
        templateUrl: '/views/templates/tags.html',

        //scope definition
        scope: {
            ruleBased: '=',
            type: '@',
            tag: '=value',
            size: '@'
        },
        priority: 1001,


        //controller to handle building new queries
        controller: function ($scope, SpectraQueryBuilderService, $location) {
            $scope.options = [];

            if ($scope.type == 'spectrum') {
                $scope.options = [
                    {
                        name: 'Create new query',
                        action: function (tag, status) {
                            //build a mona query based on this label
                            SpectraQueryBuilderService.prepareQuery();
                            SpectraQueryBuilderService.addTagToQuery(tag.text);

                            //run the query and show it's result in the spectra browser
                            $location.path("/spectra/browse/");
                        }
                    },
                    {
                        name: 'Add to query',
                        action: function (tag, status) {
                            SpectraQueryBuilderService.addTagToQuery(tag.text);
                            $location.path("/spectra/browse/");
                        }
                    },
                    {
                        name: 'Remove from query',
                        action: function (tag, status) {
                            SpectraQueryBuilderService.removeTagFromQuery(tag.text);
                            $location.path("/spectra/browse/");
                        }
                    }
                ];
            }
        },

        //decorate our elements based on there properties
        link: function (scope, element, attrs, ctrl) {

            // Set default tag status
            scope.status = {
                active: false
            };

        }
    }
});

/**
 * a directive to display our tags and keep track of selections/deselections
 */
app.directive('gwTagDisplay', function () {
    return {

        replace: true,
        transclude: true,
        templateUrl: '/views/templates/tagsDisplay.html',
        restrict: 'A',
        scope: {
            selectedTags: "=selection"
        },
        link: function ($scope, element, attrs, ngModel) {

        },

        //controller to handle building of the queires
        controller: function ($scope, $element, $log, TaggingService) {

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
            $scope.tagClass = function (tag) {
                var tagClass = [];

                //if something is selected
                if ($scope.selectedTags[tag.text] == '+') {
                    tagClass.push('btn-success');
                }
                else if ($scope.selectedTags[tag.text] == '-') {
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
            $scope.selectTag = function (tag, what) {
                $scope.selectedTags[tag.text] = what;
            };

            /**
             * load initial data
             */
            (function list() {
                $scope.options = [
                    {
                        name: '+ has tag',
                        action: function (tag, status) {
                            $scope.selectTag(tag, '+');
                        }
                    },
                    {
                        name: '- does not have tag',
                        action: function (tag, status) {
                            $scope.selectTag(tag, '-');
                        }
                    }
                ];

                TaggingService.query(
                    function (data) {
                        $scope.tags = data;
                    },
                    function (error) {
                        $log.error('failed: ' + error);
                    }
                );

                TaggingService.statistics(
                    function (data) {
                        $scope.maxTagsCount = 0;
                        $scope.tagsCount = {};

                        for (var i = 0; i < data.length; i++) {
                            $scope.tagsCount[data[i].tag] = data[i].count;

                            if (data[i].count > $scope.maxTagsCount)
                                $scope.maxTagsCount = data[i].count;
                        }
                    },
                    function (error) {
                        $log.error('failed: ' + error);
                    }
                );
            })();
        }
    }
});
