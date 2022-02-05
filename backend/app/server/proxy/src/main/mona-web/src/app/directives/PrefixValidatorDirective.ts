/**
 * Created by nolanguzman on 01/20/22.
 * Implementation based off https://weblog.west-wind.com/posts/2019/Nov/18/Creating-Angular-Synchronous-and-Asynchronous-Validators-for-Template-Validation
 */
import {Directive} from "@angular/core";
import {
  AbstractControl,
  NG_ASYNC_VALIDATORS,
  ValidationErrors,
  AsyncValidator
} from "@angular/forms";
import {Spectrum} from "../services/persistence/spectrum.resource";
import {map} from "rxjs/operators";
import {Observable, of} from "rxjs";

@Directive({
  selector: '[prefixValidator][ngModel],[prefixValidator][FormControl]',
  providers: [
    {provide: NG_ASYNC_VALIDATORS, useExisting: PrefixValidator, multi: true}
  ]
})

export class PrefixValidator implements AsyncValidator {
  constructor(public spectrum: Spectrum) {
  }

  validate(control: AbstractControl): Observable<ValidationErrors> | null {
    const val = control.value;
    if (!val) {
      return null;
    }

    //Insure that only numbers and letters are used, if not return error
    const hasNumAndLetterOnly = /[^A-Za-z0-9]+/.test(val);
    if(hasNumAndLetterOnly) {
      return of({prefixValidator: 'Prefix contains invalid characters'});
    }

    //Make query to backend to see if a spectrum with given ID prefix exists, if so return error
    const searchQuery = `id==\'${val}000001\'`
    const call = this.spectrum.searchSpectraCount({
      endpoint: 'count',
      query: searchQuery,
      text: ''
    }).pipe(
      map((x) => {
        return (x.count === 0) ? null : {prefixValidator: 'Prefix already exists.'};
      })
    );

    return call;
  }
}
