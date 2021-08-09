/**
 * links a metadata field to a query builder and executes the spectra query for us
 */
import {SpectraQueryBuilderService} from "../../services/query/spectra-query-builder.service";
import {NGXLogger} from "ngx-logger";
import {Component, Input} from "@angular/core";

@Component({
    selector: 'metadata-query',
    templateUrl: '../../views/templates/query/metadataQuery.html'
})
export class MetadataQueryComponent {

    @Input() public compound;
    @Input() public metaData;
    @Input() public classification;

    constructor( public spectraQueryBuilderService: SpectraQueryBuilderService,
                 public logger: NGXLogger) {
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
        } else if (typeof this.classification !== 'undefined') {
            this.spectraQueryBuilderService.addClassificationToQuery(this.metaData.name, this.metaData.value, undefined);
        } else {
            this.spectraQueryBuilderService.addMetaDataToQuery(this.metaData.name, this.metaData.value, undefined);
        }

        this.spectraQueryBuilderService.executeQuery(undefined);
    };
}