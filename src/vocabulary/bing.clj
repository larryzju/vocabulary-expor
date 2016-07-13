(ns vocabulary.bing
  (:require [jsoup.soup :as soup]
            [hiccup.page :as page]
            [hiccup.core :refer [html]])
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


(defn search-word-once [word]
  (let [params (URLEncoder/encode word)
        url (str "http://cn.bing.com/dict/search?q=" params)
        root (soup/get! url)]
    {:word (get-word root)
     :pronounce (get-pronounce root)
     :mean (get-mean root)
     :sentences (get-sample-sentences root)}))


(defn search-word
  ([word] (search-word word 3))
  ([word cnt]
   (if (zero? cnt)
     nil
     (try (search-word-once word)
          (catch Exception e
            (search-word word (dec cnt)))))))


(defn- word-hiccuper [word]
  [:div#item
   [:div#word (:word word)]
   [:div#pronounce
    (for [[id pron] (:pronounce word)] [(str "span#" (name id)) pron])]
   [:ul#mean
    (for [{:keys [pos def]} (:mean word)]
      [:li [:span.pos pos] [:span.def def]])]
   [:div#sample
    (for [{:keys [cn en]} (:sentences word)]
      [:ul.sentence [:li#en en] [:li#cn cn]])]])

(defn generate-html-page [& words]
  (page/xhtml
   [:head
    (page/include-css "word.css")
    [:meta {:charset "UTF-8"}]]
   [:body
    (interpose
     [:hr]
     (pmap (fn [w] (word-hiccuper (search-word w))) words ))]))



