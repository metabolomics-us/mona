/**
 * Created by Gert on 6/5/2014.
 */

/**
 * this service is used to calculate several chemical properties for us
 */
app.service('CalculateProperties', function () {

		/**
		 * calculates additonal properties for the given molecule
		 *
		 * @param mol
		 * @returns {Array<T>}
		 */
		this.convertToInchiKey = function (mol) {

			var molecule = ChemDoodle.readMOL(mol);

			var result = [];

			ChemDoodle.iChemLabs.calculate(molecule, {
				descriptors : [ 'mf', 'ef', 'mw', 'miw', 'deg_unsat', 'hba', 'hbd', 'rot', 'electron', 'pol_miller', 'cmr', 'tpsa', 'vabc', 'xlogp2', 'bertz' ]
			}, function(content) {
				var sb = [];

				/**
				 * adds the given elements to the array
				 * @param title
				 * @param value
				 * @param unit
				 */
				function addToArray(title, value, unit) {
					var myValue = {};
					myValue.title = title;
					myValue.value = value;
					myValue.unit = unit;
					sb.push(myValue);
				}

				addToArray('Molecular Formula', content.mf, '');
				addToArray('Empirical Formula', content.ef, '');
				addToArray('Molecular Mass', content.mw, 'amu');
				addToArray('Monoisotopic Mass', content.miw, 'amu');
				addToArray('Degree of Unsaturation', content.deg_unsat, '');
				addToArray('Hydrogen Bond Acceptors', content.hba, '');
				addToArray('Hydrogen Bond Donors', content.hbd, '');
				addToArray('Rotatable Bonds', content.rot, '');
				addToArray('Total Electrons', content.rot, '');
				addToArray('Molecular Polarizability', content.pol_miller, 'A^3');
				addToArray('Molar Refractivity', content.cmr, 'cm^3/mol');
				addToArray('Polar Surface Area', content.tpsa, 'A^2');
				addToArray('vdW Volume', content.vabc, 'A^3');
				addToArray('logP', content.xlogp2, '');
				addToArray('Complexity', content.bertz, '');

				result = sb;
			});

			return result;
		}
	}
);
