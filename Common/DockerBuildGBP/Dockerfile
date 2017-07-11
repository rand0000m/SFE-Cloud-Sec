#FROM debian:jessie-slim
FROM debian:jessie

MAINTAINER Mathieu Rousse version:0.1
VOLUME /build

ADD ./install-dep.sh /tmp/install-dep.sh
ADD ./deploy_key /tmp/deploy_key
ADD ./deploy_key.pub /tmp/deploy_key.pub
RUN /bin/sh /tmp/install-dep.sh
ADD ./build.sh /tmp/build.sh

CMD ["/tmp/build.sh"]
