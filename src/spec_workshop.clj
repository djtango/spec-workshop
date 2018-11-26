(ns spec-workshop
  (:require [clojure.spec.alpha :as s]
            [clojure.spec.test.alpha :as stest]))

(defn reload! []
  (require '[spec-workshop] :reload))

(def tasty-chilli-burgers
  {:serves 4
   :time-to-cook "20 minutes"
   :ingredients [{:name "lean minced beef"
                  :quantity {:amount 900
                             :units :grams}}
                 {:name "soy sauce"
                  :quantity {:amount 1
                             :units :tbsp}}
                 {:name "worcestershire sauce"
                  :quantity {:amount 1
                             :units :tbsp}}
                 {:name "red chilli"
                  :quantity {:amount 1
                             :units :unit}
                  :notes "deseeded and finely diced"}
                 {:name "black pepper"
                  :notes "freshly ground"}
                 {:name "parsley"
                  :quantity {:amount 0.5
                             :units :bunch}
                  :notes "leaves chopped finely"}
                 {:name "feta"
                  :quantity {:amount 150
                             :units :grams}}
                 {:name "egg"
                  :quantity {:amount 1
                             :units :unit}}
                 {:name "olive oil"}
                 {:name "crusty bread buns"
                  :quantity {:amount 4}}
                 {:name "little gem lettuce"
                  :quantity {:amount 1
                             :units :unit}}
                 {:name "beef tomato"
                  :quantity {:amount 4
                             :unit :slices}}]
   :instructions [{:text "place the minced beef soy and worcestershire sauces, chilli
                         pepper and parsley in a bowl and mix with your hands until
                         evenly blended. Add the feta and mix it in, then add the egg
                         to bind. Divide the mixture into 4 and shape each portion
                         into burgers."}
                  {:text "heat the griddle pan or frying pan, brush it with
                         olive oil, then cook the burgers over a medium heat
                         for 3-4 minutes each side for rare, 5 for medium and 6
                         for well-done meat (if you must)."}
                  {:text "serve the burgers in buns with lettuce and sliced tomato."}]})

;; the basics

;; specs are predicates

(println "a text name should be valid"
         (s/valid? string? "lean beef mince"))

(println "a number should not be a valid string"
         (s/valid? string? 1))
(s/def :ingredient/name string?)

(s/def ::amount number?)
;; exercise can be used to generate candidate data
;; (s/exercise ::amount 10)
;;=> ([-1.0 -1.0] [-1 -1] [-1.5 -1.5] [2.0 2.0] [0 0] [1.0 1.0] [2.0 2.0] [-2.0 -2.0] [10 10] [1 1])

;; predicates can be composed
(s/def ::amount (s/and number?
                       #(>= % 0)))
;; (s/explain ::amount -1)
;;=> val: -1 fails spec: :spec-workshop/amount predicate: (>= % 0)
;;(println (s/exercise ::amount 10))
;; ([0 0] [3.0 3.0] [1.5 1.5] [1.9375 1.9375] [5 5] [0 0] [2.34375 2.34375]
;; [1.25 1.25] [0.595703125 0.595703125] [3.99609375 3.99609375])

(s/def :ingredient/notes string?)

;; v1
;; (s/def ::units #{:tbsp :unit :grams :slices :bunch})
(s/def :ingredient/quantity (s/keys :req [::amount ::units]))
(s/def ::ingredient (s/keys :req [:ingredient/name]
                            :opt [:ingredient/quantity
                                  :ingredient/notes]))


(s/def ::ingredients (s/coll-of ::ingredient :min-count 1))

(s/def ::text string?)
(s/def ::instruction (s/keys :req [::text]))
(s/def ::recipe (s/keys :req [::ingredients
                              ::instructions
                              ::serves
                              ::time-to-cook]))


(s/def ::units #{:tbsp :unit :grams :slices :bunch :ml :tsp :cup :lbs})

;; refactor specs
(s/def ::positive-number (s/and number? #(>= % 0)))
(s/def ::conversion-factor ::positive-number)
(s/def ::conversions
  (s/map-of (s/cat :from ::units
                   :to   ::units)
            ::conversion-factor))
(def conversions
  {[:tbsp :ml]    17.582
   [:tsp  :ml]    5.91939
   [:cup  :ml]    284.131
   [:lbs  :grams] 453.592})

(def reverse-conversions
  (->> conversions
       (map (fn [[conversion factor]]
              [(vec (reverse conversion)),
               (/ 1 factor)]))
       (into {})))

;; v1
;; (s/fdef convert
;;         :args (s/cat :from ::units
;;                      :to   ::units)
;;         :ret ::conversion-factor)
(defn convert [from to]
  (let [symmetric-conversions (merge conversions reverse-conversions)]
    (get symmetric-conversions [from to])))

;; (println (stest/check `convert))
;; whoops forgot nil
;; explodes
(s/fdef convert
        :args (s/cat :from ::units
                     :to   ::units)
        :ret (s/nilable ::conversion-factor))

(assert (:result (:clojure.spec.test.check/ret (first (stest/check `convert)))))

(defn convert-quantity [from-quantity to-new-units]
  (let [{:keys [units amount]} from-quantity]
    (if-let [conversion-factor (convert units to-new-units)]
      (* conversion-factor amount))))


(defn scale [ingredients factor])
