(ns failed.log)


(defn- conj-log
  [entries msg log-size]
  (let [cut-log (if (>= (count entries) log-size)
                  #(subvec % 1)
                  identity)]
    (cut-log (conj entries msg))))


(defn log-event
  [game msg]
  (update-in game [:log :entries] conj-log msg (-> game :log :max-size)))


(defn log-event-after
  [game-fn msg]
  (fn [game]
    (-> game game-fn (log-event msg))))
