#!/usr/bin/env bb

(ns harmony.be
  (:require [clojure.java.io :as io]
            [clojure.string :as str])
  (:import [java.net ServerSocket]))

(defn get-port-from-file
  [file]
  (-> (last (str/split file #"/"))
      (str/split #"_")
      first
      parse-long))

(defn handle-request
  [client-socket]
  (with-open [in (io/reader (.getInputStream client-socket))
              out (io/writer (.getOutputStream client-socket))]
    (let [request-line (.readLine in)
          headers (loop [headers []]
                    (let [line (.readLine in)]
                      (if (or (nil? line) (str/blank? line))
                        headers
                        (recur (conj headers line)))))]
      (println "Type of client socket is " (type client-socket))
      (println "Recieved request from : " (.getLocalPort client-socket))
      (println request-line)
      (doseq [header headers]
        (println header))
      ;; send a simple http response
      (.write out "HTTP/1.1 200 OK\r\n")
      (.write out "Content-Type: text/plain\r\n")
      (.write out "\r\n")
      (.write out "<h1>Hello world</h1>")
      (.write out "\r\n")
      (.flush out))))

(defn start-server
  [port]
  (let [server-socket (ServerSocket. port)]
    (println "Server started on port " port)
    (while true
      (let [client-socket (.accept server-socket)
            _ (prn "Connected to a client....")]
        (future (handle-request client-socket))))))


(let [port (try
             (parse-long (first *command-line-args*))
             (catch Exception ex
               (prn "Usage: ./be.bb <port-number>")
               (System/exit 0)))]
  (do
    (prn "The commandline args are : " *command-line-args*)
    (start-server port)))
