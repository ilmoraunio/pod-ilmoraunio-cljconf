(ns pod-conftest-clj.api
      (:require [pod.babashka.conftest-parser :as conftest-parser]))

(defn parse
      [& filenames]
      (apply conftest-parser/parse filenames))