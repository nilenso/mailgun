# mailgun
[![Clojars Project](https://img.shields.io/clojars/v/nilenso/mailgun.svg)](https://clojars.org/nilenso/mailgun)

A Clojure wrapper for mailgun API.

## Leiningen
```clj
[nilenso/mailgun "0.2.1"]
```

## Maven
```xml
<dependency>
  <groupId>nilenso</groupId>
  <artifactId>mailgun</artifactId>
  <version>0.2.1</version>
</dependency>
```

## Usage
```clj
(:require [mailgun.mail :as mail])
```

The `send-mail` function takes two argument mailgun credentials and email content which has to be given in the following format
```clj
(def creds {:key "key-3ax6xnjp29jd6fds4gc373sgvjxteol1" :domain "bar.com"})

(def content {:from "no-reply@bar.com"
              :to "someone@foo.com"
              :subject "Test"
              :html "test body"
              :attachment [(clojure.java.io/file "/path/to/file1.doc")
                           (clojure.java.io/file "/path/to/file2.doc")]})
```
The value of the `:attachment` has to be a vector of files to be attached. If there are no files to be attached then don't include the `:attachment` keyword in the content.

### send message
```clj
(mail/send-mail creds content)
```

### get stored message
There are functions that help you retrieve stored messages from mailgun and parse them as required and also download attachments if any. The `get-stored-message` function gets the complete mail response from mailgun, and then `:body` of the response can me parsed using `parse` or `parse-message` functions.
```clj
(let [msg-key "eyJRhImsiOiAiZ1IiwgInMiOiAiNmNlTQ3NTY4ZGZSwg0MWRmLWEwODQtNzCJjIjogImJpZ3RhbmtzMiJMtMzQ0OC0NWJiY2Q4ODQMDkwMzk4ZCIsIwIjogdHJ19"
      msg-body (->> msg-key
                    (mail/get-stored-message creds)
                    :body)
      recipients (mail/parse ["to" "cc" "bcc"] msg-body)
      message (mail/parse-message msg-body)]
  (println recipients)
  (println message))
=> {:to "bar124@foomail.com" :bcc "someone_else@foo.in" :cc nil}
=> {:sender "foo@bar.com", :to "bar123@foomail.com", :bcc "someone_else@foo.in", :cc nil, :subject "Message Subject", :date "Mon, 2 May 2016 14:43:28 +0530", :body-html "<div dir=\"ltr\"><br clear=\"all\"><div><br></div><br>\r\n</div>\r\n", :attachments [{"url" "https://api.mailgun.net/v2/domains/foomail.in/messages/eyJRhImsiOiAiZ1IiwgInMiOiAiNmNlTQ3NTY4ZGZSwg0MWRmLWEwODQtNzCJjIjogImJpZ3RhbmtzMiJMtMzQ0OC0NWJiY2Q4ODQMDkwMzk4ZCIsIwIjogdHJ19/attachments/0", "content-type" "image/jpeg", "name" "Image1.jpg", "size" 267928} {"url" "https://api.mailgun.net/v2/domains/foomail.in/messages/eyJRhImsiOiAiZ1IiwgInMiOiAiNmNlTQ3NTY4ZGZSwg0MWRmLWEwODQtNzCJjIjogImJpZ3RhbmtzMiJMtMzQ0OC0NWJiY2Q4ODQMDkwMzk4ZCIsIwIjogdHJ19/attachments/1", "content-type" "image/jpeg", "name" "Image2.jpg", "size" 477946}]}
```

### download attachments
The `download-attachment` function can be used to download attachments give the attachment url and the credentials
```clj
(let [attch-url "https://api.mailgun.net/v2/domains/foomail.in/messages/eyJRhImsiOiAiZ1IiwgInMiOiAiNmNlTQ3NTY4ZGZSwg0MWRmLWEwODQtNzCJjIjogImJpZ3RhbmtzMiJMtMzQ0OC0NWJiY2Q4ODQMDkwMzk4ZCIsIwIjogdHJ19/attachments/0"]
  (mail/download-attachment creds attch-url))
=> #object[java.io.BufferedInputStream 0xffb1f95 "java.io.BufferedInputStream@ffb1f95"]
```
## License

Copyright © 2016 Nilenso

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
