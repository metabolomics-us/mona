'use strict';

describe('service: Upload Library Service', function(){
  beforeEach(module('moaClientApp'), function($httpProvider) {
    $httpProvider.interceptors.push('moaClientApp');
  });

  var UploadLibraryService,rootScope,q,httpBackend;

  beforeEach(function() {
    angular.mock.inject(function($injector,_UploadLibraryService_) {
      UploadLibraryService = _UploadLibraryService_;
      rootScope = $injector.get('$rootScope');
      q = $injector.get('$q');
      httpBackend = $injector.get('$httpBackend');

      spyOn(UploadLibraryService, 'processData').and.callThrough();
      spyOn(UploadLibraryService, 'uploadSpectraFiles').and.callThrough();
    });
  });

  var spectra = function() {
    return {name: 'testSpectra',
            metadata: 'test',
            meta: [{name: 'ABI Chem',value: {eq: 'AC1Q2J5R'}}],
            inchiKey: '12098',
            inchi: '123456'
            }
  };



  var flush = function() {
    httpBackend.expectGET('views/main.html').respond(200);
    httpBackend.flush();
  };

  var specCallback = function(s){return s};

  it('submits a spectrum', function() {
    var res = UploadLibraryService.submitSpectrum(spectra());
    expect(res instanceof q);
  });

  it ('submits a spectra data(s) and return user to main view', function() {
    var spec = spectra();
    spec.molFile = 'file1.txt';
    spec.names = ['alias1','alias2'];
    spec.tags = ['tag1','tag2'];
    spec.comments = 'testing our spec uploader';
    var res = UploadLibraryService.submitSpectrum(spec, null, specCallback);
    flush();
    rootScope.$digest();
    expect(res.$$state.value).isDefined;
  });

  it('adds additionalData to spectra when provided', function() {
    var spec = spectra();
    var aData = {comments: 'additional data', tags:['moreTags1','moreTags2'], meta: [{name: 'one'}, {name: 'two'}]};
    var res = UploadLibraryService.submitSpectrum(spec, null, specCallback, aData);
    flush();
    rootScope.$digest();
    expect(res.$$state.value.comments[1].comment).toBe('additional data');
  });

  it('records the submitter email', function() {
    var spec = spectra();
    var res = UploadLibraryService.submitSpectrum(spec,'testUser@fiehnlab.com',specCallback);
    flush();
    rootScope.$digest();
    expect(res.$$state.value.submitter).toBe('testUser@fiehnlab.com');
  });

  it('loads spectra file and returns the data to a callback function', function(){
    var f = {name: 'testfile.txt'};
    /*
    var eventListener = jasmine.createSpy();
    spyOn(window, "FileReader").andReturn({
      addEventListener: eventListener
    });*/
    UploadLibraryService.loadSpectraFile(f,specCallback,true);
  });

  it('counts data', function() {
    var data = ['test','Num Peaks'];
    UploadLibraryService.countData(data);
  });

  it('alerts user of unsupported file formats', function() {
    spyOn(window, 'alert');
    var data = ['test','Num Peaks'];
    var origin = 'fake';
    UploadLibraryService.countData(data,origin);
    expect(window.alert).toHaveBeenCalledWith('not supported file format!');
  });

  it('counts data of .msp origin', function() {
    var data = ['test','Num Peaks'];
    var origin = 'testdata.msp';
    var res = UploadLibraryService.countData(data,origin);
    expect(res >=0).toBeTruthy();
  });

  it('counts data of .mgf origin', function() {
    var data = ['test','Num Peaks'];
    var origin = 'testdata.mgf';
    var res = UploadLibraryService.countData(data,origin);
    expect(res >=0).toBeTruthy();
  });

  it('counts data of .txt origin', function() {
    var data = ['test','Num Peaks'];
    var origin = 'testdata.txt';
    var res = UploadLibraryService.countData(data,origin);
    expect(res >=0).toBeTruthy();
  });

  it('processes .txt data', function() {
    var data = ['test','Num Peaks'];
    var origin = 'testdata.txt';
    UploadLibraryService.processData(data, specCallback, origin);
    expect(UploadLibraryService.processData).toHaveBeenCalled();
  });

  it('processes .msp data', function() {
    var data = ['test','Num Peaks'];
    var origin = 'testdata.msp';
    UploadLibraryService.processData(data, specCallback, origin);
    expect(UploadLibraryService.processData).toHaveBeenCalled();
  });

  it('processes .mgf data', function() {
    var data = ['test','Num Peaks'];
    var origin = 'testdata.mgf';
    UploadLibraryService.processData(data, specCallback, origin);
    expect(UploadLibraryService.processData).toHaveBeenCalled();
  });

  it('adds data without origin', function() {
    var data = ['test', 'Num Peaks'];
    UploadLibraryService.processData(data,specCallback);
    expect(UploadLibraryService.processData).toHaveBeenCalledWith(data,specCallback);
  });

  it('alerts user file format is not supported', function() {
    spyOn(window, 'alert');
    var data = ['test','Num Peaks'];
    var origin = 'testdata.jar';
    UploadLibraryService.processData(data, specCallback, origin);
    expect(window.alert).toHaveBeenCalledWith('not supported file format!');
  });

  it('upload spectra files', function() {
    var files = ['file1.txt', 'file2.mgf', 'file3.msp'];
    var wizardData = {power: 'lightning'};
    UploadLibraryService.uploadSpectraFiles(files, specCallback, wizardData);
    expect(UploadLibraryService.uploadSpectraFiles).toHaveBeenCalledWith(files,specCallback,wizardData);
  });
/*
  it('uploads a spectra',function() {
    var spec = [spectra(),spectra(),spectra()];
    rootScope.currentUser = {name: 'test', submitter: 'test@fiehnlab.com'};
    rootScope.currentUser.access_token = 'TOKEN';
    var aData = {comments: 'additional data', tags:['moreTags1','moreTags2'], meta: [{name: 'one'}, {name: 'two'}]};
    UploadLibraryService.uploadSpectrum(spec,specCallback,aData);
    rootScope.$digest();
  });*/



});
