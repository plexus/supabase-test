(ns supa.main
  (:require ["@supabase/supabase-js" :as supabase]
            [applied-science.js-interop :as j]
            [clojure.pprint :as pprint]
            [kitchen-async.promise :as p]
            [lambdaisland.ornament :as o]
            [lambdaisland.thicc :as dom]
            [reagent.core :as reagent]
            [reagent.dom :as reagent-dom]
            [reitit.frontend :as rf]
            [reitit.frontend.easy :as rfe]
            [supa.base :as sb]
            [supa.easy :as se]
            [supa.views :as v]))


(goog-define supabase-url "")
(goog-define supabase-key "")

(se/connect! supabase-url supabase-key)

(def state (reagent/atom {:route nil
                          :user (se/user)}))

(o/defstyled inspector :pre
  :fixed :bottom-2 :right-2 :border-1 :border-slate-300
  :overflow-auto :max-w-4xl
  ([]
   (with-out-str (pprint/pprint @state))))

(defn app []
  [:div
   (when-let [{{view :view} :data} (:route @state)]
     [view])
   [inspector]])

(o/defstyled foo :div
  {:color :blue})

(o/defstyled flash :div
  )

(o/defstyled page-layout :main
  :px-4 :py-3
  :bg-gray-100
  :min-h-screen
  ([& children]
   [:<>
    (when-let [msg (:flash @state)]
      [flash msg])
    children]))

(o/defstyled welcome-page page-layout
  :flex :h-screen  [:>* :m-auto]
  :text-center
  [:>:div :flex :flex-col]
  [:h1 :text-2xl :mb-4]
  [:a v/button :min-w-20]
  [:.buttons :flex]
  ([]
   [:<>
    [:div
     [:h1 "Let's get started!"]
     [:div.buttons
      [:a {:href (rfe/href :register)} "Register"]
      [:a {:href (rfe/href :sign-in)} "Sign in"]]]]))

(o/defstyled home-page page-layout
  ([]
   [:p "Welcome"]))

(o/defstyled user-form page-layout
  :flex :h-screen [:>* :my-auto]
  [:label :p-3]
  [:* [:input :p-2]]
  [:>* :p-1]
  [:form :w-full]
  [:.fields :table :w-full :mb-4]
  [:.submit :block  {:margin "0 auto"}]
  ([{:keys [caption on-submit]}]
   (reagent/with-let [u (reagent/atom "")
                      p (reagent/atom "")]
     [:form {:on-submit #(do
                           (.preventDefault %)
                           (on-submit @u @p))}
      [:div.fields
       [v/input-field "Email" {:id "email" :size 1 :value @u
                               :on-change #(reset! u (.. % -target -value))}]
       [v/input-field "Password" {:id "password" :type "password" :size 1 :value @p
                                  :on-change #(reset! p (.. % -target -value))}]]
      [:input.submit {:type "submit" :value caption}]])))

(defn register []
  [user-form {:caption "Register"
              :on-submit (fn [email password]
                           (js/console.log "registering!")
                           (p/let [res (se/sign-up {:email email :password password})]
                             (swap! state assoc
                                    :res res
                                    :flash "Please follow the confirmation link in your inbox."))
                           (js/console.log "register" email password)
                           )}])

(defn sign-in []
  [user-form {:caption "Sign In"
              :on-submit (fn [email password]
                           (js/console.log "signing in!")
                           (p/let [res (se/sign-in {:email email :password password})]
                             (swap! state assoc
                                    :res res
                                    :user (:user res)
                                    :flash (str "Welcome " email))
                             (rfe/push-state :home))
                           (js/console.log "register" email password))}])

(def routes [["/" {:name :home :view home-page}]
             ["/welcome" {:name :welcome :view welcome-page}]
             ["/register" {:name :register :view register}]
             ["/sign-in" {:name :sign-in :view sign-in}]])

(defn main []
  (rfe/start!
   (rf/router routes)
   (fn [m]
     (swap! state assoc :route m))
   {:use-fragment true})
  (when-not (:user @state)
    (rfe/push-state :welcome))
  (reagent-dom/render [app] (dom/el-by-id "app")))

(defonce init (main))

(se/user)

(comment
  (def signup-res
    (auth-method client :signUp
                 #js {:email "q5zie5+a1tg078v7suv4@sharklasers.com"
                      :password "abc123"}))

  (p/then
   (auth-method client :signIn
                #js {:email "q5zie5+a1tg078v7suv4@sharklasers.com"
                     :password "abc123"})
   prn)


  (auth-method client :user)

  (p/let [p (auth-method client :update #js {:data #js {:foo "bar"}})]
    (def xxx p))

  {:user
   {:role "authenticated",
    :email "q5zie5+a1tg078v7suv4@sharklasers.com",
    :aud "authenticated",
    :confirmation_sent_at "2022-01-19T23:18:39.754252147Z",
    :user_metadata {},
    :phone "",
    :updated_at "2022-01-19T23:18:40.750424Z",
    :app_metadata {:provider "email", :providers ["email"]},
    :id "e8696105-8453-4cf8-aa99-2abe583ca235",
    :identities
    [{:id "e8696105-8453-4cf8-aa99-2abe583ca235",
      :user_id "e8696105-8453-4cf8-aa99-2abe583ca235",
      :identity_data {:sub "e8696105-8453-4cf8-aa99-2abe583ca235"},
      :provider "email",
      :last_sign_in_at "2022-01-19T23:18:39.749512018Z",
      :created_at "2022-01-19T23:18:39.750023Z",
      :updated_at "2022-01-19T23:18:39.750023Z"}],
    :created_at "2022-01-19T23:18:39.742987Z"},
   :session nil,
   :error nil}

  (js->clj
   #js {:user #js {:id "e8696105-8453-4cf8-aa99-2abe583ca235", :aud "authenticated", :role "authenticated", :email "q5zie5+a1tg078v7suv4@sharklasers.com", :phone "", :confirmation_sent_at "2022-01-19T23:18:39.754252147Z", :app_metadata #js {:provider "email", :providers #js ["email"]}, :user_metadata #js {}, :identities #js [#js {:id "e8696105-8453-4cf8-aa99-2abe583ca235", :user_id "e8696105-8453-4cf8-aa99-2abe583ca235", :identity_data #js {:sub "e8696105-8453-4cf8-aa99-2abe583ca235"}, :provider "email", :last_sign_in_at "2022-01-19T23:18:39.749512018Z", :created_at "2022-01-19T23:18:39.750023Z", :updated_at "2022-01-19T23:18:39.750023Z"}], :created_at "2022-01-19T23:18:39.742987Z", :updated_at "2022-01-19T23:18:40.750424Z"}, :session nil, :error nil} :keywordize-keys true
   ))

(comment
  (.-searchParams
   (.-url
    (.from @client "events")))

  (kitchen-async.promise/let [res
                              (sb/beanp
                               (.select (.from @client "countries") "*"))]
    (def xxx res))


  (user)
  (p/then
   (.select
    (.from @se/client "countries")
    "*"
    )
   #(def xxx %))
  (:data (sb/deep-bean xxx))
  (p/then
   (.insert
    (.from @se/client "countries")
    #js [#js {:name "France", :capital "Paris"}]
    )
   #(def xxx %))

  (let [q (.select (.from @se/client "countries") "*")]
    (.append (.-searchParams (.-url q))
             #_#_"\"bar-baz:baq\"" "eq.bcd"
             "and" "(and(\"foo.bar-baz:baq\".not.eq.\"abc\n\"))")
    (p/then q #(def yyy %))
    )

  (se/user)

  (p/then
   (se/query {:select ["*"]
              :from "countries"
              :where [:and [:not [:in :capital ["Brussels"]]]]})
   println
   println)
  (sb/render-filter  [:not [:or [:= :capital "Taipei"]]])
  (p/then
   (se/query {:insert-into "countries"
              :values [{:name "Taiwan" :capital "Taipei"}]})
   println
   println)

  (p/then
   (se/query {:update "countries"
              :set [{:capital "Taibei"}]})
   println
   println)

  {:select ["*"]
   :from "countries"
   :where [[:= :capital "Brussels"]]}

  (sb/render-filters [[:= :capital "Brussels"]]))
