#!usr/bin/env bb

(require '[babashka.process :as proc])

(def repos
  ["github.com/sourcegraph/scip-go"
   "github.com/sourcegraph-testing/etcd"])

(defn download-repo [repo]
  (println "Downloading repositories")
  (println "git clone --shallow" (str "https://" repo)))

(defn print-current-directory []
  (prn (proc/sh "pwd")))

(comment
  (def repo (first repos))
  repo
  (download-repo repo)
  (print-current-directory)
  ,)
