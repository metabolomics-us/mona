/**
 * defines a metadata text field combo with autocomplete and typeahead functionality
 */

import * as angular from 'angular';

class GwMetaQueryInputDirective {
    constructor() {
        return {
            restrict: 'E',
            templateUrl: '../../views/templates/metaQueryInput.html',
            replace: true,
            transclude: true,
            scope: {
                query: '=',
                editable: '=?',
                fullText: '=?'
            },
            controller: GwMetaQueryInputController,
            controllerAs: '$ctrl',
            link: (scope, element, attrs, ngModel) => {

            }
        }
    }
}

class GwMetaQueryInputController{
    private static $inject = ['$scope', '$element', 'SpectraQueryBuilderService', '$location', 'REST_BACKEND_SERVER', '$http', '$filter', '$log', 'limitToFilter'];
    private $scope;
    private $element;
    private SpectraQueryBuilderService;
    private $location;
    private REST_BACKEND_SERVER;
    private $http;
    private $filter;
    private $log;
    private limitToFilter;
    private select;

    constructor($scope, $element, SpectraQueryBuilderService, $location, REST_BACKEND_SERVER, $http, $filter, $log, limitToFilter) {
        this.$scope = $scope;
        this.$element = $element;
        this.SpectraQueryBuilderService = SpectraQueryBuilderService;
        this.$location = $location;
        this.REST_BACKEND_SERVER = REST_BACKEND_SERVER;
        this.$http = $http;
        this.$filter = $filter;
        this.$log = $log;
        this.limitToFilter = limitToFilter;
    }

    $onInit = () => {
        this.select = [
            {name: "equal", value: "eq"},
            {name: "not equal", value: "ne"},
            {name: "like", value: "match"}
        ];

        if (!angular.isDefined(this.$scope.query)) {
            this.$scope.query = [];
        }

        // Set blank entry if query list is empty
        if (this.$scope.query.length === 0) {
            this.addMetadataQuery();
        }

        // Set editable option if not set
        if (!angular.isDefined(this.$scope.editable)) {
            this.$scope.editable = false;
        }
    }

    /**
     * tries to find meta data names for us
     * @param value
     */
    queryMetadataNames = (value) => {
        if (angular.isUndefined(value) || value.replace(/^\s*/, '').replace(/\s*$/, '') === '') {
            return this.$http.get(this.REST_BACKEND_SERVER + '/rest/meta/searchNames/', {}).then((data) => {
                return this.limitToFilter(data.data, 50);
            });

        }
        else {
            return this.$http.get(this.REST_BACKEND_SERVER + '/rest/meta/searchNames/' + value + "?max=10", {}).then((data) => {
                return this.limitToFilter(data.data, 25);
            });
        }
    };

    /**
     * queries our values
     * @param name
     * @param value
     */
    queryMetadataValues = (name, value) => {

        if (angular.isUndefined(value) || value.replace(/^\s*/, '').replace(/\s*$/, '') === '') {
            return this.$http.post(this.REST_BACKEND_SERVER + '/rest/meta/data/search', {
                query: {
                    name: name,
                    value: {isNotNull: ''},
                    property: 'stringValue',
                    deleted: false
                }
            }).then((data) => {
                return this.limitToFilter(data.data, 25);
            });

        }
        else if (angular.isDefined(this.$scope.fullText)) {
            return this.$http.post(this.REST_BACKEND_SERVER + '/rest/meta/data/search?max=10', {
                query: {
                    name: name,
                    value: {ilike: '%' + value + '%'},
                    property: 'stringValue',
                    deleted: false
                }
            }).then((data) => {
                return this.limitToFilter(data.data, 25);
            });
        }
        else {
            return this.$http.post(this.REST_BACKEND_SERVER + '/rest/meta/data/search?max=10', {
                query: {
                    name: name,
                    value: {ilike: value + '%'},
                    property: 'stringValue',
                    deleted: false

                }
            }).then((data) => {
                return this.limitToFilter(data.data, 25);
            });
        }

    };

    isNumber = (n) => {
        return !isNaN(parseFloat(n)) && isFinite(n);
    };

    /**
     * adds a metadata query
     */
    addMetadataQuery = () => {
        this.$scope.query.push({name: '', value: '', selected: this.select[0]});
    };
}

angular.module('moaClientApp')
    .directive('gwMetaQueryInput', GwMetaQueryInputDirective);

