#!/usr/bin/env bb

(ns harmony.lb
  (:require [clojure.java.io :as io]
            [clojure.string :as str])
  (:import [java.net
            ServerSocket
            Socket]))

(defonce ^:private  backend-host "localhost")

;; Use an hahsmap and assign each port a number
(def ^:private ports
  {:0 :1010
   :1 :1011
   :2 :1012
   :3 :1013})

(defonce ^:private helper-number (atom 0))

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

(defn port-idx
  [ports]
  (let [port-count (count ports)
         idx (rem @helper-number port-count)
        _ (swap! helper-number inc)]
    (keyword (str idx))))

(defn select-port
  [ports]
  (parse-long (name ((port-idx ports) ports))))

(defn healthy?
  "Takes a port and checks the health of the server at that port"
  [port]
  (prn "Checking the health of the server running in the port: " port)
  (try
    (let [health-check-socket (Socket. backend-host port)
          in (io/reader (.getInputStream health-check-socket))
          out (io/writer (.getOutputStream health-check-socket))]
      (.write out "HEAD /health HTTP/1.1\r\n")
      (.write out (str "Host: " (str backend-host ":" port) "\r\n"))
      (.write out "\r\n")
      (.flush out)
      (let [request-line (.readLine in)]
        (str/includes? request-line "200 OK")))
    (catch Exception ex
      (prn "OOPS! Health check failed......")
      (prn "Forwarding the request to the next port...")
      false)))

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
      (let [port (loop []
                   (let [current-port (select-port ports)]
                     (if (healthy? current-port)
                       current-port
                       (recur))))
            backend-response (forward->backend port request-line headers)]
        (println "Forwarding to backend server on port : " port)
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
        ;; Handle the request in async
        (future (handle-request client-socket))))))

(defn -main [& args]
  (let [port 80]
    (start-server port)))

(-main)
