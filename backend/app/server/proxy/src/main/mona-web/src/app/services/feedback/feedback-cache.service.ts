import {NGXLogger} from "ngx-logger";
import {Injectable} from "@angular/core";
import {Feedback} from "../persistence/feedback.resource";
import {catchError, first, shareReplay} from "rxjs/operators";
import {Observable} from "rxjs";

@Injectable()
export class FeedbackCacheService {
  currentFeedback;

  constructor(public logger: NGXLogger, public feedback: Feedback) {
    this.currentFeedback = {};
  }

  resolveFeedback(id: string): Observable<any> {
    if (this.currentFeedback[id]) {
      this.logger.info('Returning cached feedback');
      return this.currentFeedback[id];
    } else {
      this.currentFeedback[id] = this.feedback.get(id).pipe(
        shareReplay(1),
        catchError((err) => {
          delete this.currentFeedback[id];
          return [];
        })
      );

      return this.currentFeedback[id];
    }
  }
}
