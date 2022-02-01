import {RegistrationModalComponent} from "./registration-modal.component";
import {SubmitterFormComponent} from "../submitter/submitter-form.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {AuthenticationService} from "../../services/authentication.service";
import {BehaviorSubject, Observable} from "rxjs";
import {NgbModule, NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {HttpClientModule} from "@angular/common/http";
import {LoggerTestingModule} from "ngx-logger/testing";
import {User} from "../../mocks/user.model";
import {RegistrationService} from "../../services/registration.service";
import {FormsModule} from "@angular/forms";
import {NewSubmitter} from "../../mocks/newSubmitter.model";

describe('Authentication Modal Component', () => {
  let component: RegistrationModalComponent;
  let ngbModal: NgbActiveModal;
  let fixture: ComponentFixture<RegistrationModalComponent>;
  let authenticationServiceStub: Partial<AuthenticationService>;
  let registrationServiceStub: Partial<RegistrationService>
  let authenticationService: AuthenticationService;
  let registrationService: RegistrationService;
  let fakeIsAdmin$: BehaviorSubject<boolean>;
  let fakeIsLoggedIn$: BehaviorSubject<boolean>;
  let fakeIsAuthenticated$: BehaviorSubject<boolean>;
  let fakeCurrentUser$: BehaviorSubject<User>;
  let fakeModalRequest$: BehaviorSubject<boolean>;
  let fakeLoginPaylod$: BehaviorSubject<any>;

  let fakeRegistrationSubmit$: BehaviorSubject<boolean>;
  let fakeRegistrationAuthorization$: BehaviorSubject<boolean>;
  let fakeRegistrationAsSubmitter$: BehaviorSubject<boolean>;

  beforeEach(async () => {
    fakeIsAdmin$ = new BehaviorSubject(false);
    fakeIsLoggedIn$ = new BehaviorSubject(false);
    fakeIsAuthenticated$ = new BehaviorSubject(false);
    fakeCurrentUser$ = new BehaviorSubject<User>(null)
    fakeModalRequest$ = new BehaviorSubject<boolean>(false);
    fakeLoginPaylod$ = new BehaviorSubject<any>(false);
    fakeRegistrationSubmit$ = new BehaviorSubject<boolean>(false);
    fakeRegistrationAuthorization$ = new BehaviorSubject<boolean>(false);
    fakeRegistrationAsSubmitter$ = new BehaviorSubject<boolean>(false);

    authenticationServiceStub = {
      isLoggedIn(): boolean {
        return fakeIsLoggedIn$.value;
      },
      isAdmin(): boolean {
        return fakeIsAdmin$.value;
      },
      isAuthenticated: fakeIsAuthenticated$.asObservable(),
      logout(): void {

      },
      validate(): void {

      },
      currentUser: fakeCurrentUser$.asObservable(),
      modalRequest: fakeModalRequest$.asObservable(),
      login(): Observable<any> {
        return fakeLoginPaylod$.asObservable();
      }
    };

    registrationServiceStub = {
      submit(): Observable<any> {
        return fakeRegistrationSubmit$.asObservable();

      },
      authorize(): Observable<any> {
        return fakeRegistrationAuthorization$.asObservable();
      },
      registerAsSubmitter(): Observable<any> {
        return fakeRegistrationAsSubmitter$.asObservable()
      },
      resetSubmitter(): void {
        this.newSubmitter = new NewSubmitter();
      },
      newSubmitter: new NewSubmitter()
    };

    spyOn(authenticationServiceStub, 'isLoggedIn').and.callThrough();
    spyOn(authenticationServiceStub, 'isAdmin').and.callThrough();
    spyOn(authenticationServiceStub, 'validate').and.callThrough();
    spyOn(authenticationServiceStub, 'logout').and.callThrough();
    spyOn(authenticationServiceStub, 'login').and.callThrough();
    spyOn(registrationServiceStub, 'submit').and.callThrough();
    spyOn(registrationServiceStub, 'authorize').and.callThrough();
    spyOn(registrationServiceStub, 'registerAsSubmitter').and.callThrough();
    spyOn(registrationServiceStub, 'resetSubmitter').and.callThrough();

    await TestBed.configureTestingModule({
      declarations: [RegistrationModalComponent, SubmitterFormComponent],
      providers: [{provide: AuthenticationService, useValue: authenticationServiceStub}, NgbActiveModal,
                  {provide: RegistrationService, useValue: registrationServiceStub}],
      imports: [
        NgbModule, HttpClientModule, LoggerTestingModule, FormsModule
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(RegistrationModalComponent);
    component = fixture.componentInstance;
    ngbModal = TestBed.inject(NgbActiveModal);

    authenticationService = TestBed.inject(AuthenticationService);
    registrationService = TestBed.inject(RegistrationService);
    fixture.detectChanges();
  });

  it('can cancel a registration dialog', () => {
    spyOn(ngbModal, 'dismiss');
    component.cancelDialog();
    expect(ngbModal.dismiss).toHaveBeenCalledWith('cancel');
  });

  it('register a user that submits all the correct information and is in a registering state', () => {
    registrationServiceStub.newSubmitter.firstName = 'test';
    registrationServiceStub.newSubmitter.lastName = 'user';
    registrationServiceStub.newSubmitter.institution = 'UC Davis';
    registrationServiceStub.newSubmitter.emailAddress = 'testuser@fiehnlab.com';
    registrationServiceStub.newSubmitter.password = 'super';
    component.submitRegistration();
    expect(component.state).toBe('success');
  });

  it('returns an error message when registration data is not submitted correctly', () => {
    registrationServiceStub.newSubmitter.lastName = 'user';
    registrationServiceStub.newSubmitter.institution = 'UC Davis';
    registrationServiceStub.newSubmitter.emailAddress = 'testuser@fiehnlab.com';
    registrationServiceStub.newSubmitter.password = 'super';
    fakeRegistrationSubmit$.next(false);
    fakeRegistrationAsSubmitter$.error({status: 422, name: 'Forbidden', error: {
        error: 'No first name',
        exception: 'stuff',
        message: 'No first name',
      }
    });
    component.submitRegistration();
    expect(component.errors).toEqual([{status: 422, name: 'Forbidden', error: 'No first name', exception: 'stuff', message: 'No first name'}]);
  });

  it('returns an error if a user registers with an existing email address', () => {
    registrationServiceStub.newSubmitter.lastName = 'user';
    registrationServiceStub.newSubmitter.institution = 'UC Davis';
    registrationServiceStub.newSubmitter.emailAddress = 'testuser@fiehnlab.com';
    registrationServiceStub.newSubmitter.password = 'super';
    fakeRegistrationAsSubmitter$.next(false);
    fakeRegistrationSubmit$.error({status: 409, name: 'Unauthorized'});
    component.submitRegistration();
    expect(component.errors).toEqual([{status: 409, name: 'Unauthorized', error: 'REST API Error',
      exception: 'Problem when calling Registration REST API',
      message: 'This problem typically arises when the account being registered already exists. Please contact the admin if you need your password reset.'
    }]);
  });
});
