(ns failed.entities.lichen
  (:require
    [failed.components.destructible :refer [Destructible take-damage]]
    [failed.entities.core :refer [Entity get-id]]
    [failed.world :refer [find-empty-neighbor]]))


(defrecord Lichen
  [id glyph color location hp])


(defn make-lichen
  [location]
  (->Lichen (get-id) "F" :white location 1))


(defn grow?
  [lichen]
  (and
    (not (:grown? lichen))
    (< (rand) 0.05)))


(defn grow
  [lichen world]
  (if-let [target (find-empty-neighbor world (:location lichen))]
    (let [grown (assoc (make-lichen target) :grown? true)]
      (assoc-in world [:entities (:id grown)] grown))
    world))


(extend-type Lichen
  Entity
  (tick [this world]
    (if (grow? this)
      (grow this world)
      world)))


(extend-type Lichen
  Destructible
  (take-damage [this damage world]
    (let [lichen (update this :hp - damage)]
      (if-not (pos? (:hp lichen))
        (update world :entities dissoc (:id this))
        (update-in world [:entities (:id this)] assoc lichen)))))
