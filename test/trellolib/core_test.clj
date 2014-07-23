(ns trellolib.core-test
  (:require [midje.sweet :refer :all]
            [trellolib.core :as core]
            [trellolib.validation :refer :all]))


(def test-card {:name "hello" :desc "world" :idList "<youridhere>"})
(def test-card-with-errors {:name "hello"
                            :desc "world"
                            :idList "<youridhere>"
                            :errors '({:a "old"})})
(def test-client {:key "<yourkey>"
                  :token "<yourtoken>"})

(facts "Core Tests"
       (fact "key-exists confirms key exists in hashmap"
             (key-exists test-card test-client :key) => test-card
             (key-exists test-card test-client :token) => test-card
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
                                      :field ":name"})})))
