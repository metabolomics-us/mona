/**
 * Created by Gert on 5/28/2014.
 * Updated by nolanguzman on 10/31/2021
 */
import {Component, OnInit} from '@angular/core';

@Component({
    selector: 'documentation-term',
    templateUrl: '../../views/documentation/terms.html'
})
export class DocumentationTermComponent implements OnInit {
    terms;

    constructor() {}

    ngOnInit() {
            this.terms = [
                {
                    name: 'Alex',
                    info: 'Automated Liner Exchange, produced by Gerstel corporation'
                },
                {
                    name: 'CIS',
                    info: 'Cooled Injection System, produced by Gerstel corporation'
                },
                {
                    name: 'GC',
                    info: 'Gas Chromatography'
                },
                {
                    name: 'TOF',
                    info: 'Time of Flight mass spectrometer'
                },
                {
                    name: 'MS',
                    info: 'Mass Spectrometry. After hard ionization by electron ionization, one electron gets abstracted, from the intact molecules which hence become positively charged. The standardized -70 eV ionization voltage is so high that molecules fragment into multiple product ions, which may also form rearrangements among each other. Fragments are then analyzed by time of flight mass spectrometry which is made here by the vendor Leco corporation not to obtain accurate mass information at high resolution but instead to obtain mass spectra at very high sensitivity and speed'
                },
                {
                    name: 'QC',
                    info: 'Quality Control'
                } ,
                {
                    name: 'IS (istds)',
                    info: 'Internal Standards'
                },
                {
                    name: 'FAME',
                    info: 'Fatty Acid Methyl Esters'
                },
                {
                    name: 'v/v',
                    info: 'Volumetric ratio'
                },
                {
                    name: 'InChI',
                    info: 'International Chemical Identifier key. Denotes the exact stereochemical and atomic description of chemicals and used as universal identifier in chemical databases.'
                },
                {
                    name: 'KEGG',
                    info: 'Kyoto Encyclopedia of Genes and Genomes'
                },
                {
                    name: 'PubChem',
                    info: 'A public database of chemicals and chemical information'
                },
                {
                    name: 'RT',
                    info: 'Retention Time (seconds)'
                },
                {
                    name: 'RI',
                    info: 'Retention Index, AKA ret.index, a conversion of absolute retention times to relative retention times based on a set of pre-defined internal standards. Classically, Kovats retention indices are used based on hydrocarbons. We use Fiehn retention indices based on FAME istds because FAME mass spectra are much easier to correctly annotate in automatic assays.'
                },
                {
                    name: 's/n',
                    info: 'Signal to noise ratios'
                },
                {
                    name: 'IUPAC',
                    info: 'International Union of Pure and Applied Chemistry'
                },
                {
                    name: 'PCA',
                    info: 'Principal Component Analysis'
                },
                {
                    name: 'MZ (m/z)',
                    info: 'Mass to charge ratio. In metabolomics, ions are almost exclusively detected as singly charged species.'
                }
            ];

            // Sort by alphabetical order
            this.terms.sort((a, b) =>
              a.name.localeCompare(b.name, undefined, { sensitivity: 'base'})
            );
    }
}
