/**
 * Updated by nolanguzman on 10/31/2021
 * renders download button for nav bar
 */

import {Component} from '@angular/core';
import {faCloudDownloadAlt} from '@fortawesome/free-solid-svg-icons';

@Component({
    selector: 'download-button',
    templateUrl: '../../views/navbar/download.html'
})
export class DownloadComponent {
    faCloudDownloadAlt = faCloudDownloadAlt;
    constructor() {}
}
