(ns mailgun.util
  (:require [cheshire.core :as json]
            [clojure.java.io :as io]))

(defn json-to-clj
  "Takes in a mailgun response and converts the json body to clojure map"
  [response]
  (update response :body json/parse-string))

(defn ensure-sequential
  "Checks if `x` is a vector or not, if not then returns a vector `x`"
  [x]
  (if (sequential? x)
    x
    [x]))

(defn to-file
  "Converts a list of attachments to list of file objects"
  [attachments]
  (when-not (nil? attachments)
    (->> attachments
         ensure-sequential
         (map #(io/file %)))))
