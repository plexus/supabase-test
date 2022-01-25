(ns supa.base
  (:require ["@supabase/supabase-js" :as supabase]
            [kitchen-async.promise :as p]
            [applied-science.js-interop :as j]
            [cljs-bean.core :as bean]))

(defn beanp [^js promise]
  (.then promise bean/bean))

(defn client
  ([api-url supabase-key]
   (supabase/createClient api-url supabase-key))
  ([api-url supabase-key opts]
   (supabase/createClient api-url supabase-key (bean/->js opts))))

(defn auth-method [method ^js client & args]
  (let [auth (.-auth client)]
    (.apply (j/get auth method) auth (into-array (map bean/->js args)))))

(def user (partial auth-method :user))
(def sign-up (partial auth-method :signUp))
(def sign-in (partial auth-method :signIn))
(def set-user-attrs (partial auth-method :update))
