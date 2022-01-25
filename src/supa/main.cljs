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
            [supa.views :as v]))

(goog-define supabase-url "")
(goog-define supabase-key "")

(def state (reagent/atom {:route nil}))

(def sb (sb/client supabase-url supabase-key))

(defn app []
  (when-let [{{view :view} :data} (:route @state)]
    [view])
  #_[:pre (with-out-str (pprint/pprint @state))])

(o/defstyled foo :div
  {:color :blue})

(o/defstyled home-page v/main
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

(o/defstyled user-form v/main
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
     [:form {:on-submit #(on-submit @u @p)}
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
                           (p/let [res (sb/sign-up sb {:email email :password password})]
                             (swap! state assoc :res res))
                           (js/console.log "register" email password)
                           )}])

(def routes [["/" {:name :home :view home-page}]
             ["/register" {:name :register :view register}]])

(defn main []
  (rfe/start!
   (rf/router routes)
   (fn [m]
     (swap! state assoc :route m))
   {:use-fragment true})
  (reagent-dom/render [app] (dom/el-by-id "app")))


(main)

(sb/user sb)

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
