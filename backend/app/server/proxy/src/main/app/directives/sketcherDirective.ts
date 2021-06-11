/**
 * Created by Gert on 5/30/2014.
 *
 * a directive to draw or display chemical formulas
 */

import * as angular from 'angular';
declare const ChemDoodle: any;

class SketcherDirective {
    constructor() {
        return {
            restrict: 'A',
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
            controller: SketcherController,
            controllerAs: '$ctrl',
            link: ($scope, element, attrs, $ctrl) => {

                /**
                 * default properties
                 * @type {number|*}
                 */
                $scope.width = $scope.width || 500;
                $scope.height = $scope.height || 300;
                $scope.readonly = $scope.readonly || false;


                //only render if we got an id object
                if (angular.isDefined($scope.id)) {
                    let myId = $scope.id + '_sketcher';

                    element.append('<canvas id="' + myId + '"></canvas>');

                    let sketcher = null;

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

                    //sketcher.specs.atoms_displayTerminalCarbonLabels_2D = true;
                    sketcher.styles.atoms_displayTerminalCarbonLabels_2D = true;
                    sketcher.styles.atoms_useJMOLColors = true;
                    sketcher.styles.bonds_clearOverlaps_2D = true;


                    /**
                     * checks if our model has a molFile attribute or assumes that it's an inchiKey
                     * @param model spectrum object or inchi key
                     */
                    let getMoleculeForModel = (model) => {

                        // model is array of object, we need to get the first one only
                        if (Array.isArray(model)) {
                            model = model[0];
                        }

                        if (angular.isDefined(model)) {
                            if (angular.isDefined(model.molFile) || (angular.isString(model) && model.indexOf('M  END') > -1)) {
                                // Load and sanitize MOL file
                                let molFile = angular.isDefined(model.molFile) ? model.molFile : model;
                                //$ctrl.$log.debug('rendering mol file: \n' + molFile);

                                molFile = molFile.split('$$$$')[0];

                                if (molFile.indexOf('\n') > 0) {
                                    molFile = "\n" + molFile + "\n";
                                }

                                if (molFile[molFile.length - 1] != '\n') {
                                    molFile += '\n';
                                }

                                try {
                                    let mol = ChemDoodle.readMOL(molFile);
                                    sketcher.loadMolecule(mol);
                                    sketcher.repaint();
                                } catch (e) {
                                    $ctrl.$log.warn('problem rendering mol file:\n\n' + molFile);
                                    $ctrl.$log.warn(e);
                                }

                                return 'mol';
                            }

                            else if (angular.isString(model) && /^[A-Z]{14}-[A-Z]{10}-[A-Z]$/.test(model)) {
                                $ctrl.$log.debug('Converting from InChIKey to MOL: '+ model);

                                $ctrl.gwCtsService.convertInchiKeyToMol(model, (molecule) => {
                                    let mol = ChemDoodle.readMOL(molecule);
                                    sketcher.loadMolecule(mol);
                                });

                                return 'inchikey';
                            }
                        }

                    };

                    /**
                     * get an initial value, which was set in our model.
                     */
                    let moleculeType = 'mol';

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
                    $scope.$watch(() => {
                        return $scope.bindModel;
                    }, (newValue, oldValue) => {
                        if (newValue !== oldValue) {
                            getMoleculeForModel(newValue);
                        }
                    });


                    /**
                     * destroy our sketcher - doesn't work
                     */
                    $scope.$on("$destroy", () => {
                        sketcher = null;
                        let sameLevelElems = element.children();

                        for (let i = 0; i < sameLevelElems.length; i++) {
                            sameLevelElems[i].remove();
                        }
                    });
                }
            }
        }

    }
}

class SketcherController {
    private static $inject = ['gwCtsService', '$log'];
    private gwCtsService;
    private $log;
    constructor(gwCtsService, $log) {
        this.gwCtsService = gwCtsService;
        this.$log = $log;
    }
}
angular.module('moaClientApp')
    .directive('chemicalSketcher', SketcherDirective);
