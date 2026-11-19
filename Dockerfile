# Pass It On on Render: Tomcat 11 + JDK 25, no build tool. The app is served at the site root.
FROM tomcat:11.0.29-jdk25-temurin

COPY src/main/webapp /usr/local/tomcat/webapps/ROOT
COPY src/main/java /tmp/java
RUN find /tmp/java -name '*.java' > /tmp/sources.txt \
 && javac --release 25 -encoding UTF-8 -cp "/usr/local/tomcat/lib/*" \
          -d /usr/local/tomcat/webapps/ROOT/WEB-INF/classes @/tmp/sources.txt \
 && rm -rf /tmp/java /tmp/sources.txt

# our DAOs close every connection, so Connector/J's cleanup thread is not needed
ENV CATALINA_OPTS="-Dcom.mysql.cj.disableAbandonedConnectionCleanup=true"
EXPOSE 8080
CMD ["catalina.sh", "run"]
