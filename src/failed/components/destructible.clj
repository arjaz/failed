(ns failed.components.destructible)


(defprotocol Destructible

  (take-damage
    [this damage world]
    "Take the given amount of damage")

  (defense-value
    [this world]
    "Get the defence value"))
