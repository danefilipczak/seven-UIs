(ns seven.core
  (:require
   [reagent.core :as reagent :refer [atom]]
   [reagent.dom :as rdom]
   [reagent.session :as session]
   [seven.timer :as timer]
   [seven.temperatures :as temperatures]
   [seven.cells :as cells]
   [seven.flights :as flights]
   [seven.circles :as circles]
   [seven.counter :as counter]
   [seven.crud :as crud]
   [reitit.frontend :as reitit]
   [clerk.core :as clerk]
   [accountant.core :as accountant]))

;; -------------------------
;; Routes

(def router
  (reitit/router
   [["/" :index]
    ["/items"
     ["" :items]
     ["/:item-id" :item]]
    ["/crud" :crud]
    ["/temperatures" :temperatures]
    ["/timer" :timer]
    ["/flights" :flights]
    ["/counter" :counter]
    ["/cells" :cells]
    ["/circles" :circles]]))

(defn path-for [route & [params]]
  (if params
    (:path (reitit/match-by-name router route params))
    (:path (reitit/match-by-name router route))))

;; -------------------------
;; Page components

(defn home-page []
  (fn []
    [:span.main
     [:h1 "7 GUIs"]
     [:a {:href "https://eugenkiss.github.io/7guis/"}
      "problem statement"]]))



(defn items-page []
  (fn []
    [:span.main
     [:h1 "The items of seven"]
     [:ul (map (fn [item-id]
                 [:li {:name (str "item-" item-id) :key (str "item-" item-id)}
                  [:a {:href (path-for :item {:item-id item-id})} "Item: " item-id]])
               (range 1 60))]]))


(defn item-page []
  (fn []
    (let [routing-data (session/get :route)
          item (get-in routing-data [:route-params :item-id])]
      [:span.main
       [:h1 (str "Item " item " of seven")]
       [:p [:a {:href (path-for :items)} "Back to the list of items"]]])))

;; -------------------------
;; Translate routes -> page components

(defn page-for [route]
  (case route
    :index #'home-page
    :timer #'timer/root
    :cells #'cells/root
    :crud #'crud/root
    :counter #'counter/root
    :temperatures #'temperatures/root
    :flights #'flights/root
    :circles #'circles/root
    :items #'items-page
    :item #'item-page))


;; -------------------------
;; Page mounting component

(defn current-page []
  (fn []
    (let [page (:current-page (session/get :route))]
      [:div
       [:header
        [:p [:a {:href (path-for :index)} "Home"] " | "
         [:a {:href (path-for :cells)} "Cells"] " | "
         [:a {:href (path-for :crud)} "CRUD"] " | "
         [:a {:href (path-for :counter)} "Counter"] " | "
         [:a {:href (path-for :flights)} "Flights"] " | "
         [:a {:href (path-for :temperatures)} "Temperature Converter"] " | "
         [:a {:href (path-for :circles)} "Circles"] " | "
         [:a {:href (path-for :timer)} "Timer"]]]
       [page]])))

;; -------------------------
;; Initialize app

(defn mount-root []
  (rdom/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (clerk/initialize!)
  (accountant/configure-navigation!
   {:nav-handler
    (fn [path]
      (let [match (reitit/match-by-path router path)
            current-page (:name (:data  match))
            route-params (:path-params match)]
        (reagent/after-render clerk/after-render!)
        (session/put! :route {:current-page (page-for current-page)
                              :route-params route-params})
        (clerk/navigate-page! path)
        ))
    :path-exists?
    (fn [path]
      (boolean (reitit/match-by-path router path)))})
  (accountant/dispatch-current!)
  (mount-root))
