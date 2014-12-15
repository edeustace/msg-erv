(ns erv-msg.core.byte-range
  (:require [ring.util.response :as ring-resp :refer 
             [header status get-header content-type]]
            [ring.util.time :refer [parse-date format-date]]
            [ring.util.request :as request]
            [ring.util.codec :as codec]
            [pantomime.mime :refer [mime-type-of]]
            [clojure.string :as str]
            [clojure.java.io :as io])
  (:import [java.io RandomAccessFile]
           [java.util Date]
           [java.util.zip GZIPOutputStream]))

(def default-buffer-size 10240)
(def default-expiry-time 604800000)
(def multipart-boundary "MULTIPART_BYTERANGES")

(defn range-ex
  [message length]
  (ex-info message {:type :response
                    :headers {"Content-Range" (str "bytes /* " length)}
                    :error 416}))

(defn get-date-header [req key]
  (if-let [header (ring-resp/get-header req key)]
    (.getTime (parse-date header))
    -1))

(defn date-header [req key value]
  (ring-resp/header req key (format-date (Date. value))))

(defprotocol IToByteRange
  (byte-range [_]))

(extend-protocol IToByteRange
  clojure.lang.PersistentArrayMap
  (byte-range 
    ^{:pre (and start end total)}
    [{:keys [start end total] :as this}] 
    (assoc this :length (inc (- end start))))
  java.io.File
  (byte-range [this]
    (let [length (.length this)]
      (byte-range {:start 0 :end (dec length) :total length})))
  java.lang.String
  (byte-range [this]
    (let [[_ start end total] (first (re-seq #"(\d*)-(\d*)/(\d+)" this))
          start  (Long/parseLong start)
          end    (Long/parseLong end)
          total (Long/parseLong total)
          [start end] (cond 
                       (= start -1) [(- total end) (dec total)]
                       (= end -1) [start (dec total)]
                       :else [start end])]
      (if (> start end)
        (throw (range-ex "invalid range" total))
        (byte-range {:start start :end end :total total})))))

(defn content-range
  [{:keys [start end total]}]
  (str "bytes " start "-" end "/" total))

(defn load-ranges 
  [range length]
  (if-not range 
    []
    (map (fn [s] (byte-range (str s "/" length)))
         (-> range (.substring 6) (.split ",")))))

(defn find-range 
  [req file e-tag]
  (let [length   (.length file)
        frange   (byte-range file)
        range    (get-header req "Content-Range")
        if-range (get-header req "If-Range")]
    (if (and range (re-seq #"^bytes=\d*-\d*(,\d*-\d*)*$" range))
      (throw (range-ex "invalid-range-format" length)))
    (if (and if-range (= if-range e-tag))
      (try
        (let [if-range-time (get-date-header req "If-Range")]
          (if (and (not= if-range-time -1) 
                   (< (+ if-range-time 1000) (.lastModified file)))
            [frange]
            (load-ranges range length)))
        (catch IllegalArgumentException _ [frange]))
      (load-ranges range length))))

(defn accepts? [header value]
  (let [accept-values (str/split header #"\s*(,|;)\s*")]
    (first (filter (fn [s]
                     (or (= s value)
                         (= (str/replace s #"/.*$" "/*") s)
                         (= s "*/*")))
                   accept-values))))

(defn accepts-gzip? [req]
  (if-let [accept-encoding (ring-resp/get-header req "Accept-Encoding")]
    (accepts? accept-encoding "gzip")))

(defn write-all-output [output file]
  (let [buffer (byte-array default-buffer-size)]
    (loop []
      (let [read (.read file buffer)]
        (when (pos? read)
          (.write output buffer 0 read)
          (recur))))))

(defn write-partial-output [output file start length]
  (.seek file start)
  (loop [to-read length]
    (let [buffer (byte-array default-buffer-size)
          read (.read file buffer)]
      (when (pos? read)
        (if (pos? (- to-read read))
          (do 
            (.write output buffer 0 read)
            (recur (- to-read read)))
          (.write output buffer 0 (int to-read)))))))

(defn file-output
  [file {:keys [start end length]}]
  (ring.util.io/piped-input-stream 
   (fn [output]
     (with-open [file (RandomAccessFile. file "r")]
       (if (= (.length file) length)
         (write-all-output output file)
         (write-partial-output output file start length)))))) 

(defn multipart-file-output
  [file ranges]
  (ring.util.io/piped-input-stream
   (fn [output]
     (binding [*out* (io/make-writer output {})]
       (loop [ranges ranges]
         (when-let [r (first ranges)]
           (println)
           (println (str "--" multipart-boundary))
           (println (str "Content-Type: " (mime-type-of file)))
           (println (str "Content-Ranges: " (content-range r)))
           (file-output file r)
           (recur (rest ranges))))
       (println)
       (println (str "--" multipart-boundary "--"))))))

(defn single-range-response
  [req file range]
  (-> (ring-resp/response (if (= :get (:request-method req))
                            (file-output file range)))
      (ring-resp/header "Content-Range" (content-range range))
      (ring-resp/header "Content-Length" (str (:length range)))
      (ring-resp/status (if (= (:length range) (.length file)) 200 206))))

(defn multipart-response
  [req file ranges]
  (-> (ring-resp/response (if (= :get (:request-method req))
                            (multipart-file-output file ranges)))
      (ring-resp/content-type (str "multipart/byteranges; boundary=" 
                                   multipart-boundary))
      (ring-resp/status 206)))

(defn matches
  [header to-match]
  (let [values (str/split header #"\s*,\s*")]
    (first (filter (fn [s]
                     (or (= s to-match) (= s "*")))
                   values))))

(defn range-response
  [req file ranges]
  (let [ranges (if (empty? ranges) [(byte-range file)] ranges)]
    (if (= 1 (count ranges))
      (single-range-response req file (first ranges))
      (multipart-response req file))))

(defn cached? [req e-tag last-modified]
  (let [if-none-match (get-header req "If-None-Match")
        if-modified-since (get-date-header req "If-Modified-Since")]
    (or (and if-none-match (matches if-none-match e-tag))
        (and (nil? if-none-match)
             (not= if-modified-since -1)
             (> (+ if-modified-since 1000) last-modified)))))            

(defn resume? [req e-tag last-modified]
  (let [if-match (get-header req "If-Match")
        if-unmodified-since (get-date-header req "If-Unmodified-Since")]
    (or (and if-match (matches if-match e-tag))
        (and (not= if-unmodified-since -1)
             (<= (+ if-unmodified-since 1000) last-modified)))))

(defn disposition [req content-type]
  (let [accept (get-header req "Accept")]
    (if (or (.startsWith content-type "image")
            (and accept (accepts? accept content-type)))
      "inline"
      "attachment")))

(defn file-response
  [req filepath & [opts]]
  (if-let [file ((var ring-resp/find-file) filepath opts)]
    (let [length   (.length file)
          last-modified (.lastModified file)
          e-tag    (str (.getName file) "_" length "_" last-modified)
          ctype    (mime-type-of filepath)
          expires  (+ (System/currentTimeMillis) 
                      (:default-expiry-time opts default-expiry-time))]
      (cond
       (cached? req e-tag last-modified) 
       (-> (ring-resp/response)
           (status 304)
           (header "ETag" e-tag)
           (date-header "Expires" expires))
       (resume? req e-tag last-modified)
       (-> (ring-resp/response)
           (status 416))
       :else 
       (let [range  (find-range req file e-tag)]
         (-> (range-response req file range)
             (content-type ctype)
             (header "Content-Disposition" (disposition req ctype))
             (header "Accept-Ranges" "bytes")
             (header "ETag" e-tag)
             (date-header "Last-Modified" last-modified)
             (date-header "Expires" expires)))))))

(defn file-request 
  [req root-path opts]
  (let [opts (merge {:root root-path, :index-files? true, 
                     :allow-symlinks? false
                     :default-expiry-time default-expiry-time} opts)]
    (if (= :get (:request-method req))
      (let [path (subs (codec/url-decode (request/path-info req)) 1)]
        (file-response path opts)))))