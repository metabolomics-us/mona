/**
 * Created by Gert on 5/30/2014.
 */
'use strict';

/**
 * a directive to draw or display chemical formulas
 */
app.directive('chemicalSketcher', function (gwCtsService, $log) {
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
                    sketcher = new ChemDoodle.SketcherCanvas(myId, $scope.width, $scope.height, {
                        useServices: false,
                        oneMolecule: true
                    });
                }

                sketcher.specs.atoms_displayTerminalCarbonLabels_2D = true;
                sketcher.specs.atoms_useJMOLColors = true;
                sketcher.specs.bonds_clearOverlaps_2D = true;

                /**
                 * checks if our model has a molFile attribute or assumes that it's an inchiKey
                 * @param model spectrum object or inchi key
                 */
                var getMoleculeForModel = function (model) {

                    if (angular.isDefined(model)) {
                        if (angular.isDefined(model.molFile)) {
                            try {
                                //$log.debug('rendering mol file: \n' + model.molFile);
                                if (model.molFile.indexOf('\n') > 0) {
                                    var mol = ChemDoodle.readMOL("\n" + model.molFile + "\n");
                                    sketcher.loadMolecule(mol);
                                }
                                else {
                                    var mol = ChemDoodle.readMOL(model.molFile);
                                    sketcher.loadMolecule(mol);

                                }
                            } catch (e) {
                                $log.warn('problem rendering mol file:\n\n'+ model.molFile);
                            }

                            return 'mol';
                        }

                        else {
                            //$log.debug('converting from inchi: ' + model);
                            gwCtsService.convertInchiKeyToMol(model, function (molecule) {
                                var mol = ChemDoodle.readMOL(molecule);
                                sketcher.loadMolecule(mol);
                            });

                            return 'inchikey';
                        }
                    }

                };

                /**
                 * get an initial value, which was set in our model.
                 */
                var moleculeType = 'mol';

                if (angular.isDefined($scope.bindModel)) {
                    moleculeType = getMoleculeForModel($scope.bindModel);
                }

                /**
                 * get the actual molecule information and tell our parent scope that this value needs updating. Obviously in case of a read only sketcher we ignore it
                 */
                if ($scope.readonly == false) {
                    sketcher.click = function () {
                        //make sure everything is in the angular context
                        $scope.$apply(
                            function () {
                                var mol = sketcher.getMolecule();
                                var molFile = ChemDoodle.writeMOL(mol);

                                //$log.debug('received click event and trying to generate inchi for: ' + molFile);
                                gwCtsService.convertToInchiKey(molFile, function (result) {

                                    //$log.debug('received result: ' + result);
                                    $scope.bindModel = result.inchikey;

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
                    if ($scope.readonly == false) {
                        if (newValue != oldValue) {
                            getMoleculeForModel(newValue);
                        }
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