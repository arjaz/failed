(ns failed.core
  (:gen-class)
  (:require
    [failed.entities.core :refer [tick]]
    [failed.ui.core :refer [->UI]]
    [failed.ui.drawing :refer [draw-game]]
    [failed.ui.input :refer [get-input process-input]]
    [lanterna.screen :as s]))


(defrecord World
  [])


(defrecord Game
  [world uis input hud log pause?])


(defn new-game
  []
  (map->Game {:world  nil
              ;; TODO: keymap
              :uis    [(->UI :start nil)]
              :input  nil
              :pause? nil
              :hud    {:height 4}
              :log    {:entries  []
                       :max-size 32}}))


(defn tick-entity
  [world entity]
  (tick entity world))


(defn tick-world
  [world pause?]
  (if-not pause?
    (reduce tick-entity world (vals (:entities world)))
    world))


(defn run-game
  [game screen]
  (loop [{:keys [input uis] :as game} game]
    (when (seq uis)
      (if (nil? input)
        (let [game (-> game
                       (update :world tick-world (:pause? game))
                       (dissoc :pause?))]
          (draw-game game screen)
          (recur (get-input game screen)))
        (recur (process-input (dissoc game :input) input))))))


(comment
  (main :swing)
  )


(defn main
  ([] (main :swing false))
  ([screen-type] (main screen-type false))
  ([screen-type block?]
   (letfn [(go
             []
             (let [screen (s/get-screen screen-type)]
               (s/in-screen screen
                            (run-game (new-game) screen))))]
     (if block?
       (go)
       (future (go))))))


(defn -main
  [& args]
  (let [args (set args)
        screen-type (cond
                      (args ":swing") :swing
                      (args ":text")  :text
                      :else           :auto)]
    (main screen-type true)))

