FROM ubuntu
MAINTAINER Gert Wohlgemuth "wohlgemuth@ucdavis.edu"
#nginx - runs on port 9090
EXPOSE 9090

#jetty - runs on port 8080
EXPOSE 8080

#squid - runs on port 80
EXPOSE 80

#update apt-get
RUN apt-get update -y

# Install Java
RUN apt-get -qq update
RUN apt-get -qq upgrade
RUN apt-get -qq install openjdk-7-jdk
RUN apt-get -qq install bash

#install unzip
RUN apt-get -y install unzip

# Install Jetty
ADD http://eclipse.org/downloads/download.php?file=/jetty/stable-8/dist/jetty-distribution-8.1.16.v20140903.tar.gz&r=1 /opt/jetty.tar.gz
RUN tar -xvf /opt/jetty.tar.gz -C /opt/
RUN rm /opt/jetty.tar.gz
RUN mv /opt/jetty-distribution-* /opt/jetty
RUN rm -rf /opt/jetty/webapps.demo
RUN useradd jetty -U -s /bin/false
RUN chown -R jetty:jetty /opt/jetty

#install nginx
RUN apt-get install -y nginx
RUN rm /etc/nginx/sites-enabled/default

#add and configure the mona client application
RUN mkdir /opt/mona
ADD mona-client.zip /opt/mona/mona.zip
RUN unzip /opt/mona/mona.zip -d /opt/mona

ADD mona-client.conf /etc/nginx/sites-enabled/mona-client.conf


#add our custom nginx files

# install vim
RUN apt-get install -y vim
# Install and configure squid
RUN apt-get install -y squid
ADD squid.conf /etc/squid3/squid.conf

#create the caching directories
squid3 -z

# Prepare dirs required by squid
RUN mkdir -p /var/spool/squid3
RUN mkdir -p /var/log/squid3

# Make cache dirs 
RUN squid3 -z -F

# Install awstats

RUN apt-get install awstats -y
RUN apt-get install -y libnet-ip-perl
RUN apt-get install -y libgeo-ipfree-perl

# Install Mona Server Apps
ADD root.war /opt/jetty/webapps/root.war

#adding and configuring our start file itself
ADD start.sh /opt/start.sh
RUN chmod +x /opt/start.sh



###
#useful utilities for testing and so, should not be on prodcution system
#
RUN apt-get install -y lynx mc vim

# Run Jetty
CMD ["/opt/start.sh"]
