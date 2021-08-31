/*
 * renders upload button for nav bar
 */

import {Component} from '@angular/core';
import {faCloudUploadAlt} from '@fortawesome/free-solid-svg-icons';

@Component({
    selector: 'upload-button',
    templateUrl: '../../views/navbar/upload.html'
})
export class UploadComponent {
    faCloudUploadAlt = faCloudUploadAlt;
    constructor() {}

}
