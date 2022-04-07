import './polyfills';

import {enableProdMode, NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ReactiveFormsModule} from '@angular/forms';
import {TagInputModule} from 'ngx-chips';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {BrowserModule} from '@angular/platform-browser';
import {CtsLibModule} from 'angular-cts-service/dist/cts-lib';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {ToasterModule} from 'angular2-toaster';
import {NgxGoogleAnalyticsModule} from 'ngx-google-analytics';
import {RouterModule} from '@angular/router';
import {CookieService} from 'ngx-cookie-service';
import {HttpClientModule} from '@angular/common/http';
import {NgChemdoodleModule} from '@wcmc/ng-chemdoodle';
import {platformBrowserDynamic} from '@angular/platform-browser-dynamic';
import {LoggerModule} from 'ngx-logger';
import {FormsModule} from '@angular/forms';
import {NvD3Module} from 'ng2-nvd3';
import {FontAwesomeModule} from '@fortawesome/angular-fontawesome';
import {NgcCookieConsentModule, NgcCookieConsentConfig} from 'ngx-cookieconsent';
import {ClipboardModule} from 'ngx-clipboard';

import {CtsService, ChemifyService} from 'angular-cts-service/dist/cts-lib';
import {NgMassSpecPlotterModule} from '@wcmc/ng-mass-spec-plotter';
import {MassbankParserLibService, MassbankParserLibModule} from 'angular-massbank-parser/dist/massbank-parser-lib';
import {MgfParserLibModule, MgfParserLibService} from 'angular-mgf-parser/dist/mgf-parser-lib';
import {MspParserLibService, MspParserLibModule} from 'angular-msp-parser/dist/msp-parser-lib';

import {Download} from './app/services/persistence/download.resource';
import {Metadata} from './app/services/persistence/metadata.resource';
import {Spectrum} from './app/services/persistence/spectrum.resource';
import {SpectrumCacheService} from './app/services/cache/spectrum-cache.service';
import {Statistics} from './app/services/persistence/statistics.resource';
import {Submitter} from './app/services/persistence/submitter.resource';
import {TagService} from './app/services/persistence/tag.resource';
import {MetadataOptimization} from './app/services/optimization/metadata-optimization.service';
import {SpectraQueryBuilderService} from './app/services/query/spectra-query-builder.service';
import {QueryCacheService} from './app/services/cache/query-cache.service';
import {CookieMain} from './app/services/cookie/cookie-main.service';
import {AsyncService} from './app/services/upload/async.service';
import {UploadLibraryService} from './app/services/upload/upload-library.service';
import {AuthenticationService} from './app/services/authentication.service';
import {CompoundConversionService} from './app/services/compound-conversion.service';
import {RegistrationService} from './app/services/registration.service';
import {Feedback} from './app/services/persistence/feedback.resource';
import {FeedbackCacheService} from './app/services/feedback/feedback-cache.service';
import {AdminService} from './app/services/persistence/admin.resource';
import {MassDeletionService} from './app/services/persistence/mass-deletion.service';

import {SpectrumReviewComponent} from './app/components/feedback/spectrum-review.component';
import {ErrorHandleComponent} from './app/components/compound/error-handle.component';
import {DisplayCompoundComponent} from './app/components/compound/display-compound.component';
import {GwMetaQueryInputComponent} from './app/components/metadata/gw-meta-query-input.component';
import {MetadataQueryComponent} from './app/components/metadata/metadata-query.component';
import {SplashQueryComponent} from './app/components/metadata/splash-query.component';
import {SubmitterQueryComponent} from './app/components/metadata/submitter-query.component';
import {TagDisplayComponent} from './app/components/metadata/tag-display.component';
import {TagQueryComponent} from './app/components/metadata/tag-query.component';
import {AdminDropDownComponent} from './app/components/navbar/admin-dropdown.component';
import {BrowseDropDownComponent} from './app/components/navbar/browse-dropdown.component';
import {DownloadComponent} from './app/components/navbar/download.component';
import {ResourceDropDownComponent} from './app/components/navbar/resource-dropdown.component';
import {TitleHeaderComponent} from './app/components/navbar/title-header.component';
import {UploadComponent} from './app/components/navbar/upload.component';
import {KeywordSearchFormComponent} from './app/components/query/keyword-search-form.component';
import {QueryTreeViewComponent} from './app/components/query/query-tree-view.component';
import {SpectraUploadProgressComponent} from './app/components/spectra/spectra-upload-progress.component';
import {SimilaritySearchFormComponent} from './app/components/query/similarity-search-form.component';
import {DownloadNotifErrorModalComponent} from './app/components/spectra/download-notif-error-modal.component';
import {DownloadNotifModalComponent} from './app/components/spectra/download-notif-modal.component';
import {SpectraDownloadComponent} from './app/components/spectra/spectra-download.component';
import {SpectraLibraryComponent} from './app/components/spectra/spectra-library.component';
import {SpectraPanelComponent} from './app/components/spectra/spectra-panel.component';
import {SpectraCountForUserComponent} from './app/components/submitter/spectra-count-for-user.component';
import {SpectraScoreForUserComponent} from './app/components/submitter/spectra-score-for-user.component';
import {SpectraTopScoresForUsersComponent} from './app/components/submitter/spectra-top-scores-for-users.component';
import {SubmitterFormComponent} from './app/components/submitter/submitter-form.component';
import {AuthenticationComponent} from './app/components/authentication/authentication.component';
import {AuthenticationModalComponent} from './app/components/authentication/authentication-modal.component';
import {RegistrationModalComponent} from './app/components/authentication/registration-modal.component';
import {QueryTreeComponent} from './app/components/browser/query-tree.component';
import {DocumentationTermComponent} from './app/components/documentation/documentation-term.component';
import {SubmitterComponent} from './app/components/submitter/submitter.component';
import {SubmitterModalComponent} from './app/components/submitter/submitter-modal.component';
import {SubmitterProfileComponent} from './app/components/submitter/submitter-profile.component';
import {SpectrumViewerComponent} from './app/components/browser/spectrum-viewer.component';
import {AdvancedUploaderComponent} from './app/components/upload/advanced-uploader.component';
import {BasicUploaderComponent} from './app/components/upload/basic-uploader.component';
import {SpectraUploadComponent} from './app/components/upload/spectra-upload.component';
import {UploadPageComponent} from './app/components/upload/upload-page.component';
import {MainComponent} from './app/components/homepage/main.component';
import {SearchBoxComponent} from './app/components/search/search-box.component';
import {SearchComponent} from './app/components/search/search-page.component';
import {DocumentationQueryComponent} from './app/components/documentation/documentation-query.component';
import {DocumentationLicenseComponent} from './app/components/documentation/documentation-license.component';
import {SpectraBrowserComponent} from './app/components/browser/spectra-browser.component';
import {SpectraDatabaseIndexComponent} from './app/components/browser/spectra-database-index.component';
import {SpectrumFeedbackResultsCurationComponent} from './app/components/feedback/spectrum-feedback-results-curation';
import {SpectrumFeedbackResultsCommunityComponent} from './app/components/feedback/spectrum-feedback-results-community';
import {DocumentationUploadLibraryComponent} from './app/components/documentation/documentation-upload-library.component';
import {DocumentationEntropyComponent} from './app/components/documentation/documentation-entropy.component';
import {ManageSpectraComponent} from './app/components/admin/manage-spectra.component';
import {MassDeleteModalComponent} from './app/components/browser/mass-delete-modal.component';
import {SearchMetadataComponent} from './app/components/search/search-metadata.component';

import {PrefixValidator} from './app/directives/PrefixValidatorDirective';

import {FilterPipe} from './app/filters/filter.pipe';
import {CurlPipe} from './app/filters/curl.pipe';
import {OrderbyPipe} from './app/filters/orderby.pipe';
import {BytesPipe} from './app/filters/bytes.pipe';
import {SlicePipe} from '@angular/common';
import {AppRootComponent} from './app/components/app.component';
import {environment} from './environments/environment';
import {routes} from './app/components/app.routes';
import {SpectrumResolver} from './app/resolvers/spectrum.resolver';
import {AdvancedUploadModalComponent} from './app/components/upload/advanced-upload-modal.component';

const cookieConfig: NgcCookieConsentConfig = {
  cookie: {
    domain: window.location.hostname
  },
  position: 'bottom',
  palette: {
    popup: {
      background: '#022851'
    },
    button: {
      background: '#FFBF00'
    }
  },
  theme: 'classic',
  type: 'info'
};

@NgModule({
    imports: [
        BrowserModule,
        CommonModule,
        HttpClientModule,
        FormsModule,
        ReactiveFormsModule,
        CtsLibModule.forRoot({apiUrl: environment.ctsUrl}),
        NgMassSpecPlotterModule,
        MassbankParserLibModule,
        MgfParserLibModule,
        MspParserLibModule,
        NgChemdoodleModule,
        NgbModule,
        LoggerModule.forRoot({
            level: environment.loggerLevel,
            serverLogLevel: environment.serverLevel
        }),
        BrowserAnimationsModule,
        ToasterModule.forRoot(),
        NgxGoogleAnalyticsModule.forRoot(environment.google_analytics),
        RouterModule.forRoot(routes, {useHash: false}),
        TagInputModule,
        NvD3Module,
        FontAwesomeModule,
        NgcCookieConsentModule.forRoot(cookieConfig),
        ClipboardModule
    ],
    providers: [
        CookieService,
        CookieMain,
        Download,
        Metadata,
        Spectrum,
        SpectrumCacheService,
        Statistics,
        Submitter,
        TagService,
        MetadataOptimization,
        SpectraQueryBuilderService,
        QueryCacheService,
        AsyncService,
        UploadLibraryService,
        CtsService,
        ChemifyService,
        AuthenticationService,
        RegistrationService,
        CompoundConversionService,
        MassbankParserLibService,
        MgfParserLibService,
        MspParserLibService,
        FilterPipe,
        CurlPipe,
        OrderbyPipe,
        BytesPipe,
        SlicePipe,
        SpectrumResolver,
        Feedback,
        FeedbackCacheService,
        AdminService,
        MassDeletionService
    ],

    declarations: [
        FilterPipe,
        CurlPipe,
        BytesPipe,
        OrderbyPipe,
        SpectrumReviewComponent,
        ErrorHandleComponent,
        DisplayCompoundComponent,
        GwMetaQueryInputComponent,
        MetadataQueryComponent,
        SplashQueryComponent,
        SubmitterQueryComponent,
        TagDisplayComponent,
        TagQueryComponent,
        AdminDropDownComponent,
        BrowseDropDownComponent,
        DownloadComponent,
        ResourceDropDownComponent,
        TitleHeaderComponent,
        UploadComponent,
        KeywordSearchFormComponent,
        QueryTreeViewComponent,
        QueryTreeComponent,
        SpectraUploadProgressComponent,
        SimilaritySearchFormComponent,
        DownloadNotifErrorModalComponent,
        DownloadNotifModalComponent,
        SpectraDownloadComponent,
        SpectraLibraryComponent,
        SpectraPanelComponent,
        SpectraCountForUserComponent,
        SpectraScoreForUserComponent,
        SpectraTopScoresForUsersComponent,
        SubmitterFormComponent,
        AuthenticationComponent,
        AuthenticationModalComponent,
        RegistrationModalComponent,
        DocumentationTermComponent,
        SubmitterComponent,
        SubmitterModalComponent,
        SubmitterProfileComponent,
        SpectrumViewerComponent,
        AdvancedUploaderComponent,
        BasicUploaderComponent,
        SpectraUploadComponent,
        UploadPageComponent,
        MainComponent,
        SearchBoxComponent,
        SearchComponent,
        DocumentationQueryComponent,
        DocumentationLicenseComponent,
        SpectraBrowserComponent,
        SpectraDatabaseIndexComponent,
        AppRootComponent,
        AdvancedUploadModalComponent,
        SpectrumFeedbackResultsCurationComponent,
        SpectrumFeedbackResultsCommunityComponent,
        DocumentationUploadLibraryComponent,
        DocumentationEntropyComponent,
        PrefixValidator,
        ManageSpectraComponent,
        MassDeleteModalComponent,
        SearchMetadataComponent
    ],
    bootstrap: [
        AppRootComponent
    ]
})

export class AppModule {
  constructor() {
  }
}

if (environment.production) {
  enableProdMode();
}
platformBrowserDynamic().bootstrapModule(AppModule).finally();

