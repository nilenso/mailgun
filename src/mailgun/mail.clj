(ns mailgun.mail
  (:require [clj-http.client :as client]))

(def api-url "api.mailgun.net/v3/")

(defn build-url
  [creds]
  (str "https://api:"
       (:key creds)
       "@"
       api-url
       (:domain creds)
       "/messages"))

(defn gen-multipart
  [{:keys [attachment] :as params}]
  (let [key-list (remove #(= :attachment %) (keys params))
        format (fn [k v] {:name k :content v})
        attachments (reduce #(into %1 [(format "attachment" %2)])
                            []
                            attachment)
        remaining (reduce #(into %1 [(format (name %2) (%2 params))])
                     []
                     key-list)]
    (into [] (concat remaining attachments))))

(defn build-body
  [{:keys [attachment] :as params}]
  (if attachment
    {:multipart (gen-multipart params)}
    {:form-params params}))

(defn send'
  [creds params]
  (let [url (build-url creds)
        body (build-body params)]
    (client/post url body)))
