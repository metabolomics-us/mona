#!/bin/bash


###
# update the route
###

sed -i -e 's,http://mona.fiehnlab.ucdavis.edu,http://cloud-mona.apps.fiehnlab.ucdavis.edu,g' /opt/mona/scripts/*.scripts.js 

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
