(ns erv-msg.core.dao
  (:require
    [monger.json]
    [monger.core :as mg]
    [monger.collection :as mc]
    [clojure.java.io :as io]
    [clojure.string :as str]))


(defn list-msgs [db] "List list-messages" (mc/find-maps db "messages"))
(defn get-msg [db recipient] "get-message")
(defn update-msg [db recipient] "update-message")
(defn delete-msg [db recipient] "update-message")
