(ns seven.circles
  (:require [reagent.core :as r]))

(defn root []
  "I am circles"
  (let [state (r/atom {:cursor 0 :states []})
        selected (r/atom nil)]
    (fn []
      (let [{:keys [cursor states]} @state
            current-state (get states (- cursor 1) [])]
        [:<>
         [:button
          {:disabled (< cursor 1)
           :on-click #(swap! state update :cursor dec)}
          "undo"]
         [:button
          {:on-click #(swap! state update :cursor inc)
           :disabled (or
                       (= (count states) 0)
                       (= cursor (count states))) ;todo is this right
                       }
          "redo"]
         [:svg
          {:on-click #(let [dim (-> % .-currentTarget .getBoundingClientRect)]
                        (swap! state update :states assoc cursor ;; todo don't just assoc, also blow out all later elements in the vec
                               (conj current-state
                                     {:radius 20
                                      :location
                                      [(- (.-clientX %) (.-left dim))
                                       (- (.-clientY %) (.-top dim))]}))
                        (swap! state update :cursor inc))}
          (doall
            (map-indexed
              (fn [i {:keys [location radius]}]
                [:circle
                 {:cx (first location)
                  :cy (second location)
                  :fill (if (= @selected i) "gray" "black")
                  :r radius
                  :on-mouse-enter #(reset! selected i)
                  :on-mouse-leave #(reset! selected nil)}])
              current-state))]]))))
