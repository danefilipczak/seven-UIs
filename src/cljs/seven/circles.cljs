(ns seven.circles
  (:require [reagent.core :as r]))

(defn persist-state [state xfn]
  (let [{:keys [cursor states]} @state
        current-state (get states (- cursor 1) [])]
   (swap! state update :states #(assoc
                                  (->> % (take cursor) vec)
                                  cursor
                                  (xfn current-state)))
   (swap! state update :cursor inc)))

(defn add-circle [e state]
  (let [dim (-> e .-currentTarget .getBoundingClientRect)
        location [(- (.-clientX e) (.-left dim))
                  (- (.-clientY e) (.-top dim))]]
    (persist-state
      state
      #(conj % {:diameter 20
                :location location}))))

(defn resize-circle [state selected temp-diameter]
  (persist-state
    state
    #(assoc-in % [selected :diameter] temp-diameter)))

(defn root []
  (let [state (r/atom {:cursor 0 :states []})
        buffer (r/atom nil)]
    (fn []
      (let [{:keys [cursor states]} @state
            current-state (get states (- cursor 1) [])]
        [:div.circles
         [:div
          [:button
           {:disabled (or (< cursor 1) @buffer)
            :on-click #(swap! state update :cursor dec)}
           "undo"]
          [:button
           {:on-click #(swap! state update :cursor inc)
            :disabled (or
                        (= (count states) 0)
                        (= cursor (count states))
                        @buffer)}
           "redo"]]
         [:svg
          {:on-click #(when-not @buffer (add-circle % state))}
          (doall
            (map-indexed
              (fn [i {:keys [location diameter]}]
                (let [selected? (= (first @buffer) i)]
                 [:circle
                  {:cx (first location)
                   :cy (second location)
                   :fill (if selected? "gray" "black")
                   :r (if selected?
                        (/ (second @buffer) 2)
                        (/ diameter 2))
                   :on-click #(do (-> % .stopPropagation)
                                  (reset! buffer [i diameter]))}]))
              current-state))]
         [:div
          (when @buffer
            [:<>
             [:input
              {:type :range
               :min 5
               :max 100
               :value (second @buffer)
               :on-change #(swap! buffer assoc 1 (-> % .-target .-value))}]
             [:button
              {:on-click (fn []
                           (reset! buffer nil))}
              "cancel"]
             [:button
              {:on-click (fn []
                           (apply resize-circle state @buffer)
                           (reset! buffer nil))}
              "save"]])]]))))
