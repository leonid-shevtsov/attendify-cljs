(ns attendify-cljs.views
  (:require [re-frame.core :as re-frame]))

(defn file-form []
  [:form
   [:div.form-group
    [:label "Upload a CSV file:"]
    [:input {:type "file"
             :placeholder "CSV file"
             :on-change #(re-frame/dispatch [:handle-file (.-target %)])}]]])

(defn field [row column]
  (let [focus (re-frame/subscribe [:focus])
        value (re-frame/subscribe [:field-value row column])]
    (fn []
      (if (and (= (:column @focus) column) (= (:row @focus) row))
        [:div
         [:input#focusInput {:value (:value @focus)
                             :on-change #(re-frame/dispatch [:focus-field-change (.. % -target -value)])
                             :on-key-up #(re-frame/dispatch [:focus-field-control (.-key %)])}]
         (when-let [error (:error @focus)] [:span error])]
        [:div {:on-click #(re-frame/dispatch [:set-focus row column])}  @value]))))

(defn remove-row-button [row-number]
  (let [is-edit-mode (re-frame/subscribe [:is-edit-mode])]
    (fn []
      (when-not @is-edit-mode
        [:button {:on-click #(re-frame/dispatch [:remove-row row-number])} "x"]))))

(defn editable-row [{:keys [name value]} row-number]
  [:tr {:key row-number}
   [:td [field row-number :name]]
   [:td [field row-number :value]]
   [:td [remove-row-button row-number]]])

(defn statistics []
  (let [sum (re-frame/subscribe [:sum])
        average (re-frame/subscribe [:average])]
    (fn []
      (when (or @sum @average)
        [:div
         [:h1 "Statistics"]
         (when @sum [:div "Sum: " @sum])
         (when @average [:div "Average: " @average])]))))

(defn add-row-button []
  (let [is-edit-mode (re-frame/subscribe [:is-edit-mode])]
    (fn []
      (when-not @is-edit-mode
        [:button {:on-click #(re-frame/dispatch [:add-row])} "Add row"]))))

(defn data-block [{{header-name :name header-value :value} :header rows :rows}]
  [:div
   [:h1 "Data"]
   [:table
    [:thead
     [:tr
      [:td [field :header :name]]
      [:td [field :header :value]]]]
    [:tbody
     (map editable-row rows (iterate inc 0))]]
   [add-row-button]
   [statistics]])

(defn errors-block [errors]
  [:div
   [:h1 "Errors loading CSV file"]
   [:ul (map #(vector :li %) errors)]])

(defn main-panel []
  (let [errors (re-frame/subscribe [:errors])
        table-data (re-frame/subscribe [:table-data])]
    (fn []
      [:div
       [file-form]
       (when @errors [errors-block @errors])
       (when @table-data [data-block @table-data])])))


