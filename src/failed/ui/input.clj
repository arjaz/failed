(ns failed.ui.input
  (:require
    [failed.entities.bunny :refer [make-bunny]]
    [failed.entities.lichen :refer [make-lichen]]
    [failed.entities.player :refer [make-player move-player]]
    [failed.ui.core :refer [->UI]]
    [failed.world :refer [random-world smooth-world find-empty-tile]]
    [lanterna.screen :as s]))


(defmulti process-input
  (fn [game input]
    (-> game :uis last :kind)))


(defn add-creature
  [world make-creature]
  (let [creature (make-creature (find-empty-tile world))]
    (assoc-in world [:entities (:id creature)] creature)))


(defn add-creatures
  [world make-creature n]
  (nth (iterate #(add-creature % make-creature) world)
       n))


(defn populate-world
  [world]
  (let [world (assoc-in world [:entities :player]
                        (make-player (find-empty-tile world)))]
    (-> world
        (add-creatures make-lichen 30)
        (add-creatures make-bunny 20))))


(defn reset-game
  [game]
  (let [fresh-world (random-world)]
    (-> game
        (assoc :world fresh-world)
        (update :world populate-world)
        (assoc :uis [(->UI :play)]))))


(defmethod process-input :start [game input]
  (reset-game game))


(defmethod process-input :play [game input]
  ;; That should be a data structure
  (case input
    :enter  (assoc game :uis [(->UI :win)])
    :escape (assoc game :uis [(->UI :lose)])
    \q      (assoc game :uis [])

    :up    (update game :world move-player :n)
    :left  (update game :world move-player :w)
    :right (update game :world move-player :e)
    :down  (update game :world move-player :s)

    game))


(defmethod process-input :win [game input]
  (if (= input :escape)
    (assoc game :uis [])
    (assoc game :uis [(->UI :start)])))


(defmethod process-input :lose [game input]
  (if (= input :escape)
    (assoc game :uis [])
    (assoc game :uis [(->UI :start)])))


(defn get-input
  [game screen]
  (assoc game :input (s/get-key-blocking screen)))
