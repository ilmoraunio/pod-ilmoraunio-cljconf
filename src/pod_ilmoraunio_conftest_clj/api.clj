(ns pod-ilmoraunio-conftest-clj.api
  (:require [pod.ilmoraunio.conftest :as conftest]
            [babashka.fs :as fs]
            [cheshire.core :as json]
            [clojure.edn :as edn]
            [clojure.java.io :as io])
  (:import (java.io PushbackReader)))

(def supported-native-parser #{"json" "edn"})

(defn edn-read
  [s]
  (edn/read (PushbackReader. (io/reader (.getBytes s)))))

(defn parse
  [& filenames]
  (let [{parseable-files-with-native-parser true
         parseable-files-with-conftest-parser false}
        (group-by #(boolean (supported-native-parser (fs/extension %)))
                  (mapcat #(fs/glob "." %) filenames))]
    (let [files-parsed-with-native-parser (->> (pmap (fn [f]
                                                   (let [filename (str f)
                                                         extension (fs/extension f)
                                                         contents (slurp filename)]
                                                     {filename (case extension
                                                                 "json" (json/decode contents true)
                                                                 "edn" (edn-read contents))}))
                                                 parseable-files-with-native-parser)
                                               (mapcat identity)
                                               (into {}))
          files-parsed-with-conftest-parser (if (seq parseable-files-with-conftest-parser)
                                              (apply conftest/parse (map str parseable-files-with-conftest-parser))
                                              [])]
      (apply merge-with merge
             (cond-> []
               (seq parseable-files-with-native-parser)
               (conj files-parsed-with-native-parser)

               (seq files-parsed-with-conftest-parser)
               (conj files-parsed-with-conftest-parser))))))