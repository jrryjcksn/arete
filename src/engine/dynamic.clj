(ns engine.dynamic
  (:require [engine.core :refer :all]
            [clojure.edn :as edn]))

(defrule rule-definer
  [?rule ::rule]
  =>
  (eval (edn/read-string
         (str "(engine.core/defrule " (symbol "engine.dynamic" (name (:name ?rule))) " "
              {:priority (or (:priority ?rule) 0)} " "
              (:definition ?rule)
              ")")))
  (insert! {:type ::__defined-rule :name (symbol "engine.dynamic" (:name ?rule))})
  (update-with-rule (:__id ?rule) [(last (:engine.dynamic @engine.runtime/rulesets))]))

(defrule rule-undefiner
  [?leftover ::__defined-rule]
  [:not [?rule ::rule (= (:name ?leftover) (:name ?rule))]]
  =>
  (remove! ?leftover)
                                        ; (undefine-rule (:name ?rule))
  )
