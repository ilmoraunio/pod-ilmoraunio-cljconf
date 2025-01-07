(ns api-test
  (:require [babashka.pods :as pods]
            [clojure.test :refer [deftest is testing]]))

(pods/load-pod "./pod-ilmoraunio-conftest-clj")
(require '[pod-ilmoraunio-conftest-clj.api :as api])

(deftest parse-test
  (testing "parse"
    (is (= {"test-resources/test.json" {:hello [1 2 4], "@foo" "bar"},
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
           (api/parse "test-resources/*{edn,json,yaml,.dockerignore}"))))
 (testing "parse-as"
    (is (= {"test-resources/hocon.conf" {"play" {"editor" "<<unknown value>>",
                                                 "server" {"debug" {"addDebugInfoToRequests" false},
                                                           "dir" "<<unknown value>>",
                                                           "http" {"address" "0.0.0.0",
                                                                   "idleTimeout" "\"75 seconds\"",
                                                                   "port" 9001.0},
                                                           "https" {"wantClientAuth" false,
                                                                    "address" "0.0.0.0",
                                                                    "engineProvider" "play.core.server.ssl.DefaultSSLEngineProvider",
                                                                    "idleTimeout" "\"75 seconds\"",
                                                                    "keyStore" {"algorithm" "<<unknown value>>",
                                                                                "password" "\"\"",
                                                                                "path" "<<unknown value>>",
                                                                                "type" "JKS"},
                                                                    "needClientAuth" false,
                                                                    "port" "<<unknown value>>",
                                                                    "trustStore" {"noCaVerification" false}},
                                                           "pidfile" {"path" "<<unknown value>>"},
                                                           "websocket" {"frame" {"maxLength" "64k"}}}}}}
           (api/parse-as "hocon" "test-resources/hocon.conf")))
    (is (= [{"test-resources/test.edn" {:foo :bar, :duration "#duration 20m"}}
            {"test-resources/test.edn" {":duration" "#duration 20m", ":foo" ":bar"}}]
           [(api/parse-as "edn" "test-resources/test.edn")
            (api/parse-go-as "edn" "test-resources/test.edn")]))
    (is (= {"test-resources/test.json" {"@foo" "bar", "hello" [1.0 2.0 4.0]},
            "test-resources/test.yaml" {"spec" {"ports" [{"port" 80.0, "targetPort" 8080.0}],
                                                "selector" {"app" "hello-kubernetes"},
                                                "type" "LoadBalancer"},
                                        "apiVersion" "v1",
                                        "kind" "Service",
                                        "metadata" {"name" "hello-kubernetes"}}}
           (api/parse-as "yaml" "test-resources/test.json" "test-resources/test.yaml")))
    (is (thrown-with-msg? Exception
                          #"unsupported SPDX version"
                         (api/parse-as "spdx" "deps.edn"))))
 (testing "parse-go"
    (is (= {"test-resources/test.yaml" {"apiVersion" "v1",
                                        "kind" "Service",
                                        "metadata" {"name" "hello-kubernetes"},
                                        "spec" {"ports" [{"port" 80.0, "targetPort" 8080.0}],
                                                "selector" {"app" "hello-kubernetes"},
                                                "type" "LoadBalancer"}},
            "test-resources/test.json" {"hello" [1.0 2.0 4.0], "@foo" "bar"},
            "test-resources/.dockerignore" [[{"Kind" "Path", "Value" ".idea", "Original" ".idea"}
                                             {"Original" "", "Kind" "Empty", "Value" ""}]],
            "test-resources/test.edn" {":foo" ":bar", ":duration" "#duration 20m"}}
           (api/parse-go "test-resources/*{edn,json,yaml,.dockerignore}"))))
 (testing "parse-go-as"
    (is (= {"test-resources/test.edn" {":foo" ":bar", ":duration" "#duration 20m"}}
           (api/parse-go-as "edn" "test-resources/test.edn")))))
