(ns trellolib.core-test
  (:require [midje.sweet :refer :all]
            [trellolib.core :as core]
            [trellolib.validation :refer :all]
            [oauth.client :as oauth]))


(def test-card {:name "hello" :desc "world" :idList "<youridhere>"})
(def test-card-with-errors {:name "hello"
                            :desc "world"
                            :idList "<youridhere>"
                            :errors '({:a "old"})})
(def test-client {:key "<yourkey>"
                  :secret "<yoursecret>"
                  :callback "<yourcallback>"})

(def auth-client {:consumer {:key "<yourkey>"
                             :secret "<yoursecret>"
                             :request-uri "https://trello.com/1/OAuthGetRequestToken"
                             :access-uri "https://trello.com/1/OAuthGetAccessToken"
                             :authorize-uri "https://trello.com/1/OAuthAuthorizeToken"
                             :signature-method :hmac-sha1}
                  :key "<yourkey>"
                  :authorize-url "https://trello.com/1/OAuthAuthorizeToken?oauth_token=%3Ctemptoken%3E&name=MyApp&expiration=never&scope=read,write"
                  :secret "<yoursecret>"
                  :request-token
                  {:oauth_token "<temptoken>"
                   :oauth_token_secret "<tempsecret>"
                   :oauth_callback_confirmed "true"}
                  :callback "<yourcallback>"})

(def access-client {:consumer
                    {:key "<yourkey>"
                     :secret "<yoursecret>"
                     :request-uri "https://trello.com/1/OAuthGetRequestToken"
                     :access-uri "https://trello.com/1/OAuthGetAccessToken"
                     :authorize-uri "https://trello.com/1/OAuthAuthorizeToken"
                     :signature-method :hmac-sha1}
                    :key "<yourkey>"
                    :authorize-url "https://trello.com/1/OAuthAuthorizeToken?oauth_token=%3Ctemptoken%3E&name=MyApp&expiration=never&scope=read,write"
                    :secret "<yoursecret>"
                    :request-token
                    {:oauth_token "<temptoken>"
                     :oauth_token_secret "<tempsecret>"
                     :oauth_callback_confirmed "true"}
                    :callback "<yourcallback>"
                    :access-token {:oauth_token "<accesstoken>"
                                   :oauth_token_secret "<accesssecret>"}})

(facts "Core Tests"
       (fact "key-exists confirms key exists in hashmap"
             (key-exists test-card test-client :key) => test-card
             (key-exists test-card test-client :secret) => test-card
             (key-exists test-card test-client :not-key)
             => (contains {:errors '({:desc "Expected key not found"
                                      :field ":not-key"})}))

       (fact "add-error will return the card with the error added to the\\
             sequence"
             (add-error
              test-card {:a "new"}) => (contains {:errors '({:a "new"})})
             (add-error
              test-card-with-errors
              {:a "new"}) => (contains {:errors '({:a "new"} {:a "old"})}))
                                        ;(fact "validate-client-keys detects errors in clients"
                                        ;      (validate-client-keys test-card test-client) => test-card
                                        ;      (validate-client-keys test-card {:key "1234"
                                        ;                                            :not-token "4321"})
                                        ;      => (contains {:errors '({:desc "Expected key not found"
                                        ;                               :field ":token"})})
                                        ;      (validate-client-keys
                                        ;       test-card
                                        ;       {:not-key "1234" :not-token "4321"})
                                        ;      => (contains {:errors
                                        ;                    '({:desc "Expected key not found" :field ":token"}
                                        ;                      {:desc "Expected key not found" :field ":key"})}))
       (fact "validate-card-keys detects errors in cards"
             (validate-card-keys test-card) => test-card
             (validate-card-keys {:name "a" :desc "b" :not-idList "c"})
             => (contains {:errors '({:desc "Expected key not found"
                                      :field ":idList"})})
             (validate-card-keys {:name "a" :not-desc "b" :not-idList "c"})
             => (contains {:errors '({:desc "Expected key not found"
                                      :field ":idList"}
                                     {:desc "Expected key not found"
                                      :field ":desc"})}))
       (fact "validate-string-length ensures string length in range 1-16384"
             (validate-string-length test-card :name) => test-card
             (validate-string-length {:name "1"} :name) => {:name "1"}
             (validate-string-length {:name ""} :name)
             => (contains {:errors '({:desc
                                      "String length not in the range 1-16384"
                                      :field ":name"})})
             (validate-string-length
              {:name (apply str (take 16384 (repeat "a")))} :name)
             => {:name (apply str (take 16384 (repeat "a")))}
             (validate-string-length
              {:name (apply str (take 16385 (repeat "a")))} :name)
             => (contains {:errors '({:desc
                                      "String length not in the range 1-16384"
                                      :field ":name"})}))
       (against-background [(oauth/request-token anything anything)
                            => {:oauth_token "<temptoken>"
                                :oauth_token_secret "<tempsecret>"
                                :oauth_callback_confirmed "true"}]
                           (fact "authorize-client will return the
                                  client passed in with a consumer,
                                  request-token and authorize-url"
                                 (core/authorize-client test-client)
                                 => (just {:consumer
                                           {:key "<yourkey>"
                                            :secret "<yoursecret>"
                                            :request-uri "https://trello.com/1/OAuthGetRequestToken"
                                            :access-uri "https://trello.com/1/OAuthGetAccessToken"
                                            :authorize-uri "https://trello.com/1/OAuthAuthorizeToken"
                                            :signature-method :hmac-sha1}
                                           :key "<yourkey>"
                                           :authorize-url "https://trello.com/1/OAuthAuthorizeToken?oauth_token=%3Ctemptoken%3E&name=MyApp&expiration=never&scope=read,write"
                                           :secret "<yoursecret>"
                                           :request-token
                                           {:oauth_token "<temptoken>"
                                            :oauth_token_secret "<tempsecret>"
                                            :oauth_callback_confirmed "true"}
                                           :callback "<yourcallback>"})))

       (against-background [(oauth/access-token anything anything "<yourverifier>")
                            => {:oauth_token "<accesstoken>"
                                :oauth_token_secret "<accesssecret>"}]
                           (fact "get-access-token will return the
                                  client with an access-token"
                                 (core/get-access-token auth-client "<yourverifier>")
                                 => {:consumer
                                     {:key "<yourkey>"
                                      :secret "<yoursecret>"
                                      :request-uri "https://trello.com/1/OAuthGetRequestToken"
                                      :access-uri "https://trello.com/1/OAuthGetAccessToken"
                                      :authorize-uri "https://trello.com/1/OAuthAuthorizeToken"
                                      :signature-method :hmac-sha1}
                                     :key "<yourkey>"
                                     :authorize-url "https://trello.com/1/OAuthAuthorizeToken?oauth_token=%3Ctemptoken%3E&name=MyApp&expiration=never&scope=read,write"
                                     :secret "<yoursecret>"
                                     :request-token
                                     {:oauth_token "<temptoken>"
                                      :oauth_token_secret "<tempsecret>"
                                      :oauth_callback_confirmed "true"}
                                     :callback "<yourcallback>"
                                     :access-token {:oauth_token "<accesstoken>"
                                                    :oauth_token_secret "<accesssecret>"}}))

       (against-background [(oauth/credentials anything anything anything anything anything)
                            => {:oauth_signature "<signature>"
                                :oauth_token "<accesstoken>"
                                :oauth_consumer_key "<yourkey>"
                                :oauth_signature_method "HMAC-SHA1"
                                :oauth_timestamp "<timestamp>"
                                :oauth_nonce "<nonce>"
                                :oauth_version "1.0"}]
                           (fact "get-credentials will return the
                                  clinet with credentials attached"
                                 (core/get-credentials
                                  access-client
                                  :PUT
                                  (core/get-url "cards"))
                                 => (contains {:credentials
                                               {:oauth_signature "<signature>"
                                                :oauth_token "<accesstoken>"
                                                :oauth_consumer_key "<yourkey>"
                                                :oauth_signature_method "HMAC-SHA1"
                                                :oauth_timestamp "<timestamp>"
                                                :oauth_nonce "<nonce>"
                                                :oauth_version "1.0"}} ))))
