(ns cursive.inline.nodes
  "Example of a simple DSL that you might use to create inline viewers. Note that there's
   nothing special about these functions, feel free to use them or create others which make
   more sense for your use case.")

(defn node [params & children]
  (tagged-literal 'cursive/node
                  (assoc params :children (into []
                                                (remove nil?)
                                                (flatten children)))))

(defn labeled-forms [label & forms]
  (tagged-literal 'cursive/node
                  {:presentation (into [{:text label}]
                                       (comp
                                         (remove nil?)
                                         (map (fn [form]
                                                {:form form}))
                                         (interpose {:text " "}))
                                       forms)
                   :children     (remove nil? forms)}))

(defn title-node [text & children]
  (let [params (if (map? (first children))
                 (first children)
                 nil)
        remainder (if params (rest children) children)]
    (tagged-literal 'cursive/node
                    (merge {:presentation [{:text text}]
                            :children     (remove nil? remainder)}
                           params))))

(defn link-node [text url]
  (tagged-literal 'cursive/node
                  {:presentation [{:text  text
                                   :color :link}]
                   :action       :browse
                   :url          url}))

(defn update-node
  ([n f])
  ([n f x])
  ([n f x y])
  ([n f x y & args]))
