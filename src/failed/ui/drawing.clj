(ns failed.ui.drawing
  (:require
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


(defn get-viewport-coords-of
  "Get the viewport coordinates for the given coords adjusted to the origin"
  [origin coords]
  (map - coords origin))


(defn draw-entity
  [screen origin entity]
  (let [[x y] (get-viewport-coords-of origin (:location entity))]
    (s/put-string screen x y (:glyph entity) {:fg (:color entity)})))


(defn highlight-player
  [screen origin player]
  (let [[x y] (get-viewport-coords-of origin (:location player))]
    (s/move-cursor screen x y)))


(defn draw-world
  [screen vrows vcols [ox oy] tiles]
  (letfn [(render-tile
            [tile]
            [(:glyph tile) {:fg (:color tile)}])]
    (let [tiles (shear tiles ox oy vcols vrows)
          sheet (map2d render-tile tiles)]
      (s/put-sheet screen 0 0 sheet))))


(defn draw-hud
  [screen game [ox oy]]
  (let [hud-row (dec (second (s/get-size screen)))
        [x y] (get-in game [:world :entities :player :location])
        info (str "loc: [" x "-" y "]")
        info (str info " viewport origin: [" ox "-" oy "]")]
    (s/put-string screen 0 hud-row info)))


(defmethod draw-ui :play [ui game screen]
  (let [world (:world game)
        tiles (:tiles world)
        entities (:entities world)
        player (:player entities)
        [cols rows] (s/get-size screen)
        vcols cols
        vrows (dec rows)
        origin (get-viewport-coords game (:location player) vcols vrows)]
    (draw-world screen vrows vcols origin tiles)
    (doseq [entity (vals entities)]
      (draw-entity screen origin entity))
    (draw-hud screen game origin)
    (highlight-player screen origin player)))


(defn draw-game
  [game screen]
  (s/clear screen)
  (doseq [ui (:uis game)]
    (draw-ui ui game screen))
  (s/redraw screen))
