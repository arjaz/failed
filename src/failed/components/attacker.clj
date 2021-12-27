(ns failed.components.attacker)


(defprotocol Attacker

  (attack
    [this target world]
    "Attack the target")

  (attack-value
    [this world]
    "Get the attack value"))
