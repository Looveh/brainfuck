(ns brainfuck
  (:require [clojure.core.match :refer [match]]))

;; Return index of the matching bracket at index ip in source code c.
(defn matching-bracket-idx [ip c]
  (let [[t t' d] (if (= (get c ip) \[) [\[ \] 1] [\] \[ -1])]
    (loop [ip (+ ip d) n 0]
      (match [(get c ip) n]
             [t' 0] ip
             [t' _] (recur (+ ip d) (dec n))
             [t  _] (recur (+ ip d) (inc n))
             :else  (recur (+ ip d) n)))))

;; Map operators to functions that update a state.
(def ops {\> #(update-in % [:dp] inc)
          \< #(update-in % [:dp] dec)
          \+ #(update-in % [:t (:dp %)] (fnil inc 0))
          \- #(update-in % [:t (:dp %)] (fnil dec 0))
          \[ #(when (zero? ((:t %) (:dp %) 0))
                (update-in % [:ip] matching-bracket-idx (:c %)))
          \] #(when-not (zero? ((:t %) (:dp %) 0))
                (update-in % [:ip] matching-bracket-idx (:c %)))
          \. #(print (char ((:t %) (:dp %) 0)))
          \, #(update-in % [:t (:dp %)] (byte (.read System/in)))})

;; Given a program state generate the next state. May produce side effects.
(defn step [state]
  (update-in (if-let [new-state ((ops (get (:c state) (:ip state)) identity) state)]
               new-state state)
             [:ip] inc))

;; Lazily generate a sequence of states from source code.
;; Fetching a state may produce side effects.
(defn state-seq [source-code]
  ((fn generate [state]
     (when (< (:ip state) (count (:c state)))
       (cons state (lazy-seq (generate (step state))))))
   {:c (apply str (filter ops source-code)) :t {} :ip 0 :dp 0}))

;; Interpret and run source code.
(defn run [source-code]
  (dorun (state-seq source-code)))
