#!/usr/bin/env bb

(ns harmony.be
  (:require [clojure.java.io :as io]
            [clojure.string :as string])
  (:import [java.net ServerSocket]))

(defn handle-request
  [client-socket]
  (with-open [in (io/reader (.getInputStream client-socket))
              out (io/writer (.getOutputStream client-socket))]
    (let [request-line (.readLine in)
          headers (loop [headers []]
                    (let [line (.readLine in)]
                      (if (or (nil? line) (string/blank? line))
                        headers
                        (recur (conj headers line)))))]
      ;; send a simple http response
      (if (string/includes? request-line "HEAD /health")
        (do
          (prn "Health check request received...")
          (.write out "HTTP/1.1 200 OK\r\n")
          (.write out "Content-Type: text/plain\r\n")
          (.write out "\r\n")
          (.write out "Healthy")
          (.write out "\r\n")
          (.flush out))
        (do
          (println "Recieved request from : " (.getLocalPort client-socket))
          (println request-line)
          (doseq [header headers]
            (println header))
          (.write out "HTTP/1.1 200 OK\r\n")
          (.write out "Content-Type: text/plain\r\n")
          (.write out "\r\n")
          (.write out "Hello there, this is a response from a server")
          (.write out "\r\n")
          (.flush out))))))

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
    (prn "Setting up server...")
    (start-server port)))
