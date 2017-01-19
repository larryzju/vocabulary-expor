(ns vocabulary.dictionary
  (:gen-class))


(defprotocol Word
  (word       [this])
  (pronounce  [this])
  (mean       [this])
  (sentences  [this]))


(defn invalid-word
  [raw-word]
  (reify Word
    (word       [this] raw-word)
    (pronounce  [this] [])
    (mean       [this] [])
    (sentences  [this] [])))


(defn get-dictionary
  [id]
  ;; how to avoid hard coding namespace??
  (let [ns-str (format "vocabulary.dictionary.%s" id)]
    (require (symbol ns-str))
    (var-get (ns-resolve (symbol ns-str) 'search))))




