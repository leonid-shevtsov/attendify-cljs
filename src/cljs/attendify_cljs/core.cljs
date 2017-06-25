(ns attendify-cljs.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [attendify-cljs.events]
            [attendify-cljs.subs]
            [attendify-cljs.views :as views]
            [attendify-cljs.config :as config]))


(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (re-frame/dispatch-sync [:initialize-db])
  (dev-setup)
  (mount-root))
