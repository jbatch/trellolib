(ns trellolib.ring
  "Namespace for the Ring middleware for Trello auth."
  (:require [ring.util.response :refer [redirect]]
            [trellolib.core :as trello]))

(defn token
  "Returns the current Trello token, otherwise."
  [request]
  (or ((:session request {}) :trello-token)
      ((:query-params request {}) "token")))

(defn wrap-trello-auth
  "Checks if the current user is authenticated with Trello, if not, it
  will issue a redirect to the Trello authentication URL. If they're
  returning from Trello with an OAuth token it will save the token and
  allow the user to proceed.
  Uses sessions for storing token, must have wrap-session from Ring.
  Uses params, wrap-params needed.
  The apps key and name are passed in via the client argument."
  [handler client]
  (fn [request]
    (if-not (token request)
      (redirect (:authorize-url (trello/authorize-client client)))
      (assoc-in (handler request) [:session :trello-token] (token request)))))
