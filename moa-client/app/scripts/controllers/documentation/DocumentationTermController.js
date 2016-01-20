/**
 * Created by Gert on 5/28/2014.
 */
(function() {
    'use strict';
    angular.module('moaClientApp')
      .controller('DocumentationTermController', DocumentationTermController)

    /* @ngInject */
    function DocumentationTermController($scope) {
        $scope.terms = [
            {
                name: "Alex",
                info: "automated liner exchange, produced by Gerstel corporation"
            },
            {
                name: "CIS",
                info: "cold injection syste, produced by Gerstel corporation"
            },
            {
                name: "GC",
                info: "gas chromatography"
            },
            {
                name: "TOF",
                info: "time of flight mass spectrometer"
            },
            {
                name: "MS",
                info: "mass spectrometry. After hard ionization by electron ionization, one electron gets abstracted, from the intact molecules which hence become positively charged. The standardized -70 eV ionization voltage is so high that molecules fragment into multiple product ions, which may also form rearrangements among each other. Fragments are then analyzed by time of flight mass spectrometry which is made here by the vendor Leco corporation not to obtain accurate mass information at high resolution but instead to obtain mass spectra at very high sensitivity and speed"
            },
            {
                name: "QC",
                info: "quality control"
            } ,
            {
                name: "IS",
                info: "also istd, internal standards"
            },
            {
                name: "FAME",
                info: "fatty acid methyl esters"
            },
            {
                name: "v/v",
                info: "volumetric ratio"
            },
            {
                name: "InChI",
                info: "International Chemical Identifier key. Denotes the exact stereochemical and atomic description of chemicals and used as universal identifier in chemical databases."
            },
            {
                name: "KEGG",
                info: "Kyoto Encyclopedia of Genes and Genomes"
            },
            {
                name: "PubcChem",
                info: "a public database of chemicals and chemical information"
            },
            {
                name: "rt",
                info: "retention time (seconds)"
            },
            {
                name: "Retention index",
                info: "also ret.index, retention index, a conversion of absolute retention times to relative retention times based on a set of pre-defined internal standards. Classically, Kovats retention indices are used based on hydrocarbons. We use Fiehn retention indices based on FAME istd because FAME mass spectra are much easier to correctly annotate in automatic assays. mz	also m/z, or mass-to-charge ratio. In metabolomics, ions are almost exclusively detected as singly charged species."
            },
            {
                name: "s/n",
                info: "signal to noise ratios"
            },
            {
                name: "IUPAC",
                info: "International Union of Pure and Applied Chemistry"
            },
            {
                name: "PCA",
                info: "Principal Component Analysis"
            }
        ];
    }
})();
