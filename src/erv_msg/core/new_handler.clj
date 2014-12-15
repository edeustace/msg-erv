(ns erv-msg.core.new-handler
  (:require [compojure.core :refer :all]
            [ring.middleware.json :as middleware]
            [compojure.handler :as handler]
            [erv-msg.core.dao :as dao]
            [compojure.route :as route]
            [confil.core :as confil]
            [monger.core :as mg])

  (:import
    [com.mongodb MongoOptions ServerAddress]
    ))

(def app
  (let [ uri (get (System/getenv) "MONGOLAB_URL" "mongodb://localhost/erv-msg") 
         {:keys [conn db]} (mg/connect-via-uri uri)]

    (defroutes app-routes
      (GET "/" [] {:body {:message "Hello"}})
      (GET "/editor/messages" [] "load all messages"
        (dao/list-msgs db))
      (route/resources "/")
      (route/not-found "Not Found"))

    ( ->
         (handler/api app-routes)
         (middleware/wrap-json-body)
         (middleware/wrap-json-params)
         (middleware/wrap-json-response)
         )))