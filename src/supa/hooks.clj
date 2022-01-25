(ns supa.hooks
  (:require [lambdaisland.ornament :as o]
            [garden.compiler :as gc]
            [supa.styles :as styles]
            [girouette.tw.preflight :as girouette-preflight]))

(defn write-styles-hook
  {:shadow.build/stage :flush}
  [build-state & args]
  (require 'supa.styles :reload)
  (spit "resources/public/styles.css"
        (str
         (gc/compile-css (concat
                          girouette-preflight/preflight
                          styles/global-styles))
         "\n"
         (o/defined-styles)))
  build-state)
