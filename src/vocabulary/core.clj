(ns vocabulary.core
  (:require [vocabulary.dictionary :as dict]
            [garden.core :refer [css]]
            [hiccup.page :as page]
            [hiccup.core :refer [html]]
            [vocabulary.dictionary :as dict])  
  (:gen-class))

(def ^:dynamic *max-retry-count* 3)

;;; see
;;; http://stackoverflow.com/questions/1879885/clojure-how-to-to-recur-upon-exception
(defn try-times*
  [n thunk]
  (loop [n n]
    (if-let [result
             (try (thunk)
                  (catch Exception e (when (zero? n) (throw e))))]
      result
      (recur (dec n)))))

(defmacro try-times
  [n & body]
  `(try-times* ~n (fn [] ~@body)))


(defn- dictionary
  [id]
  (dict/get-dictionary id))


(defn- search
  [d word]
  (try
    (try-times *max-retry-count* (d word))
    (catch Exception e (dict/invalid-word word))))

(defn- word-hiccuper [word]
  [:div.item
   [:div.word (dict/word word)]
   [:div.pronounce.en
    (for [[id pron] (dict/pronounce word)]
      [:span.en {:class (get {:en "en", :us "us"} id)} pron])]
   [:ul.mean
    (for [{:keys [pos def]} (dict/mean word)]
      [:li [:span.en pos] [:span.cn def]])]
   [:div.sample
    (for [{:keys [cn en]} (dict/sentences word)]
      [:ul.sentence [:li.en en] [:li.cn cn]])]])

(defn- generate-css
  []
  (css
   []
   
   [:body
    {:font-size "15pt"}]
   
   [:.cn
    {:font-family "'WenQuanYi Zen Hei Sharp', 'Noto Sans CJK SC Light', 'SimSun'"}]
   
   [:.en
    {:font-family "'Noto Serif', 'Arial'"}]

   [:.pronounce
    [:span
     {:color      "green"
      :padding    "5pt"
      :margin     "20pt auto 20pt auto"}]]
   
   [:.item
    {:border    "5pt"
     :background-color "white"}]
   
   [:.word
    {:font-family "Courier"
     :color       "blue"
     :font-size   "120%"}]

   [:.sentence
    [:#cn
     {:color      "grey"}]]
   
   [:ul
    {:margin-left "10pt"}]
   
   [:br
    {:color       "black"}]))

(defn generate-html-page [d words]
  (page/xhtml
   [:head
    [:title "vocabulary"]
    [:style (generate-css)]
    [:meta {:charset "UTF-8"}]]
   [:body
    (interpose
     [:hr]
     (pmap
      #(word-hiccuper (search d %))
      words))]))

(defn -main
  [dictionary-id word-file]
  (let [words  (clojure.string/split-lines (slurp word-file))
        tfmt   (java.text.SimpleDateFormat. "yyyy-MM-dd")
        now    (.format tfmt (java.util.Date.))
        title  (str "vocabulary-" now)
        output (str title ".html")
        dic    (dictionary dictionary-id)]
    (->> (generate-html-page dic words)
         (spit output))))







