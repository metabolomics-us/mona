import {Routes} from "@angular/router";

import {MainComponent} from "./components/main.component";
import {UploadPageComponent} from "./components/upload/upload-page.component";
import {BasicUploaderComponent} from "./components/upload/basic-uploader.component";
import {AdvancedUploaderComponent} from "./components/upload/advanced-uploader.component";
import {SpectraUploadComponent} from "./components/upload/spectra-upload.component";
import {DownloadComponent} from "./directives/navbar/download.component";
import {SearchComponent} from "./components/search-page.component";
import {SubmitterComponent} from "./components/submitter/submitter.component";
import {SubmitterProfileComponent} from "./components/submitter/submitter-profile.component";
import {DocumentationTermComponent} from "./components/documentation/documentation-term.component";
import {DocumentationLicenseComponent} from "./components/documentation/documentation-license.component";
import {DocumentationQueryComponent} from "./components/documentation/documentation-query.component";
import {SpectrumViewerComponent} from "./components/browser/spectrum-viewer.component";
import {SpectraBrowserComponent} from "./components/browser/spectra-browser.component";
import {SpectraDatabaseIndexComponent} from "./components/browser/spectra-database-index.component";

export const routes: Routes = [
    {path: '', component: MainComponent},
    {path: 'upload', component: UploadPageComponent},
    {path: 'upload/basic', component: BasicUploaderComponent},
    {path: 'upload/advanced', component: AdvancedUploaderComponent},
    {path: 'upload/status', component: SpectraUploadComponent},
    {path: 'downloads', component: DownloadComponent},
    {path: 'spectra/search', component: SearchComponent},
    {path: 'profile', component: SubmitterProfileComponent},
    {path: 'admin/submitters', component: SubmitterComponent},
    {path: 'documentation/license', component: DocumentationLicenseComponent},
    {path: 'documentation/query', component: DocumentationQueryComponent},
    {path: 'spectra/splash/:splash', redirectTo: '/spectra/browse'},
    {path: 'spectra/inchikey/:inchikey', redirectTo: '/spectra/browse'},
    {path: 'spectra/querytree', redirectTo: '/downloads'},
    {path: 'documentation/terms', component: DocumentationTermComponent},
    {path: '500', redirectTo: 'documentation/terms'},
    {path: 'spectra/display/:id', component: SpectrumViewerComponent},
    {path: 'spectra/browse', component: SpectraBrowserComponent},
    {path: 'spectra/similaritySearch', component: SpectraBrowserComponent},
    {path: 'spectra/statistics', component: SpectraDatabaseIndexComponent}
]


























