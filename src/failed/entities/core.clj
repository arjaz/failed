(ns failed.entities.core)

(defonce ids (atom 0))


(defprotocol Entity

  (tick [this world]))


(defn get-id
  []
  (let [id @ids]
    (swap! ids inc)
    id))

