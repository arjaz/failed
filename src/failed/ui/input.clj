(ns failed.ui.input
  (:require
    [failed.entities.bunny :refer [make-bunny]]
    [failed.entities.lichen :refer [make-lichen]]
    [failed.entities.player :refer [make-player move-player]]
    [failed.log :refer [log-event-after]]
    [failed.ui.core :refer [->UI]]
    [failed.world :refer [random-world find-empty-tile]]
    [lanterna.screen :as s]))


(defmulti process-input
  (fn [game _input]
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
        (assoc :uis [(->UI :play)])
        (assoc-in [:log :entries] []))))


(defmethod process-input :start [game _input]
  (reset-game game))


(defn read-keymap
  [keymap input]
  (get keymap input identity))


(def play-keymap
  {:enter  (fn [game] (assoc game :uis [(->UI :win)]))
   :escape (fn [game] (assoc game :uis [(->UI :lose)]))
   \q      (fn [game] (assoc game :uis []))
   :up     (log-event-after (fn [game] (update game :world move-player :n))
                            "Moved north")
   :left   (log-event-after (fn [game] (update game :world move-player :w))
                            "Moved west")
   :right  (log-event-after (fn [game] (update game :world move-player :e))
                            "Moved east")
   :down   (log-event-after (fn [game] (update game :world move-player :s))
                            "Moved south")})


(defmethod process-input :play [game input]
  ((read-keymap play-keymap input) game))


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
