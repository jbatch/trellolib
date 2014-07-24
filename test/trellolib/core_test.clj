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
