(ns attendify-cljs.logic)

(defn numeric-string? [value]
  (re-matches #"(-|\+)?\d+(\.\d+)?" value))
