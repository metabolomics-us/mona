import {ComponentFixture, TestBed} from '@angular/core/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {ActivatedRoute} from '@angular/router';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {Location} from '@angular/common';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {of, throwError} from 'rxjs';

import {SpectraBrowserComponent} from './spectra-browser.component';
import {AuthenticationService} from '../../services/authentication.service';
import {Spectrum} from '../../services/persistence/spectrum.resource';
import {MassDeletionService} from '../../services/persistence/mass-deletion.service';
import {SpectraQueryBuilderService} from '../../services/query/spectra-query-builder.service';
import {SpectrumCacheService} from '../../services/cache/spectrum-cache.service';
import {FeedbackCacheService} from '../../services/feedback/feedback-cache.service';
import {Metadata} from '../../services/persistence/metadata.resource';
import {CookieMain} from '../../services/cookie/cookie-main.service';
import {SpectrumModel} from '../../mocks/spectrum.model';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {ToasterService} from 'angular2-toaster';
import {NGXLogger} from 'ngx-logger';
import {GoogleAnalyticsService} from 'ngx-google-analytics';

const ADMIN_USER = {
    emailAddress: 'admin@test.com', accessToken: 'admin-token',
    firstName: 'Admin', lastName: '', institution: '',
    roles: [{authority: 'ADMIN'}]
};

const REGULAR_USER = {
    emailAddress: 'user@test.com', accessToken: 'user-token',
    firstName: 'User', lastName: '', institution: '',
    roles: []
};

function makeSpectrum(id: string, submitterEmail: string): SpectrumModel {
    const s = new SpectrumModel();
    s.id = id;
    s.submitter = {emailAddress: submitterEmail, firstName: '', lastName: '', institution: ''};
    s.compound = [{names: [], metaData: [], molFile: null, kind: 'biological', classification: [], inchiKey: '', tags: [], computed: false, inchi: ''}];
    s.metaData = [];
    s.score = {score: 3, relativeScore: 0.6, scaledScore: 3, impacts: []};
    s.metaDataMap = {};
    return s;
}

describe('SpectraBrowserComponent', () => {
    let component: SpectraBrowserComponent;
    let fixture: ComponentFixture<SpectraBrowserComponent>;
    let authService: jasmine.SpyObj<AuthenticationService>;
    let spectrumService: jasmine.SpyObj<Spectrum>;
    let toasterService: jasmine.SpyObj<ToasterService>;
    let modalService: jasmine.SpyObj<NgbModal>;

    beforeEach(async () => {
        authService = jasmine.createSpyObj('AuthenticationService', ['isLoggedIn', 'isAdmin', 'getCurrentUser']);
        spectrumService = jasmine.createSpyObj('Spectrum', ['searchSpectra', 'searchSpectraCount', 'delete']);
        toasterService = jasmine.createSpyObj('ToasterService', ['pop']);
        modalService = jasmine.createSpyObj('NgbModal', ['open']);

        // Default: not logged in, no spectra loaded
        authService.isLoggedIn.and.returnValue(false);
        authService.isAdmin.and.returnValue(false);
        spectrumService.searchSpectra.and.returnValue(of([]));
        spectrumService.searchSpectraCount.and.returnValue(of({count: 0}));

        const spectraQueryBuilderMock = jasmine.createSpyObj('SpectraQueryBuilderService',
            ['hasSimilarityQuery', 'executeQuery', 'prepareQuery', 'addUserToQuery', 'getSimilarityQuery']);
        const spectrumCacheMock = jasmine.createSpyObj('SpectrumCacheService',
            ['hasCurrentCount', 'getCurrentCount', 'setCurrentCount']);
        spectrumCacheMock.hasCurrentCount.and.returnValue(false);

        const cookieMock = jasmine.createSpyObj('CookieMain', ['get', 'update', 'getBooleanValue', 'remove']);
        cookieMock.get.and.returnValue(undefined);
        cookieMock.getBooleanValue.and.returnValue(false);

        const locationMock = jasmine.createSpyObj('Location', ['path']);
        locationMock.path.and.returnValue('/spectra/browse');

        await TestBed.configureTestingModule({
            imports: [RouterTestingModule, NgbModule],
            declarations: [SpectraBrowserComponent],
            providers: [
                {provide: AuthenticationService, useValue: authService},
                {provide: Spectrum, useValue: spectrumService},
                {provide: MassDeletionService, useValue: jasmine.createSpyObj('MassDeletionService', ['executeDeletion'])},
                {provide: ToasterService, useValue: toasterService},
                {provide: SpectraQueryBuilderService, useValue: spectraQueryBuilderMock},
                {provide: SpectrumCacheService, useValue: spectrumCacheMock},
                {provide: CookieMain, useValue: cookieMock},
                {provide: NGXLogger, useValue: jasmine.createSpyObj('NGXLogger', ['debug', 'info', 'error'])},
                {provide: GoogleAnalyticsService, useValue: jasmine.createSpyObj('GoogleAnalyticsService', ['event'])},
                {provide: FeedbackCacheService, useValue: jasmine.createSpyObj('FeedbackCacheService', ['resolveFeedback'])},
                {provide: Metadata, useValue: jasmine.createSpyObj('Metadata', ['getMetaData'])},
                {provide: Location, useValue: locationMock},
                {provide: NgbModal, useValue: modalService},
                {provide: ActivatedRoute, useValue: {queryParams: of({})}}
            ],
            schemas: [NO_ERRORS_SCHEMA]
        })
        // These are logic-only tests; overriding the template with an empty string avoids compiling
        // the real one, which references custom pipes (e.g. curlPipe) that NO_ERRORS_SCHEMA does not stub
        .overrideComponent(SpectraBrowserComponent, {set: {template: ''}})
        .compileComponents();

        fixture = TestBed.createComponent(SpectraBrowserComponent);
        component = fixture.componentInstance;
    });

    // -------------------------------------------------------------------------
    // canDelete — permission checks
    // -------------------------------------------------------------------------
    describe('canDelete()', () => {
        const ownSpectrum   = makeSpectrum('MoNA_own', REGULAR_USER.emailAddress);
        const otherSpectrum = makeSpectrum('MoNA_other', 'other@test.com');

        it('allows admin to delete any spectrum, including those they do not own', () => {
            authService.isLoggedIn.and.returnValue(true);
            authService.isAdmin.and.returnValue(true);
            authService.getCurrentUser.and.returnValue(ADMIN_USER);
            expect(component.canDelete(otherSpectrum)).toBeTrue();
        });

        it('allows a logged-in user to delete their own spectrum', () => {
            authService.isLoggedIn.and.returnValue(true);
            authService.isAdmin.and.returnValue(false);
            authService.getCurrentUser.and.returnValue(REGULAR_USER);
            expect(component.canDelete(ownSpectrum)).toBeTrue();
        });

        it('prevents a logged-in user from deleting another user\'s spectrum', () => {
            authService.isLoggedIn.and.returnValue(true);
            authService.isAdmin.and.returnValue(false);
            authService.getCurrentUser.and.returnValue(REGULAR_USER);
            expect(component.canDelete(otherSpectrum)).toBeFalse();
        });

        it('prevents an anonymous user from deleting any spectrum', () => {
            authService.isLoggedIn.and.returnValue(false);
            authService.isAdmin.and.returnValue(false);
            expect(component.canDelete(otherSpectrum)).toBeFalse();
        });
    });

    // -------------------------------------------------------------------------
    // deleteSpectrum — opens the confirmation modal; performDelete does the work
    // -------------------------------------------------------------------------
    describe('deleteSpectrum() / performDelete()', () => {
        const spectrum = makeSpectrum('MoNA_0010', REGULAR_USER.emailAddress);

        beforeEach(() => {
            authService.isLoggedIn.and.returnValue(true);
            authService.getCurrentUser.and.returnValue(REGULAR_USER);
            component.spectra = [spectrum];
        });

        it('opens the confirmation modal instead of a native confirm', () => {
            // result never settles so no delete is triggered during this test
            modalService.open.and.returnValue({componentInstance: {}, result: new Promise(() => {})} as any);
            component.deleteSpectrum('MoNA_0010', new MouseEvent('click'));
            expect(modalService.open).toHaveBeenCalled();
        });

        it('performDelete calls spectrum.delete with the correct id and user token', () => {
            spectrumService.delete.and.returnValue(of(null));
            component.performDelete('MoNA_0010');
            expect(spectrumService.delete).toHaveBeenCalledWith('MoNA_0010', REGULAR_USER.accessToken);
        });

        it('performDelete removes the spectrum from the list on success', () => {
            spectrumService.delete.and.returnValue(of(null));
            component.performDelete('MoNA_0010');
            expect(component.spectra.find(s => s.id === 'MoNA_0010')).toBeUndefined();
        });

        it('performDelete shows an error toast on failure and keeps the spectrum in the list', () => {
            spectrumService.delete.and.returnValue(throwError({message: 'Server error'}));
            component.performDelete('MoNA_0010');
            expect(toasterService.pop).toHaveBeenCalledWith(jasmine.objectContaining({type: 'error'}));
            expect(component.spectra.find(s => s.id === 'MoNA_0010')).toBeDefined();
        });
    });
});
