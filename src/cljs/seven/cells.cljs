(ns seven.cells
  (:require
    [cljs.tools.reader :refer [read-string]]
    [cljs.js :refer [empty-state eval js-eval]]
    [reagent.core :as r])
  )

(defn eval-program [program]
  (eval (empty-state)
        ;'(+ 1 3)
        program
        {:eval js-eval
         :source-map true
         :context :expr}
        :value))

(defn evaluate! [cell state]
  (let [program (read-string (get-in @state (conj cell :program)))
        dependencies (set (filter vector? program))
        [new-dependencies obsolete-dependencies] (clojure.data/diff dependencies (get-in @state (conj cell :dependencies)))
        program-with-values-substituted (map (fn [s] (if (vector? s)
                                                  (get-in @state (conj s :value))
                                                  s))
                                        program)
        value (eval-program program-with-values-substituted)]

    (swap! state assoc-in
           (conj cell :value) value)

    (mapv
      #(evaluate! % state)
      (get-in @state (conj cell :dependents)))

    (mapv
      #(swap! state update-in (conj % :dependents) conj cell)
      new-dependencies)

    (mapv
      #(swap! state update-in (conj % :dependents) disj cell)
      obsolete-dependencies)

    (swap! state assoc-in
           (conj cell :dependencies) dependencies)))

(defn root* [state]
  [:table
   (doall (for [x (range (count @state))]
            [:tr
             {:key x}
             (doall (for [y (range (count (first @state)))]
                      (let [cell (get-in @state [x y])]
                        [:td
                         {:key (str x y)}
                         [:div (:value cell)]
                         [:input
                          {:type :text
                           :value (:program cell)
                           :on-key-down (fn [e]
                                          (when (= 13 (-> e .-keyCode))
                                            (evaluate! [x y] state)))
                           :on-change (fn [e]
                                        (let [program (-> e .-target .-value)]
                                          (swap! state update-in [x y] assoc :program program)))}]])))]))])

(def state (r/atom (into [] (for [x (range 3)]
                      (into []
                            (for [y (range 3)]
                              {:id (keyword (str x y))
                               :value nil
                               :program nil
                               :dependents #{}}))))))

(comment

  @state


  (eval-program "(+ 1 2)")
  (get-in @state [0 1])
  )

(defn root []
  [root* state])
