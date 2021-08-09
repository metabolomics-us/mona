/**
 * defines a metadata text field combo with autocomplete and typeahead functionality
 */

import {SpectraQueryBuilderService} from "../../services/query/spectra-query-builder.service";
import {environment} from "../../../environments/environment";
import {HttpClient} from "@angular/common/http";
import {NGXLogger} from "ngx-logger";
import {SlicePipe} from "@angular/common";
import {Component, Input} from "@angular/core";
import {map} from "rxjs/operators";


@Component({
    selector: 'gw-meta-query-input',
    templateUrl: '../../views/templates/metaQueryInput.html'
})
export class GwMetaQueryInputComponent{
    @Input() public query;
    @Input() public editable;
    @Input() public fullText;
    public select;

    constructor( public spectraQueryBuilderService: SpectraQueryBuilderService,
                 public http: HttpClient,  public logger: NGXLogger,
                 public slice: SlicePipe) {}

    $onInit = () => {
        this.select = [
            {name: "equal", value: "eq"},
            {name: "not equal", value: "ne"},
            {name: "like", value: "match"}
        ];

        if (typeof this.query === 'undefined') {
            this.query = [];
        }

        // Set blank entry if query list is empty
        if (this.query.length === 0) {
            this.addMetadataQuery();
        }

        // Set editable option if not set
        if (typeof this.editable === 'undefined') {
            this.editable = false;
        }
    }

    /**
     * tries to find meta data names for us
     * @param value
     */
    queryMetadataNames = (value) => {
        if (typeof value === 'undefined' || value.replace(/^\s*/, '').replace(/\s*$/, '') === '') {
            return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/meta/searchNames/`, {}).subscribe((res: any) => {
                return res.data.slice(0, 50);
            });

        }
        else {
            return this.http.get(`${environment.REST_BACKEND_SERVER}/rest/meta/searchNames/${value}?max=10`, {}).subscribe((res:any) => {
                return res.data.slice(0, 25);
            });
        }
    };

    /**
     * queries our values
     * @param name
     * @param value
     */
    queryMetadataValues = (name, value) => {

        if (typeof value === 'undefined' || value.replace(/^\s*/, '').replace(/\s*$/, '') === '') {
            return this.http.post(`${environment.REST_BACKEND_SERVER}/rest/meta/data/search`, {
                query: {
                    name: name,
                    value: {isNotNull: ''},
                    property: 'stringValue',
                    deleted: false
                }
            }).pipe(map((x: string) => {
                return x.slice(0,25);
            }));

        }
        else if (typeof this.fullText !== 'undefined') {
            return this.http.post(`${environment.REST_BACKEND_SERVER}/rest/meta/data/search?max=10`, {
                query: {
                    name: name,
                    value: {ilike: '%' + value + '%'},
                    property: 'stringValue',
                    deleted: false
                }
            }).pipe(map((res: string) => {
                return res.slice(0, 25);
            }));
        }
        else {
            return this.http.post(`${environment.REST_BACKEND_SERVER}/rest/meta/data/search?max=10`, {
                query: {
                    name: name,
                    value: {ilike: value + '%'},
                    property: 'stringValue',
                    deleted: false

                }
            }).pipe(map((res: string) => {
                return res.slice(0, 25);
            }));
        }

    };

    isNumber = (n) => {
        return !isNaN(parseFloat(n)) && isFinite(n);
    };

    /**
     * adds a metadata query
     */
    addMetadataQuery = () => {
        this.query.push({name: '', value: '', selected: this.select[0]});
    };
}
