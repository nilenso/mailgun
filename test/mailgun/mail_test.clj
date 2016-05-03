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
