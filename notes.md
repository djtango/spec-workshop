Key aspects of clojure.spec

#the basics
qualified keywords
(cljc bug) - aliased qualified keywords are expanded at read-time so if you
have a qualified keyword that is clj or cljs only referring to a spec, compiler
can complain

## Using spec
Spec is less about types and more about describing data and so specs revolve
around *predicates*


```clj
(def greater-than-one? #(> % 1))
(def less-than-six? (partial > 6))
(s/valid? greater-than-one? 5) ;;=> true
(s/valid? less-than-six? 3) ;;=> true

(def between-one-and-six?
  #(and (greater-than-one? %)
        (less-than-six? %)))

(s/valid? between-one-and-six? 0) ;; => false
(s/valid? between-one-and-six? 7) ;; => false
(s/valid? between-one-and-six? 2) ;; => true
```

Anything that behaves like a one-arg function can behave like a spec - it
simply needs to return a truthy value:

```clj
(s/valid? {:a 1} :a) ;;=> true
(s/valid? {:a false} :a) ;;=> false
(s/valid? {:a nil} :a) ;;=> false
(s/valid? (fn [k _] true) :a) ;;=> ArityException Wrong number of args (1) ...
```

## Naming your specs
`s/def` is used to give a name to your spec. `s/def` takes a name and a
predicate / spec:
```clj

(s/def ::non-negative-number #(>= % 0))
```
(names are usually qualified keywords - unqualified keywords are not accepted, but symbols)

You can reuse specs:
```clj
(s/def ::inventory ::non-negative-number)
(s/def ::price ::non-negative-number)
(s/def ::average-rating ::non-negative-number)
(s/def ::number-of-pages ::non-negative-number)
```

## Composing specs
Spec provides facilities for composing your specs:
### `s/and`
```clj
(s/def ::die-roll (s/and number? ;; this is superfluous
                         ::non-negative-number ;; so is this...
                         between-one-and-six?)) ;; examples are hard.
```

###`s/and` vs `and`:
```clj
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
```

### `s/or`
`s/or` expects a name followed by a spec/predicate
```clj
(s/def ::clj-index-of-return-value
  (s/or :found ::non-negative-number
        :not-found nil?))

(s/def ::java.indexOf-return-value
  (s/or :found ::non-negative-number
        :not-found #{-1}))
```

### Exercise!?:
- a spec for a number that is divisible by three
(s/def ::divisible-by-three ())
- a spec for that a value can only be the name of the chess pieces
- word that is longer than 5 letters
- a spec for UK postcodes
- a spec for ISBNs




###dialling it up a notch
`s/conform` ;;fizz buzz
`s/keys`
`s/coll-of`
`s/map-of`
`s/+`
`s/*`
`s/keys`


###using it in prod:
`s/fdef`
`defn` `:pre` and `:post`

;; test check and gen
`s/exercise`
`stest/check`
`stest/instrument`

;; stubbing with instrument

```
(def a {:gowerstreet.forecast-watcher/primary-title-no "asdf" })
(def b {:gowerstreet.coalescer/primary-title-no 1})
(def c {:primary-title-no 2
        :gowerstreet.coalescer/primary-title-no 10})
(def d {:primary-title-no "2"})

(s/def :gowerstreet.forecast-watcher/primary-title-no string?)
(s/def :gowerstreet.coalescer/primary-title-no number?)
(def ::a (s/keys :req [:gowerstreet.forecast-watcher/primary-title-no]))
(def ::b (s/keys :req [:gowerstreet.coalescer/primary-title-no]))
(def ::c (s/keys :req-un [:gowerstreet.coalescer/primary-title-no]
                 :opt [:gowerstreet.coalescer/primary-title-no]))
(def ::d (s/keys :req-un [:gowerstreet.forecast-watcher/primary-title-no]))
```
