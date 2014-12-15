(ns erv-msg.core.vtt
  (:require [clojure.string :as str]))

(def times [
  "00:06.42 --> 00:09.20"
  "00:10.88 --> 00:13.28"
  "00:13.28 --> 00:17.28"
  "00:19.52 --> 00:24.08"
  "00:25.52 --> 00:28.08"
  "00:32.24 --> 00:34.40"
  "00:35.60 --> 00:39.04"
  "00:40.80 --> 00:43.20"
  "00:44.08 --> 00:47.60"
  "00:48.08 --> 00:49.76"
  "00:51.68 --> 00:54.24"
  "00:54.24 --> 00:56.24"])

(defn vtt [msgs] "return some vtt"

  (defn mapper [index time] "map index and time"
    (str time "\n" (nth msgs index)))

  (def mapped (map-indexed mapper times))
  (str "WEBVTT\n\n" (str/join "\n\n" mapped)))


