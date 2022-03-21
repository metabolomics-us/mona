import {Routes} from '@angular/router';

import {MainComponent} from './homepage/main.component';
import {UploadPageComponent} from './upload/upload-page.component';
import {BasicUploaderComponent} from './upload/basic-uploader.component';
import {AdvancedUploaderComponent} from './upload/advanced-uploader.component';
import {SpectraUploadComponent} from './upload/spectra-upload.component';
import {SearchComponent} from './search/search-page.component';
import {SubmitterComponent} from './submitter/submitter.component';
import {SubmitterProfileComponent} from './submitter/submitter-profile.component';
import {DocumentationTermComponent} from './documentation/documentation-term.component';
import {DocumentationLicenseComponent} from './documentation/documentation-license.component';
import {DocumentationQueryComponent} from './documentation/documentation-query.component';
import {SpectrumViewerComponent} from './browser/spectrum-viewer.component';
import {SpectraBrowserComponent} from './browser/spectra-browser.component';
import {SpectraDatabaseIndexComponent} from './browser/spectra-database-index.component';
import {SpectrumResolver} from '../resolvers/spectrum.resolver';
import {QueryTreeComponent} from './browser/query-tree.component';
import {DocumentationUploadLibraryComponent} from './documentation/documentation-upload-library.component';
import {DocumentationEntropyComponent} from './documentation/documentation-entropy.component';
import {ManageSpectraComponent} from './admin/manage-spectra.component';

export const routes: Routes = [
    {path: '', component: MainComponent},
    {path: 'upload', component: UploadPageComponent},
    {path: 'upload/basic', component: BasicUploaderComponent},
    {path: 'upload/advanced', component: AdvancedUploaderComponent},
    {path: 'upload/status', component: SpectraUploadComponent},
    {path: 'downloads', component: QueryTreeComponent},
    {path: 'spectra/search', component: SearchComponent},
    {path: 'profile', component: SubmitterProfileComponent},
    {path: 'admin/submitters', component: SubmitterComponent},
    {path: 'admin/manage-spectra', component: ManageSpectraComponent},
    {path: 'documentation/license', component: DocumentationLicenseComponent},
    {path: 'documentation/query', component: DocumentationQueryComponent},
    {path: 'spectra/splash/:splash', redirectTo: '/spectra/browse'},
    {path: 'spectra/inchikey/:inchikey', redirectTo: '/spectra/browse'},
    {path: 'spectra/querytree', redirectTo: '/downloads'},
    {path: 'documentation/terms', component: DocumentationTermComponent},
    {path: 'documentation/uploadLibrary', component: DocumentationUploadLibraryComponent},
    {path: 'documentation/entropy', component: DocumentationEntropyComponent},
    {path: '500', redirectTo: 'documentation/terms'},
    {
      path: 'spectra/display/:id',
      component: SpectrumViewerComponent,
      resolve: {spectrum: SpectrumResolver}
    },
    {path: 'spectra/browse', component: SpectraBrowserComponent},
    {path: 'spectra/similaritySearch', component: SpectraBrowserComponent},
    {path: 'spectra/statistics', component: SpectraDatabaseIndexComponent}
];


























