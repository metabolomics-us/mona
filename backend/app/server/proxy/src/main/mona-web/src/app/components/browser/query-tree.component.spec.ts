import {QueryTreeComponent} from "./query-tree.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {Download} from "../../services/persistence/download.resource";
import {HttpClientModule} from "@angular/common/http";
import {LoggerTestingModule} from "ngx-logger/testing";
import {BehaviorSubject, Observable} from "rxjs";

describe('Query Tree Component', () => {
  let component: QueryTreeComponent;
  let fixture: ComponentFixture<QueryTreeComponent>;
  let downloadServiceStub: Partial<Download>;
  let download: Download;
  let fakePredefinedQueries: BehaviorSubject<any>;
  let fakeStaticQueries: BehaviorSubject<any>;
  const node = {query: 'test'};

  beforeEach(async () => {
    fakePredefinedQueries = new BehaviorSubject<any>([{
      "label": "Libraries - MassBank",
      "description": "MassBank",
      "query": "tags.text==\"MassBank\"",
      "queryCount": 72439,
      "jsonExport": {
        "id": "86fb1b69-14ee-4df4-abfe-808686eae197",
        "label": "Libraries - MassBank",
        "query": "tags.text==\"MassBank\"",
        "format": "json",
        "date": 1643399447951,
        "count": 72439,
        "size": 47946428,
        "queryFile": "MoNA-export-MassBank-query.txt",
        "exportFile": "MoNA-export-MassBank-json.zip"
      },
      "mspExport": {
        "id": "8d488857-05a8-4e26-b61a-ee096c5b28ea",
        "label": "Libraries - MassBank",
        "query": "tags.text==\"MassBank\"",
        "format": "msp",
        "date": 1643399447951,
        "count": 72439,
        "size": 26057637,
        "queryFile": "MoNA-export-MassBank-query.txt",
        "exportFile": "MoNA-export-MassBank-msp.zip"
      },
      "sdfExport": {
        "id": "b6f75099-b166-40ec-8e0b-80b55ed13454",
        "label": "Libraries - MassBank",
        "query": "tags.text==\"MassBank\"",
        "format": "sdf",
        "date": 1643399447951,
        "count": 72439,
        "size": 35379415,
        "queryFile": "MoNA-export-MassBank-query.txt",
        "exportFile": "MoNA-export-MassBank-sdf.zip"
      }
    }]);

    fakeStaticQueries = new BehaviorSubject<any>([{
      "fileName": "MoNA-export-All_Spectra-identifier-table-ids.zip",
      "description": "Table of spectral and chemical identifiers for all MoNA records"
    }]);

    downloadServiceStub = {
      getPredefinedQueries(): Observable<any> {
        return fakePredefinedQueries.asObservable();
      },
      getStaticDownloads(): Observable<any> {
        return fakeStaticQueries.asObservable();
      }
    };

    spyOn(downloadServiceStub, 'getPredefinedQueries').and.callThrough();
    spyOn(downloadServiceStub, 'getStaticDownloads').and.callThrough();

    await TestBed.configureTestingModule({
      declarations: [QueryTreeComponent],
      providers: [{provide: Download, useValue: downloadServiceStub}],
      imports: [
        HttpClientModule, LoggerTestingModule
      ]
    });
    fixture = TestBed.createComponent(QueryTreeComponent);
    component = fixture.componentInstance;
    download = TestBed.inject(Download);

    fixture.detectChanges();
  });

  it('executes queries', () => {
    component.executeQuery(node);
    expect(downloadServiceStub.getStaticDownloads).toHaveBeenCalled();
    expect(downloadServiceStub.getPredefinedQueries).toHaveBeenCalled();
  });
});
