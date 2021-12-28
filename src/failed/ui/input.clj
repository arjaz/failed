(ns failed.ui.input
  (:require
    [failed.entities.bunny :refer [make-bunny]]
    [failed.entities.lichen :refer [make-lichen]]
    [failed.entities.player :refer [make-player move-player]]
    [failed.keymap :refer [read-keymap]]
    [failed.log :refer [with-log]]
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


(defn with-help
  [help fn]
  (vary-meta fn assoc :help help))


(def play-keymap
  {:enter  (with-help "Win"
             ;; TODO: keymap
             (fn [game] (assoc game :uis [(->UI :win nil)])))
   :escape (with-help "Lose"
             ;; TODO: keymap
             (fn [game] (assoc game :uis [(->UI :lose nil)])))
   \q      (with-help "Quit"
             (fn [game] (assoc game :uis [])))
   \?      (with-help "Available actions"
             (fn [game]
               (-> game
                   ;; TODO: keymap
                   (update :uis conj (->UI :keymap-help nil))
                   (assoc :pause? true))))
   \a      (with-help "Event log"
             (fn [game]
               (-> game
                   ;; TODO: keymap
                   (update :uis conj (->UI :event-list nil))
                   (assoc :pause? true))))
   :up     (with-help "Move/attack/dig up"
             (with-log "Moved north"
               (fn [game] (update game :world move-player :n))))
   :left   (with-help "Move/attack/dig left"
             (with-log "Moved west"
               (fn [game] (update game :world move-player :w))))
   :right  (with-help "Move/attack/dig right"
             (with-log "Moved east"
               (fn [game] (update game :world move-player :e))))
   :down   (with-help "Move/attack/dig down"
             (with-log "Moved south"
               (fn [game] (update game :world move-player :s))))})


(defn reset-game
  [game]
  (let [fresh-world (random-world)]
    (-> game
        (assoc :world fresh-world)
        (update :world populate-world)
        ;; TODO: keymap
        (assoc :uis [(->UI :play play-keymap)])
        (assoc-in [:log :entries] []))))


(defmethod process-input :start [game _input]
  (reset-game game))


(defmethod process-input :play [game input]
  ((read-keymap play-keymap input) game))


(defmethod process-input :keymap-help [game input]
  (-> game
      (update :uis pop)
      (assoc :pause? true)))


(defmethod process-input :event-list [game input]
  (-> game
      (update :uis pop)
      (assoc :pause? true)))


(defmethod process-input :win [game input]
  (if (= input :escape)
    (assoc game :uis [])
    ;; TODO: keymap
    (assoc game :uis [(->UI :start nil)])))


(defmethod process-input :lose [game input]
  (if (= input :escape)
    (assoc game :uis [])
    ;; TODO: keymap
    (assoc game :uis [(->UI :start nil)])))


(defn get-input
  [game screen]
  (assoc game :input (s/get-key-blocking screen)))
