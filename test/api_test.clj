(ns api-test
  (:require [babashka.pods :as pods]
            [clojure.test :refer [deftest is testing]]))

(pods/load-pod "./pod-ilmoraunio-conftest-clj")
(require '[pod-ilmoraunio-conftest-clj.api :as api])

(deftest parse-test
  (testing "parse"
    (is (= {"test-resources/test.json" {:hello [1 2 4]},
            "test-resources/test.edn" {:foo :bar
                                       :duration "#duration 20m"},
            "test-resources/test.yaml" {"apiVersion" "v1",
                                        "kind" "Service",
                                        "metadata" {"name" "hello-kubernetes"},
                                        "spec" {"type" "LoadBalancer",
                                                "ports" [{"port" 80.0, "targetPort" 8080.0}],
                                                "selector" {"app" "hello-kubernetes"}}}
            "test-resources/.dockerignore" [[{"Original" ".idea", "Kind" "Path", "Value" ".idea"}
                                             {"Value" "", "Original" "", "Kind" "Empty"}]]}
           (api/parse "test-resources/**"))))
  (testing "parse-go"
    (is (= {"test-resources/test.yaml" {"apiVersion" "v1",
                                        "kind" "Service",
                                        "metadata" {"name" "hello-kubernetes"},
                                        "spec" {"ports" [{"port" 80.0, "targetPort" 8080.0}],
                                                "selector" {"app" "hello-kubernetes"},
                                                "type" "LoadBalancer"}},
            "test-resources/test.json" {"hello" [1.0 2.0 4.0]},
            "test-resources/.dockerignore" [[{"Kind" "Path", "Value" ".idea", "Original" ".idea"}
                                             {"Original" "", "Kind" "Empty", "Value" ""}]],
            "test-resources/test.edn" {":foo" ":bar", ":duration" "#duration 20m"}}
           (api/parse-go "test-resources/**")))))
