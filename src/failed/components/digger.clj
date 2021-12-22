(ns failed.components.digger)


(defprotocol Digger

  (dig
    [this world dest]
    "Dig a location")

  (can-dig?
    [this world dest]
    "Can the entity dig that destination"))
