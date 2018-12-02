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

### Exercises
- a spec for a number that is divisible by three
- a spec for that a value can only be the name of the chess pieces
- single words that are longer than 5 letters
- a spec for UK postcodes

## Next Steps
Spec provides `s/conform` as a means of annotating and destructuring your data:
```clj
(s/def ::number-or-string
  (s/or :number number?
        :string string?))

(defn extract-number [number-or-string]
  (let [conformed (s/conform ::number-or-string number-or-string)]
    (if (= ::s/invalid conformed)
      :not-a-number-or-string!
      (let [[tag value] conformed]
        (case tag
          :number value
          :string (Float/parseFloat value))))))
```
`s/conform` appears in various points...

###Collections
####`s/keys`
The map is the bread and butter of clojure - `s/keys` is about describing map
requirements. Contrary to many other common typing patterns or validation
libraries, spec requires you to describe your entities independently from the
structure of the map attributes.

This is an opinionated design decision and is meant to encourage code reuse of
common project entities and the use of map subsets or supersets.
Usage:

```clj
(s/def ::name string?)
(s/def ::age pos?)
(s/def ::breed #{:corgi :pug :poodle :german-shepherd :golden-retriever :labrador})
(s/def ::dog (s/keys :req [::name ::age ::breed ::colour]))
(s/def ::colour #{:black :brown :white :golden})
(s/valid? ::dog {::name "Fenton" ::age 1 ::breed :labrador ::colour :black})
```

`s/keys` supports specifying optional fields:
```clj

(s/def ::favourite-meal #{:shoes :chicken :homework})
(s/def ::dog (s/keys :req [::name ::age ::breed ::color] :opt [::favourite-meal]))
```
A lot of clojure maps in the wild still use unqualified keywords - `s/keys`

allows you to specify unqualified keywords:
```clj
;;https://api.slack.com/web#responses
(s/def ::ok boolean?)
(s/def ::error string?)
(s/def ::warning string?)
(s/def ::slack-web-api-response (s/keys :req-un [::ok] :opt-un [::error ::warning]))
(s/valid? ::slack-web)
```

`s/keys` will check the value assigned to any key against a registered spec of
the same name where possible.

`s/coll-of` is used to describe homogenous collections:
```clj
(s/valid? (s/coll-of number?) [1 2 3])
(s/valid? (s/coll-of odd?) [1 3 5])
```

`s/coll-of` allows you to supply options to give additional details:
```clj
(s/def ::board-position #{:O :X :empty})
(s/def ::tic-tac-toe-row (s/coll-of ::board-position :count 3))
(s/valid? ::tic-tac-toe-row [:O :X :O]) ;; => true
```

`s/map-of` can be used to specify maps of arbitrary size but predictable k-v
pairings:

```clj
(s/def ::user-id uuid?)
(s/def ::user (s/keys :req [::name ::user-id]))
(s/valid? (s/map-of ::user-id ::user) {(java.util.UUID/randomUUID) {::name "foo" ::user-id (java.util.UUID/randomUUID)}})
```

`s/conform` ;;fizz buzz


### Regex ops
spec provides regex expressions for describing structure in a sequence of data.
These ops borrow from regex:
- `s/*`: 0 or more occurences
- `s/+`: 1 or more occurences
- `s/?`: 0 or 1 occurence
```clj
(s/valid? (s/* string?) ["a" "b"]) ;; => true
(s/valid? (s/* string?) []) ;; => true
(s/valid? (s/+ string?) []) ;; => false
(s/valid? (s/+ string?) ["a"]) ;; => true
(s/valid? (s/? string?) ["a"]) ;; => true
(s/valid? (s/? string?) ["a" "b"]) ;; => false
(s/valid? (s/? string?) []) ;; => true
```

In addition there is `s/tuple`, `s/cat` and `s/alt`
`s/tuple` allows you to specify ordered structure in a sequence:
```clj
(s/def ::datom (s/tuple number? keyword? any? number? boolean?))
```

`s/cat` allows you to give names to the elements:
```clj
(s/def ::reframe-event (s/cat :event-name keyword?
                              :arg any?))
(s/valid? ::reframe-event [:add-todo "write more specs"]) ;;=> true
```
What is special about `s/cat` is that conforming `s/cat` returns a map with the
named positional elements:
```clj
(s/conform ::reframe-event [:add-todo "write more specs"]) ;; => {:event-name :add-todo :arg "write more specs"}
```

### Exercises
- using `s/or` and conform, set up a spec `::fizzbuzz` which will conform any
  input with its fizzbuzz outcome
- in the same namespace define specs for which these are valid:
```clj
(def map-1 {:number/a 1})
(def map-2 {:string/a "1"})
(def map-3 {:number/a 1, :b "2"})
(def map-4 {:a "1", :number/b 2})
(def map-5 {:a "1"}) ;; using the same spec that worked for map-4

(s/valid? ::map-1 map-1) ;;=> true
```
- define a ::normalized-vector spec that describes a vector of fractional
  numbers that sums up to one
- using `s/cat` define a seq of command-line option flags to value pairs:
```clj
(s/valid? ::cli-option-pairs ["-server" "foo" "-verbose" true "-user" "joe"])
```
- spec a CSV-like input [1 "foo" "2018-01-01"] using `s/coll-of` and `s/cat`
- what happens when you use `s/*` or `s/+` instead of `s/coll-of`, and why?
- spec a ring-like HTTP req object


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
