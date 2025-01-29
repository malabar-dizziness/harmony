#!/usr/bin/env bb

(require '[babashka.process :as proc]
         '[babashka.fs :as fs]
         '[clojure.string :as string])

(def repos
  ["github.com/sourcegraph/scip-go"
   "github.com/sourcegraph-testing/etcd"])



(defn download-repo [output repo]
  (println "Downloading repositories")
  (let [repo-basename (last (string/split repo #"/"))
        output-dir (str output "/" repo-basename)]
    (if (fs/exists? output-dir)
      (println "Skipping the repo " repo " because " output-dir " aready exists")
      (do (proc/shell "git" "clone" (str "https://" repo) output-dir)
          (println "Done downloading...")))))

(defn print-current-directory []
  (prn (proc/sh "pwd")))

(defn run []
  (let [repo (first repos)]
    (download-repo "scratch" repo)))

(run)

(comment
  (def repo (first repos))
  repo
  (download-repo "scratch" repo)
  (print-current-directory)
  (->> [(prn "hello world") (prn "this the world")]
       (map #(future (fn [arg]
                       (do
                         (Thread/sleep 2000)
                         (inc arg)))
                     %))
       (run! #(prn (deref %))))
  ,
  )
