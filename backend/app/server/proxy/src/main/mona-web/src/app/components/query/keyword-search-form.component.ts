/**
 * Updated by nolanguzman on 10/31/2021
 */
import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import {SpectraQueryBuilderService} from '../../services/query/spectra-query-builder.service';
import {TagService} from '../../services/persistence/tag.resource';
import {NGXLogger} from 'ngx-logger';
import {faSpinner, faSearch, faCaretDown} from '@fortawesome/free-solid-svg-icons';
import {NgbAccordion} from '@ng-bootstrap/ng-bootstrap';

@Component({
    selector: 'keyword-search-form',
    templateUrl: '../../views/spectra/query/keywordSearchForm.html'
})
export class KeywordSearchFormComponent implements OnInit, AfterViewInit {
    @ViewChild('acc') accordion: NgbAccordion;
    query;
    sourceIntroduction;
    ionizationMethod;
    msType;
    ionMode;
    libraryTags;
    queryTags;
    test;
    faSpinner = faSpinner;
    faSearch = faSearch;
    faCaretDown = faCaretDown;

    expandedLibraries = false;
    toggleLibraryExpansion() {
      this.expandedLibraries = !this.expandedLibraries;
    }

    constructor(public spectraQueryBuilderService: SpectraQueryBuilderService,
                public tagService: TagService, public logger: NGXLogger) {}

    ngOnInit() {
        this.query = {
            exactMassTolerance: 0.5
        };

        this.queryTags = [];
        this.libraryTags = [];
        /**
         * Form fields
         */
        this.sourceIntroduction = [
            {name: 'Liquid Chromatography', abv: 'LC'},
            {name: 'Gas Chromatography', abv: 'GC'},
            {name: 'Capillary Electrophoresis', abv: 'CE'}
        ];

        this.ionizationMethod = [
            {name: 'Atmospheric Pressure Chemical Ionization', abv: '(APCI)'},
            {name: 'Chemical Ionization', abv: '(CI)'},
            {name: 'Electron Impact', abv: '(EI)'},
            {name: 'Electrospray Ionization', abv: '(ESI)'},
            {name: 'Fast Atom Bombardment', abv: '(FAB)'},
            {name: 'Matrix Assisted Laser Desorption Ionization', abv: '(MALDI)'}
        ];

        this.msType = [{name: 'MS1'}, {name: 'MS2'}, {name: 'MS3'}, {name: 'MS4'}];
        this.ionMode = [{name: 'Positive'}, {name: 'Negative'}];

        this.tagService.allTags().subscribe(
             (tags: any) => {
                 if (tags.length > 0) {
                   this.queryTags = tags.filter((x) => {
                     return x.category !== 'library' && !x.ruleBased;
                   });

                   this.libraryTags = tags.filter((x) => {
                     return x.category === 'library';
                   });
                 }
            },
            (error) => {
                this.logger.error('Tag pull failed: ' + error);
            }
        );
    }

    ngAfterViewInit() {
      // setTimeout(() => {
      //   this.accordion.expand('additionalTags');
      // }, 1000);
    }

  submitQuery() {
        this.spectraQueryBuilderService.prepareQuery();

        // Query Name/InChIKey search
        if (/^[A-Z]{14}-[A-Z]{10}-[A-Z]$/.test(this.query.name)) {
            this.spectraQueryBuilderService.addCompoundMetaDataToQuery('InChIKey', this.query.name, undefined);
        } else if (/^[A-Z]{14}$/.test(this.query.name)) {
            this.spectraQueryBuilderService.addCompoundMetaDataToQuery('InChIKey', this.query.name, true);
        } else if (typeof this.query.name !== 'undefined') {
            this.spectraQueryBuilderService.addNameToQuery(this.query.name);
        }

        // Query compound classification
        if (typeof this.query.classification !== 'undefined') {
            this.spectraQueryBuilderService.addGeneralClassificationToQuery(this.query.classification);
        }

        // Query molecular formula
        if (typeof this.query.formula !== 'undefined') {
            this.spectraQueryBuilderService.addCompoundMetaDataToQuery('molecular formula', this.query.formula, true);
        }

        // Query exact mass
        if (typeof this.query.exactMass !== 'undefined') {
            this.spectraQueryBuilderService
              .addNumericalCompoundMetaDataToQuery('total exact mass', this.query.exactMass, this.query.exactMassTolerance);
        }

        // Handle chromatography
        const chromatography = this.sourceIntroduction.reduce((result, element) => {
            if (element.selected) {
              result.push(element.abv + '-MS');
            }
            return result;
        }, []);

        if (chromatography.length > 0) {
            this.spectraQueryBuilderService.addTagToQuery(chromatography, undefined);
        }

        // Handle ionization mode
        const ionMode = this.ionMode.reduce((result, element) => {
            if (element.selected) {
              result.push(element.name.toLowerCase());
            }
            return result;
        }, []);

        if (ionMode.length > 0) {
            this.spectraQueryBuilderService.addMetaDataToQuery('ionization mode', ionMode, undefined);
        }

        // Handle MS type
        const msType = this.msType.reduce((result, element) => {
            if (element.selected) {
              result.push(element.name);
            }
            return result;
        }, []);

        if (msType.length > 0) {
            this.spectraQueryBuilderService.addMetaDataToQuery('ms level', msType, undefined);
        }

        // Handle library tags
        const libraryTags = this.libraryTags.reduce((result, element) => {
            if (element.selected) {
              result.push(element.text);
            }
            return result;
        }, []);

        if (libraryTags.length > 0) {
            this.spectraQueryBuilderService.addTagToQuery(libraryTags, undefined);
        }

        // Handle all other tags
        this.queryTags.forEach((tag) => {
            if (tag.selected === '+') {
                this.spectraQueryBuilderService.addTagToQuery(tag.text, undefined);
            } else if (tag.selected === '-') {
                this.spectraQueryBuilderService.addTagToQuery(tag.text, 'ne');
            }
        });

        // Redirect to the spectra browser
        this.spectraQueryBuilderService.executeQuery();
    }
}
