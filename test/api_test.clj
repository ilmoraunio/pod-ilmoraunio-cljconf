(ns api-test
  (:require [babashka.pods :as pods]
            [clojure.test :refer [deftest is testing]]))

(pods/load-pod "./pod-conftest-clj")
(require '[pod-conftest-clj.api :as api])

(deftest parse-test
  (testing "parse smoke test"
    (is (= {"test.json" {"hello" [1.0 2.0 4.0]}}
           (api/parse "test.json")))))
