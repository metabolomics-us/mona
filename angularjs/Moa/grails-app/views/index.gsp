<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Welcome to the MassSpec's to america database</title>
</head>

<body>
<div ng-app="massspecsOfAmerica">
    <!-- general navigation -->

    <div class="container">
        <header class="navbar navbar-default">
            <div class="navbar-inner">
                <div class="container">
                    <div class="navbar-header">
                        <a class="navbar-brand" href="#">MassSpecs of America</a>
                    </div>

                    <ul class="nav navbar-nav" ng-controller="NavigationController">
                        <li ng-class="navClass('home')"><a href='#/home'>Compounds</a></li>
                        <li ng-class="navClass('about')"><a href='#/submitters'>Submitters</a></li>
                        <li ng-class="navClass('contact')"><a href='#/upload/single'>Upload</a></li>
                    </ul>
                </div>
            </div>
        </header>
        ​
        <div class="header-placeholder"></div>

        <div role="main">
            <div ng-view=""></div>
        </div>
    </div>
</div>
</body>
</html>
