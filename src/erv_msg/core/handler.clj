(ns erv-msg.core.handler
  (:require 
            [erv-msg.core.templates :as tmpl]
            [erv-msg.core.vtt :as vtt]
            [erv-msg.core.recipient :as rec]
            [compojure.core :refer :all]
            [clojure.string :as str]
            [compojure.route :as route]
            [ring.util.response :as r]
            [ring.middleware.content-type :refer [wrap-content-type]
            [ring.middleware.not-modified :refer [wrap-not-modified]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]))

(defn- vtt-path [r] (str/join "" ["/vtt/" r ".vtt"]))
(defroutes app-routes

  ;editing
  ;(GET "/editor/messages" [] "load all messages")
  ;(POST "/editor/message" [] "create new message ")
  ;(PUT "/editor/message/:recipient" [recipient] (str/join "update msg for" recipient))
  ;(DELETE "/editor/message/:recipient" [recipient] (str/join "delete msg for" recipient))

  (GET "/:recipient" [recipient] 
    (tmpl/index recipient (vtt-path recipient)))
  
  (GET "/vtt/:recipient.vtt" [recipient]
    (def r (rec/msg recipient))

    (-> 
      (r/response (->> (vtt/vtt r)))
      (r/header "Content-Type" "text/vtt; charset=utf-8")))

  (route/resources "/")

  (route/not-found "Not Found"))

(def app
  (wrap-defaults wrap-content-type wrap-not-modified app-routes site-defaults))