(ns spec-basics
  (:require [clojure.spec.alpha :as s]))

(def greater-than-one? #(> % 1))
(def less-than-six? (partial > 6))
(s/valid? greater-than-one? 5) ;;=> true
(s/valid? less-than-six? 3) ;;=> true

(def between-one-and-six?
  #(and (greater-than-one? %)
        (less-than-six? %)))

(comment
 (s/valid? between-one-and-six? 0) ;; => false
 (s/valid? between-one-and-six? 7) ;; => false
 (s/valid? between-one-and-six? 2) ;; => true

 (s/valid? {:a 1} :a) ;;=> true
 (s/valid? {:a false} :a) ;;=> false
 (s/valid? {:a nil} :a) ;;=> false
 (s/valid? (fn [k _] true) :a) ;;=> ArityException Wrong number of args (1) ...
 )

(s/def ::non-negative-number #(>= % 0))

(s/def ::inventory ::non-negative-number)
(s/def ::dollar-value ::non-negative-number)
(s/def ::average-rating ::non-negative-number)
(s/def ::number-of-pages ::non-negative-number)

(s/def ::die-roll (s/and number? ;; this is superfluous
                         ::non-negative-number ;; so is this...
                         between-one-and-six?)) ;; examples are hard.

(s/explain ::die-roll "1")
;; => val: "1" fails spec: :spec-basics/die-roll predicate: number?
(s/explain ::die-roll -1)
;; => val: -1 fails spec: :spec-basics/non-negative-number predicate: (>= % 0)
(s/explain ::die-roll 7)
;; => val: 7 fails spec: :spec-basics/die-roll predicate: between-one-and-six?
(s/def ::die-roll-2
  #(and (number? %)
        (s/valid? ::non-negative-number %) ;; keywords by themselves are not specs
        (between-one-and-six? %)))
(s/explain ::die-roll-2 7)
;; => val: 7 fails spec: :spec-basics/die-roll-2 predicate: (and (number? %) (valid? :spec-basics/non-negative-number %) (between-one-and-six? %))

