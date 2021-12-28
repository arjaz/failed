(ns failed.entities.fungus
  (:require
    [failed.components.destructible :refer [Destructible]]
    [failed.components.named :refer [Named]]
    [failed.entities.core :refer [Entity get-id]]
    [failed.world :refer [find-empty-neighbor]]))


(defrecord Fungus
  [id glyph color location hp])


(defn make-fungus
  [location]
  (->Fungus (get-id) "F" :white location 1))


(extend-type Fungus
  Named
  (ask-name [this]
    "A mushroom"))


(defn- grow?
  [fungus]
  (and
    (not (:grown? fungus))
    (< (rand) 0.05)))


(defn- grow
  [fungus world]
  (if-let [target (find-empty-neighbor world (:location fungus))]
    (let [grown (assoc (make-fungus target) :grown? true)]
      (assoc-in world [:entities (:id grown)] grown))
    world))


(extend-type Fungus
  Entity
  (tick [this world]
    (if (grow? this)
      (grow this world)
      world)))


(extend-type Fungus
  Destructible
  (take-damage [this damage world]
    (let [fungus (update this :hp - damage)]
      (if-not (pos? (:hp fungus))
        (update world :entities dissoc (:id this))
        (update-in world [:entities (:id this)] assoc fungus)))))
