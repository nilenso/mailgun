(ns mailgun.mail
  (:require [mailgun.util :as util]
            [clj-http.client :as client]
            [clojure.string :as string]
            [clojure.java.io :as io]))

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

(defn gen-message-url
  "Generate the mailgun url to get a message with a message-key"
  [route message-key domain]
  (let [domain (str "domains/" domain)
        route (str route "/" message-key)]
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
  [{:keys [domain key] :as creds} message-content]
  (let [url (gen-url "/messages" domain)
        content (merge (gen-auth key)
                       (gen-body message-content))]
    (client/post url content)))

(defn get-stored-events
  "Returns stored events"
  [{:keys [domain key]}]
  (let [url (gen-url "/events" domain)
        auth (gen-auth key)]
    (util/json-to-clj (client/get url auth))))

(defn get-stored-message
  "Returns a stored message given the message-key"
  [{:keys [domain key]} message-key]
  (let [url (gen-mail-url "/messages" message-key domain)
        auth (gen-auth key)]
    (util/json-to-clj (client/get url auth))))

(defn parse
  "Pares the message-body based on the vector of keys given as input"
  [key-vec message-body]
  (reduce (fn [m k]
            (assoc m (keyword (string/lower-case k)) (message-body k)))
          {}
          key-vec))

(defn parse-message
  "Parse the message from mailgun to basic message tags"
  [message-body]
  (parse
   ["sender" "To" "Bcc" "Cc" "Subject" "Date" "body-html" "attachments"]
   message-body))

(defn download-attachment
  "Download attachment from message stored in mailgun providing login credentials
   and attachment mailgun url"
  [{:keys [key]} url]
  (let [params (merge {:socket-timeout 10000
                       :conn-timeout 10000
                       :as :byte-array}
                      (gen-auth key))]
    (->> params
         (client/get url)
         :body
         io/input-stream)))
