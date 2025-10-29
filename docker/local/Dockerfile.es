FROM docker.elastic.co/elasticsearch/elasticsearch:7.17.13
RUN elasticsearch-plugin install analysis-nori