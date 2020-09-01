(ns engine.api
  (:gen-class)
  (:require [liberator.core :refer [resource defresource]]
            [liberator.representation :refer [ring-response]]
            [liberator.dev :refer [wrap-trace]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.adapter.jetty :as jetty]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [compojure.core :refer [routes GET POST PUT DELETE]]
            [clojure.java.io :as io]
            [clojure.walk :as walk]
            [clojure.data.json :as json]
            [clojure.tools.logging :as log]
            [engine.core :as eng]))

(def ^:dynamic *engine* nil)

(defn body-as-string
  [ctx]
  (if-let [body (get-in ctx [:request :body])]
    (condp instance? body
      java.lang.String body
      (slurp (io/reader body)))))

(defn protect-qualified-keywords [exp]
  (walk/postwalk #(if (and (keyword? %)
                           (not= (inc (count (name %)))
                                 (count (str %))))
                    (subs (str %) 1)
                    %)
                 exp))

(defn body-as-data [ctx]
  (protect-qualified-keywords
   (json/read-str (body-as-string ctx) :key-fn keyword)))

(defn do-get-wmes [_]
  (*engine* :wmes))

(defn do-get-wme-list [_]
  (*engine* :wme-list))

(defn post-wmes [ctx]
  (let [input (body-as-data ctx)]
    (*engine* :add-wmes (map #(update % :type keyword) input))
    {::retval "ok"}))

(defn put-wmes [ctx]
  (let [input (body-as-data ctx)]
    (*engine* :update-wmes (map #(update % :type keyword) input))
    {::retval "ok"}))

(defn delete-wmes [ctx]
  (let [input (body-as-data ctx)]
    (*engine* :remove-wmes (map #(update % :type keyword) input))
    {::retval "ok"}))

(defn batch-wmes [ctx]
  (let [input (body-as-data ctx)]
    (*engine* :batch-wme-ops (reduce-kv
                              (fn [m k v]
                                (assoc m k (map #(update % :type keyword) v)))
                              {}
                              input))
    {::retval "ok"}))

(defresource get-wmes
  :allowed-methods [:get]
  :available-media-types ["application/json"]
  :handle-ok do-get-wmes)

(defresource get-wme-list
  :allowed-methods [:get]
  :available-media-types ["application/json"]
  :handle-ok do-get-wme-list)

(defresource add-new-wmes
  :allowed-methods [:post]
  :available-media-types ["application/json"]
  :post! post-wmes
  :handle-created (fn [_] {::retval "ok"}))

(defresource update-wmes
  :allowed-methods [:put]
  :available-media-types ["application/json"]
  :put! put-wmes
  :handle-ok (fn [_] {::retval "ok"}))

(defresource remove-wmes
  :allowed-methods [:delete]
  :available-media-types ["application/json"]
  :delete! delete-wmes)

(defresource batch-wme-ops
  :allowed-methods [:post]
  :available-media-types ["application/json"]
  :post! batch-wmes
  :handle-created (fn [_] {::retval "ok"}))

(def api-routes
  (routes
   (GET "/wmes" [] get-wmes)
   (GET "/wme-list" [] get-wme-list)
   (POST "/wmes" [] add-new-wmes)
   (PUT "/wmes" [] update-wmes)
   (DELETE "/wmes" [] remove-wmes)
   (POST "/wme-batch" [] batch-wme-ops)
   (route/not-found (format (json/write-str {:message "Not found!"})))))

(defn wrap-fallback-exception [engine]
  (fn [handler]
    (fn [request]
      (try
        (binding [*engine* engine] (handler request))
        (catch Exception e
          (log/error e "unhandled exception")
          ;; since I don't like to ever return 500 ;^)
          {:status 418 :body (.getMessage e)})))))

(defn handler [modules config-map]
  (let [engine (apply eng/spawn modules)]
    (engine :configure config-map)
    ((wrap-fallback-exception engine)
     (-> api-routes handler/api wrap-params))))

(defn start
  ([modules]
   (start modules {}))
  ([modules config-map]
   (println "Running jetty...")
   (jetty/run-jetty (handler modules config-map) {:port 3000 :join? false})))
