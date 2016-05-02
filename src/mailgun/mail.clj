(ns mailgun.mail
  (:require [clj-http.client :as client]))

(defn build-url
  "Build the url based on the mailgun passkey and the domain specified.
  The url so generated would be : https://api:key@api.mailgun.net/v3/domain/messages"
  [{:keys [key domain]}]
  (str "https://api:" key "@api.mailgun.net/v3/" domain "/messages"))

(defn gen-multipart
  "Generate the multipart request param incase the request has an attachment"
  [{:keys [attachment] :as params}]
  (let [key-list (remove #(= :attachment %) (keys params))
        format (fn [k v] {:name k :content v})
        attachments (map #(format "attachment" %) attachment)
        remaining (map #(format (name %) (% params)) key-list)]
    (into [] (concat remaining attachments))))

(defn build-body
  "Build the request body that has to be sent to mailgun, it could be a map of simple form-params
  or could be a multipart request body. If the request has one or more attachments then the
  it would be a multipart else it would be a form-param"
  [{:keys [attachment] :as params}]
  (if attachment
    {:multipart (gen-multipart params)}
    {:form-params params}))

(defn send-mail
  "Send email to mailgun with the passed creds and the content

  A sample request would look like
  (send-mail {:key \"key-3ax6xnjp29jd6fds4gc373sgvjxteol1\" :domain \"bar.com\"}
             {:from \"no-reply@bar.com\"
              :to \"someone@foo.com\"
              :subject \"Test mail\"
              :html \"Hi ,</br> How are you ?\"
              :attachment [(clojure.java.io/file \"path/to/file\")]})"
  [creds mail-content]
  (let [url (build-url creds)
        body (build-body mail-content)]
    (client/post url body)))
