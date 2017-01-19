(ns vocabulary.dictionary.youdao
  (:require [vocabulary.dictionary :as dict]
            [jsoup.soup :as soup]
            [clojure.string :as string])
  (:import  [java.net URLEncoder])
  (:gen-class))

(defn- get-pronounce
  [root]
  (let [known-pronounce-type {"英" :en, "美" :us}]
    (->> (.. root (select "div.baav span.pronounce"))
         (map #(.text %))
         (filter #(pos? (count %)))
         (map #(string/split % #"\s+" 2))
         (map (fn [[t p]] (vector (known-pronounce-type t t) p)))
         (into {}))))

(defn- get-mean
  [root]
  (->> (.. root (select "#phrsListTab > div.trans-container li"))
       (map #(->> (vector "" (.text %))
                  (zipmap [:pos :def])))))


(defn- get-senetences
  [root]
  (->> (.. root (select "#bilingual li"))
       (map (fn [li]
              (->> (take 2 (.select li "p"))
                   (map #(.text %))
                   (zipmap [:en :cn]))))))

(defn search
  [word]
  (let [params    (URLEncoder/encode word)
        url       (format "http://youdao.com/w/eng/%s" params)
        root      (soup/get! url)
        real-word (.. root (select "span.keyword") text)]
    (if-not (and (string? real-word) (pos? (count real-word)))
      (throw (IllegalArgumentException. word)))
    (reify dict/Word
      (word      [this]  real-word)
      (pronounce [this]  (get-pronounce root))
      (mean      [this]  (get-mean root))
      (sentences [this]  (get-senetences root)))))


