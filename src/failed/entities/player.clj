(ns failed.entities.player
  (:require
    [failed.components.digger :refer [Digger dig can-dig?]]
    [failed.components.mobile :refer [Mobile move can-move?]]
    [failed.coords :refer [destination-coords]]
    [failed.entities.core :refer [Entity]]
    [failed.world :refer [find-empty-tile get-tile-kind set-tile-floor]]))


(defrecord Player
  [id glyph location])


(defn check-tile
  "Check that the tile at the destination passes the given predicate."
  [world dest predicate]
  (predicate (get-tile-kind world dest)))


(extend-type Player
  Entity
  (tick [this world]
    world))


(extend-type Player
  Mobile
  (move [this world dest]
    {:pre [(can-move? this world dest)]}
    (assoc-in world [:player :location] dest))
  (can-move? [this world dest]
    (check-tile world dest #{:floor})))


(extend-type Player
  Digger
  (dig [this world dest]
    {:pre [(can-dig? this world dest)]}
    (set-tile-floor world dest))
  (can-dig? [this world dest]
    (check-tile world dest #{:wall})))


(defn make-player
  [world]
  (->Player :player "@" (find-empty-tile world)))


(defn move-player
  [world dir]
  (let [player (:player world)
        target (destination-coords (:location player) dir)]
    (cond
      (can-move? player target world) (move player world target)
      (can-dig? player target world)  (dig player world target)
      :else                           world)))
