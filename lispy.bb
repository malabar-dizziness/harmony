#!/usr/bin/env bb

(defn print-number-and-wait
  [i]
  (println i " yellow is my color")
  (Thread/sleep 1000))

(def f
  (future
    (run! (var print-number-and-wait)
          (range))))

(future-cancel f)
