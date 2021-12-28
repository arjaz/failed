(ns failed.components.named)


(defprotocol Named

  (ask-name
    [this]
    "Get the name"))
