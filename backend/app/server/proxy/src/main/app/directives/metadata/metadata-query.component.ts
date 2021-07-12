/**
 * links a metadata field to a query builder and executes the spectra query for us
 */
import {SpectraQueryBuilderService} from "../../services/query/spectra-query-builder.service";
import {NGXLogger} from "ngx-logger";
import * as angular from 'angular';
import {Component, Inject, Input} from "@angular/core";
import {downgradeComponent} from "@angular/upgrade/static";

@Component({
    selector: 'metadataQuery',
    templateUrl: '../../views/templates/query/metadataQuery.html'
})
export class MetadataQueryComponent {

    @Input() private value;
    @Input() private metaData;
    @Input() private classification;
    private compound;

    constructor(@Inject([SpectraQueryBuilderService, NGXLogger]) private spectraQueryBuilderService: SpectraQueryBuilderService,
                private logger: NGXLogger) {
            this.compound = this.value;
    }

    /**
     * Create a new query based on the selected metadata value
     */
    newQuery = () => {
        this.spectraQueryBuilderService.prepareQuery();
        this.addToQuery();
    };

    /**
     * Add selected metadata value to the current query
     */
    addToQuery = () => {
        if (typeof this.compound !== 'undefined') {
            this.spectraQueryBuilderService.addCompoundMetaDataToQuery(this.metaData.name, this.metaData.value, undefined);
        } else if (angular.isDefined(this.classification)) {
            this.spectraQueryBuilderService.addClassificationToQuery(this.metaData.name, this.metaData.value, undefined);
        } else {
            this.spectraQueryBuilderService.addMetaDataToQuery(this.metaData.name, this.metaData.value, undefined);
        }

        this.spectraQueryBuilderService.executeQuery(undefined);
    };
}

angular.module('moaClientApp')
    .directive('metadataQuery', downgradeComponent({
        component: MetadataQueryComponent,
        inputs: ['value', 'metaData', 'classification']
    }));
