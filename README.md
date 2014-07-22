# trellolib

A Clojure library designed to interact with trello through clojure

# Latest Version
[![Clojars Project](http://clojars.org/trellolib/latest-version.svg)](http://clojars.org/trellolib)

## Installing

add `[trellolib "0.1.0-SNAPSHOT"]` as a Leiningen dependency to get the latest release

## Running Tests

run `lein midje` from the command line

## Example
``` clojure
(require '[trellolib :as trello])

; First you will need to create a client to test
(def test-client {:key <your key>
                  :name <yourname})

; Now you can use this to generate the url your user needs to go to
; to authorize your application

(trello/authorize-url test-client)

; Your user will go to that url and *hopefully* let your application access
; thier Trello account. They will be given a key to give back to you. You
; can now update your client to include this

(def test-client {:key <your key>
                  :name <yourname>
                  :token <tokenfromuser>})

; Next we need a card to post
(def test-card {:name "This is my test card"
                :desc "This is the description of my test card"
                :idList <id of list you want to put card on>})

;now we can simply post our card
(trello/post-card test-card test-client)
```


## License

Copyright © 2014 Josh Batchelor

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
