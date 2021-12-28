(ns failed.entities.player
  (:require
    [failed.components.attacker :refer [Attacker attack]]
    [failed.components.destructible :refer [Destructible take-damage]]
    [failed.components.digger :refer [Digger dig can-dig?]]
    [failed.components.mobile :refer [Mobile move can-move?]]
    [failed.components.named :refer [Named]]
    [failed.coords :refer [destination-coords]]
    [failed.entities.core :refer [Entity]]
    [failed.world :refer [get-entity-at empty-tile? get-tile-kind set-tile-floor]]))


(defrecord Player
  [id glyph color location])


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
  (move [this dest world]
    {:pre [(can-move? this dest world)]}
    (assoc-in world [:entities :player :location] dest))
  (can-move? [this dest world]
    (empty-tile? world dest)))


(extend-type Player
  Digger
  (dig [this dest world]
    {:pre [(can-dig? this dest world)]}
    (set-tile-floor world dest))
  (can-dig? [this dest world]
    (check-tile world dest #{:wall})))


(extend-type Player
  Attacker
  (attack [this target world]
    {:pre [(satisfies? Destructible target)]}
    (let [damage 1]
      (take-damage target damage world))))


(extend-type Player
  Named
  (ask-name [this]
    "Player"))


(defn make-player
  [location]
  (->Player :player "@" :white location))


(defn move-player
  [world dir]
  (let [player        (:player (:entities world))
        target        (destination-coords (:location player) dir)
        entity-target (get-entity-at world target)]
    (cond
      (some? entity-target)           (attack player entity-target world)
      (can-move? player target world) (move player target world)
      (can-dig? player target world)  (dig player target world)
      :else                           world)))
