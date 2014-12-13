(ns erv-msg.core.recipient
  (:require 
    [clojure.java.io :as io]
    [clojure.string :as str]))

(defn msg [recipient] "read file"
  (let 
    [p (str recipient ".txt")
     txt (slurp (io/resource p))
     s (str/split txt #"\n")]
     s))

