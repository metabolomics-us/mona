#!/bin/bash

###
# Mona client part
###
service nginx start

####
# JETTY PART
###

cd /opt/jetty

java -XX:MaxPermSize=1024m -server -Xmx16164m -jar start.jar "jetty.home=/opt/jetty"

#java -XX:-UseGCOverheadLimit -XX:MaxPermSize=1024m -server -Xmx16164m -jar start.jar "jetty.home=/opt/jetty"
