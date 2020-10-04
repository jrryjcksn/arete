(defproject arete "0.6.1"
  :description "Clojure rule engine"
  :url "https://github.com/yipee.io/arete.git"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main engine.viewer
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/tools.logging "0.4.0"]
                 [org.flatland/ordered "1.5.7"]
                 ;; https://mvnrepository.com/artifact/javax.xml.bind/jaxb-api
                 [javax.xml.bind/jaxb-api "2.3.0"]
                 ;; https://mvnrepository.com/artifact/org.glassfish.jaxb/jaxb-runtime
                 [org.glassfish.jaxb/jaxb-runtime "2.3.3"]
                 [javax.xml/jaxws-api "2.0EA3"]
                 [clj-yaml "0.4.0"]
                 [potemkin "0.4.5"]
                 [org.clojure/data.json "0.2.6"]
                 [org.javasimon/javasimon-core "4.1.3"]
                 [org.clojure/core.async "1.3.610"]
                 [liberator "0.15.1"]
                 [compojure "1.6.0"]
                 [ring/ring-jetty-adapter "1.6.2"]
                 [ring/ring-core "1.6.2"]
                 [com.rpl/specter "1.1.3"]]
  :jvm-opts ["-XX:+IgnoreUnrecognizedVMOptions" "--add-modules java.xml.bind"]
  :profiles {:uberjar {:aot :all}}
  :deploy-repositories [["releases"
                         {:sign-releases false :url "https://clojars.org/repo"}]
                        ["snapshots"
                         {:sign-releases false :url "https://clojars.org/repo"}]])

