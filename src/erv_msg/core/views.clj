(ns erv-msg.core.views
  (:require [clojure.string :as str])
  (:use
    [hiccup.page :only (html5 include-css include-js)]))

(defn- msg-link [l]
  [:div {:class "link"}
    [:span {:class "name"} (:recipient l)]
    [:a {:href (str "/editor/messages/edit/" (:recipient l) ".html") } "Edit" ]
    [:a {:href (str "/editor/messages/delete/" (:recipient l) ".html") } "Delete" ]]
  )

(defn edit-message [msg times] "...."
  (html5 {:lang "en"}
    [:head
      [:title "Messages..."]
      (include-css "/main.css")
      (include-css "/editor.css")
      (include-css "//maxcdn.bootstrapcdn.com/bootstrap/3.3.1/css/bootstrap.min.css")
      (include-js "//code.jquery.com/jquery-2.1.1.min.js")
      (include-js "/edit-message.js")]
   [:body
      [:div {:class "container"}

        [:a {:href (str "/to/" (get msg "recipient"))} "View Video"]
        [:br]
        [:div {:role "form"}
          [:div {:class "form-group"}
            [:label "recipient"]
            [:input {:id "recipient" :class "form-control" :type "text" :value  (get msg "recipient")}]
          ]
          [:p {:class "bg-success"}
            "You must enter exactly 12 lines below"]
          [:table {:style "width: 100%"}
            [:tr
              [:td  {:style "width: 200px"}
                [:p {:class "bg-primary"} (map #(str % "<br/>") times)]
              ]
              [:td 
                [:div {:class "form-group"}
                  [:label "msg"]
                  [:textarea {:rows 12 :class "form-control" :id "msg"} (get msg "msg")]
                ]

              ]
            ]
          ]
          [:div {:class "form-group"}
            [:label ""]
            [:button {:class "btn btn-primary"} "Save"]
          ]
        ]
        [:br]
      ]]))

(defn messages [msg-list] "...."
  (html5 {:lang "en"}
    [:head
      [:title "Messages..."]
      (include-css "//vjs.zencdn.net/4.10/video-js.css")
      (include-css "/main.css")
      (include-css "//maxcdn.bootstrapcdn.com/bootstrap/3.3.1/css/bootstrap.min.css")
      (include-css "/editor.css")
      (include-js "//vjs.zencdn.net/4.10/video.js")
   [:body
      [:div {:class "container"}
        [:div {:class "row"}
          [:a {:href "/editor/messages/edit/new.html"} "New message"]
          (map msg-link msg-list)]]]]))
