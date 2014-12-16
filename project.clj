(defproject erv-msg "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.3.1"]
                 [ring/ring-defaults "0.1.2"]
                 [ring/ring-json "0.3.1"]
                 [ring/ring-jetty-adapter "1.2.2"]
                 [org.clojure/data.json "0.2.5"]
                 [com.novemberain/monger "2.0.0"]
                 [environ "0.5.0"]
                 [cheshire "5.4.0"]
                 [com.novemberain/pantomime "2.3.0"]
                 [hiccup "1.0.5"]]
  :plugins [[lein-ring "0.8.13"]]
  :uberjar-name "erv-msg-standalone.jar"
  :ring {:handler erv-msg.core.handler/app}
  :profiles {
     :production {:env {:production true}}
     :dev { :dependencies [[javax.servlet/servlet-api "2.5"]
                           [ring-mock "0.1.5"]
                           [ring/ring-devel "1.3.2"]]}})
