FROM clojure:lein-2.8.1-alpine AS build
ENV DEBUG_COMPILE ${DEBUG_COMPILE}
ENV NO_PERF_COMPILE ${NO_PERF_COMPILE}
ENV SHOW_RULES ${SHOW_RULES}
WORKDIR /app
ADD . /app
RUN lein clean && \
    lein uberjar

FROM openjdk:jre-alpine
COPY --from=build /app/target/arete-0.6.1-standalone.jar /
CMD ["java", "-cp", "/arete-0.6.1-standalone.jar", "engine.api", "id-function=(fn [x] [(:namespace x) (:kind x) (:name x)])"]
