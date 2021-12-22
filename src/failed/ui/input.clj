(ns failed.ui.input
  (:require
    [failed.entities.player :refer [make-player move-player]]
    [failed.ui.core :refer [->UI]]
    [failed.world :refer [random-world smooth-world]]
    [lanterna.screen :as s]))


(defmulti process-input
  (fn [game input]
    (-> game :uis last :kind)))


(defn reset-game
  [game]
  (let [fresh-world (random-world)]
    (-> game
        (assoc :world fresh-world)
        (assoc-in [:world :player] (make-player fresh-world))
        (assoc :uis [(->UI :play)]))))


(defmethod process-input :start [game input]
  (reset-game game))


(defn move
  [[x y] [dx dy]]
  [(+ x dx) (+ y dy)])


(defmethod process-input :play [game input]
  ;; That should be a data structure
  (case input
    :enter  (assoc game :uis [(->UI :win)])
    :escape (assoc game :uis [(->UI :lose)])
    \q      (assoc game :uis [])

    \s (assoc game :world (smooth-world (:world game)))

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
