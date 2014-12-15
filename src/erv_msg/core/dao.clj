(ns erv-msg.core.dao
  (:require
    [monger.json]
    [monger.core :as mg]
    [monger.collection :as mc]
    [clojure.java.io :as io]
    [clojure.string :as str]))

(defn list-msgs [db] "List list-messages" (mc/find-maps db "messages"))

(defn get-msg [db recipient] "get-message"
  (mc/find-one db "messages" { :recipient recipient }))

(defn update-msg [db recipient data] "update-message"
  (def merged (merge {:recipient recipient} data))
  (prn "data --> " data)
  (mc/update db "messages" {:recipient recipient} merged {:upsert true}))

(defn delete-msg [db recipient] "update-message"
  (mc/remove db "messages" { :recipient recipient }))
