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

; If you don't know your application id you can get it from Trello
; [here](https://trello.com/1/appKey/generate)

; Now you can use this to generate the url your user needs to go to
; to authorize your application

(trello/authorize-url test-client)

; Your user will go to that url and *hopefully* let your application access
; thier Trello account. They will be given a key to give back to you. You
; can now update your client to include this

(def test-client {:key <your key>
                  :name <yourname>
                  :token <tokenfromuser>})

; Now we need the id of the list we want to post the card to
; If you don't know this you can get all the lists from a board
; like so

(trello/trello-get (trello/get-lists test-client "<yourboardid>"))

; You can get a board id by going to the board you like and copying
; it from the URL. It will look like https://trello.com/b/<boardid>/hello-world

; With this we can now create our card
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
