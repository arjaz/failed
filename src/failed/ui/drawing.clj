(ns failed.ui.drawing
  (:require
    [failed.keymap :refer [keymap-help]]
    [failed.utils :refer [map2d shear]]
    [lanterna.screen :as s]))


;; The drawing stuff


(defmulti draw-ui
  (fn [ui game screen] (:kind ui)))


(defmethod draw-ui :start [ui game screen]
  (s/put-sheet screen 0 0
               ["Welcome to the Caves of Moon"
                ""
                "Press any key to continue"]))


(defmethod draw-ui :win [ui game screen]
  (s/put-sheet screen 0 0
               ["Congratulations, you've won"
                "Press escape to exit, any key to restart"]))


(defmethod draw-ui :lose [ui game screen]
  (s/put-sheet screen 0 0
               ["You just lost"
                "Press escape to exit, any key to restart"]))


(defn get-viewport-coords
  [game player-location vcols vrows]
  (let [[center-x center-y] player-location
        tiles (:tiles (:world game))

        map-rows (count tiles)
        map-cols (count (first tiles))

        start-x (- center-x (int (/ vcols 2)))
        start-x (max 0 start-x)

        start-y (- center-y (int (/ vrows 2)))
        start-y (max 0 start-y)

        end-x (+ start-x vcols)
        end-x (min end-x map-cols)

        end-y (+ start-y vrows)
        end-y (min end-y map-rows)

        start-x (- end-x vcols)
        start-y (- end-y vrows)]
    [start-x start-y]))


(defn viewport-coords-of
  "Get the viewport coordinates for the given coords adjusted to the origin"
  [origin coords]
  (map - coords origin))


(defn in-viewport?
  [vcols vrows x y]
  (and (< x vcols) (< y vrows)))


(defn draw-entity
  [screen origin vrows vcols entity]
  (let [[x y] (viewport-coords-of origin (:location entity))]
    (when (in-viewport? vcols vrows x y)
      (s/put-string screen x y (:glyph entity) {:fg (:color entity)}))))


(defn highlight-player
  [screen origin vrows vcols player]
  (let [[x y] (viewport-coords-of origin (:location player))]
    (when (in-viewport? vcols vrows x y)
      (s/move-cursor screen x y))))


(defn draw-world
  [screen vrows vcols [ox oy] tiles]
  (letfn [(render-tile
            [tile]
            [(:glyph tile) {:fg (:color tile)}])]
    (let [tiles (shear tiles ox oy vcols vrows)
          sheet (map2d render-tile tiles)]
      (s/put-sheet screen 0 0 sheet))))


(defn draw-log
  [screen game row col size]
  (let [{:keys [entries]} (:log game)]
    (s/put-sheet screen col row (take-last size entries))))


(defn draw-hud
  [screen game]
  (let [log-size (-> game :hud :height)
        log-row (- (second (s/get-size screen))
                   log-size)
        log-col 0]
    (draw-log screen game log-row log-col log-size)))


(defmethod draw-ui :play [_ui game screen]
  (let [world       (:world game)
        tiles       (:tiles world)
        entities    (:entities world)
        player      (:player entities)
        [cols rows] (s/get-size screen)
        vcols       cols
        vrows       (- rows (-> game :hud :height))
        origin      (get-viewport-coords game (:location player) vcols vrows)]
    (draw-world screen vrows vcols origin tiles)
    (doseq [entity (vals entities)]
      (draw-entity screen origin vrows vcols entity))
    (draw-hud screen game)
    (highlight-player screen origin vrows vcols player)))


;; TODO: padding
(defn filler-sheet
  [sheet]
  (let [sheet-width  (count (last (sort-by count sheet)))
        sheet-height (count sheet)]
    (repeat sheet-height (apply str (repeat sheet-width " ")))))


(defn draw-keymap-help
  [screen keymap]
  ;; TODO: extract out and make it columnar
  (letfn [(key->msg [key] (if (keyword? key) (name key) key))
          (kv-pair->msg [[key msg]] (str (key->msg key) ": " (or msg "<No help>") " "))]
    (let [keymap     (keymap-help keymap)
          help-sheet (sort (map kv-pair->msg keymap))
          filler     (filler-sheet help-sheet)]
      (s/put-sheet screen 0 0 filler)
      (s/put-sheet screen 0 0 help-sheet))))


(defmethod draw-ui :keymap-help [_ui game screen]
  (when-let [target-ui (last (butlast (:uis game)))]
    (when-let [keymap (:keymap target-ui)]
      (draw-keymap-help screen keymap))))


(defn draw-event-list
  [screen events max-rows]
  (let [events (take-last max-rows events)
        filler (filler-sheet events)]
    (s/put-sheet screen 0 0 filler)
    (s/put-string screen 0 0 "Event log:")
    (s/put-sheet screen 0 1 (butlast events))))


(defmethod draw-ui :event-list [_ui game screen]
  (let [events (-> game :log :entries)
        rows   (second (s/get-size screen))]
    (draw-event-list screen events (/ rows 2))))


(defn draw-game
  [game screen]
  (s/clear screen)
  (doseq [ui (:uis game)]
    (draw-ui ui game screen))
  (s/redraw screen))
