;;   Copyright (c) 7theta. All rights reserved.
;;   The use and distribution terms for this software are covered by the
;;   Eclipse Public License 1.0 (http://www.eclipse.org/legal/epl-v10.html)
;;   which can be found in the LICENSE file at the root of this
;;   distribution.
;;
;;   By using this software in any fashion, you are agreeing to be bound by
;;   the terms of this license.
;;   You must not remove this notice, or any others, from this software.

(defproject com.7theta/via-auth "0.4.0"
  :description "Common authentication strategies for via"
  :url "https://github.com/7theta/via-auth"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [com.7theta/via "8.1.0"]
                 [buddy/buddy-sign "3.3.0"]
                 [buddy/buddy-hashers "1.7.0"]
                 [integrant "0.8.0"]]

  :profiles {:dev {:source-paths ["dev" "example/src"]
                   :resource-paths ["example/resources"]
                   :clean-targets ^{:protect false} ["example/resources/public/js/compiled" "target"]
                   :env {:malli "true"}
                   :plugins [[lein-environ "0.4.0"]]
                   :dependencies [[binaryage/devtools "1.0.2"]
                                  [org.clojure/test.check "1.1.0"]
                                  [thheller/shadow-cljs "2.11.13"]
                                  [integrant/repl "0.3.2"]
                                  [org.clojure/clojurescript "1.10.773"]
                                  [metosin/malli "0.2.1"]]}}
  :prep-tasks ["compile"]
  :scm {:name "git"
        :url "https://github.com/7theta/via-auth"})
