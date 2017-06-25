(ns attendify-cljs.csv
  (:require [clojure.string :as str]
            [attendify-cljs.logic :as logic]))

(defn parse-header [[name-label value-label & other-columns]]
  (if (and (seq name-label) (seq value-label) (empty? other-columns))
    [nil {:name name-label :value value-label}]
    ["Header must contain exactly two non empty columns" nil]))

(defn parse-row [[name value & other-columns]]
  (if (or (seq other-columns) (empty? name) (empty? value))
    ["Row must contain exactly two non-empty columns" nil]
    (if-not (logic/numeric-string? value)
            ["Second column must contain a number" nil]
            [nil {:name name :value (js/parseInt value)}])))

(defn parse-csv [file]
  (let [lines (str/split file #"\n")
        [header & rows] (map #(str/split % #",") lines)
        [header-error header-values] (parse-header header)
        parsed-rows (map parse-row rows)
        [row-errors structured-rows] (apply map vector parsed-rows)
        errors (filter some? (conj row-errors header-error))]
    (if (seq errors)
      [errors nil]
      [nil {:header header-values :rows structured-rows}])))
