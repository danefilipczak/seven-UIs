(ns seven.timer
  (:require [reagent.core :as r]))


(defn root* [state]
  (let [{:keys [cursor duration]} @state]
    [:<>
     [:progress {:value cursor
                 :max duration}]
     [:div "elapsed time" cursor]
     [:input {:type :range
              :min 1
              :max 100
              :value duration
              :on-change (fn [e]
                           (let [val (-> e .-target .-value)]
                             (swap! state assoc :duration val)))}]
     [:br]
     [:button
      {:on-click #(swap! state assoc :cursor 0)}
      "reset"]]))

(defn tick [state]
  (let [{:keys [cursor duration]} @state]
    (when (< cursor duration)
      (swap! state update :cursor (partial + 0.1)))))

(defn root []
  (let [state (r/atom {:duration 100
                       :cursor 0})
        _ (js/setInterval #(tick state) 100)]
    [root* state]))
