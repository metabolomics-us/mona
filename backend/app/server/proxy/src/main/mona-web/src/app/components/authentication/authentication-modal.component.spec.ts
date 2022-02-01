import {AuthenticationModalComponent} from "./authentication-modal.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {AuthenticationService} from "../../services/authentication.service";
import {BehaviorSubject, Observable} from "rxjs";
import {NgbModule, NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {HttpClientModule} from "@angular/common/http";
import {LoggerTestingModule} from "ngx-logger/testing";
import {User} from "../../mocks/user.model";

describe('Authentication Modal Component', () => {
  let component: AuthenticationModalComponent;
  let ngbModal: NgbActiveModal;
  let fixture: ComponentFixture<AuthenticationModalComponent>;
  let authenticationServiceStub: Partial<AuthenticationService>;
  let authenticationService: AuthenticationService;
  let fakeIsAdmin$: BehaviorSubject<boolean>;
  let fakeIsLoggedIn$: BehaviorSubject<boolean>;
  let fakeIsAuthenticated$: BehaviorSubject<boolean>;
  let fakeCurrentUser$: BehaviorSubject<User>;
  let fakeModalRequest$: BehaviorSubject<boolean>;
  let fakeLoginPaylod$: BehaviorSubject<any>;

  beforeEach(async () => {
    fakeIsAdmin$ = new BehaviorSubject(false);
    fakeIsLoggedIn$ = new BehaviorSubject(false);
    fakeIsAuthenticated$ = new BehaviorSubject(false);
    fakeCurrentUser$ = new BehaviorSubject<User>(null)
    fakeModalRequest$ = new BehaviorSubject<boolean>(false);
    fakeLoginPaylod$ = new BehaviorSubject<any>(false);

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

    spyOn(authenticationServiceStub, 'isLoggedIn').and.callThrough();
    spyOn(authenticationServiceStub, 'isAdmin').and.callThrough();
    spyOn(authenticationServiceStub, 'validate').and.callThrough();
    spyOn(authenticationServiceStub, 'logout').and.callThrough();
    spyOn(authenticationServiceStub, 'login').and.callThrough();

    await TestBed.configureTestingModule({
      declarations: [AuthenticationModalComponent],
      providers: [{provide: AuthenticationService, useValue: authenticationServiceStub}, NgbActiveModal],
      imports: [
        NgbModule, HttpClientModule, LoggerTestingModule
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(AuthenticationModalComponent);
    component = fixture.componentInstance;
    ngbModal = TestBed.inject(NgbActiveModal);

    authenticationService = TestBed.inject(AuthenticationService);
    fixture.detectChanges();
  });

  it('instantiate the controller properly', () => {
    expect(component).not.toBeUndefined();
  });

  it('can cancel a dialog', () => {
    spyOn(ngbModal, 'dismiss');
    component.cancelDialog();
    expect(ngbModal.dismiss).toHaveBeenCalledWith('cancel');
  });

  it('pushes error when a user attempt to login without an email address or password', () => {
    component.submitLogin();
    expect(component.errors[0]).toBe('Please enter your email address');
    expect(component.errors[1]).toBe('Please enter your password');
  });

  it('submits email and password credentials to authentication service', () => {
    component.credentials = {
      email: 'testuser@fiehnlab.com',
      password: 'super'
    };
    component.submitLogin();
    expect(authenticationServiceStub.login).toHaveBeenCalledWith('testuser@fiehnlab.com', 'super');
  });

  it('is in a success state on success-login', () => {
    component.credentials = {
      email: 'testuser@fiehnlab.com',
      password: 'super'
    };
    fakeLoginPaylod$.next({payload: 'aUser'});
    component.submitLogin();
    expect(component.state).toBe('success');
  });

  it('returns an error for invalid email or password', () => {
    component.credentials = {
      email: 'testuser@fiehnlab.com',
      password: 'fake'
    };
    fakeLoginPaylod$.error({status: 'fake', name: '401', error: {
          error: 'fake',
          message: 'error'
      }
    });
    component.submitLogin();
    expect(component.errors).toEqual([{status: 'fake', name: '401', error: 'fake', message: 'error'}]);
  });
});
