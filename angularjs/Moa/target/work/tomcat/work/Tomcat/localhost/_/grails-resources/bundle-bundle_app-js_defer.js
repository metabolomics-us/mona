/**
 * Main application file
 */
var massspecsOfAmerica = angular.module('massspecsOfAmerica', ['ngRoute', 'ngResource','ui.bootstrap']);

/**
 * Defines all our routes in this application
 */
massspecsOfAmerica.config(['$routeProvider',
    function ($routeProvider) {
        $routeProvider.
            when('/submitters', {
                templateUrl: 'partial/submitters/list.html',
                controller: 'SubmitterController'
            }
        ).
            when('/upload/single', {
                templateUrl: 'partial/upload/single.html',
                controller: 'SpectraController'
            }
        ).
            otherwise({
                redirectTo: '/submitters'
            }
        )
    }
]
);
/**
 * all our services are defined in this section
 */

/**
 * storing and retrieving submitters from the backend
 */
massspecsOfAmerica.factory("Submitter", function ($resource) {
    return $resource(
        "/rest/submitters/:id",
        {id: "@id"},
        {
            'update': {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                }
            }
        }
    )
});

/**
 * these are all our controllers
 */

var controllers = {};

/**
 * handles all interactions with submitters
 * @param $scope
 * @param Submitter
 * @constructor
 */
controllers.SubmitterController = function ($scope, Submitter, $modal) {

    /**
     * contains all local objects
     * @type {Array}
     */
    $scope.submitters = [];

    /**
     * list all our submitters in the system
     */
    $scope.listSubmitter = list();

    /**
     * deletes our submitter from the system
     * @param submitterId
     */
    $scope.remove = function (index) {
        var submitterToRemove = $scope.submitters[index];

        Submitter.delete({id: submitterToRemove.id}, function (data) {

                //remove it from the scope and update our table
                $scope.submitters.splice(index, 1);
            },
            function (errors) {
                alert('oh noes an error...');
            }
        );
    };

    /**
     * displays our dialog to create a new submitter
     */
    $scope.displayCreateDialog = function () {

        var modalInstance = $modal.open({
            templateUrl: '/partial/submitters/dialog/createDialog.html',
            controller: controllers.SubmitterModalController,
            size: 'lg',
            resolve: {
                //just an empty object
                newSubmitter: function () {
                    return {};
                }
            }
        });

        //retrieve the result from the dialog and save it
        modalInstance.result.then(function (submitter) {
            //push our object to the scope now so that our table can show it
            $scope.submitters.push(submitter);
        })
    };

    /**
     * displays the edit dialog for the select submitter
     * @param index
     */
    $scope.displayEditDialog = function (index) {
        var modalInstance = $modal.open({
            templateUrl: '/partial/submitters/dialog/editDialog.html',
            controller: controllers.SubmitterModalController,
            size: 'lg',
            resolve: {
                //populate the dialog with the given submitter at this index
                newSubmitter: function () {
                    return $scope.submitters[index];
                }
            }
        });

        //retrieve the result from the dialog and save it
        modalInstance.result.then(function (submitter) {
            //will be handled automatically by angular js
        });
    };

    /**
     * helper function
     */
    function list() {
        $scope.submitters = Submitter.query(function (data) {
        }, function (error) {
            alert('failed: ' + error);
        })
    }

};

/**
 *
 * general controller for modal dialog, which are used to create submitters
 * @param $scope
 * @param Submitter
 * @param $modalInstance
 * @param newSubmitter
 * @constructor
 */
controllers.SubmitterModalController = function ($scope, Submitter, $modalInstance, newSubmitter) {

    /**
     * contains our results
     * @type {{}}
     */
    $scope.newSubmitter = newSubmitter;

    /**
     * cancels any dialog in this controller
     */
    $scope.cancel = function () {
        $modalInstance.dismiss('cancel');
    };

    /**
     * takes care of updates
     */
    $scope.update = function () {

        var submitter = createSubmitterFromScope();
        //update the submitter
        Submitter.update(submitter, function (data) {
            $modalInstance.close(submitter);
        }, function (error) {
            handleDialogError(error);
        });

    };
    /**
     * takes care of creates
     */
    $scope.create = function () {

        var submitter = createSubmitterFromScope();
        //no submitter id so create a new one
        Submitter.save(submitter, function (savedSubmitter) {
            $modalInstance.close(savedSubmitter);
        }, function (error) {
            handleDialogError(error);
        });
    };

    /**
     * creates our submitter object
     */
    function createSubmitterFromScope() {
        //build our object
        var submitter = new Submitter();
        submitter.firstName = $scope.newSubmitter.firstName;
        submitter.lastName = $scope.newSubmitter.lastName;
        submitter.emailAddress = $scope.newSubmitter.emailAddress;
        submitter.password = $scope.newSubmitter.password;

        if ($scope.newSubmitter.id) {
            submitter.id = $scope.newSubmitter.id;
        }
        return submitter;
    }

    /**
     * handles our dialog errors
     * @param error
     */
    function handleDialogError(error) {
        var errorReport = [];

        if (error.data) {
            for (var i = 0; i < error.data.errors.length; i++) {
                var obj = error.data.errors[i];

                //remove the none needed object
                delete obj.object;
                errorReport.push(obj);

            }

            $scope.formErrors = errorReport;
        }
        else {
            $scope.formErrors = "we had an unexpected error, please check the JS console";
        }
    }
};

/**
 * handles all interactions with spectra
 * @constructor
 */
controllers.SpectraController = function ($scope) {

};

/**
 * handles all our navigations
 * @param $scope
 * @param $location
 * @constructor
 */
controllers.NavigationController = function ($scope, $location) {
    $scope.navClass = function (page) {
        var currentRoute = $location.path().substring(1) || 'home';
        return page === currentRoute ? 'active' : '';
    };
};

//register the actual controllers
massspecsOfAmerica.controller(controllers);


/**
 * this defines a simple data entry form for our sbumitters to create and update them
 */
massspecsOfAmerica.directive("submitterForm", function() {
    return {
        restrict: "E",
        replace: true,
        templateUrl: "/partial/submitters/template/createUpdateForm.html"
    };
});

