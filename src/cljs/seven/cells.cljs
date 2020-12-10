(ns seven.cells
  (:require
    [cljs.tools.reader :refer [read-string]]
    [cljs.js :refer [empty-state eval js-eval]]
    [reagent.core :as r]))

(def ENTER 13)

(defn eval-program [program]
  (eval (empty-state)
        program
        {:eval js-eval
         :source-map true
         :context :expr}
        :value))

(defn read-program [program-string]
  (let [program (read-string program-string)]
    (if (number? program)
      (list identity program)
      program)))

(defn evaluate! [cell state buffer]
  (let [old-program (read-program (get-in @state (conj cell :program)))
        old-program-dependencies (set (filter vector? old-program))
        new-program (if buffer (read-program buffer) old-program)
        new-program-dependencies (set (filter vector? new-program))
        [novel-dependencies obsolete-dependencies] (clojure.data/diff new-program-dependencies old-program-dependencies)
        new-program-with-values-substituted (map #(if (vector? %)
                                                    (get-in @state (conj % :value))
                                                    %)
                                                 new-program)
        value (eval-program new-program-with-values-substituted)]

    (when buffer (swap! state update-in cell assoc :program buffer))

    (swap! state assoc-in
           (conj cell :value) value)

    (mapv
      #(evaluate! % state nil)
      (get-in @state (conj cell :dependents)))

    (mapv
      #(swap! state update-in (conj % :dependents) conj cell)
      novel-dependencies)

    (mapv
      #(swap! state update-in (conj % :dependents) disj cell)
      obsolete-dependencies)))

(defn root* [state]
  (let [buffer (r/atom nil)
        focused (r/atom nil)]
    (fn []
      [:<>
       [:div.cells
        [:table
         [:tr
          [:th nil]
          (doall (for [x (range (count (first @state)))]
                   [:th {:key x} x]))]
         (doall (for [y (range (count @state))]
                  [:tr
                   {:key y}
                   [:th y]
                   (doall (for [x (range (count (first @state)))]
                            (let [location [x y]
                                  cell (get-in @state location)]
                              [:td
                               {:key (str x y)}
                               [:input
                                {:type :text
                                 :value (if
                                          (= @focused location)
                                          @buffer
                                          (:value cell))
                                 :on-focus #(do (.stopPropagation %)
                                                (reset! buffer (:program cell))
                                                (reset! focused location))
                                 :on-blur #(do (.stopPropagation %)
                                               (reset! focused nil))
                                 :on-key-down #(when (= ENTER (-> % .-keyCode))
                                                 (do (evaluate! location state @buffer)
                                                     (reset! buffer nil)
                                                     (reset! focused nil)
                                                     (-> % .-target .blur)))
                                 :on-change #(reset! buffer (-> % .-target .-value))}]])))]))]]
       [:div
        "Cells may contain either numerical values or s expressions.
        Cells can be referenced by a vector of column / row indexes, e.g. (+ [0 0] 1)"]])))

(def state (r/atom (into [] (for [y (range 10)]
                      (into []
                            (for [x (range 10)]
                              {:id (keyword (str x y))
                               :value nil
                               :program nil
                               :dependents #{}}))))))

(defn root []
  (evaluate! [0 0] state "7")
  (evaluate! [0 1] state "(+ [0 0] 4)")
  (evaluate! [1 1] state "(* [0 1] 2)")
  (fn []
    [root* state]))
