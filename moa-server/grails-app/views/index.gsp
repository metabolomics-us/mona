%{--<!DOCTYPE html>--}%
%{--<html>--}%
%{--<head>--}%
    %{--<meta name="layout" content="main"/>--}%
    %{--<title>server only nothing to see</title>--}%
%{--</head>--}%

%{--<body>--}%
    %{--this app is only a rest server, nothing else is supposed to happen here--}%
%{--</body>--}%
%{--</html>--}%

<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap.css">

%{--<g:javascript>--}%
    %{--$.ajax({--}%
       %{--/* url: '/controller/ajax_wrapper/',--}%
        %{--success: function( aResponse ) {--}%
            %{--var response = $.parseJSON( aResponse );--}%
%{----}%
            %{--console.log( response );--}%
        %{--}*/--}%
    %{--});--}%
%{--</g:javascript>--}%

<div>
<div ng-value="error"></div>
<div class="container">
<div class="blog-header">
    <h2 class="blog-title">Endpoint Documentation</h2>
</div>

<div class="row">

<div class="col-sm-10">

<h3 id="information">Information</h3>
<p>All results are in json format.</p>
<p>More specific searches can be made by using /?{query} on the end of the URI.
</p>

<h3 id="Dynamic Mappings">Dynamic Mappings</h3>
<table class="table table-hover" style="table-layout: fixed;">
    <thead>
    <tr>
        <th class="text-center" style="width: 110px">URI</th>
        <th class="text-center" style="width: 110px">Method</th>
        <th class="text-center" style="width: 110px">Details</th>

    </tr>
    </thead>
    <tbody>
    <tr>    <td>/</td>                                          <td class="text-center">*</td>
        <td>Shows default message</td>                      </tr>
    <tr>    <td>/$ {controller}/$ {action}?/$ {id}?</td>           <td class="text-center">*</td>
        <td>This is the formula for all endpoints</td>      </tr>
    <tr>    <td>/rest</td>                                      <td class="text-center">*</td>
        <td>Defaults to default message</td>                </tr>

    </tbody>
</table>

<h3 id="Controller: Compound">Controller: Compound</h3>
<table class="table table-hover" style="table-layout: fixed;">
    <thead>
    <tr>
        <th class="text-center" style="width: 110px">URI</th>
        <th class="text-center" style="width: 110px">Method</th>
        <th class="text-center" style="width: 110px">Details</th>

    </tr>
    </thead>
    <tbody>
    <tr>    <td>/rest/compounds</td>                            <td class="text-center">GET</td>
        <td>Shows the list of compounds</td>                </tr>
    <tr>    <td>/rest/compounds/create</td>                      <td class="text-center">GET</td>
        <td>Creates a compound with the given id</td>           </tr>
    <tr>    <td>/rest/compounds</td>                             <td class="text-center">POST</td>
        <td>Saves a list of compounds</td>                   </tr>
    <tr>    <td>/rest/compounds/$ {id}</td>                      <td class="text-center">GET</td>
        <td>Shows the compound with the given id</td>             </tr>
    <tr>    <td>/rest/compounds/$ {id}/edit</td>                 <td class="text-center">GET</td>
        <td>Allows editing of the compound with the given id</td>             </tr>
    <tr>    <td>/rest/compounds/$ {id}</td>                      <td class="text-center">PUT</td>
        <td>Updates the compound with the given id</td>           </tr>
    <tr>    <td>/rest/compounds/$ {id}</td>                      <td class="text-center">DELETE</td>
        <td>Deletes the compound with the given id</td>           </tr>
    <tr>    <td>/rest/meta/category/$ {MetaDataCategoryId}/data/$ {MetaDataId}/compounds</td>                      <td class="text-center">GET</td>
        <td>Shows the compounds with the given a metaDataCategory id and a metaData id</td>           </tr>
    <tr>    <td>/rest/meta/category/$ {MetaDataCategoryId}/data/$ {MetaDataId}/compounds/create</td>               <td class="text-center">GET</td>
        <td>Creates a compound with the given metaDataCategory id and metaData id</td>           </tr>
    <tr>    <td>/rest/meta/category/$ {MetaDataCategoryId}/data/$ {MetaDataId}/compounds</td>                      <td class="text-center">POST</td>
        <td>Saves the compounds with the given metaDataCategory id and metaData id</td>           </tr>
    <tr>    <td>/rest/meta/category/$ {MetaDataCategoryId}/data/$ {MetaDataId}/compounds/$ {id}</td>                <td class="text-center">GET</td>
        <td>Shows the compound with the given metaDataCategory id and metaData id and compound id</td>           </tr>
    <tr>    <td>/rest/meta/category/$ {MetaDataCategoryId}/data/$ {MetaDataId}/compounds/$ {id}/edit</td>           <td class="text-center">GET</td>
        <td>Allows editing of the compound with the given metaDataCategory id and metaData id and compound id</td>           </tr>
    <tr>    <td>/rest/meta/category/$ {MetaDataCategoryId}/data/$ {MetaDataId}/compounds/$ {id}</td>                <td class="text-center">PUT</td>
        <td>Updates the compound with the given metaDataCategory id and metaData id and compound id</td>           </tr>
    <tr>    <td>/rest/meta/category/$ {MetaDataCategoryId}/data/$ {MetaDataId}/compounds/$ {id}</td>                <td class="text-center">DELETE</td>
        <td>Deletes the compound with the given metaDataCategory id and metaData id and compound id</td>           </tr>

    </tbody>
</table>

<h3 id="Controller: MetaData">Controller: MetaData</h3>
<table class="table table-hover" style="table-layout: fixed;">
    <thead>
    <tr>
        <th class="text-center" style="width: 110px">URI</th>
        <th class="text-center" style="width: 110px">Method</th>
        <th class="text-center" style="width: 110px">Details</th>

    </tr>
    </thead>
    <tbody>
    <tr>    <td>/rest/meta/data</td>                                            <td class="text-center">GET</td>
        <td>Displays list of metaData</td>             </tr>
    <tr>    <td>/rest/meta/data/create</td>                                     <td class="text-center">GET</td>
        <td>Creates a metaData, given an id</td>           </tr>
    <tr>    <td>/rest/meta/data</td>                                            <td class="text-center">POST</td>
        <td>Saves a list of the metaData</td>             </tr>
    <tr>    <td>/rest/meta/data/$ {id}</td>                                      <td class="text-center">GET</td>
        <td>Shows the metaData with the given id</td>             </tr>
    <tr>    <td>/rest/meta/data/$ {id}/edit</td>                                 <td class="text-center">GET</td>
        <td>Allows editing of the metaData with the given id</td>             </tr>
    <tr>    <td>/rest/meta/data/$ {id}</td>                                      <td class="text-center">PUT</td>
        <td>Updates the metaData with the given id</td>           </tr>
    <tr>    <td>/rest/meta/data/$ {id}</td>                                      <td class="text-center">DELETE</td>
        <td>Deletes the meta data with the given id</td>           </tr>
    <tr>    <td>/rest/meta/category/$ {MetaDataCategoryId}/data</td>             <td class="text-center">GET</td>
        <td>Displays the data with the metaDataCategory (given its id)</td>            </tr>
    <tr>    <td>/rest/meta/category/$ {MetaDataCategoryId}/data/create</td>      <td class="text-center">GET</td>
        <td>Creates data with the given metaDataCategory (given its id)</td>           </tr>
    <tr>    <td>/rest/meta/category/$ {MetaDataCategoryId}/data</td>             <td class="text-center">POST</td>
        <td>Saves the data of the given metaDataCategory (given its id)</td>             </tr>
    <tr>    <td>/rest/meta/category/$ {MetaDataCategoryId}/data/$ {id}</td>       <td class="text-center">GET</td>
        <td>Shows the metaData with the given id and given metaDataCategory (given its id)</td>             </tr>
    <tr>    <td>/rest/meta/category/$ {MetaDataCategoryId}/data/$ {id}/edit</td>  <td class="text-center">GET</td>
        <td>Allows editing of the data with the given id and given metaDataCategory (given its id)</td>             </tr>
    <tr>    <td>/rest/meta/category/$ {MetaDataCategoryId}/data/$ {id}</td>       <td class="text-center">PUT</td>
        <td>Updates the data with the given id and given metaDataCategory (given its id)</td>           </tr>
    <tr>    <td>/rest/meta/category/$ {MetaDataCategoryId}/data/$ {id}</td>       <td class="text-center">DELETE</td>
        <td>Deletes the data with the given id and given metaDataCategory (given its id)</td>           </tr>

    </tbody>
</table>

<h3 id="Controller: MetaDataCategory">Controller: MetaDataCategory</h3>
<table class="table table-hover" style="table-layout: fixed;">
    <thead>
    <tr>
        <th class="text-center" style="width: 110px">URI</th>
        <th class="text-center" style="width: 110px">Method</th>
        <th class="text-center" style="width: 110px">Details</th>

    </tr>
    </thead>
    <tbody>
    <tr>    <td>/rest/meta/category</td>                  <td class="text-center">GET</td>
        <td>Displays a list of the metaData categories</td>             </tr>
    <tr>    <td>/rest/meta/category/create</td>           <td class="text-center">GET</td>
        <td>Creates a metaData category</td>   </tr>
    <tr>    <td>/rest/meta/category</td>                  <td class="text-center">POST</td>
        <td>Saves the list of the metaData categories</td>   </tr>
    <tr>    <td>/rest/meta/category/$ {id}</td>            <td class="text-center">GET</td>
        <td>Shows the metaData category with the given id</td>   </tr>
    <tr>    <td>/rest/meta/category/$ {id}/edit</td>       <td class="text-center">GET</td>
        <td>Allows editing of the metaData category with the given id</td>   </tr>
    <tr>    <td>/rest/meta/category/$ {id}</td>            <td class="text-center">PUT</td>
        <td>Updates the metaData category with the given id</td>   </tr>
    <tr>    <td>/rest/meta/category/$ {id}</td>            <td class="text-center">DELETE</td>
        <td>Deletes the metaData category with the given id</td>   </tr>

    </tbody>
</table>

<h3 id="Controller: MetaDataValue">Controller: MetaDataValue</h3>
<table class="table table-hover" style="table-layout: fixed;">
    <thead>
    <tr>
        <th class="text-center" style="width: 110px">URI</th>
        <th class="text-center" style="width: 110px">Method</th>
        <th class="text-center" style="width: 110px">Details</th>

    </tr>
    </thead>
    <tbody>
    <tr>    <td>/rest/meta/data/$ {MetaDataId}/value</td>                                            <td class="text-center">GET</td>
        <td>Displays the values for the metaData with the given id</td>   </tr>
    <tr>    <td>/rest/meta/data/$ {MetaDataId}/value/create</td>                                     <td class="text-center">GET</td>
        <td>Creates a value for the metaData with the given id</td>   </tr>
    <tr>    <td>/rest/meta/data/$ {MetaDataId}/value</td>                                            <td class="text-center">POST</td>
        <td>Saves the values of the metaData with the given id</td>   </tr>
    <tr>    <td>/rest/meta/data/$ {MetaDataId}/value/$ {id}</td>                                      <td class="text-center">GET</td>
        <td>Shows the value with the given id and given metaData (via its id)</td>   </tr>
    <tr>    <td>/rest/meta/data/$ {MetaDataId}/value/$ {id}/edit</td>                                 <td class="text-center">GET</td>
        <td>Allows editing of the value with the given id and given metaData (via its id)</td>   </tr>
    <tr>    <td>/rest/meta/data/$ {MetaDataId}/value/$ {id}</td>                                      <td class="text-center">PUT</td>
        <td>Updates the value with the given id and given metaData (via its id)</td>   </tr>
    <tr>    <td>/rest/meta/data/$ {MetaDataId}/value/$ {id}</td>                                      <td class="text-center">DELETE</td>
        <td>Deletes the value with the given id and given metaData (via its id)</td>   </tr>
    <tr>    <td>/rest/meta/category/$ {MetaDataCategoryId}/data/$ {MetaDataId}/value</td>             <td class="text-center">GET</td>
        <td>Shows the value of the data with the given metaData id and metaDataCategory id</td>   </tr>
    <tr>    <td>/rest/meta/category/$ {MetaDataCategoryId}/data/$ {MetaDataId}/value/create</td>      <td class="text-center">GET</td>
        <td>Creates a value of the data with the given metaData id and metaDataCategory id</td>   </tr>
    <tr>    <td>/rest/meta/category/$ {MetaDataCategoryId}/data/$ {MetaDataId}/value</td>             <td class="text-center">POST</td>
        <td>Saves the value of the data with the given metaData id and metaDataCategory id</td>   </tr>
    <tr>    <td>/rest/meta/category/$ {MetaDataCategoryId}/data/$ {MetaDataId}/value/$ {id}</td>       <td class="text-center">GET</td>
        <td>Shows the value with the id of the data with the given metaData id and metaDataCategory id</td>   </tr>
    <tr>    <td>/rest/meta/category/$ {MetaDataCategoryId}/data/$ {MetaDataId}/value/$ {id}/edit</td>  <td class="text-center">GET</td>
        <td>Allows editing of the value with the id of the data with the given metaData id and metaDataCategory id</td>   </tr>
    <tr>    <td>/rest/meta/category/$ {MetaDataCategoryId}/data/$ {MetaDataId}/value/$ {id}</td>       <td class="text-center">PUT</td>
        <td>Updates the value with the id of the data with the given metaData id and metaDataCategory id</td>   </tr>
    <tr>    <td>/rest/meta/category/$ {MetaDataCategoryId}/data/$ {MetaDataId}/value/$ {id}</td>       <td class="text-center">DELETE</td>
        <td>Deletes the value with the id of the data with the given metaData id and metaDataCategory id</td>   </tr>

    </tbody>
</table>

<h3 id="Controller: Quartz">Controller: Quartz</h3>
<table class="table table-hover" style="table-layout: fixed;">
    <thead>
    <tr>
        <th class="text-center" style="width: 110px">URI</th>
        <th class="text-center" style="width: 110px">Method</th>
        <th class="text-center" style="width: 110px">Details</th>

    </tr>
    </thead>
    <tbody>
    <tr>    <td>/info/jobs</td>        <td class="text-center">*</td>
        <td>Lists the information on the current jobs</td>   </tr>

    </tbody>
</table>

<h3 id="Controller: Spectrum">Controller: Spectrum</h3>
<table class="table table-hover" style="table-layout: fixed;">
    <thead>
    <tr>
        <th class="text-center" style="width: 110px">URI</th>
        <th class="text-center" style="width: 110px">Method</th>
        <th class="text-center" style="width: 110px">Details</th>

    </tr>
    </thead>
    <tbody>
    <tr>    <td>/rest/submitters/$ {SubmitterId}/spectra</td>                                            <td class="text-center">GET</td>
        <td>Displays the spectra submitted by the submitter with the given id</td>   </tr>
    <tr>    <td>/rest/submitters/$ {SubmitterId}/spectra/create</td>                                     <td class="text-center">GET</td>
        <td>Creates a spectra with the submitter id given</td>   </tr>
    <tr>    <td>/rest/submitters/$ {SubmitterId}/spectra</td>                                            <td class="text-center">POST</td>
        <td>Saves a list of the spectra submitted by the submitter with the given id</td>   </tr>
    <tr>    <td>/rest/submitters/$ {SubmitterId}/spectra/$ {id}</td>                                      <td class="text-center">GET</td>
        <td>Shows the spectra with the given id submitted by the submitter with the given id</td>   </tr>
    <tr>    <td>/rest/submitters/$ {SubmitterId}/spectra/$ {id}/edit</td>                                 <td class="text-center">GET</td>
        <td>Allows editing of the spectra with the given id submitted by the submitter with the given id</td>   </tr>
    <tr>    <td>/rest/submitters/$ {SubmitterId}/spectra/$ {id}</td>                                      <td class="text-center">PUT</td>
        <td>Updates the spectra with the given id submitted by the submitter with the given id</td>   </tr>
    <tr>    <td>/rest/submitters/$ {SubmitterId}/spectra/$ {id}</td>                                      <td class="text-center">DELETE</td>
        <td>Deletes the spectra with the given id submitted by the submitter with the given id</td>   </tr>
    <tr>    <td>/rest/tags/$ {TagId}/spectra</td>                                                        <td class="text-center">GET</td>
        <td>Displays the spectra with the given tag id</td>   </tr>
    <tr>    <td>/rest/tags/$ {TagId}/spectra/create</td>                                                 <td class="text-center">GET</td>
        <td>Creates a spectra with the given tag id</td>   </tr>
    <tr>    <td>/rest/tags/$ {TagId}/spectra</td>                                                        <td class="text-center">POST</td>
        <td>Saves the spectra with the given tag id</td>   </tr>
    <tr>    <td>/rest/tags/$ {TagId}/spectra/$ {id}</td>                                                  <td class="text-center">GET</td>
        <td>Shows the spectra with the given id and given tag id</td>   </tr>
    <tr>    <td>/rest/tags/$ {TagId}/spectra/$ {id}/edit</td>                                             <td class="text-center">GET</td>
        <td>Allows editing of the spectra with the given id and given tag id</td>   </tr>
    <tr>    <td>/rest/tags/$ {TagId}/spectra/$ {id}</td>                                                  <td class="text-center">PUT</td>
        <td>Updates the spectra with the given id and given tag id</td>   </tr>
    <tr>    <td>/rest/tags/$ {TagId}/spectra/$ {id}</td>                                                  <td class="text-center">DELETE</td>
        <td>Deletes the spectra with teh given id and given tag id</td>   </tr>
    <tr>    <td>/rest/compounds/$ {CompoundId}/spectra</td>                                              <td class="text-center">GET</td>
        <td>Displays the spectra with the given compound id</td>   </tr>
    <tr>    <td>/rest/compounds/$ {CompoundId}/spectra/create</td>                                       <td class="text-center">GET</td>
        <td>Creates a spectra with the given compound id</td>   </tr>
    <tr>    <td>/rest/compounds/$ {CompoundId}/spectra</td>                                              <td class="text-center">POST</td>
        <td>Saves the spectra with the given compound id</td>   </tr>
    <tr>    <td>/rest/compounds/$ {CompoundId}/spectra/$ {id}</td>                                        <td class="text-center">GET</td>
        <td>Shows the spectra with the given id and given compound id</td>   </tr>
    <tr>    <td>/rest/compounds/$ {CompoundId}/spectra/$ {id}/edit</td>                                   <td class="text-center">GET</td>
        <td>Allows editing of the spectra with the given id and given compound id</td>   </tr>
    <tr>    <td>/rest/compounds/$ {CompoundId}/spectra/$ {id}</td>                                        <td class="text-center">PUT</td>
        <td>Updates the spectra with the given id and given compound id</td>   </tr>
    <tr>    <td>/rest/compounds/$ {CompoundId}/spectra/$ {id}</td>                                        <td class="text-center">DELETE</td>
        <td>Deletes the spectra with the given id and given compound id</td>   </tr>
    <tr>    <td>/rest/spectra</td>                                                                      <td class="text-center">GET</td>
        <td>Displays the spectra</td>   </tr>
    <tr>    <td>/rest/spectra/create</td>                                                               <td class="text-center">GET</td>
        <td>Creates a spectra</td>   </tr>
    <tr>    <td>/rest/spectra</td>                                                                      <td class="text-center">POST</td>
        <td>Saves a list of the spectra</td>   </tr>
    <tr>    <td>/rest/spectra/$ {id}</td>                                                                <td class="text-center">GET</td>
        <td>Shows the spectra with the given id</td>   </tr>
    <tr>    <td>/rest/spectra/$ {id}/edit</td>                                                           <td class="text-center">GET</td>
        <td>Allows editing of the spectra with the given id</td>   </tr>
    <tr>    <td>/rest/spectra/$ {id}</td>                                                                <td class="text-center">PUT</td>
        <td>Updates the spectra with the given id</td>   </tr>
    <tr>    <td>/rest/spectra/$ {id}</td>                                                                <td class="text-center">DELETE</td>
        <td>Deletes the spectra with the given id</td>   </tr>
    <tr>    <td>/rest/meta/category/$ {MetaDataCategoryId}/data/$ {MetaDataId}/spectra</td>               <td class="text-center">GET</td>
        <td>Displays the spectra with the given metaData id and given metaDataCategory id</td>   </tr>
    <tr>    <td>/rest/meta/category/$ {MetaDataCategoryId}/data/$ {MetaDataId}/spectra/create</td>        <td class="text-center">GET</td>
        <td>Creates a spectra with the given metaData id and given metaDataCategory id</td>   </tr>
    <tr>    <td>/rest/meta/category/$ {MetaDataCategoryId}/data/$ {MetaDataId}/spectra</td>               <td class="text-center">POST</td>
        <td>Saves the spectra with the given metaData id and given metaDataCategory id</td>   </tr>
    <tr>    <td>/rest/meta/category/$ {MetaDataCategoryId}/data/$ {MetaDataId}/spectra/$ {id}</td>         <td class="text-center">GET</td>
        <td>Shows the spectra with the given id and given metaData id and given metaDataCategory id</td>   </tr>
    <tr>    <td>/rest/meta/category/$ {MetaDataCategoryId}/data/$ {MetaDataId}/spectra/$ {id}/edit</td>    <td class="text-center">GET</td>
        <td>Allows editing of the spectra with the given id and given metaData id and given metaDataCategory id</td>   </tr>
    <tr>    <td>/rest/meta/category/$ {MetaDataCategoryId}/data/$ {MetaDataId}/spectra/$ {id}</td>         <td class="text-center">PUT</td>
        <td>Updates the spectra with the given id and given metaData id and given metaDataCategory id</td>   </tr>
    <tr>    <td>/rest/meta/category/$ {MetaDataCategoryId}/data/$ {MetaDataId}/spectra/$ {id}</td>         <td class="text-center">DELETE</td>
        <td>Deletes the spectra with the given id and given metaData id and given metaDataCategory id</td>   </tr>

    </tbody>
</table>

<h3 id="Controller: Submitter">Controller: Submitter</h3>
<table class="table table-hover" style="table-layout: fixed;">
    <thead>
    <tr>
        <th class="text-center" style="width: 110px">URI</th>
        <th class="text-center" style="width: 110px">Method</th>
        <th class="text-center" style="width: 110px">Details</th>

    </tr>
    </thead>
    <tbody>
    <tr>    <td>/rest/submitters</td>               <td class="text-center">GET</td>
        <td>Displays a list of the submitters</td>   </tr>
    <tr>    <td>/rest/submitters/create</td>        <td class="text-center">GET</td>
        <td>Creates a submitter</td>   </tr>
    <tr>    <td>/rest/submitters</td>               <td class="text-center">POST</td>
        <td>Saves the submitters</td>   </tr>
    <tr>    <td>/rest/submitters/$ {id}</td>         <td class="text-center">GET</td>
        <td>Shows the submitter with the given id</td>   </tr>
    <tr>    <td>/rest/submitters/$ {id}/edit</td>    <td class="text-center">GET</td>
        <td>Allows editing of the submitter with the given id</td>   </tr>
    <tr>    <td>/rest/submitters/$ {id}</td>         <td class="text-center">PUT</td>
        <td>Updates the submitter with the given id</td>   </tr>
    <tr>    <td>/rest/submitters/$ {id}</td>         <td class="text-center">DELETE</td>
        <td>Deletes the submitter with the given id</td>   </tr>

    </tbody>
</table>

<h3 id="Controller: Tag">Controller: Tag</h3>
<table class="table table-hover" style="table-layout: fixed;">
    <thead>
    <tr>
        <th class="text-center" style="width: 110px">URI</th>
        <th class="text-center" style="width: 110px">Method</th>
        <th class="text-center" style="width: 110px">Details</th>

    </tr>
    </thead>
    <tbody>
    <tr>    <td>/rest/tags</td>                     <td class="text-center">GET</td>
        <td>Displays a list of the tags</td>   </tr>
    <tr>    <td>/rest/tags/create</td>              <td class="text-center">GET</td>
        <td>Creates a tag</td>   </tr>
    <tr>    <td>/rest/tags</td>                     <td class="text-center">POST</td>
        <td>Saves the tags</td>   </tr>
    <tr>    <td>/rest/tags/$ {id}</td>               <td class="text-center">GET</td>
        <td>Shows the tag with the given id</td>   </tr>
    <tr>    <td>/rest/tags/$ {id}/edit</td>          <td class="text-center">GET</td>
        <td>Allows editing of the tag with the given id</td>   </tr>
    <tr>    <td>/rest/tags/$ {id}</td>               <td class="text-center">PUT</td>
        <td>Updates the tag with the given id</td>   </tr>
    <tr>    <td>/rest/tags/$ {id}</td>               <td class="text-center">DELETE</td>
        <td>Deletes the tag with the given id</td>   </tr>

    </tbody>
</table>

<h3 id="Controller: metaDataQuery">Controller: metaDataQuery</h3>
<table class="table table-hover" style="table-layout: fixed;">
    <thead>
    <tr>
        <th class="text-center" style="width: 110px">URI</th>
        <th class="text-center" style="width: 110px">Method</th>
        <th class="text-center" style="width: 110px">Details</th>

    </tr>
    </thead>
    <tbody>
    <tr>    <td>/rest/meta/data/search</td>        <td class="text-center">*</td>
        <td>Queries the metaData</td>   </tr>

    </tbody>
</table>

<h3 id="Controller: spectraQuery">Controller: spectraQuery</h3>
<table class="table table-hover" style="table-layout: fixed;">
    <thead>
    <tr>
        <th class="text-center" style="width: 110px">URI</th>
        <th class="text-center" style="width: 110px">Method</th>
        <th class="text-center" style="width: 110px">Details</th>

    </tr>
    </thead>
    <tbody>
    <tr>    <td>/rest/spectra/search</td>               <td class="text-center">*</td>
        <td>Searches the spectra.</td>   </tr>
    <tr>    <td>/rest/spectra/batch/update</td>         <td class="text-center">*</td>
        <td>Searches and updates the spectra batch</td>   </tr>

    </tbody>
</table>

<h3 id="Controller: spectrum">Controller: spectrum</h3>
<table class="table table-hover" style="table-layout: fixed;">
    <thead>
    <tr>
        <th class="text-center" style="width: 110px">URI</th>
        <th class="text-center" style="width: 110px">Method</th>
        <th class="text-center" style="width: 110px">Details</th>

    </tr>
    </thead>
    <tbody>
    <tr>    <td>/rest/spectra/batch/save</td>       <td class="text-center">*</td>
        <td>Batches and saves the spectra</td>   </tr>

    </tbody>
</table>
</div> <!--main-->

<div class="col-sm-2">
    <div>
        <h4>Links</h4>
        <ol class="list-unstyled">
            <li><a href="#information">Information</a> </li>
            <li><a href="#Dynamic Mappings">Dynamic Mappings</a> </li>
            <li><a href="#Controller: Compound">Controller: Compound</a> </li>
            <li><a href="#Controller: MetaData">Controller: MetaData</a> </li>
            <li><a href="#Controller: MetaDataCategory">Controller: MetaDataCategory</a> </li>
            <li><a href="#Controller: MetaDataValue">Controller: MetaDataValue</a> </li>
            <li><a href="#Controller: Quartz">Controller: Quartz</a> </li>
            <li><a href="#Controller: Spectrum">Controller: Spectrum</a> </li>
            <li><a href="#Controller: Submitter">Controller: Submitter</a> </li>
            <li><a href="#Controller: Tag">Controller: Tag</a> </li>
            <li><a href="#Controller: metaDataQuery">Controller: metaDataQuery</a> </li>
            <li><a href="#Controller: spectraQuery">Controller: spectraQuery</a> </li>
            <li><a href="#Controller: spectrum">Controller: spectrum</a> </li>
        </ol>
    </div>
</div><!-- links -->

</div> <!-- row-->

</div>
</div>