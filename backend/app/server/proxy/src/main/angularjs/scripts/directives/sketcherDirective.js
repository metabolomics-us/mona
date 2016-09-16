/**
 * Created by Gert on 5/30/2014.
 *
 * a directive to draw or display chemical formulas
 */

(function() {
    'use strict';

    chemicalSketcher.$inject = ['gwCtsService', '$log'];
    angular.module('moaClientApp')
      .directive('chemicalSketcher', chemicalSketcher);

    /* @ngInject */
    function chemicalSketcher(gwCtsService, $log) {
        var directive = {
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
            link: function($scope, element, attrs, ngModel) {

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
                    var getMoleculeForModel = function(model) {

                        // model is array of object, we need to get the first one only
                        if (Array.isArray(model)) {
                            model = model[0];
                        }

                        if (angular.isDefined(model)) {
                            if (angular.isDefined(model.molFile) || (angular.isString(model) && model.indexOf('M  END') > -1)) {
                                var molFile = angular.isDefined(model.molFile) ? model.molFile : model;

                                try {
                                    //$log.debug('rendering mol file: \n' + molFile);

                                    if (molFile.indexOf('\n') > 0) {
                                        var mol = ChemDoodle.readMOL("\n" + molFile + "\n");
                                        sketcher.loadMolecule(mol);
                                    }
                                    else {
                                        var mol = ChemDoodle.readMOL(molFile);
                                        sketcher.loadMolecule(mol);
                                    }
                                    sketcher.repaint();
                                } catch (e) {
                                    $log.warn('problem rendering mol file:\n\n' + molFile);
                                    $log.warn(e);
                                }

                                return 'mol';
                            }

                            else if (angular.isString(model) && /^[A-Z]{14}-[A-Z]{10}-[A-Z]$/.test(model)) {
                                $log.debug('Converting from InChIKey to MOL: '+ model);
                                
                                gwCtsService.convertInchiKeyToMol(model, function(molecule) {
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

                    if (angular.isDefined($scope.bindModel) && $scope.readonly === true) {
                        moleculeType = getMoleculeForModel($scope.bindModel);
                    }

                    /**
                     * get the actual molecule information and tell our parent scope that this value needs updating. Obviously in case of a read only sketcher we ignore it
                     */
                    // if ($scope.readonly === false) {
                    //     sketcher.click = function() {
                    //         //make sure everything is in the angular context
                    //         $scope.$apply(
                    //           function() {
                    //               var mol = sketcher.getMolecule();
                    //               var molFile = ChemDoodle.writeMOL(mol);
                    //
                    //               //$log.debug('received click event and trying to generate inchi for: ' + molFile);
                    //               //gwCtsService.convertToInchiKey(molFile, function (result) {
                    //               //
                    //               //    //$log.debug('received result: ' + result);
                    //               //    $scope.bindModel = result.inchikey;
                    //               //
                    //               //});
                    //
                    //               console.log(molFile);
                    //
                    //               // Export as MOL file
                    //               $scope.bindModel = molFile;
                    //           }
                    //         );
                    //     };
                    // }

                    /**
                     * tracks changes to the model and if it's changes attempt to draw the structure
                     */
                    $scope.$watch(function() {
                        return $scope.bindModel;
                    }, function(newValue, oldValue) {
                        if (newValue !== oldValue) {
                            getMoleculeForModel(newValue);
                        }
                    });


                    /**
                     * destroy our sketcher - doesn't work
                     */
                    $scope.$on("$destroy", function() {
                        sketcher = null;
                        var sameLevelElems = element.children();

                        for (var i = 0; i < sameLevelElems.length; i++) {
                            sameLevelElems[i].remove();
                        }
                    });
                }
            }
        };

        return directive;
    }
})();
