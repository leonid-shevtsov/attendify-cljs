(ns attendify-cljs.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :as re-frame]
            [reagent.ratom :as ratom]))

(re-frame/reg-sub
 :errors
 :errors)

(re-frame/reg-sub
  :table-data
  :table-data)

(re-frame/reg-sub
  :focus
  :focus)

(re-frame/reg-sub
  :is-edit-mode
  (fn [db]
    (not= nil (:row (:focus db)))))

(defn get-field-value [db row column]
  (if (= row :header)
    (get-in db [:table-data :header column])
    (get (nth (get-in db [:table-data :rows]) row) column)))

(re-frame/reg-sub
  :field-value
  (fn [db [_ row column]]
    (get-field-value db row column)))

(re-frame/reg-sub
  :sum
  (fn [db]
    (when (:table-data db) (reduce + 0 (map :value (:rows (:table-data db)))))))

(re-frame/reg-sub
  :count
  (fn [db]
    (when (:table-data db) (count (:rows (:table-data db))))))

(re-frame/reg-sub
  :average
  (fn [query-v _]
    [(re-frame/subscribe [:sum]) (re-frame/subscribe [:count])])
  (fn [[sum count] _]
    (when (pos? count) (/ sum count))))
