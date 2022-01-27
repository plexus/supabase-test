(ns supa.views
  (:require [lambdaisland.ornament :as o]))

(o/defstyled group :div
  :bg-gray-200
  :p-2
  [:>.caption
   :text-xl
   :mb-2]
  ([caption & elements]
   `[:<>
     [:h2.caption ~caption]
     ~@elements]))

(o/defstyled button :button
  :border-1 :border-black
  :p-2 :m-1 :rounded)

(o/defstyled button-link :a
  :border-1 :border-black
  :p-2 :m-1 :rounded)

(o/defstyled v-center :div
  :flex :h-screen :text-center
  [:>* :m-auto])

(o/defstyled stack :div
  :flex :flex-col)

(o/defstyled flow :div
  :flex)

(o/defstyled shout :div
  :text-xl
  :mb-2)

(o/defstyled input-field :div
  :table-row
  [:label :table-cell :w-1]
  [:input :table-cell :w-full]
  ([caption opts]
   (with-meta
     [:<> [:label {:for (:id opts)} caption] [:input opts]]
     opts)))
