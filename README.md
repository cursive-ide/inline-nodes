# Inline Nodes

This repository contains implementations of support functions for Cursive's new 
[Inline Node Customisation](https://cursive-ide.com/blog/customising-inline-nodes.html) feature. There are some basic functions for creating nodes,
and some examples of ways that it can be used with common Clojure libraries. If you have 
examples of ways that you've used this that you would like to share, pull requests to this 
repo are very welcome.

If you're just getting started, it's best to start with the 
[tutorials](https://github.com/cursive-ide/inline-tutorials), which walk through how to use
the basic functionality and some of the real-world examples. 

To use these examples, you will need to be using at least version
[1.14.0-eap2](https://cursive-ide.com/blog/cursive-1.14.0-eap2.html) of Cursive. Instructions on how to sign up for
the EAPs (if you're not already) are
[here](https://cursive-ide.com/userguide/#choosing-to-receive-eap-beta-builds).

If you have any questions or comments about this, the best place is probably `#cursive` in 
the Clojurians Slack. Otherwise you can also use our [mailing lists](https://cursive-ide.com/mailinglist.html) or just email me
directly on [cursive@cursive-ide.com](mailto:cursive@cursive-ide.com).


## API

The inline nodes are created by using 
[tagged literals](https://clojure.org/reference/reader#tagged_literals). The basic shape is:

```clojure
(tagged-literal 'cursive/node {:presentation [{:text  "Hello world"
                                               :color :red}]})
```

The data structure contains the node configuration, and is always a map. This example will 
create a simple node with no children, which says "Hello world" in red text.

### :presentation key

The `:presentation` is a vector of chunks. Each chunk is either a form or a text chunk.
Text chunks can be individually styled. Here's an example containing both types:

```clojure
{:presentation [{:text "Type: "}
                {:form (type e)}]}
```

Text chunks can be passed a colour and/or a style, both of which are optional. The colours
are taken from JetBrains' 
[UI Kit](https://www.figma.com/design/UowbJhRZZgcqa5Wmb4npee/Int-UI-Kit-(Community)?node-id=305-26005&node-type=canvas),
so they'll be consistent with the rest of the UI. 

The available colours (set in a text block with the `:color` key) are:

```clojure
:green :red :blue :yellow :orange :purple :teal :gray
```

There are also some special colours which are useful for UI elements, they'll be styled
like the corresponding elements in the actual IDE UI:

```clojure
:error :inactive :link
```

The available styles (set in a text block with the `:style` key) are:

```clojure
:italic :bold :underline :strikeout
```

You can also set `:color` to some special values, which will style the text like
the corresponding forms in the editor. In this case the `:style` is ignored because
the styling (e.g. bold/italic/etc) comes from the editor:

```clojure
:comment :keyword :symbol :number :string :brace :paren :literal :char
```

### :auto-expand? key

The `:auto-expand?` key can be set to true or false. If true, this section of the tree will
auto-expand when shown in the editor. Defaults to false.

### :children key

The `:children` key contains a vector of child nodes, which will be added to the tree as
children of this node. The children can be either tagged literals as described in this doc,
or can be arbitrary Clojure data which will be displayed in the tree as usual.

### :action key

The `:action` key allows the node to be interactive. The user can execute the action using
either the Enter key if they are navigating the tree with the keyboard, or by double 
clicking. Additional parameters for the action appear at the top level as siblings of the
`:action` key.

I often style these nodes like links to make it clear you can do something with
them, but that's not required. Stacktrace elements in errors don't look like
links, but you can still use them to navigate.

There are 3 action types:

#### :navigate action

The `:navigate` action will navigate the user to the location specified in the parameters.
There are two types of navigation, to a `:class` specified as an FQN, or a `:file` specified
as a path. Both options accept `:line` and `:column` parameters, both optional. Here is an 
example from the tutorial showing both types:

```clojure
; Example using the node functions in cursive.inline.nodes:
(title-node "Test navigate"
  (node {:presentation [{:text  "Take me"
                         :color :link}]
         :action       :navigate
         :class        "some.JavaClass"
         :line         8
         :column       16})
  (node {:presentation [{:text  "Even better"
                         :color :link}]
         :action       :navigate
         :file         "cursive/inline/nodes.clj"
         :line         23
         :column       7}))
```

#### :browse action

The `:browse` action will open a `:url` in the user's browser. Here's what this looks like:

```clojure
(title-node "Test browse"
  (node {:presentation [{:text  "Show me the doc"
                         :color :link}]
         :action       :browse
         :url          "https://cursive-ide.com/userguide/repl.html"})
  ; This can be made nicer using the node functions in cursive.inline.nodes:
  (link-node "Show me the examples"
             "https://github.com/cursive-ide/inline-nodes"))
```

#### :eval action

The `:eval` action will evaluate a `:var` in the REPL, and pass the whole data structure from
the tagged literal to it. Here's an example from the tutorial:

```clojure
(defn print-it [params]
  (pr params)
  params)

(title-node "Test exec"
  (node {:presentation [{:text  "Do it!"
                         :color :link}]
         :action       :eval
         :var          'cursive.tutorials.inline.a-start-here/print-it}))
```

The `:var` entry This will invoke the `print-it` var, and passes the whole map from the tagged literal to it.
In this case, all the data is related to the representation of the node itself, but you can
put whatever you want in there, IDs, paths to data, etc. Here's an example from the Scope
Capture functions, which passes an ID for the var to operate on:

```clojure
(node {:presentation [{:text  "Def them all!"
                       :color :link}]
       :action       :eval
       :var          'cursive.inline.scope-capture/defsc
       :ep-id        ep-id})
```
