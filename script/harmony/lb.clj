#!/usr/bin/env bb

(ns harmony.lb
  (:require [clojure.java.io :as io]
            [clojure.string :as str])
  (:import [java.net ServerSocket]))

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
      (println "Recieved request from : " (.getInetAddress client-socket))
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


(defn -main [& args]
  (let [port (if (empty? args)
               ;; Use port 80 by default
               80
               (parse-long (first args)))]
    (start-server port)))

(-main)
