(ns trellolib.core
  (:refer-clojure :exclude [get])
  (:require [trellolib.validation :refer :all]
            [cheshire.core :as cheshire]
            [org.httpkit.client :as httpclient]
            [clojure.string :as str]
            [oauth.client :as oauth]))

(def trello-base-url "https://trello.com/1/")

(defn has-keys? [m keys]
  (apply = (map count [keys (select-keys m keys)])))

;; Taken, from:
;; https://github.com/ring-clojure/ring-codec/blob/master/src/ring/util/codec.clj
(defn encode-param
  "Given a hash map, will return a string of the hash map encoded URL."
  [params]
  (letfn [(encode-param [[k v]] (str (name k) "=" v))]
    (->> params
         (mapcat
          (fn [[k v]]
            (if (or (seq? v) (sequential? v))
              (map #(encode-param [k %]) v)
              [(encode-param [k v])])))
         (str/join "&"))))

(defn credentials-to-string
  "Turns a set of OAuth credentials into a string for the authorization header"
  [credentials]
  (letfn [(encode-param [[k v]] (str (name k) "=" "\"" v "\""))]
    (->> credentials
         (mapcat
          (fn [[k v]]
            (if (or (seq? v) (sequential? v))
              (map #(encode-param [k %]) v)
              [(encode-param [k v])])))
         (str/join ","))))

(defn authorize-client
  "Takes a client with a key and secret and returns the client with a
  request-token and authorize-url attached."
  [client]
  {:pre [(has-keys? client [:key :secret :callback])]
   :post [(has-keys? % [:key :secret :callback :request-token :consumer :authorize-url])]}
  (let [consumer (oauth/make-consumer (:key client)
                                      (:secret client)
                                      (str trello-base-url
                                           "OAuthGetRequestToken")
                                      (str trello-base-url
                                           "OAuthGetAccessToken")
                                      (str trello-base-url
                                           "OAuthAuthorizeToken")
                                      (:signature-method client :hmac-sha1))
        request-token (oauth/request-token consumer (:callback client))
        authorize-url (str (oauth/user-approval-uri consumer
                                                    (:oauth_token request-token))
                           "&name=" (:name client "MyApp")
                           "&expiration=" (:expiration client "never")
                           "&scope=" (:scope client "read,write"))]
    (-> client
        (assoc :consumer consumer)
        (assoc :request-token request-token)
        (assoc :authorize-url authorize-url))))

(defn get-access-token
  "Takes a client and a verifier and associates the client with an
  access token response "
  [client verifier]
  {:pre [(has-keys? client [:key :secret :callback
                            :request-token :consumer :authorize-url])]
   :post [(has-keys? % [:key :secret :callback
                        :request-token :consumer :authorize-url
                        :access-token])]}
  (let [access-token-response (oauth/access-token (:consumer client)
                                                  (:request-token client)
                                                  verifier)]
    (assoc client :access-token access-token-response)))

(defn get-credentials
  "Takes a client and associates it with OAuth credentials for a given request"
  [client request-type uri]
  {:pre [(has-keys? client [:key :secret :callback
                            :request-token :consumer :authorize-url
                            :access-token])]}
  (let [credentials (oauth/credentials (:consumer client)
                                       (:oauth_token (:access-token client))
                                       (:oauth_token_secret (:access-token client))
                                       request-type
                                       uri)]
    credentials))

(defn wrap-credentials
  "Wraps crediantials in an Authorization header"
  [credentials]
  {:Authorization (str "OAuth realm=\"http://trello.com\","
                       (credentials-to-string credentials))})

(defn get-json
  "Takes an http response and returns the body as a clojure datatype"
  [response]
  (cheshire/decode (:body response) true))

(defn get-url
  "Generates the url requires given the RESTful extension (ie
  \"cards\")"
  [req]
  (str trello-base-url
       req))

(defn post
  "Performs a POST request on a trello uri"
  [body client uri]
  (let [credentials (get-credentials client :POST uri)]
    (httpclient/post uri {:headers (wrap-credentials credentials)
                          :content-type :json
                          :body (cheshire/encode body)})))

(defn get
  "Performs a GET request on a trello uri"
  [uri client]
  (let [credentials (get-credentials client :GET uri)]
    (get-json (httpclient/get uri {:headers (wrap-credentials credentials)}))))

(defn get-lists
  "Requests a sequence of the lists on a board"
  [board client]
  (get (get-url (str "boards/" board "/lists")) client))

(defn post-card
  "Posts a new card on a list"
  [card client]
  (-> card
      (post client (get-url "cards"))
      (:body)
      (cheshire/decode true)))
