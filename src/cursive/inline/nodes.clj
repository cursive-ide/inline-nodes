(ns cursive.inline.nodes
  "Example of a simple DSL that you might use to create inline viewers. Note that there's
   nothing special about these functions, feel free to use them or create others which make
   more sense for your use case.")

(defn with-children [params children]
  (cond-> params
    (not (empty? children))
    (assoc :children (into []
                           (comp
                             (mapcat #(if (seq? %) % [%]))
                             (remove nil?))
                           children))))

(defn node
  "Creates a node from params, adding children if passed."
  [params & children]
  (tagged-literal 'cursive/node (with-children params children)))

(defn title-node
  "Creates a node using text, adding children if passed."
  [text & children]
  (tagged-literal 'cursive/node
                  (with-children {:presentation [{:text text}]}
                                 children)))

(defn link-node
  "Creates a node styled like a link, which will open a browser to URL when activated."
  [text url]
  (tagged-literal 'cursive/node
                  {:presentation [{:text  text
                                   :color :link}]
                   :action       :browse
                   :url          url}))

(defn comment-node [text]
  (tagged-literal 'cursive/node
                  {:presentation [{:text  text
                                   :color :comment}]}))
