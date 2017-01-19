(ns vocabulary.dictionary.bing
  (:require [vocabulary.dictionary :as dict]
            [jsoup.soup :as soup])
  (:import [java.net URLEncoder]))


(defn- get-sample-sentences [root]
  (->> (.select root "#sentenceSeg .se_li1")
       (map (fn [li1]
              {:en (-> (.select li1 ".sen_en") .text)
               :cn (-> (.select li1 ".sen_cn") .text)}))))

(defn- get-word [root]
  (-> (.select root "#headword")
      (.text)))


(defn- get-pronounce [root]
  {:us (. (.select root ".hd_prUS") text)
   :en (. (.select root ".hd_pr") text)})


(defn- get-mean [root]
  (->> (.select root ".qdef > ul:nth-child(2) li")
       (map (fn [li]
              {:pos (-> (.select li ".pos") .text)
               :def (-> (.select li ".def") .text)}))))


(defn search
  [word]
  (let [params (URLEncoder/encode word)
        url (str "http://cn.bing.com/dict/search?q=" params)
        root (soup/get! url)]
    (reify dict/Word
      (word   [this]  (get-word root))
      (pronounce [this] (get-pronounce root))
      (mean      [this] (get-mean      root))
      (sentences [this] (get-sample-sentences root)))))











