FROM 1science/sbt:0.13.8-oracle-jre-8

# Install applications
RUN apk update
RUN apk upgrade
RUN apk add curl bash git openssh

# Download definiti-0.0.0
RUN mkdir /opt
RUN cd /opt && git clone https://github.com/definiti/definiti.git
RUN cd /opt/definiti && sbt stage
RUN ln -s /opt/definiti/target/universal/stage/bin/definiti /bin/definiti

# Download dependencies to include them into docker image directly
RUN cd /opt/definiti/dependencies && sbt update