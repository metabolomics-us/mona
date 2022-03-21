import {Injectable} from '@angular/core';
import {Spectrum} from './spectrum.resource';
import {Observable} from 'rxjs';

@Injectable()
export class MassDeletionService {
  public idsForDeletion: Array<any>;

  constructor( public spectrum: Spectrum) {
    this.idsForDeletion = [];
  }

  addForDeletion(e: any) {
    this.idsForDeletion.push(e);
  }

  executeDeletion(token: any): Observable<any> {
    const selected = this.idsForDeletion.filter((x) => {
      if (x.selected) {
        return x;
      }
    }).map((x) => {
      return x.id;
    });
    this.idsForDeletion = [];

    return this.spectrum.batchDeleteByIds(selected, token);
  }

  getObject(id: string): any {
    return this.idsForDeletion.find(x => x.id === id);
  }

  toggleCheckbox(id: string) {
    const found = this.idsForDeletion.find(x => x.id === id);
    const newValue = {
      id: found.id,
      selected: !found.selected
    };

    const index = this.idsForDeletion.findIndex(x => x.id === id);
    if (index > -1) { this.idsForDeletion.splice(index, 1); }
    this.idsForDeletion.push(newValue);
  }

  showCurrentSelected(): Array<string> {
    return this.idsForDeletion.filter((x) => {
      if (x.selected) {
        return x;
      }
    }).map((x) => {
      return x.id;
    });
  }
}
