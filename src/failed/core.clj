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
  [world uis input log])


(defn tick-entity
  [world entity]
  (tick entity world))


(defn tick-world
  [world]
  (reduce tick-entity world (vals (:entities world))))


(defn run-game
  [game screen]
  (loop [{:keys [input uis] :as game} game]
    (when (seq uis)
      (if (nil? input)
        (let [game (update game :world tick-world)]
          (draw-game game screen)
          (recur (get-input game screen)))
        (recur (process-input (dissoc game :input) input))))))


(comment
  (main :swing)
  )


(defn new-game
  []
  (->Game nil [(->UI :start)] nil {:entries [] :size 4}))


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

