(ns cursive.inline.scope-capture
  (:require [cursive.inline.nodes :refer :all]
            [sc.impl :as i]))

; There is a problem with this at the moment. The vars are interned into `*ns*`, which means
; that your REPL command will have to be executed from the namespace containing the `(spy)`.
; Unfortunately, scope capture doesn't store the namespace in the code site, so there's no
; way to fix this right now. I'm going to make a simpler version for use with Cursive since
; scope capture contains a lot of logging functionality which is no longer required when
; used in Cursive with the inline functionality.

(defn defsc [{:keys [ep-id]}]
  (let [cs (i/resolve-code-site ep-id)
        ep-id (i/resolve-ep-id ep-id)]
    (into []
          (map (fn [ln]
                 (intern *ns* ln (i/ep-binding ep-id ln))))
          (:sc.cs/local-names cs))))


(defn show-ep-info []
  (title-node "Execution points"
    (let [ids (keys (:execution-points @sc.impl.db/db))]
      (reduce (fn [ret ep-id]
                (let [info (sc.api/ep-info ep-id)
                      {:keys [:sc.ep/code-site]} info
                      {:keys [:sc.cs/file :sc.cs/line :sc.cs/column]} code-site]
                  (into ret
                        [(labeled-forms "Info: " info)
                         (node {:presentation [{:text  "Take me!"
                                                :color :link}
                                               {:text  (str " "
                                                            file
                                                            ":"
                                                            line)
                                                :color :inactive}]
                                :action       :navigate
                                :file         file
                                :line         line
                                :column       column})
                         (node {:presentation [{:text  "Def them all!"
                                                :color :link}]
                                :action       :eval
                                :var          'cursive.inline.scope-capture/defsc
                                :ep-id        ep-id})])))
              []
              (reverse ids)))))