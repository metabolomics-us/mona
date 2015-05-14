#!/bin/bash

###
# Mona client part
###
service nginx start

####
# JETTY PART
###

cd /opt/jetty

java -Xmx8096m -jar start.jar "jetty.home=/opt/jetty"
