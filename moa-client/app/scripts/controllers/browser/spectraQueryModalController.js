/**
 * Created by wohlgemuth on 7/11/14.
 */
moaControllers.QuerySpectrumModalController = function ($scope, $modalInstance, SpectraQueryBuilderService, $log, $http, REST_BACKEND_SERVER, AppCache) {
    $scope.metadata = {};
    $scope.metadataNames = [];


    /**
     * Store accordion status
     * @type {{name: boolean}}
     */
    $scope.queryAccordion = { name: true };


    /**
     * List of tags loaded from the REST api
     * @type {Array}
     */
    $scope.tags = [];

    /**
     * Tags selected in query window
     * @type {{}}
     */
    $scope.selectedTags = {};

    $scope.tagClass = function(tag) {
        var tagClass = [];

        if($scope.selectedTags[tag.text]) {
            tagClass.push('btn-primary');
        } else {
            tagClass.push('btn-default');
        }

        if($scope.maxTagsCount != 0) {
            if (tag.spectraCount / $scope.maxTagsCount < 0.25) {
                tagClass.push('btn-xs');
            } else if (tag.spectraCount / $scope.maxTagsCount < 0.5) {
                tagClass.push('btn-sm');
            } else if (tag.spectraCount / $scope.maxTagsCount > 0.75) {
                tagClass.push('btn-lg');
            }
        }

        return tagClass;
    };

    $scope.selectTag = function(tag) {
        $scope.selectedTags[tag.text] = $scope.selectedTags[tag.text] ? false : true;
    };


    /**
     * Store all metadata query data
     * @type {{name: string, value: string}[]}
     */
    $scope.metadataQuery = [
        { name: '', value: '' }
    ];

    /**
     * Add new metadata query field pair
     */
    $scope.addMetadataQuery = function() {
        $scope.metadataQuery.push({ name: '', value: '' });
    };


    /**
     * contains our build query object
     * @type {{}}
     */
    $scope.query = {};

    $scope.cancelDialog = function () {
        $modalInstance.dismiss('cancel');
    };

    /**
     * closes the dialog and finishes and builds the query
     */
    $scope.submitQuery = function() {
        // Add metadata queries to query object
        for(var i = 0; i < $scope.metadataQuery.length; i++) {
            if($scope.metadataQuery[i].name && $scope.metadataQuery[i].name != '') {
                $scope.query[$scope.metadataQuery[i].name] = $scope.metadataQuery[i].value;
            }
        }

        // Add selected tags to query
        var tags = [];

        for (var key in $scope.selectedTags) {
            if ($scope.selectedTags.hasOwnProperty(key) && $scope.selectedTags[key]) {
                tags.push(key);
            }
        }



        var result = SpectraQueryBuilderService.compileQuery($scope.query, tags);
        $modalInstance.close(result);
    };

    /**
     * perform metadata query
     */
    $scope.queryMetadataValues = function (name, value) {
        return $http.post(REST_BACKEND_SERVER + '/rest/meta/data/search?max=10', {
            query: {
                name: name,
                value: {ilike: '%' + value + '%'},
                property: 'stringValue'
            }
        }).then(function(data) {
            return data.data;
        });
    };


    /**
     * initialization and population of default values
     */
    (function list() {
        $scope.metadata = {};

        AppCache.getTags(function(data) {
            $scope.tags = data;
            $scope.maxTagsCount = 0;

            for(var i = 0; i < data.length; i++) {
                if(data[i].spectraCount > $scope.maxTagsCount)
                    $scope.maxTagsCount = data[i].spectraCount;
            }
        });

        AppCache.getMetadata(function(data) {
            var metadataNames = {};

            for(var i = 0; i < data.length; i++) {
                if(data[i].category.visible) {
                    var name = data[i].category.name;

                    metadataNames[data[i].name] = true;

                    if (!$scope.metadata.hasOwnProperty(name)) {
                        $scope.metadata[name] = [];
                    }

                    $scope.metadata[name].push(data[i]);
                }
            }

            $scope.metadataNames = Object.keys(metadataNames);
        });
    })();

};


/**
 * TODO
 * FIX MULTIPLE META FIELDS ON SERVER SIDE
 */
app.filter('unique', function() {
    return function(input, key) {
        var unique = {};
        var uniqueList = [];
        if(input != null) {
            for (var i = 0; i < input.length; i++) {
                if (typeof unique[input[i][key]] == "undefined") {
                    unique[input[i][key]] = "";
                    uniqueList.push(input[i]);
                }
            }
        }
        return uniqueList;
    };
});