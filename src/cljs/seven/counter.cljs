(ns seven.counter
  (:require [reagent.core :as r]))

(defn root []
  (let [count (r/atom 0)]
    (fn []
      [:div.counter
       @count
       [:button
        {:on-click #(swap! count inc)}
        "Count"]])))
