# trellolib

A Clojure library designed to interact with trello through clojure

# Latest Version
[![Clojars Project](http://clojars.org/trellolib/latest-version.svg)](http://clojars.org/trellolib)

## Installing

add `[trellolib "0.2.0-SNAPSHOT"]` as a Leiningen dependency to get the latest release

## Running Tests

run `lein midje` from the command line

## Example
``` clojure
(require '[trellolib :as trello])

; First you will need to create a client to test
(def test-client {:key <your key>
                  :secret <yoursecret>
                  :name <yourappname>
                  :callback <localhost>})

; If you don't know your application id you can get it from Trello
; at https://trello.com/1/appKey/generate

; Now you can use this to generate the url your user needs to go to
; to authorize your application

(def auth-client (trello/authorize-client test-client))
;{:consumer
; {:key "<yourkey>",
;  :secret
;  "<yoursecret>",
;  :request-uri "https://trello.com/1/OAuthGetRequestToken",
;  :access-uri "https://trello.com/1/OAuthGetAccessToken",
;  :authorize-uri "https://trello.com/1/OAuthAuthorizeToken",
;  :signature-method :hmac-sha1},
; :key "yourkey",
; :authorize-url
; "https://trello.com/1/OAuthAuthorizeToken?oauth_token=<new-token>&name=<yourappname>&expiration=never&scope=read,write",
; :name "<yourappname>",
; :secret
; "<yoursecret>",
; :request-token
; {:oauth_token "<temp-token>",
;  :oauth_token_secret "<temp-secret>",
;  :oauth_callback_confirmed "true"},
; :callback "<yourcallback>"}

; Your application can now direct the user to the url stored in :authorize-url so
; they can give your appilcation permission to access their protected resources.
; The user will be redirected to your :callback address and in their url parameters
; will be a verifier. You can now use this to swap your temporary token and secret
; for an access token and secret

(def access-client (trello/get-access-token test-client <verifier>))
;{:consumer
; {:key "<yourkey>",
;  :secret
;  "<yoursecret>",
;  :request-uri "https://trello.com/1/OAuthGetRequestToken",
;  :access-uri "https://trello.com/1/OAuthGetAccessToken",
;  :authorize-uri "https://trello.com/1/OAuthAuthorizeToken",
;  :signature-method :hmac-sha1},
; :key "18093394fb3a0e8ae6393a830df946d5",
; :authorize-url
; "https://trello.com/1/OAuthAuthorizeToken?oauth_token=<temp-token>&name=<yourappname>&expiration=never&scope=read,write",
; :name "OAuth App",
; :secret
; "<yoursecret>",
; :access-token
; {:oauth_token
;  "<youraccesstoken>",
;  :oauth_token_secret "<youraccesstokensecret>"},
; :request-token
; {:oauth_token "<temp-token>",
;  :oauth_token_secret "<temp-secret>",
;  :oauth_callback_confirmed "true"},
; :callback "<yourcallback>"}

; Now we need the id of the list we want to post the card to
; If you don't know this you can get all the lists from a board
; like so

(trello/trello-get (trello/get-lists "<yourboardid>") access-client)

; You can get a board id by going to the board you like and copying
; it from the URL. It will look like https://trello.com/b/<boardid>/hello-world

; With this we can now create our card
(def test-card {:name "This is my test card"
                :desc "This is the description of my test card"
                :idList <id of list you want to put card on>})

;now we can simply post our card
(trello/post-card test-card access-client)
```


## License

Copyright © 2014 Josh Batchelor

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
