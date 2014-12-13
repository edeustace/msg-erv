(ns erv-msg.core.handler
  (:require 
            [erv-msg.core.templates :as tmpl]
            [erv-msg.core.vtt :as vtt]
            [erv-msg.core.recipient :as rec]
            [compojure.core :refer :all]
            [clojure.string :as str]
            [compojure.route :as route]
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
    (vtt/vtt r))

  (route/resources "/")

  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))