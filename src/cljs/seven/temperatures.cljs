(ns seven.temperatures
  (:require [reagent.core :as r]))

(defn to-internal [format]
  (fn [input]
    (when (re-matches #"^\-?\d(\d|\.)*" (str input))
      (case format
        :fahrenheit input
        :celsius (+ 32 (* input (/ 9 5)))))))

(comment
  (re-matches #"\d*" "-17.777777777777")
  ((to-internal :celsius) "-")

  (re-matches #"^\-?\d(\d|\.)*" "")

  )

(defn from-internal [format]
  (case format
    :fahrenheit identity
    :celsius #(->>
                (* (- % 32) (/ 5 9))
                (.round js/Math))))

;C = (F - 32) * (5/9) and the dual direction is F = C * (9/5) + 32.

(defn root []
  (let [internal (r/atom 0)
        c-buffer (r/atom nil)
        f-buffer (r/atom nil)]
    (fn []
      [:div.temperature
       [:div
        [:label "celsius:"]
        [:input
         {:type :text
          :value (or @c-buffer ((from-internal :celsius) @internal))
          :on-blur #(reset! c-buffer nil)
          :on-change #(if-let [value ((to-internal :celsius) (-> % .-target .-value))]
                        (do (reset! internal value)
                            (reset! c-buffer nil))
                        (reset! c-buffer (-> % .-target .-value)))}]]
       [:div
        [:label "fahrenheit:"]
        [:input
         {:type :text
          :on-blur #(reset! f-buffer nil)
          :value (or @f-buffer ((from-internal :fahrenheit) @internal))
          :on-change #(if-let [value ((to-internal :fahrenheit) (-> % .-target .-value))]
                        (do (reset! internal value)
                            (reset! f-buffer nil))
                        (reset! f-buffer (-> % .-target .-value)))}]]])))
