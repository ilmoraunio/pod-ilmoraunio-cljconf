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
  (edn/read {:default #(str "#" %1 " " %2)} (PushbackReader. (io/reader (.getBytes s)))))

(defn parse-go
  "Attempts to parse `filenames` using either a Go parser (only).

   Currently supported Go parsers: cue, dockerfile, edn, hcl1, hcl2, ignore, ini, json, jsonnet, properties, spdx, toml, vcl, xml, yaml, dotenv.

   For each file found, returns map of data with `filenames` as keys, eg:

   ```
   {\"test-resources/test.yaml\" {\"spec\" {\"ports\" [{\"port\" 80.0, \"targetPort\" 8080.0}],
                                            \"selector\" {\"app\" \"hello-kubernetes\"},
                                            \"type\" \"LoadBalancer\"},
                                  \"apiVersion\" \"v1\",
                                  \"kind\" \"Service\",
                                  \"metadata\" {\"name\" \"hello-kubernetes\"}}}
   ```

   See for detailed explanation: `pod-ilmoraunio-conftest-clj.api/parse`"
  [& filenames]
  (let [files (mapcat #(fs/glob "." % {:hidden true}) filenames)]
    (apply conftest/parse (map str files))))

(defn parse
  "Attempts to parse `filenames` using either a Clojure or Go parser (in this order, whichever succeeds first).

   Currently supported Clojure parsers: json, edn.
   Currently supported Go parsers: cue, dockerfile, edn, hcl1, hcl2, ignore, ini, json, jsonnet, properties, spdx, toml, vcl, xml, yaml, dotenv.

   For each file found, returns map of data with `filenames` as keys, eg:

   ```
   {\"test-resources/test.yaml\" {\"spec\" {\"ports\" [{\"port\" 80.0, \"targetPort\" 8080.0}],
                                            \"selector\" {\"app\" \"hello-kubernetes\"},
                                            \"type\" \"LoadBalancer\"},
                                  \"apiVersion\" \"v1\",
                                  \"kind\" \"Service\",
                                  \"metadata\" {\"name\" \"hello-kubernetes\"}}}
   ```

   See also: `pod-ilmoraunio-conftest-clj.api/parse-go`"
  [& filenames]
  (let [{parseable-files-with-native-parser true
         parseable-files-with-conftest-parser false}
        (group-by #(boolean (supported-native-parser (fs/extension %)))
                  (mapcat #(fs/glob "." % {:hidden true}) filenames))]
    (let [files-parsed-with-native-parser (->> (pmap (fn [f]
                                                       (let [filename (str f)
                                                             extension (fs/extension f)
                                                             contents (slurp filename)]
                                                         {filename (case extension
                                                                     "json" (json/decode contents
                                                                                         (fn [k]
                                                                                           (cond
                                                                                             (clojure.string/starts-with? k "@") k
                                                                                             :else (keyword k))))
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