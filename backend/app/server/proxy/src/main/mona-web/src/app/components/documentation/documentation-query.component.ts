/**
 * Created by nolanguzman on 10/31/2021
 */
import {Component} from '@angular/core';
import {faEdit} from "@fortawesome/free-solid-svg-icons";

@Component({
    selector: 'documentation-query',
    templateUrl: '../../views/documentation/query.html'
})
export class DocumentationQueryComponent {
    faEdit = faEdit;
    constructor() {}
}
