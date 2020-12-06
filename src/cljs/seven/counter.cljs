(ns seven.counter
  (:require [reagent.core :as r]))

(defn root []
  (let [count (r/atom 0)]
    (fn []
      [:<>
       @count
       [:button
        {:on-click #(swap! count inc)}
        "Count"]])))
