(ns vocabulary.core
  (:require [vocabulary.bing :as bing])
  (:gen-class))

(defn -main [word-file]
  (let [words (-> (slurp word-file)
                  (clojure.string/split-lines))
        tfmt    (java.text.SimpleDateFormat. "yyyy-MM-dd")
        now     (java.util.Date.)
        output  (str "vocabulary-" (.format tfmt now) ".html")]

    (->> (apply bing/generate-html-page words)
         (spit output))

    (->> (clojure.java.io/resource "word.css")
         (slurp)
         (spit "word.css"))))



