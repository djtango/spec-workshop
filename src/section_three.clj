(ns section-three
  (:require [clojure.spec.alpha :as s]))

(s/def ::reverse-args (s/cat :coll coll?)) ;; by convention the name in your `s/cat` matches your function argument names

(s/def ::reverse-ret coll?)

(s/def ::reverse-fn
  (fn [{:keys [args ret]}]
    (let [input-coll (:coll args)]
      (= input-coll
         (reverse ret)))))

(s/fdef reverse
  :args ::reverse-args
  :ret ::reverse-ret
  :fn ::reverse-fn)
