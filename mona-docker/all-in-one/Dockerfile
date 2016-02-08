FROM ubuntu:trusty
MAINTAINER Sajjan Singh Mehta "ssmehta@ucdavis.edu"



# Repository for PostgreSQL 9.5
RUN apt-key adv --keyserver keyserver.ubuntu.com --recv-keys B97B0AFCAA1A47F044F244A07FCC7D46ACCC4CF8
RUN echo "deb http://apt.postgresql.org/pub/repos/apt/ trusty-pgdg main" > /etc/apt/sources.list.d/pgdg.list

# Repository for RabbitMQ
RUN apt-key adv --keyserver keyserver.ubuntu.com --recv-keys F78372A06FF50C80464FC1B4F7B8CEA6056E8E56
RUN echo 'deb http://www.rabbitmq.com/debian testing main' > /etc/apt/sources.list.d/rabbitmq.list

# Install build tools, MoNA dependencies, RabbitMQ and PostgreSQL
RUN apt-get update && \
    apt-get -y upgrade && \
    apt-get install -y build-essential python-software-properties software-properties-common && \
    apt-get install -y openjdk-7-jdk awstats libnet-ip-perl libgeo-ipfree-perl nginx && \
    apt-get install -y byobu bash curl git htop man unzip vim wget && \
    apt-get install -y rabbitmq-server pwgen && \
    apt-get install -y -q postgresql-9.5 postgresql-client-9.5 postgresql-contrib-9.5 && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*



# Configure RabbitMQ
ENV RABBITMQ_LOG_BASE /data/rabbitmq/log
ENV RABBITMQ_MNESIA_BASE /data/rabbitmq/mnesia

RUN rabbitmq-plugins enable rabbitmq_management
RUN echo "ERLANGCOOKIE" > /var/lib/rabbitmq/.erlang.cookie
RUN chown rabbitmq:rabbitmq /var/lib/rabbitmq/.erlang.cookie
RUN chmod 400 /var/lib/rabbitmq/.erlang.cookie

ADD includes/rabbitmq/rabbitmq.config /etc/rabbitmq/rabbitmq.config
RUN chown -R rabbitmq:rabbitmq /var/lib/rabbitmq



# Configure PostgreSQL
USER postgres

RUN /etc/init.d/postgresql start && \
    psql --command "CREATE USER mona WITH SUPERUSER PASSWORD 'mona';" && \
    createdb -O mona mona && \
    /etc/init.d/postgresql stop

USER root

ADD includes/postgresql/pg_hba.conf /etc/postgresql/9.5/main/pg_hba.conf
ADD includes/postgresql/postgresql.conf /etc/postgresql/9.5/main/postgresql.conf

RUN mkdir -p /var/run/postgresql
RUN chown -R postgres /var/run/postgresql
RUN chown -R postgres:postgres /var/lib/postgresql
RUN chmod 700 /var/lib/postgresql



# Configuire Jetty
ADD http://eclipse.org/downloads/download.php?file=/jetty/8.1.17.v20150415/dist/jetty-distribution-8.1.17.v20150415.tar.gz&r=1 /opt/jetty.tar.gz
RUN tar -xvf /opt/jetty.tar.gz -C /opt/
RUN rm /opt/jetty.tar.gz
RUN mv /opt/jetty-distribution-* /opt/jetty
RUN rm -rf /opt/jetty/webapps.demo
RUN useradd jetty -U -s /bin/false
RUN chown -R jetty:jetty /opt/jetty



# Configuire nginx
RUN rm /etc/nginx/sites-enabled/default

ADD includes/nginx/mona-client.conf /etc/nginx/sites-enabled/mona-client.conf
ADD includes/nginx/awtstats.conf /etc/awstats/awstats.client.conf

ADD includes/nginx/logrotate /etc/logrotate.d/nginx
RUN chmod 644 /etc/logrotate.d/nginx



# Configure MoNA
ADD includes/mona/root.war /opt/jetty/webapps/root.war

RUN mkdir /opt/mona
ADD includes/mona/client.zip /opt/mona/mona.zip
RUN unzip -q /opt/mona/mona.zip -d /opt/mona



# Configure environment
ENV HOME /root
WORKDIR /root
ADD includes/.bashrc /root/.bashrc

ADD includes/start.sh /root/start.sh
RUN chmod +x /root/start.sh



# Expose RabbitMQ ports
EXPOSE 5672 15672

# Expose PostgreSQL ports
EXPOSE 5432

# Expose MoNA ports
EXPOSE 80



CMD ["bash"]