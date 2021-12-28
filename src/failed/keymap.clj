(ns failed.keymap)


(defn read-keymap
  [keymap input]
  (or (keymap input) identity))


(defn keymap-help
  [keymap]
  (into {}
        (map
          (fn [[key fn]]
            [key (-> fn meta :help)]))
        keymap))
