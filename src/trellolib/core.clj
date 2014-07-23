(ns trellolib.core
  (:require [trellolib.validation :refer :all]
            [cheshire.core :as cheshire]
            [clj-http.client :as httpclient]
            [clojure.string :as str]))

(def test-card {:name "hello" :desc "world" :idList "<yourid>"})
(def test-client {:key "<yourkey>"
                  :token "<yourtoken>"
                  :name "<yourapplicationname>"})

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

(def default-authorize-opts {:expiration "never"
                             :response_type "token"
                             :scope "read,write"})

(defn authorize-url
  "Generates the url to authorize the application to access users
  Trello."
  [client]
  (let [client (select-keys client [:key :name :expiration
                                    :response_type :scope])
        client (merge default-authorize-opts client)]
    (str trello-base-url
        "authorize/?" (encode-param client))))

(defn get-access
  "Gets access token from user"
  [client]
  (println "Go to this address and copy/paste the token here")
  (println (authorize-url client))
  (assoc client :token (read-line)))

(defn get-json
  "Takes an http response and returns the body as a clojure datatype"
  [url]
  (cheshire/decode (:body (httpclient/get url))))

(defn get-url
  "Generates the url requires given the RESTful extension (ie
  \"cards\")"
  [client req]
  (str trello-base-url
       req
       "?"
       (encode-param (select-keys client [:key :token]))))

(defn get-lists
  "Requests a sequence of the lists on a board"
  [client board]
  (get-url client (str "boards/" board "/lists")))

(defn trello-post
  "Performs a POST request on a trello uri"
  [body uri]
  (httpclient/post uri {:content-type :json
                        :body (cheshire/encode body)}))

(defn trello-get
  "Performs a GET request on a trello uri"
  [uri]
  (cheshire/decode (:body (httpclient/get uri))))

(defn post-card
  "Posts a new card on a list"
  [card client]
  (-> card
      (validate-client-keys client)
      (validate-card-keys)
      (validate-string-length :name)
      (validate-string-length :desc)
      (trello-post (get-url client "cards"))
      (:body)
      (cheshire/decode true)))
