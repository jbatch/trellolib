(ns trellolib.core
  (:require [trellolib.validation :refer :all]
            [cheshire.core :as cheshire]
            [clj-http.client :as httpclient]))

(def test-card {:name "hello" :desc "world" :idList "53c8c8207dfb7da6d039cb95"})
(def test-client {:key "<yourkey>"
                  :token "<yourtoken>"
                  :name "<yourapplicationname>"})

(defn authorize-url
  "Generates the url to authorize the application to access users
  trello"
  [client]
  (str "https://trello.com/1/authorize"
       "/?key="
       (:key client)
       "&name=" (:name client)
       "&expiration=never"
       "&response_type=token&scope=read,write"))

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
  "Generates the url requires given the RESTful extension (ie \"cards\")"
  [client req]
  (str "https://trello.com/1/"
       req
       "?key=" (:key client)
       "&token=" (:token client)))

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
      (cheshire/decode)))
