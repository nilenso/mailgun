(ns mailgun.mail
  (:require [mailgun.util :as util]
            [clj-http.client :as client]))

(defn gen-auth
  "Returns the basic authentication with the mailgun api key as password"
  [password]
  {:basic-auth ["api" password]})

(defn base-url
  "Returns the base mailgun api url"
  [domain]
  (str "https://api.mailgun.net/v3/" domain))

(defn gen-url
  "Build the mailgun url based on the mailgun domain and the end route.
   Eg : (build-url \"/messages\" \"foo.org\" )
      => https://api.mailgun.net/v3/foo.org/messages"
  [route domain]
  (-> domain
      base-url
      (str route)))

(defn gen-mail-url
  "Generate the mailgun url to get a message with a message-key"
  [route mail-key domain]
  (let [domain (str "domains/" domain)
        route (str route "/" mail-key)]
    (gen-url route domain)))

(defn gen-multipart
  "Generate the multipart request param incase the request has an attachment"
  [{:keys [attachment] :as params}]
  (let [key-list (remove #(= :attachment %) (keys params))
        format (fn [k v] {:name k :content v})
        attachments (map #(format "attachment" %) attachment)
        remaining (map #(format (name %) (% params)) key-list)]
    (into [] (concat remaining attachments))))

(defn gen-body
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
  [{:keys [domain key] :as creds} mail-content]
  (let [url (gen-url "/messages" domain)
        content (merge (gen-auth key)
                       (gen-body mail-content))]
    (client/post url content)))

(defn get-stored-events
  "Returns stored events"
  [{:keys [domain key]}]
  (let [url (gen-url "/events" domain)
        auth (gen-auth key)]
    (util/json-to-clj (client/get url auth))))

(defn get-stored-mail
  "Returns a stored message given the message-key"
  [{:keys [domain key]} mail-key]
  (let [url (gen-mail-url "/messages" mail-key domain)
        auth (gen-auth key)]
    (util/json-to-clj (client/get url auth))))
