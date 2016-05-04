(ns mailgun.mail-test
  (:require [clojure.test :refer :all]
            [mailgun.mail :as mail]))

(deftest test-build-url
  (testing "build-url funtion return a valid mailgun url"
    (is (= "https://api.mailgun.net/v3/foo.org/messages"
           (mail/gen-url "/messages" "foo.org")))))

(deftest test-build-body
  (testing "build-body to see if it returns a valid map when ther is no attachment"
    (let [params {:from "foo@bar.com"
                  :to "bar@foo.com"
                  :subject "Test"
                  :html "Body"}]
      (is (= {:form-params params}
             (mail/gen-body params)))))
  (testing "build-body to see if return a valid multipart map"
    (let [multi-part-map {:multipart [{:name "from" :content "foo@bar.com"}
                                       {:name "to" :content "bar@foo.com"}
                                       {:name "subject" :content "Test"}
                                       {:name "html" :content "Body"}
                                       {:name "attachment" :content "attachment1"}
                                       {:name "attachment" :content "attachment2"}]}
          request-params {:from "foo@bar.com"
                          :to "bar@foo.com"
                          :subject "Test"
                          :html "Body"
                          :attachment ["attachment1" "attachment2"]}]
      (is (= multi-part-map (mail/gen-body request-params))))))

(deftest parse-message-body
  (testing "parsing the mesage body gives valid fields"
    (let [sample-body-reponse {"X-Envelope-From" "<test@foo.com>"
                               "body-plain" "\r\n"
                               "Bcc" "test@bcc.com"
                               "stripped-signature" ""
                               "To" "someone@bar.com"
                               "Date" "Mon, 2 May 2016 14:43:28 +0530"
                               "X-Mailgun-Incoming" "Yes"
                               "stripped-text" ""
                               "Subject" "Test subject"
                               "subject" "Test subject"
                               "Content-Type" "multipart/mixed; boundary=\"001a113d5d48e2b5490531d86713\""
                               "message-headers" [["X-Mailgun-Incoming" "Yes"]
                                                  ["X-Envelope-From" "<test@foo.com>"]
                                                  ["Received" "from mail-oi0-f66.foogle.com (mail-oi0-f66.foogle.com [209.85.218.66]) by mxa.mailgun.org with ESMTP id 57271a3a.7f5255f9ee30-in2; Mon, 02 May 2016 09:13:30 -0000 (UTC)"]
                                                  ["Received" "by mail-oi0-f66.foogle.com with SMTP id i2so26491577oib.3        for <test@bcc.com>; Mon, 02 May 2016 02:13:29 -0700 (PDT)"]
                                                  ["Mime-Version" "1.0"]
                                                  ["X-Received" "by 10.202.89.68 with SMTP id n65m8908r1239oib.144.1462189094009; Mon, 02 May 2016 02:13:29 -0700 (PDT)"]
                                                  ["Received" "by 10.157.17.66 with HTTP; Mon, 2 May 2016 02:13:28 -0700 (PDT)"]
                                                  ["Date" "Mon, 2 May 2016 14:43:28 +0530"]
                                                  ["Message-Id" "<@mail.fmail.com>"]
                                                  ["Subject" "Test subject"]
                                                  ["From" "Test User<test@foo.com>"]
                                                  ["To" "someone@bar.com"]
                                                  ["Content-Type" "multipart/mixed; boundary=\"001a113d5d48e2b5490531d86713\""]
                                                  ["Bcc" "test@bcc.com"]]
                               "content-id-map" {}
                               "body-html" "<div dir=\"ltr\"><br clear=\"all\"><div><br></div><br>\r\n</div>\r\n"
                               "from" "Test User<test@foo.com>"
                               "From" "Test User<test@foo.com>"
                               "Mime-Version" "1.0"
                               "X-Received" "by 10.202.89.68 with SMTP id n65m8908r1239oib.144.1462189094009; Mon, 02 May 2016 02:13:29 -0700 (PDT)"
                               "Received" "by 10.157.17.66 with HTTP; Mon, 2 May 2016 02:13:28 -0700 (PDT)"
                               "sender" "test@foo.com"
                               "Message-Id" "<J1zXQuaZ5TiAoy0iLaXru3zNc-0-kJAXH0s5D0J1gkcCAMzBuaZ@mail.fmail.com>"
                               "stripped-html" "<div dir=\"ltr\"><br clear=\"all\"><div><br></div><br></div>"
                               "attachments" [{"url" "https://api.mailgun.net/v3/domains/bar.com/messages/eyJRhImsiOiAiZ1IiwgInMiOiAiNmNlTQ3NTY4ZGZSwg0MWRmLWEwODQtNzCJjIjogImJpZ3RhbmtzMiJMtMzQ0OC0NWJiY2Q4ODQMDkwMzk4ZCIsIwIjogdHJ19/attachments/0"
                                               "content-type" "image/jpeg"
                                               "name" "SBOW 1.jpg"
                                               "size" 267928}
                                              {"url" "https://api.mailgun.net/v3/domains/bar.com/messages/eyJRhImsiOiAiZ1IiwgInMiOiAiNmNlTQ3NTY4ZGZSwg0MWRmLWEwODQtNzCJjIjogImJpZ3RhbmtzMiJMtMzQ0OC0NWJiY2Q4ODQMDkwMzk4ZCIsIwIjogdHJ19/attachments/1"
                                               "content-type" "image/jpeg"
                                               "name" "Timeline.jpg"
                                               "size" 477946}]
                               "X-Gm-Message-State" "AOPr4FUYj94i29RE369NXEJlboW++ahAxRJUadLJyQKcYvXcTNpNeCju/WzBZwOAgDl+cdGRUASq5A=="}]
      (is (= {:sender "test@foo.com"
              :to "someone@bar.com"
              :bcc "test@bcc.com"
              :cc nil
              :subject "Test subject"
              :date "Mon, 2 May 2016 14:43:28 +0530"
              :body-html "<div dir=\"ltr\"><br clear=\"all\"><div><br></div><br>\r\n</div>\r\n"
              :attachments [{"url" "https://api.mailgun.net/v3/domains/bar.com/messages/eyJRhImsiOiAiZ1IiwgInMiOiAiNmNlTQ3NTY4ZGZSwg0MWRmLWEwODQtNzCJjIjogImJpZ3RhbmtzMiJMtMzQ0OC0NWJiY2Q4ODQMDkwMzk4ZCIsIwIjogdHJ19/attachments/0"
                             "content-type" "image/jpeg"
                             "name" "SBOW 1.jpg"
                             "size" 267928}
                            {"url" "https://api.mailgun.net/v3/domains/bar.com/messages/eyJRhImsiOiAiZ1IiwgInMiOiAiNmNlTQ3NTY4ZGZSwg0MWRmLWEwODQtNzCJjIjogImJpZ3RhbmtzMiJMtMzQ0OC0NWJiY2Q4ODQMDkwMzk4ZCIsIwIjogdHJ19/attachments/1"
                             "content-type" "image/jpeg"
                             "name" "Timeline.jpg"
                             "size" 477946}]}
             (mail/parse-message sample-body-reponse)))
      (is (= {:sender "test@foo.com" :content nil}
             (mail/parse ["sender" "content"] sample-body-reponse)))
      (is (= {:mime-version "1.0"}
             (mail/parse ["Mime-Version"] sample-body-reponse))))))
