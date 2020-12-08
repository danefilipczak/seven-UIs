(ns seven.temperatures
  (:require [reagent.core :as r]))

(defn to-internal [format]
  (case format
    :fahrenheit identity
    :celsius #(if (re-matches #"\d*" (str %))
               (+ 32 (* % (/ 9 5)))
               %)))


(let [mem (atom nil)]
  (defn only-pos [i]
    (when (pos? i)
      (reset! mem i))
    @mem))

(comment
  ;; lol nothing about this works at all. you can't have a format agnostic inner value and still be able to read differently
  ;; just use explicit functions and be on your merry way
  (re-matches #"\d*" 10)
  (only-pos 9))

(let [mem (atom nil)
      only-nums (fn [fun val]
                  #_(.log js/console (re-matches #"\d*" (str val)))
                  (when (re-matches #"\d*" (str val))
                    (reset! mem val))
                  (fun @mem))]
  (defn from-internal [format]
    (case format
      :fahrenheit (partial only-nums identity)
      :celsius (partial only-nums #(* (- % 32) (/ 5 9))))))


;C = (F - 32) * (5/9) and the dual direction is F = C * (9/5) + 32.

(defn root []
  (let [temp (r/atom 0)]
    (fn []
      [:div
       [:input
        {:type :text
         :value ((from-internal :celsius) @temp)
         :on-change #(reset! temp ((to-internal :celsius) (-> % .-target .-value)))}]
       [:input
        {:type :text
         :value ((from-internal :fahrenheit) @temp)
         :on-change #(reset! temp ((to-internal :fahrenheit) (-> % .-target .-value)))}]])))
