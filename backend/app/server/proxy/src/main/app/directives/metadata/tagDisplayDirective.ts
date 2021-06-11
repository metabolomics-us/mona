/**
 * a directive to display our tags and keep track of selections/deselections
 */

import * as angular from 'angular';

class TagDisplayDirective {
    constructor() {
        return {
            restrict: 'A',
            templateUrl: '../../views/templates/query/tagDisplay.html',
            replace: true,
            transclude: true,
            scope: {
                tags: '='
            },
            controller: TagDisplayController,
            controllerAs: '$ctrl'
        };
    }
}

class TagDisplayController {
    private static $inject = ['$scope', '$log', '$timeout', 'TagService'];
    private $scope;
    private $log;
    private $timeout;
    private TagService;
    private maxTagsCount;

    constructor($scope, $log, $timeout, TagService) {
        this.$scope = $scope;
        this.$log = $log;
        this.$timeout = $timeout;
        this.TagService = TagService;
    }


    $onInit = () => {
        this.$timeout(() => {
            this.maxTagsCount = 0;

            if (angular.isUndefined(this.$scope.tags)) {
                this.$scope.tags = [];

                this.TagService.query(
                    (data) => {
                        this.$scope.tags = data;

                        for (let i = 0; i < data.length; i++) {
                            if (data[i].count > this.maxTagsCount)
                                this.maxTagsCount = data[i].count;
                        }
                    },
                    (error) => {
                        this.$log.error('Tag pull failed: '+ error);
                    }
                );
            } else {
                for (let i = 0; i < this.$scope.tags.length; i++) {
                    if (this.$scope.tags[i].count > this.maxTagsCount)
                        this.maxTagsCount = this.$scope.tags[i].count;
                }
            }
        });
    }

    tagClass = (tag) => {
        let tagClass = [];

        // Button color based on selection
        if (tag.selected === '+') {
            tagClass.push('btn-success');
        } else if (tag.selected === '-') {
            tagClass.push('btn-danger');
        } else {
            tagClass.push('btn-default');
        }

        // Button size based on count
        if (this.maxTagsCount > 0) {
            if (tag.count / this.maxTagsCount < 0.25) {
                tagClass.push('btn-xs');
            } else if (tag.count / this.maxTagsCount < 0.5) {
                tagClass.push('btn-sm');
            } else if (tag.count / this.maxTagsCount > 0.75) {
                tagClass.push('btn-lg');
            }
        }

        return tagClass;
    };

}

angular.module('moaClientApp')
    .directive('tagDisplay', TagDisplayDirective);
