(ns trellolib.ring-test
  (:require [midje.sweet :refer :all]
            (trellolib [core :as trello]
                       [ring :as ring])))

(def test-client {:key "noKey" :secret "deadbeef" :callback "http://localhost"})

(defn handler [request]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "Hello World"})

(defn test-wrap-handler
  "Will run the middleware for the Trello auth wrapper when called."
  [request]
  ((ring/wrap-trello-auth handler test-client) request))

(fact "Receive a redirect response when no Trello auth."
  (test-wrap-handler {})
  => (contains {:status 302 :headers {"Location" "https://trello.com/1/OAuthAuthorizeToken?oauth_token=OAuth-Token&name=MyApp&expiration=never&scope=read,write"}})
  (provided (oauth.client/request-token anything anything) => {:oauth_token "OAuth-Token"}))

(fact "Will not redirect if we have a query param with 'token'."
  (test-wrap-handler {:query-params {"token" "testtoken"}})
  => (contains {:status 200}))

(fact "Token saved in session if in query param."
  (test-wrap-handler {:query-params {"token" "testtoken"}})
  => (contains {:session {:trello-token "testtoken"}}))
