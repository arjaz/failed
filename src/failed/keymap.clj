(ns failed.keymap)


(defn read-keymap
  [keymap input]
  (or (keymap input) identity))


(defn keymap-help
  [keymap]
  (into {}
        (map
          (fn [[key fun]]
            [key (-> fun meta :help)]))
        keymap))


(defn with-help
  [help fn]
  (vary-meta fn assoc :help help))
