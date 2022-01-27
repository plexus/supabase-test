(ns supa.easy
  (:require [supa.base :as sb]))

(def client (atom nil))

(defn connect! [& args]
  (reset! client (apply sb/client args)))

(defn wrap-fn [f]
  (fn [& args]
    (apply f @client args)))

(def user (wrap-fn sb/user))
(def sign-up (wrap-fn sb/sign-up))
(def sign-in (wrap-fn sb/sign-in))
(def sign-out (wrap-fn sb/sign-out))
(def verify-otp (wrap-fn sb/verify-otp))
(def set-user-attrs (wrap-fn sb/set-user-attrs))
(def reset-password-for-email (wrap-fn sb/reset-password-for-email))
(def invite-user-by-email (wrap-fn sb/invite-user-by-email))
(def query (wrap-fn sb/query))
