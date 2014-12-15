(ns erv-msg.core.recipient
  (:require 
    [clojure.java.io :as io]
    [erv-msg.core.dao :as dao]
    [clojure.string :as str]))

(defn msg [recipient] "read file"
  (let [recipient (dao/get-msg recipient)
        msg (get recipient "msg")
        lines (str/split msg #"\n")
        cleaned (map #(str/trim %) lines)] 
      cleaned))

