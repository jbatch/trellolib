(ns trellolib.validation)

(defn add-error
  "Add an error to a card"
  [card error]
  (assoc card :errors (conj (seq (:errors card)) error)))

(defn key-exists
  "Checks that a given hashmap contains an expected key"
  [card client key]
  (if-not (contains? client key)
    (add-error card {:desc "Expected key not found"
                     :field (str key)})
    card))

(defn validate-client-keys
  "Validates that a client has the required keys"
  [card client]
  (-> card
      (key-exists client :credentials)))

(defn validate-card-keys
  "Validates that a card has the required keys"
  [card]
  (-> card
      (key-exists card :name)
      (key-exists card :desc)
      (key-exists card :idList)))

(defn validate-string-length
  "Validates that a strings length is between 1 and 16384"
  [card key]
  (let [len (count (key card))]
    (if-not (and (pos? len)
                 (< len 16385))
      (add-error card
                 {:desc "String length not in the range 1-16384"
                  :field (str key)}) card)))
