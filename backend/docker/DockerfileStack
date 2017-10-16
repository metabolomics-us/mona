FROM elasticsearch:1.7.5

ADD  conf/elastic/ /usr/share/elasticsearch/config

RUN /usr/share/elasticsearch/bin/plugin install royrusso/elasticsearch-HQ
RUN /usr/share/elasticsearch/bin/plugin install mobz/elasticsearch-head/1.x
RUN /usr/share/elasticsearch/bin/plugin install lmenezes/elasticsearch-kopf/v1.6.1
RUN /usr/share/elasticsearch/bin/plugin install jettro/elasticsearch-gui