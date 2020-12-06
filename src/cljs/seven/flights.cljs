(ns seven.flights
  (:require [reagent.core :as r]))

(def flight-type->label
  {:return "Return Flight"
   :one-way "One-Way Flight"})

(defn valid-date-string? [d]
  (re-matches #"^\d\d.\d\d.\d\d\d\d$" d))

(defn to-date [date-string]
  (when (valid-date-string? date-string)
    (let [[day month year] (clojure.string/split date-string #"\.")]
      (js/Date.
        year
        (dec month) ;; month is 0 indexed
        day))))

(defn date-before? [d1 d2]
  (when (and d1 d2)
    (neg? (- d1 d2))))

(defn booking-message [state]
  (let [{:keys [type date-strings]} @state]
    (apply str (concat
                 ["You have booked a "
                  (flight-type->label type)]
                 (if (= type :return)
                   [" from " (first date-strings) " to " (second date-strings)]
                   [" on " (first date-strings)])))))

(defn root []
  (let [state (r/atom {:type :return
                       :date-strings ["11.11.1994"
                                       "11.11.1994"]})]
    (fn []
      (let [[date-string-1 date-string-2 :as date-strings] (:date-strings @state)
            [d1 d2] (map to-date date-strings)
            {:keys [type]} @state]
        [:div.flights
         [:select
          {:on-change #(swap! state assoc :type (-> % .-target .-value keyword))}
          [:option
           {:value :return}
           (flight-type->label :return)]
          [:option
           {:value :one-way}
           (flight-type->label :one-way)]]
         [:input
          {:type :text
           :class (when-not d1 :invalid)
           :value date-string-1
           :on-change #(swap! state assoc-in [:date-strings 0] (-> % .-target .-value))}]
         [:input
          {:type :text
           :class (when-not d2 :invalid)
           :value date-string-2
           :disabled (not= type :return)
           :on-change #(swap! state assoc-in [:date-strings 1] (-> % .-target .-value))}]
         [:button
          {:disabled (case type
                       :one-way (not d1)
                       :return (not (date-before? d1 d2)))
           :on-click #(js/alert (booking-message state))}
          "book"]
         [:br]]))))
