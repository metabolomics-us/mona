FROM ubuntu
MAINTAINER Gert Wohlgemuth "wohlgemuth@ucdavis.edu"

#jetty - runs on port 8080
EXPOSE 8080

#nginx - runs on port 80
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

# Install awstats

RUN apt-get install awstats -y
RUN apt-get install -y libnet-ip-perl
RUN apt-get install -y libgeo-ipfree-perl

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

#configure nginx logrotate
ADD nginx/logrotate /etc/logrotate.d/nginx
RUN chmod 644 /etc/logrotate.d/nginx

#add and configure the mona client application
RUN mkdir /opt/mona
ADD client.zip /opt/mona/mona.zip
RUN unzip /opt/mona/mona.zip -d /opt/mona

ADD nginx/mona-client.conf /etc/nginx/sites-enabled/mona-client.conf
ADD nginx/awtstats.conf /etc/awstats/awstats.client.conf

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
