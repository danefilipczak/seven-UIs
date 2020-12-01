(ns seven.cells
  (:require
    [cljs.tools.reader :refer [read-string]]
    [cljs.js :refer [empty-state eval js-eval]])
  )


(def cells [11 22 33 44])

(get-in cells [(js/parseInt (name :1))])

(defn prepare-program [program]
  (map (fn [s] (if (keyword? s)
                 (get-in cells [(js/parseInt (name s))])
                 s))
       (read-string program)))


(comment
  ;eval

  (map (fn [s] (if (keyword? s)
                 (get-in cells [(js/parseInt (name :1))])
                 s)) (read-string "(+ :1 1)"))


  {:1 #{:2 :3}
   :3 #{:2}}

  on cell change:::
  evaluate cell, looking up reference values
  re-evaluate all in my cell's registry

  do a set diff of new and old dependents
  add me to cell registry of all new, remove from all old



  (eval (empty-state)
        ;'(+ 1 3)
        (prepare-program "(+ :2 :1)")
        {:eval js-eval
         :source-map true
         :context :expr}
        (fn [result] result)
        )


  ;; when my formula changes, I must lookup references and re-evaluate myself.
  ;; Furthermore, anyone who references me must re-evaluate.

  )
