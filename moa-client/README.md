AngularJS Best Practices
    If you are new to AngularJS or developing features for Mona-client app, the resources below will
    speed you up on implementation practices and AngularJS.

    1. Code School - AngularJS intro course sponsored by Google
    2. John Papa Style Guide - https://github.com/johnpapa/angular-styleguide
    3. Immediately Invoked Function Expression - http://benalman.com/news/2010/11/immediately-invoked-function-expression/#iife

**Implement ONE component per file to make code easy to maintain and read**

compiling source code:

1. npm -install
    install all grunt dependices to build the system using grunt

2. bower install
    install all the bower dependencies

3. grunt serve
    to start the client side application 

4. karma testing
    add desired test locations to karma.conf.js -> preprocessors: []
    open terminal and run 'karma start'
    code coverage can be viewed in /coverage
    npm install -g karma-cli  //if you have issues with karma start  
