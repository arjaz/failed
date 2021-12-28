(ns failed.entities.bunny
  (:require
    [failed.components.destructible :refer [Destructible]]
    [failed.components.mobile :refer [Mobile move can-move?]]
    [failed.components.named :refer [Named]]
    [failed.entities.core :refer [Entity get-id]]
    [failed.world :refer [empty-tile? find-empty-neighbor]]))


(defrecord Bunny
  [id glyph color location hp])


(extend-type Bunny
  Named
  (ask-name [this]
    "A bunny"))


(extend-type Bunny
  Mobile
  (move [this dest world]
    {:pre [(can-move? this dest world)]}
    (assoc-in world [:entities (:id this) :location] dest))
  (can-move? [this dest world]
    (empty-tile? world dest)))


(extend-type Bunny
  Destructible
  (take-damage [this damage world]
    (let [bunny (update this :hp - damage)]
      (if-not (pos? (:hp bunny))
        (update world :entities dissoc (:id this))
        (update-in world [:entities (:id this)] assoc bunny)))))


(extend-type Bunny
  Entity
  (tick [this world]
    (if-let [target (find-empty-neighbor world (:location this))]
      (move this target world)
      world)))


(defn make-bunny
  [location]
  (->Bunny (get-id) "v" :yellow location 1))
