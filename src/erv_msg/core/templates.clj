(ns erv-msg.core.templates
  (:require [clojure.string :as str])
  (:use 
    [hiccup.page :only (html5 include-css include-js)]))
  
(defn- title [recipient]
  (str/join " " ["A message from ervin to" recipient]))

(defn- vid [vtt-path] 
  [:video.video-js.vjs-default-skin 
    {:data-setup "{}",
    :width "769"
    :height "432" 
    :poster "really-cool-video-poster.jpg", 
    :preload "none", 
    :autoplay "autoplay",
    :controls "controls"} 
    [:source 
      {
        :type "video/mp4", 
      :src "/msg.sm.mp4"}]  
    [:track 
      {:default "default", 
      :label "English", 
      :srclang "en", 
      :src vtt-path,
      :kind "subtitles"}] 
    [:p.vjs-no-js "Upgrade"]]
  )

(defn index [recipient vtt-path]
  (html5 {:lang "en"}
         [:head
          [:title (title recipient)]
          (include-css "//vjs.zencdn.net/4.10/video-js.css")
          (include-css "/main.css")
          (include-js "//vjs.zencdn.net/4.10/video.js")
          [:body
           [:div {:class "container"} (vid vtt-path) ]]]))
