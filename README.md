# mailgun

A Clojure mailgun API.

## Usage

```clj
(:require [mailgun.mail :as mail])
```

The `send-mail` function takes two argument mailgun credentials and email content which has to be given in the following format
```clj
(def creds {:key "mail-gun-key" :domain "bar.com"})

(def content {:from "no-reply@bar.com"
              :to "someone@foo.com"
              :subject "Test"
              :html "test body"
              :attachment [(clojure.java.io/file "/path/to/file1.doc")
                           (clojure.java.io/file "/path/to/file2.doc")]})
```
The value of the `:attachment` has to be a vector of files to be attached. If there are no files to be attached then don't include the `:attachment` keyword in the content.

## License

Copyright © 2016 Nilenso

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
