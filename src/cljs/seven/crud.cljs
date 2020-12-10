(ns seven.crud
  (:require [reagent.core :as r]))

(defn create! [data buffer]
  (conj data (merge buffer {:id (random-uuid)})))

(defn update! [data buffer]
  (mapv
    (fn [entry]
      (if (= (:id entry) (:id buffer))
        buffer
        entry))
    data))

(defn delete! [data buffer]
  (remove (comp (partial = (:id buffer)) :id) data))

(defn has-prefix? [prefix entity]
  (-> entity
      :last
      clojure.string/lower-case
      (clojure.string/starts-with? (clojure.string/lower-case prefix))))

(defn root [] ;;todo give me labels, layout
  (let [buffer (r/atom nil)
        prefix (r/atom "")
        data (r/atom [])]
    (fn []
      (let [filtered-data (filter (partial has-prefix? @prefix) @data)]
        [:div.crud
         [:section
          [:div
           [:label "filter by last name:"]
           [:input
            {:type :text
             :value @prefix
             :on-change #(do
                           (reset! buffer nil)
                           (reset! prefix (-> % .-target .-value)))}]]
          [:div.entryList
           (doall (for [{:keys [first last id] :as entry} filtered-data]
                    [:div.entry
                     {:key id
                      :on-click #(reset! buffer entry)
                      :class (when (= id (:id @buffer)) "active")}
                     [:span (str last ", " first)]]))]
          [:div
           [:button
            {:disabled (not (and (:first @buffer) (:last @buffer)))
             :on-click #(do (swap! data create! @buffer)
                            (reset! buffer nil))}
            "Create"]
           [:button
            {:disabled (not (:id @buffer))
             :on-click #(do (swap! data update! @buffer)
                            (reset! buffer nil))}
            "Update"]
           [:button
            {:disabled (not (:id @buffer))
             :on-click #(do (swap! data delete! @buffer)
                            (reset! buffer nil))}
            "Delete"]]]
         (let [{:keys [first last]} @buffer]
           [:section
            [:div
             [:label "first name:"]
             [:input
              {:type :text
               :value first
               :on-change #(swap! buffer assoc :first (-> % .-target .-value))}]]
            [:div
             [:label "last name:"]
             [:input
              {:type :text
               :on-change #(swap! buffer assoc :last (-> % .-target .-value))
               :value last}]]])]))))