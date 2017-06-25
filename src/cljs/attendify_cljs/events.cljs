(ns attendify-cljs.events
  (:require [re-frame.core :as re-frame]
            [attendify-cljs.subs :refer [get-field-value]]
            [attendify-cljs.csv :as csv]
            [attendify-cljs.logic :as logic]))

(re-frame/reg-event-db
  :initialize-db
  (fn  [_ _]
    {:table-data nil
     :errors nil
     :focus {:row nil
             :field nil
             :value nil
             :error nil}}))

(re-frame/reg-event-db
  :on-load-csv
  (fn [db [_ reader]]
    (let [csv-string (.-result reader)
          [errors parsed-csv] (csv/parse-csv csv-string)]
      (assoc db :errors errors :table-data parsed-csv :focus {:row nil :field nil :error nil}))))

(re-frame/reg-event-db
  :handle-file
  (fn [db [_ input]]
    (let [file (aget (.-files input) 0)
          reader (js/FileReader.)]
      (set! (.-onload reader) #(re-frame/dispatch [:on-load-csv reader]))
      (.readAsText reader file "UTF-8"))))

(defn commit-focus
  "Commits changes from focus into table-data"
  [{rows :rows :as table-data} {row-index :row column-index :column value :value}]
  (if-not (and row-index column-index)
    table-data ; no changes
    (if (= :header row-index)
      (assoc-in table-data [:header column-index] value)
      (let [old-row (nth rows row-index)
            cast-value (if (= :value column-index) (js/parseInt value) value)
            new-row (assoc old-row column-index cast-value)
            new-rows (assoc rows row-index new-row)]
        (assoc table-data :rows new-rows)))))

(defn build-focus
  [db row-index column-index]
  (let [value (when (and row-index column-index)(get-field-value db row-index column-index))]
    {:row row-index
     :column column-index
     :value value
     :error nil}))

(defn focus-input
  []
  "Sets focus on the input of the currently edited field"
  (js/setTimeout
    #(.focus (.getElementById js/document "focusInput"))
    0))

(re-frame/reg-event-db
  :set-focus
  (fn [db [_ new-row-index new-column-index]]
    (if (get-in db [:focus :error]) ; do not re-focus in case of error
      db
      (let [new-table-data (commit-focus (:table-data db) (:focus db))
            new-focus (build-focus db new-row-index new-column-index)]
        (if new-row-index (focus-input))
        (assoc db :table-data new-table-data :focus new-focus)))))

(re-frame/reg-event-db
  :cancel-edit
  (fn [db _]
    (assoc db :focus (build-focus db nil nil))))

(defn validate [row-index column-index value]
  (if (empty? value)
    "Must not be empty"
    (when (and (not= row-index :header)
             (= column-index :value)
             (not (logic/numeric-string? value)))
      "Must be a number")))

(re-frame/reg-event-db
  :focus-field-change
  (fn [{focus :focus :as db} [_ new-value]]
    (let [new-error (validate (:row focus) (:column focus) new-value)]
      (assoc db :focus (assoc focus :value new-value :error new-error)))))

(re-frame/reg-event-db
  :focus-field-control
  (fn [db [_ key]]
    (case key
      "Enter" (re-frame/dispatch [:set-focus nil nil])
      "Escape" (re-frame/dispatch [:cancel-edit])
      nil)
    db))

(re-frame/reg-event-db
  :remove-row
  (fn [db [_ row-index]]
    (let [rows (get-in db [:table-data :rows])
          new-rows (vec (concat (subvec rows 0 row-index)
                                (subvec rows (inc row-index))))]
      (assoc-in db [:table-data :rows] new-rows))))

(re-frame/reg-event-db
  :add-row
  (fn [db _]
    (let [rows (get-in db [:table-data :rows])
          new-row {:name "" :value 0}
          new-focus {:row (count rows) :column :name :value "" :error "Must not be empty"}
          new-rows (conj rows new-row)]
      (focus-input)
      (->
        db
        (assoc-in [:table-data :rows] new-rows)
        (assoc :focus new-focus)))))
