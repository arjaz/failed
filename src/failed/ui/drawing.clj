(ns failed.ui.drawing
  (:require
    [failed.ui.core :refer [->UI]]
    [lanterna.screen :as s]))


;; The drawing stuff

(def screen-size [80 24])


(defn clear-screen
  ;; TODO: proper width and height
  [screen]
  (let [[cols rows] screen-size
        blank (apply str (repeat cols \space))]
    (doseq [row (range rows)]
      (s/put-string screen 0 row blank))))


(defmulti draw-ui
  (fn [ui game screen] (:kind ui)))


(defmethod draw-ui :start [ui game screen]
  (s/put-string screen 0 0 "Welcome to the Caves of Moon")
  (s/put-string screen 0 1 "Press enter once in the game to win"))


(defmethod draw-ui :win [ui game screen]
  (s/put-string screen 0 0 "Congratulations, you've won")
  (s/put-string screen 0 1 "Press escape to exit, r to restart"))


(defmethod draw-ui :lose [ui game screen]
  (s/put-string screen 0 0 "You just lost")
  (s/put-string screen 0 1 "Press escape to exit, r to restart"))


(defn get-viewport-coords
  [game vcols vrows]
  (let [location (:location game)
        [center-x center-y] location

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
    [start-x start-y end-x end-y]))


(defn draw-player
  [screen start-x start-y player]
  (let [[player-x player-y] (:location player)
        x (- player-x start-x)
        y (- player-y start-y)]
    (s/put-string screen x y (:glyph player) {:fg :white})
    (s/move-cursor screen x y)))


(defn draw-crosshairs
  [screen vcols vrows]
  (let [crosshair-x (int (/ vcols 2))
        crosshair-y (int (/ vrows 2))]
    (s/put-string screen crosshair-x crosshair-y "X" {:fg :red})
    (s/move-cursor screen crosshair-x crosshair-y)))


(defn draw-world
  [screen vrows vcols start-x start-y end-x end-y tiles]
  (doseq [[vrow-idx mrow-idx] (map vector
                                   (range 0 vrows)
                                   (range start-y end-y))
          :let [row-tiles (subvec (tiles mrow-idx) start-x end-x)]]
    (doseq [vcol-idx (range vcols)
            :let [{:keys [glyph color]} (row-tiles vcol-idx)]]
      (s/put-string screen vcol-idx vrow-idx glyph {:fg color}))))


(defn draw-hud
  [screen game start-x start-y]
  (let [hud-row (dec (second screen-size))
        [x y] (get-in game [:world :player :location])
        info (str "loc: [" x "-" y "]")
        info (str info " start: [" start-x "-" start-y "]")]
    (s/put-string screen 0 hud-row info)))


(defmethod draw-ui :play [ui game screen]
  (let [world (:world game)
        {:keys [tiles player]} world
        [cols rows] screen-size
        vcols cols
        vrows (dec rows)
        [start-x start-y end-x end-y] (get-viewport-coords game (:location player) vcols vrows)]
    (draw-world screen vrows vcols start-x start-y end-x end-y tiles)
    (draw-player screen start-x start-y player)))


(defn draw-game
  [game screen]
  (clear-screen screen)
  (doseq [ui (:uis game)]
    (draw-ui ui game screen))
  (s/redraw screen))
