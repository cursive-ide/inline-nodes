(ns cursive.inline.malli
  "This namespace contains functions which replicate how Malli pretty prints
   its errors, but instead of creating a fipp document, we'll create tree nodes
   for our inline viewer instead. You can compare how the -format
   implementations work to how the malli.dev.virhe/-format ones work. The
   structure is essentially the same."
  (:refer-clojure :exclude [format])
  (:require [cursive.inline.nodes :refer :all]
            [malli.core :as m]
            [malli.error :as me]))

; We duplicate slightly modified versions of some Malli internal functions here:

(defn -errors [explanation]
  (for [error (->> explanation (me/with-error-messages) :errors)]
    (into {} error)))

(defn -explain [schema value]
  (-errors (m/explain schema value)))

(defmulti -format (fn [e _] (-> e (ex-data) :type)) :default ::default)

(defn -hierarchy [^Class k]
  (loop [sk (.getSuperclass k), ks [k]]
    (if-not (= sk Object)
      (recur (.getSuperclass sk) (conj ks sk))
      ks)))

(defmethod -format ::default [e data]
  (if-let [-format (some (methods -format) (-hierarchy (class e)))]
    (-format e data)
    (node {:presentation [{:text  "Unknown Error"
                           :color :error}]}
          (node {:presentation [{:text "Type: "}
                                {:form (type e)}]})
          (node {:presentation [{:text "Message: "}
                                {:text  (ex-message e)
                                 :color :string}]})
          (when data
            (node {:presentation [{:text "Ex-data: "}]}
                  data)))))

(defn format
  "Format a Malli exception for inline viewing in Cursive."
  [e]
  (-format e (-> e (ex-data) :data)))

(defmethod -format ::m/explain [_ {:keys [schema] :as explanation}]
  (title-node "Explain"
    (title-node "Value: " (me/error-value explanation))
    (title-node "Errors: " (me/humanize (me/with-spell-checking explanation)))
    (title-node "Schema: " schema)
    (link-node "More information" "https://cljdoc.org/d/metosin/malli/CURRENT")))

(defmethod -format ::m/coercion [_ {:keys [explain]}]
  (format (m/-exception ::m/explain explain)))

(defmethod -format ::m/invalid-input [_ {:keys [args input fn-name]}]
  (title-node "Invalid Function Input"
    (apply title-node "Invalid function arguments: " args)
    (when fn-name (title-node "Function Var: " fn-name))
    (title-node "Input Schema: " input)
    (title-node "Errors: " (-explain input args))
    (link-node "More information" "https://cljdoc.org/d/metosin/malli/CURRENT/doc/function-schemas")))

(defmethod -format ::m/invalid-output [_ {:keys [value args output fn-name]}]
  (title-node "Invalid Function Output"
    (title-node "Invalid function return value: " value)
    (when fn-name (title-node "Function Var: " fn-name))
    (apply title-node "Function arguments: " args)
    (title-node "Output Schema: " output)
    (title-node "Errors: " (-explain output value))
    (link-node "More information" "https://cljdoc.org/d/metosin/malli/CURRENT/doc/function-schemas")))

(defmethod -format ::m/invalid-guard [_ {:keys [value args guard fn-name]}]
  (title-node "Function Guard Error"
    (when fn-name (title-node "Function Var: " fn-name))
    (title-node "Guard arguments: " args value)
    (title-node "Guard schema: " guard)
    (title-node "Errors: " (-explain guard [args value]))
    (link-node "More information" "https://cljdoc.org/d/metosin/malli/CURRENT/doc/function-schemas")))

(defmethod -format ::m/invalid-arity [_ {:keys [args arity schema fn-name]}]
  (title-node "Invalid Function Arity"
    (apply title-node (str "Invalid function arity (" arity "): ") args)
    (title-node "Function Schema: " schema)
    (when fn-name (title-node "Function Var: " fn-name))
    (link-node "More information" "https://cljdoc.org/d/metosin/malli/CURRENT/doc/function-schemas")))

(defmethod -format ::m/child-error [_ {:keys [type children properties] :as data}]
  (let [form (m/-raw-form type properties children)
        constraints (reduce (fn [acc k] (if-let [v (get data k)] (assoc acc k v) acc)) nil [:min :max])
        size (count children)]
    (title-node "Schema Creation Error"
      (title-node "Invalid Schema " form)
      (title-node "Reason"
        (node {:presentation [{:text (str "Schema has " size
                                          (if (= 1 size) " child" " children")
                                          ", expected: ")}
                              {:form constraints}]})
        constraints)
      (link-node "More information" "https://cljdoc.org/d/metosin/malli/CURRENT/doc/function-schemas"))))
