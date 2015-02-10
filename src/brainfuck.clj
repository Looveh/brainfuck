(ns brainfuck
  (:require [clojure.core.match :refer [match]]))

(defn matching-bracket-idx [ip c]
  "Returns the index of the matching bracket found at index ip in source code string c."
  (let [[t t' d] (if (= (get c ip) \[) [\[ \] 1] [\] \[ -1])]
    (loop [ip (+ ip d) n 0]
      (match [(get c ip) n]
             [t' 0] ip
             [t' _] (recur (+ ip d) (dec n))
             [t  _] (recur (+ ip d) (inc n))
             :else  (recur (+ ip d) n)))))

(def ops
  "Map operators to functions that update state."
  {\> #(update-in % [:dp] inc)
   \< #(update-in % [:dp] dec)
   \+ #(update-in % [:t (:dp %)] (fnil inc 0))
   \- #(update-in % [:t (:dp %)] (fnil dec 0))
   \[ #(when (zero? ((:t %) (:dp %) 0))
         (update-in % [:ip] matching-bracket-idx (:c %)))
   \] #(when-not (zero? ((:t %) (:dp %) 0))
         (update-in % [:ip] matching-bracket-idx (:c %)))
   \. #(print (char ((:t %) (:dp %) 0)))
   \, #(update-in % [:t (:dp %)] (byte (.read System/in)))})

(defn step [{c :c ip :ip :as state}]
  "Return the next state from a given state. May produce side effects."
  (update-in (if-let [new-state ((ops (get c ip) identity) state)]
               new-state state)
             [:ip] inc))

(defn state-seq [source-code]
  "Lazily generate a sequence of states from a string of source code.
  Evaluating a state may produce side effects."
  ((fn generate [{c :c ip :ip :as state}]
     (when (< ip (count c))
       (cons state (lazy-seq (generate (step state))))))
   {:c (apply str (filter ops source-code)) :t {} :ip 0 :dp 0}))

(defn run [source-code]
  "Interpret and run source code."
  (dorun (state-seq source-code)))
