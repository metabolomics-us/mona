/**
 * Created by Gert on 5/30/2014.
 */
'use strict';

/**
 * a directive to draw or display chemical formulas
 */
app.directive('chemicalSketcher', function (CTSService) {
    return {
        restrict: "A",
        replace: false,
        require: 'ngModel',

        scope: {
            //id of the object
            id: '@id',
            //our model
            bindModel: '=ngModel',
            //is it a viewer
            readonly: '=?',
            //wished width
            width: '=?',
            //wished height
            height: '=?'
        },
        /**
         * links our sketcher into the directive
         * @param $scope
         * @param element
         * @param attrs
         * @param ngModel
         */
        link: function ($scope, element, attrs, ngModel) {

            /**
             * default properties
             * @type {number|*}
             */
            $scope.width = $scope.width || 500;
            $scope.height = $scope.height || 300;
            $scope.readonly = $scope.readonly || false;


            //only render if we got an id object
            if (angular.isDefined($scope.id)) {
                var myId = $scope.id + '_sketcher';
                element.append('<canvas id="' + myId + '"></canvas>');


                var sketcher = null;

                //we can only view
                if ($scope.readonly) {
                    sketcher = new ChemDoodle.ViewerCanvas(myId, $scope.width, $scope.height);
                }
                //we can draw
                else {
                    sketcher = new ChemDoodle.SketcherCanvas(myId, $scope.width, $scope.height, {useServices: false, oneMolecule: true});
                }

                sketcher.specs.atoms_displayTerminalCarbonLabels_2D = true;
                sketcher.specs.atoms_useJMOLColors = true;
                sketcher.specs.bonds_clearOverlaps_2D = true;

                /**
                 * loads the molecule for the given key
                 * @param key
                 */
                var getMoleculeForInchiKey = function (key) {
                    CTSService.convertInchiKeyToMol(key).then(function (data) {
                        if (angular.isDefined(data.molecule) && data.molecule != '') {

                            var mol = ChemDoodle.readMOL(data.molecule);
                            sketcher.loadMolecule(mol);

                        }
                    });
                };

                /**
                 * get an intial value, which was set in our model.
                 */
                if (angular.isDefined($scope.bindModel)) {
                    getMoleculeForInchiKey($scope.bindModel);
                }

                /**
                 * get the actual molecule information and tell our parent scope that this value needs updating. Obviously in case of a read only sketcher we ignore it
                 */
                if ($scope.readonly == false) {
                    sketcher.click = function () {


                        //make sure everything is in the angluar context

                        $scope.$apply(
                            function () {
                                var mol = sketcher.getMolecule();
                                var molFile = ChemDoodle.writeMOL(mol);

                                CTSService.convertToInchiKey(molFile).then(function (data) {

                                    $scope.bindModel = data.key;

                                });
                            }
                        );


                    };
                }

                /**
                 * tracks changes to the model and if it's changes attempt to draw the structure
                 */
                $scope.$watch(function () {
                    return ngModel.$modelValue;
                }, function (newValue, oldValue) {

                    if (newValue != oldValue) {
                        getMoleculeForInchiKey(newValue);

                    }
                });


                /**
                 * destroy our sketcher - doesn't work
                 */
                $scope.$on("$destroy", function () {
                    sketcher = null;
                    var sameLevelElems = element.children();

                    for (var i = 0; i < sameLevelElems.length; i++) {
                        sameLevelElems[i].remove();
                    }
                });


            }

        }
    };
});