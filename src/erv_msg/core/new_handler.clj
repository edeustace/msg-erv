(ns erv-msg.core.new-handler
  (:require [compojure.core :refer :all]
            [ring.middleware.json :as middleware]
            [compojure.handler :as handler]
            [erv-msg.core.dao :as dao]
            [erv-msg.core.views :as views]
            [compojure.route :as route]
            [confil.core :as confil]
            [monger.core :as mg]
            [monger.cursor :as mc]
            )

  (:import
    [com.mongodb MongoOptions ServerAddress]
    ))

(def app
  (let [ uri (get (System/getenv) "MONGOLAB_URL" "mongodb://localhost/erv-msg") 
         {:keys [conn db]} (mg/connect-via-uri uri)]

    (defroutes app-routes
      (GET "/" [] {:body {:message "Hello"}})

      (PUT "/editor/message/:recipient" request 
        (let [json (:json-params request)
                  rp (:route-params request)
                  recipient (:recipient rp)
                  ]
        (prn "json -> " json)
        (dao/update-msg db recipient json)
        {:body "OK!"}
        ))

      (GET "/editor/messages" [] "load all messages"
        (dao/list-msgs db))

      (GET "/editor/messages.html" []
        (let 
          [ l (into [] (dao/list-msgs db)) ]
          (views/messages l))
        )

      (GET "/editor/messages/edit/:recipient.html" [recipient]
        (let 
          [ r (dao/get-msg db recipient)]
          (prn "----->" recipient)
          (views/edit-message r)))

      (route/resources "/")
      (route/not-found "Not Found"))

    ( ->
         (handler/api app-routes)
         (middleware/wrap-json-body)
         (middleware/wrap-json-params)
         (middleware/wrap-json-response)
         )))