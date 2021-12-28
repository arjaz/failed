(ns failed.ui.input
  (:require
    [failed.entities.bunny :refer [make-bunny]]
    [failed.entities.fungus :refer [make-fungus]]
    [failed.entities.player :refer [make-player move-player]]
    [failed.log :refer [log-event]]
    [failed.ui.core :refer [->UI]]
    [failed.ui.keymap :refer [read-keymap]]
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
        (add-creatures make-fungus 30)
        (add-creatures make-bunny 20))))


(defmacro defaction
  [name help args & body]
  `(do (defn ~name ~help ~args ~@body)
       (def ~name (vary-meta ~name assoc :help ~help))))


;; TODO: keymap
(defaction win-screen-action
  "Win"
  [game]
  (assoc game :uis [(->UI :win nil)]))


;; TODO: keymap
(defaction lose-screen-action
  "Lose"
  [game]
  (assoc game :uis [(->UI :lose nil)]))


(defaction quit-game-action
  "Quit"
  [game]
  (assoc game :uis []))


(defaction list-actions-action
  "Available actions"
  [game]
  (-> game
      ;; TODO: keymap
      (update :uis conj (->UI :keymap-help nil))
      (assoc :pause? true)))


(defaction list-events-action
  "Event log"
  [game]
  (-> game
      ;; TODO: keymap
      (update :uis conj (->UI :event-list nil))
      (assoc :pause? true)))


(defaction move-player-north-action
  "Move/attack/dig up"
  [game]
  ;; TODO: move logging inside
  (-> game
      (update :world move-player :n)
      (log-event "Moved north")))


(defaction move-player-west-action
  "Move/attack/dig left"
  [game]
  ;; TODO: move logging inside
  (-> game
      (update :world move-player :w)
      (log-event "Moved west")))


(defaction move-player-east-action
  "Move/attack/dig right"
  [game]
  ;; TODO: move logging inside
  (-> game
      (update :world move-player :e)
      (log-event "Moved east")))


(defaction move-player-south-action
  "Move/attack/dig down"
  [game]
  ;; TODO: move logging inside
  (-> game
      (update :world move-player :s)
      (log-event "Moved south")))


(def play-keymap
  {:enter  win-screen-action
   :escape lose-screen-action
   \q      quit-game-action
   \?      list-actions-action
   \a      list-events-action
   :up     move-player-north-action
   :left   move-player-west-action
   :right  move-player-east-action
   :down   move-player-south-action})


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
