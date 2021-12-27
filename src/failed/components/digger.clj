(ns failed.components.digger)


(defprotocol Digger

  (dig
    [this dest world]
    "Dig a location")

  (can-dig?
    [this dest world]
    "Can the entity dig that destination"))
