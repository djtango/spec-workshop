(ns spec-basics-test
  (:require [clojure.spec.alpha :as s]
            [clojure.test :refer :all]
            [spec-basics :as sut])) ;; subject-under-test

(deftest numbers-divisible-by-three
  (testing "these numbers should be divisible-by-three"
    (is (s/valid? ::sut/divisible-by-three 3))
    (is (s/valid? ::sut/divisible-by-three 6))
    (is (s/valid? ::sut/divisible-by-three 9))
    (is (s/valid? ::sut/divisible-by-three 99)))
  (testing "these numbers should not be divisible-by-three"
    (is (not (s/valid? ::sut/divisible-by-three 1)))
    (is (not (s/valid? ::sut/divisible-by-three 2)))
    (is (not (s/valid? ::sut/divisible-by-three 4)))
    (is (not (s/valid? ::sut/divisible-by-three 5)))
    (is (not (s/valid? ::sut/divisible-by-three 100)))))
