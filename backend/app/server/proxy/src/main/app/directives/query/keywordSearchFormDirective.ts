import * as angular from 'angular';

class KeywordSearchFormDirective {
    constructor() {
        return {
            restrict: 'E',
            templateUrl: '../../views/spectra/query/keywordSearchForm.html',
            controller: KeywordSearchFormController,
            controllerAs: 'keywordSearch'
        };
    }
}

class KeywordSearchFormController {
    private static $inject = ['SpectraQueryBuilderService', 'TagService', '$log'];
    private SpectraQueryBuilderService;
    private TagService;
    private $log;
    private query;
    private sourceIntroduction;
    private ionizationMethod;
    private msType;
    private ionMode;
    private libraryTags;
    private queryTags;
    private test;

    constructor(SpectraQueryBuilderService, TagService, $log) {
        this.SpectraQueryBuilderService = SpectraQueryBuilderService;
        this.TagService = TagService;
        this.$log = $log;
    }

    $onInit = () => {
        this.query = {
            exactMassTolerance: 0.5
        };

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

        this.TagService.query().then(
             (tags) => {
                this.queryTags = tags.data.filter((x) => {
                    return x.category != 'library' && !x.ruleBased;
                });

                this.libraryTags = tags.data.filter((x) => {
                    return x.category == 'library';
                });
            },
            (error) => {
                this.$log.error('Tag pull failed: '+ error);
            }
        );
    }

    submitQuery =  () => {
        this.SpectraQueryBuilderService.prepareQuery();

        // Query Name/InChIKey search
        if (/^[A-Z]{14}-[A-Z]{10}-[A-Z]$/.test(this.query.name)) {
            this.SpectraQueryBuilderService.addCompoundMetaDataToQuery('InChIKey', this.query.name);
        } else if (/^[A-Z]{14}$/.test(this.query.name)) {
            this.SpectraQueryBuilderService.addCompoundMetaDataToQuery('InChIKey', this.query.name, true);
        } else if (angular.isDefined(this.query.name)) {
            this.SpectraQueryBuilderService.addNameToQuery(this.query.name);
        }

        // Query compound classification
        if (angular.isDefined(this.query.classification)) {
            this.SpectraQueryBuilderService.addGeneralClassificationToQuery(this.query.classification);
        }

        // Query molecular formula
        if (angular.isDefined(this.query.formula)) {
            this.SpectraQueryBuilderService.addCompoundMetaDataToQuery('molecular formula', this.query.formula, true);
        }

        // Query exact mass
        if (angular.isDefined(this.query.exactMass)) {
            this.SpectraQueryBuilderService.addNumericalCompoundMetaDataToQuery('total exact mass', this.query.exactMass, this.query.exactMassTolerance);
        }

        // Handle chromatography
        let chromatography = this.sourceIntroduction.reduce((result, element) => {
            if (element.selected)
                result.push(element.abv + '-MS');
            return result;
        }, []);

        if (chromatography.length > 0) {
            this.SpectraQueryBuilderService.addTagToQuery(chromatography);
        }

        // Handle ionization mode
        let ionMode = this.ionMode.reduce((result, element) => {
            if (element.selected)
                result.push(element.name.toLowerCase());
            return result;
        }, []);

        if (ionMode.length > 0) {
            this.SpectraQueryBuilderService.addMetaDataToQuery('ionization mode', ionMode);
        }

        // Handle MS type
        let msType = this.msType.reduce((result, element) => {
            if (element.selected)
                result.push(element.name);
            return result;
        }, []);

        if (msType.length > 0) {
            this.SpectraQueryBuilderService.addMetaDataToQuery('ms level', msType);
        }

        // Handle library tags
        let libraryTags = this.libraryTags.reduce((result, element) => {
            if (element.selected)
                result.push(element.text);
            return result;
        }, []);

        if (libraryTags.length > 0) {
            this.SpectraQueryBuilderService.addTagToQuery(libraryTags);
        }

        // Handle all other tags
        this.queryTags.forEach((tag) => {
            if (tag.selected == '+') {
                this.SpectraQueryBuilderService.addTagToQuery(tag.text);
            } else if (tag.selected == '-') {
                this.SpectraQueryBuilderService.addTagToQuery(tag.text, 'ne');
            }
        });

        // Redirect to the spectra browser
        this.SpectraQueryBuilderService.executeQuery();
    };
}

angular.module('moaClientApp')
    .directive('keywordSearchForm', KeywordSearchFormDirective);
