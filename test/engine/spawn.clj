(ns engine.spawn
  (:require [engine.core :refer :all]))

(def values (atom []))

(defn tally [inst res]
  (remove! inst)
  (remove! res)
  (insert! (assoc res :value (swap! values conj (:value inst)))))

(defrule rule1
  [?x :x]
  [?y :y]
  [?result :result]
  =>
  (tally ?x ?result))
