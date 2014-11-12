/**
 * Created by wohlgemuth on 7/11/14.
 */
moaControllers.QuerySpectrumModalController = function ($scope, $modalInstance, SpectraQueryBuilderService, $log, $http, REST_BACKEND_SERVER, AppCache) {

  /**
   * Store accordion status
   * @type {{name: boolean}}
   */
  $scope.queryAccordion = {name: true};


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
   * Tags selected in query window
   * @type {{}}
   */
  $scope.selectedTags = {};


  /**
   * modifies the display of the tags and the class
   * @param tag
   * @returns {Array}
   */
  $scope.tagClass = function (tag) {
    var tagClass = [];

    if ($scope.selectedTags[tag.text]) {
      tagClass.push('btn-primary');
    } else {
      tagClass.push('btn-default');
    }

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

  $scope.selectTag = function (tag) {
    $scope.selectedTags[tag.text] = $scope.selectedTags[tag.text] ? false : true;
  };


  /**
   * Store all metadata query data
   * @type {{name: string, value: string}[]}
   */
  $scope.metadataQuery = [
    {name: '', value: ''}
  ];


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
  $scope.submitQuery = function () {

    //compile initial query
    SpectraQueryBuilderService.compileQuery($scope.query);

    //refine by metadata
    for (var i = 0; i < $scope.metadataQuery.length; i++) {
      SpectraQueryBuilderService.addMetaDataToQuery($scope.metadataQuery[i]);
    }

    //add tags to query
    for (var key in $scope.selectedTags) {
      if ($scope.selectedTags.hasOwnProperty(key) && $scope.selectedTags[key]) {
        SpectraQueryBuilderService.addTagToQuery(key);
      }
    }

    //submit the final query
    $modalInstance.close(SpectraQueryBuilderService.getQuery());
  };


  /**
   * initialization and population of default values
   */
  (function list() {

    AppCache.getTags(function (data) {
      $scope.tags = data;
    });

    AppCache.getTagsStatistics(function (data) {
      $scope.maxTagsCount = 0;
      $scope.tagsCount = {};

      for (var i = 0; i < data.length; i++) {
        $scope.tagsCount[data[i].tag] = data[i].count;

        if (data[i].count > $scope.maxTagsCount)
          $scope.maxTagsCount = data[i].count;
      }
    });

  })();

};


/**
 * TODO
 * FIX MULTIPLE META FIELDS ON SERVER SIDE
 */
app.filter('unique', function () {
  return function (input, key) {
    var unique = {};
    var uniqueList = [];
    if (input != null) {
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
