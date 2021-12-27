(ns failed.utils)


(defn map2d
  "Map a function across a two-dimensional sequence."
  [f s]
  (map (partial map f) s))


(defn slice
  "Slice a sequence."
  [s start num]
  (->> s
       (drop start)
       (take num)))


(defn shear
  "Shear a two-dimensional sequence, returning a smaller one."
  [s col row width height]
  (map #(slice % col width)
       (slice s row height)))
