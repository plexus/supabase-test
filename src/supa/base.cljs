(ns supa.base
  (:require ["@supabase/supabase-js" :as supabase]
            [applied-science.js-interop :as j]
            [cljs-bean.core :as bean]
            [clojure.string :as str]
            [kitchen-async.promise :as p]))

(defn deep-bean [^js obj]
  (when (some? obj)
    (bean/bean obj :recursive true)))

(defn beanp [^js promise]
  (.then promise deep-bean))

(defn client
  ([api-url supabase-key]
   (supabase/createClient api-url supabase-key))
  ([api-url supabase-key opts]
   (supabase/createClient api-url supabase-key (bean/->js opts))))

(defn auth-method [method ^js client & args]
  (let [auth (.-auth client)]
    (.apply (j/get auth method) auth (into-array (map bean/->js args)))))

(def user (comp deep-bean (partial auth-method :user)))
(def sign-up (comp beanp (partial auth-method :signUp)))
(def sign-in (comp beanp (partial auth-method :signIn)))
(def sign-out (comp beanp (partial auth-method :signOut)))
(def verify-otp (comp beanp (partial auth-method :verifyOTP)))
(def set-user-attrs (comp beanp (partial auth-method :update)))
(def reset-password-for-email (comp beanp (partial auth-method :resetPasswordForEmail)))
(def invite-user-by-email (comp beanp (partial auth-method :inviteUserByEmail)))

(def operators
  {"=" "eq"
   ">" "gt"
   ">=" "gte"
   "<" "lt"
   "<=" "lte"
   "!=" "neq"
   "<>" "neq"
   "@@" "wfts"
   "@>" "cs"
   "<@" "cd"
   "&&" "ov"
   "<<" "sl"
   ">>" "sr"
   "&<" "nxr"
   "&>" "nxl"
   "-|-" "adj"})

(def prefix-op #{"or" "and"})

(defn resolve-op [op]
  (or (get operators op)
      (get operators (name op))
      (name op)))

(declare render-filters)

(defmulti render-filter (fn [[op _lhs _rhs]]
                          (str/replace (resolve-op op) #"^not\." "")))

(defn quote-str [s]
  (let [s (if (keyword? s)
            (.-fqn s)
            s)]
    (str "\""
         (-> s
             (str/replace #"/" "\\\\")
             (str/replace #"\"" "\\\""))
         "\"")))

(defmethod render-filter :default [[op & rest]]
  (let [op (resolve-op op)]
    (if (prefix-op op)
      (str op (render-filters rest))
      (let [[lhs rhs] rest]
        (str (quote-str lhs)
             "." op
             "." (if (string? rhs)
                   (quote-str rhs)
                   rhs))))))

(defmethod render-filter "not" [[_ [op & rest]]]
  (let [op (resolve-op op)]
    (if (prefix-op op)
      (str "not." op (render-filters rest))
      (render-filter (into [(str "not." op)] rest)))))

(defn render-field-list [fields]
  (str (str/join "," (map quote-str fields))))

(defmethod render-filter "in" [[op col vals]]
  (let [op (resolve-op op)]
    (str (quote-str col)
         "." op
         ".(" (render-field-list vals) ")")))

(defn render-filters [fs]
  (str "("
       (str/join "," (map render-filter fs))
       ")"))

(defn append-query-param [^js qry-builder k v]
  (doto (.-searchParams (.-url qry-builder))
    (.append k v))
  qry-builder)

(defn query
  "Query the database (all CRUD operations). Takes a HoneySQL-style map.
  - :select / :from / :where
  - :insert-into / :values
  - :update / :set
  - :delete-from / :where"
  [^js client m]
  (cond-> (.from client (or (:from m)
                            (:insert-into m)
                            (:delete-from m)
                            (:update m)))
    (:select m)
    (.select (render-field-list (:select m)))
    (:values m)
    (.insert (bean/->js (:values m)))
    (:delete-from m)
    (.delete)
    (:set m)
    (.update (:set m))
    (:where m)
    (append-query-param "and" (render-filters [(:where m)]))
    :->
    beanp))

;; [:in "name" ["Paris" "Tokyo"]]
;; [:in "countries.1name" ["France" "Japan"]]
;; [:= "age" 13]
;; [:and
;; [:>= "age" 18]
;; [:= "student" true]]

(comment
  (render-filter [:not [:or [:in :capital ["Brussels" "Paris" ]]]])
  (render-filter [:in :capital ["Brussels" "Paris" ]]))
