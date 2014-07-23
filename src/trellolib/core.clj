(ns trellolib.core
  (:require [trellolib.validation :refer :all]
            [cheshire.core :as cheshire]
            [clj-http.client :as httpclient]
            [clojure.string :as str]
            [oauth.client :as oauth]))

(def trello-base-url "https://trello.com/1/")

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
  request-token and autorize-url attached"
  [client]
  (let [consumer (oauth/make-consumer (:key client)
                                      (:secret client)
                                      (str trello-base-url
                                           "OAuthGetRequestToken")
                                      (str trello-base-url
                                           "OAuthGetAccessToken")
                                      (str trello-base-url
                                           "OAuthAuthorizeToken")
                                      :hmac-sha1)
        request-token (oauth/request-token consumer (:callback client))
        authorize-url (str (oauth/user-approval-uri consumer
                                                    (:oauth_token request-token))
                           "&name=MyApp&expiration=never&scope=read,write")]
    (-> client
        (assoc :consumer consumer)
        (assoc :request-token request-token)
        (assoc :authorize-url authorize-url))))

(authorize-client test-client)

(defn get-access-token
  "Takes a client and a verifier and associates the client with an
  access token response "
  [client verifier]
  (let [access-token-response (oauth/access-token (:consumer client)
                                                  (:request-token client)
                                                  verifier)]
    (assoc client :access-token access-token-response)))

(get-access-token (authorize-client test-client) "2185eacd2ccfebe76bfeb0a6dae672f7")

(defn get-credentials
  "Takes a client and associates it with OAuth credentials for a given request"
  [client request-type uri]
  (let [credentials (oauth/credentials (:consumer client)
                                       (:oauth_token (:access-token client))
                                       (:oauth_token_secret (:access-token client))
                                       request-type
                                       uri)]
    (assoc client :credentials credentials)))


(def default-authorize-opts {:expiration "never"
                             :response_type "token"
                             :scope "read,write"})

(defn authorize-url-old
  "Generates the url to authorize the application to access users
  Trello."
  [client]
  (let [client (select-keys client [:key :name :expiration
                                    :response_type :scope])
        client (merge default-authorize-opts client)]
    (str trello-base-url
         "authorize/?" (encode-param client))))

(defn wrap-credentials
  "Wraps crediantials in an Authorization header"
  [credentials]
  {:Authorization (str "OAuth realm=\"http://trello.com\","
                       (credentials-to-string credentials))})

(defn get-json
  "Takes an http response and returns the body as a clojure datatype"
  [url]
  (cheshire/decode (:body (httpclient/get url))))

(defn get-url
  "Generates the url requires given the RESTful extension (ie
  \"cards\")"
  [req]
  (str trello-base-url
       req))

(defn get-lists
  "Requests a sequence of the lists on a board"
  [board]
  (get-url (str "boards/" board "/lists")))

(defn trello-post
  "Performs a POST request on a trello uri"
  [body client uri]
  (httpclient/post uri {:headers (wrap-credentials (:credentials client))
                        :content-type :json
                        :body (cheshire/encode body)}))

(defn trello-get
  "Performs a GET request on a trello uri"
  [uri client]
  (cheshire/decode (:body
                    (httpclient/get
                     uri
                     {:headers
                      (wrap-credentials (:credentials client))}))))

(defn post-card
  "Posts a new card on a list"
  [card client]
  (-> card
      (validate-client-keys client)
      (validate-card-keys)
      (validate-string-length :name)
      (validate-string-length :desc)
      (trello-post new-client (get-url "cards"))
      (:body)
      (cheshire/decode true)))
