(ns erv-msg.core.vtt
  (:require [clojure.string :as str]))
  
(def times 
  [ "00:07.22 --> 00:09.96"
    "00:11.12 --> 00:14.36"
    "00:14.70 --> 00:18.22"
    "00:19.68 --> 00:23.20"
    "00:25.04 --> 00:27.76"
    "00:28.88 --> 00:31.60"
    "00:32.24 --> 00:36.24"
    "00:36.80 --> 00:39.92"
    "00:40.80 --> 00:44.64"
    "00:46.16 --> 00:49.92"
    "00:49.92 --> 00:51.92"
    "00:52.32 --> 00:53.50"])

(defn vtt [msgs] "return some vtt"

  (defn mapper [index time] "map index and time"

    (prn "index: " index)
    (prn "time: " time)
    (prn (count msgs))
    ;(str index "\n" time "\n" (nth msgs index)))
    (str time "\n" (nth msgs index)))

  (def mapped (map-indexed mapper times))
  (prn mapped)
  (str "WEBVTT\n\n" (str/join "\n\n" mapped)))


