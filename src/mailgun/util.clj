(ns mailgun.util
  (:require [cheshire.core :as json]))

(defn json-to-clj
  "Takes in a mailgun response and converts the json body to clojure map"
  [response]
  (assoc-in response [:body] (-> response
                                 :body
                                 json/parse-string)))
