(ns erv-msg.core.handler
  (:require monger.json)
  (:require 
            [erv-msg.core.templates :as tmpl]
            [erv-msg.core.vtt :as vtt]
            [erv-msg.core.dao :as dao]
            [erv-msg.core.byte-range :as br]
            [erv-msg.core.recipient :as recipient]
            [monger.core :as mg]
            [compojure.core :refer :all]
            [clojure.string :as str]
            [compojure.route :as route]
            [ring.util.response :as r]
            [ring.middleware.json :as middleware]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.not-modified :refer [wrap-not-modified]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(defn- vtt-path [r] (str/join "" ["/vtt/" r ".vtt"]))

(def db-info 
  (let [
        uri (get (System/getenv) "MONGOLAB_URL" "mongodb://localhost/erv-msg")
        ;uri (System/genenv "MONGOLAB_URL" "mongodb://localhost/erv-msg")
       {:keys [conn db]} (mg/connect-via-uri uri)]
       (prn db)
       (prn conn)
       db
       ))

(defroutes app-routes

  ;editing
  (GET "/editor/messages" [] "load all messages" {:body (dao/list-msgs db) }))
  ;(POST "/editor/message" [] "create new message ")
  ;(PUT "/editor/message/:recipient" [recipient] (str/join "update msg for" recipient))
  ;(DELETE "/editor/message/:recipient" [recipient] (str/join "delete msg for" recipient))
  ;; see: https://github.com/ring-clojure/ring/blob/master/ring-core/src/ring/util/response.clj#L103

  (GET "/:recipient" [recipient] 
    (tmpl/index recipient (vtt-path recipient)))
  
  (GET "/vtt/:recipient.vtt" [recipient]
    (def r (recipient/msg recipient))
    (-> 
      (r/response (->> (vtt/vtt r)))
      (r/header "Content-Type" "text/vtt; charset=utf-8")))

  (route/resources "/")

  (route/not-found "Not Found"))

(def app
  ;wrap-content-type wrap-not-modified
  (wrap-defaults  app-routes site-defaults)
  )