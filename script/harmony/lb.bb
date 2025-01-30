#!/usr/bin/env bb

(ns harmony.lb
  (:require [clojure.java.io :as io]
            [clojure.string :as str])
  (:import [java.net
            ServerSocket
            Socket]))

(defonce ^:private  backend-host "localhost")
(defonce ^:private ports [1010 1011 1012 1013])

(defn forward->backend [port request-line headers]
  (let [backend-socket (Socket. backend-host port)
        in (io/reader (.getInputStream backend-socket))
        out (io/writer (.getOutputStream backend-socket))]
    (.write out (str request-line "\r\n"))
    (doseq [header headers]
      (.write out (str header "\r\n")))
    (.write out "\r\n")
    (.flush out)
    ;; Read the response from the backend server
    (let [response (slurp in)]
      (.close backend-socket)
      response)))

(defn handle-request [client-socket]
  (with-open [in (io/reader (.getInputStream client-socket))
              out (io/writer (.getOutputStream client-socket))]
    (let [request-line (.readLine in)
          headers (loop [headers []]
                    (let [line (.readLine in)]
                      (if (or (nil? line) (str/blank? line))
                        headers
                        (recur (conj headers line)))))]
      (println "Recieved request from : "
               (.getHostAddress (.getInetAddress client-socket)))
      (println request-line)
      (doseq [header headers]
        (println header))
      (println "Forwarding to the backend server")
      (let [backend-response (forward->backend 1010 request-line headers)]
        (println "Response from the backend server"
                 backend-response)
        (.write out backend-response)
        (.flush out)))))

(defn start-server
  [port]
  (let [server-socket (ServerSocket. port)]
    (println "Load balancer started on port : " port)
    (while true
    ;; TODO: accept is a blocking operation. Handle this in a non
    ;; blocking way
      (let [client-socket (.accept server-socket)]
        (println "Client request accepted successfully")
        (future (handle-request client-socket))))))

(defn -main [& args]
  (let [port 80]
    (start-server port)))

(-main)
