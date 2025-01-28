(ns pod-ilmoraunio-conftest-clj.core
  (:require [bencode.core :as bencode]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.walk :as walk]
            [pod-ilmoraunio-conftest-clj.api :as api])
  (:import (java.io EOFException PushbackInputStream OutputStream))
  (:gen-class))

(def debug? false)

(defn debug [& msg]
  (when debug?
    (binding [*out* *err*]
      (apply println msg))))

(def stdin (PushbackInputStream. System/in))
(def stdout System/out)

(defn read-bencode [in]
  (bencode/read-bencode in))

(defn write-bencode [^OutputStream out v]
  (bencode/write-bencode out v)
  (.flush out))

(defn bytes->string [^bytes bs]
  (String. bs "UTF-8"))

(defn podify-namespace
  ([ns] (podify-namespace ns *ns* nil))
  ([ns ns-prefix] (podify-namespace ns ns-prefix nil))
  ([ns ns-prefix pod-name]
   (let [pod-ns (if pod-name
                  [ns-prefix pod-name]
                  [ns-prefix])
         vars   (-> ns ns-publics keys)]
     {:name (symbol (str/join "." pod-ns))
      :vars (mapv (fn [v] {:name v}) vars)})))

(def describe-map
  (delay
    (walk/postwalk
      #(if (ident? %)
         (name %)
         %)
      {:format "edn"
       :readers {"ordered/map" "flatland.ordered.map/ordered-map"}
       :namespaces [(podify-namespace 'pod-ilmoraunio-conftest-clj.api 'pod-ilmoraunio-conftest-clj.api)]})))

(defn dispatch
  [message]
  (debug message)
  (let [id (bytes->string (get message "id"))
        _ (debug "id" id)
        var (bytes->string (get message "var"))
        _ (debug "var" var)
        args (-> (get message "args") bytes->string edn/read-string)
        _ (debug "args" args)
        value (case var
                "pod-ilmoraunio-conftest-clj.api/parse" (apply api/parse args)
                "pod-ilmoraunio-conftest-clj.api/parse-as" (apply api/parse-as args)
                "pod-ilmoraunio-conftest-clj.api/parse-go" (apply api/parse-go args)
                "pod-ilmoraunio-conftest-clj.api/parse-go-as" (apply api/parse-go-as args))
        _ (debug "value" value)]
    {"value" (pr-str value)
     "id" id
     "status" ["done"]}))

(defn error-response [id throwable]
  {"ex-message" (ex-message throwable)
   "ex-data" (pr-str
               (assoc (ex-data throwable)
                    :type
                    (str (class throwable))))
   "id"         id
   "status"     ["done" "error"]})

(defn -main []
  (loop []
    (let [message (try (read-bencode stdin)
                       (catch EOFException _
                         ::EOF))]
      (debug "message" message)
      (when-not (identical? ::EOF message)
        (let [op (-> message (get "op") bytes->string keyword)
              id (or (some-> message (get "id") bytes->string)
                     "unknown")]
          (case op
            :describe (do
                        (debug "got :describe")
                        (debug "responding with" (pr-str @describe-map))
                        (write-bencode stdout @describe-map)
                        (recur))
            :invoke (do
                      (debug "got :invoke")
                      (try
                        (let [response (dispatch message)]
                          (debug "responding with" (pr-str response))
                          (write-bencode stdout response))
                        (catch Throwable e
                          (->> e (error-response id) (write-bencode stdout))))
                      (recur))
            :shutdown (do
                        (debug "shutting down")
                        (System/exit 0))
            (do
              (let [response {"ex-message" "Unknown op"
                              "ex-data"    (pr-str {:op op})
                              "id"         id
                              "status"     ["done" "error"]}]
                (write-bencode stdout response))
              (recur))))))))