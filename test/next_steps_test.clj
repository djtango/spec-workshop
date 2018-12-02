(ns next-steps-test
  (:require [clojure.spec.alpha :as s]
            [clojure.test :refer :all]
            [next-steps :as sut])) ;; subject-under-test

(deftest fizzbuzz
  (testing "conforming 3 5 and 15 should return fizz buzz and fizzbuzz respectively"
    (is (= [:fizz 3] (s/conform ::sut/fizzbuzz 3)))
    (is (= [:buzz 5] (s/conform ::sut/fizzbuzz 5)))
    (is (= [:fizzbuzz 15] (s/conform ::sut/fizzbuzz 15)))))
