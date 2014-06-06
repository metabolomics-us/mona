/**
 * Created by Gert on 5/30/2014.
 */
'use strict';

app.directive('chemicalSketcher', function (MolConverter) {
	return {
		restrict: "A",
		replace: false,
		require: 'ngModel',

		scope: {
			id: '@id',
			bindModel: '=ngModel'
		},
		/**
		 * links our sketcher into the directive
		 * @param $scope
		 * @param element
		 * @param attrs
		 */
		link: function ($scope, element, attrs) {

			//only render if we got an id object
			if (angular.isDefined($scope.id)) {
				var myId = $scope.id + '_sketcher';
				element.append('<canvas id="' + myId + '"></canvas>');
				var sketcher = new ChemDoodle.SketcherCanvas(myId, 500, 300, {useServices: false, oneMolecule: true});

				sketcher.specs.atoms_displayTerminalCarbonLabels_2D = true;
				sketcher.specs.atoms_useJMOLColors = true;
				sketcher.specs.bonds_clearOverlaps_2D = true;

				/**
				 * get the actual molecule information and tell our parent scope that this value needs updating
				 */
				sketcher.click = function () {
					var mol = sketcher.getMolecule();
					var molFile = ChemDoodle.writeMOL(mol);

					MolConverter.convertToInchiKey(molFile).then(function (data) {

						//update our bound model with the given key
						$scope.bindModel = data.key;
					});

				};

				/**
				 * destroy our sketcher
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