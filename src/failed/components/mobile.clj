(ns failed.components.mobile)


(defprotocol Mobile

  (move
    [this dest world]
    "Move this entity to a new location.")

  (can-move?
    [this dest world]
    "Return whether the entity can move to the new location."))
