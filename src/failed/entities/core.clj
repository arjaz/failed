(ns failed.entities.core)


(defprotocol Entity

  (tick [this world]))
