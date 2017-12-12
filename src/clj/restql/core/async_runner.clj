(ns restql.core.async-runner
  (:require [clojure.core.async :as a :refer [go-loop go <! >! chan alt! timeout]]
            [restql.core.query :as query]
            [restql.core.log :refer [debug info warn error]]
            [clojure.set :as s]))

(defn generate-uuid! []
  (.toString (java.util.UUID/randomUUID)))

(defn initialize-state [query]
  {:done []
   :requested []
   :to-do query})

(defn all-done?
  "given a state with queries :done :requested and :to-do returns
   true if all entries are :done"
  [state]
  (and (empty? (:to-do state)) (empty? (:requested state))))

(defn can-request?
  "given a single query item and the map with the current results
   returns true if all the dependencies of the query-item are
   resolved"
  [query-item state]
  (let [deps  (query/get-dependencies query-item)
        dones (->> state :done (map first) (into #{}))]
    (empty? (s/difference deps dones))))

(defn all-that-can-request
  "takes a state with queries :done :requested and :to-do and returns
   a sequence of pairs with only the queries that can be executed, because all
   their dependencies are already met.

   Example return: ([:cart {:with ...}] [:freight {:with ...})"
  [state]
  (filter #(can-request? % state) (:to-do state)))

(defn is-done? [[query-item-key _] state]
  (->> state
      :done
      (map first)
      (into #{})
      query-item-key
      nil?
      not))

(defn update-state
  "it passes all to-do queries that could be requested to :requested state and
   adds a completed request to the :done state"
  [state completed]
  {:done (conj (:done state) completed)
   :requested (filter
                #(and (not= (first completed) (first %)) (not (is-done? % state)))
                   (into (:requested state) (all-that-can-request state)))
   :to-do (filter #(not (can-request? % state)) (:to-do state))})

(defn log-status [uid resource result]
  (let [status (-> result second :status)]
    (cond
      (= status 408) (warn {:session uid :resource resource} "Request timed out")
      (nil? status)  (warn {:session uid :resource resource} "Request aborted")
      :else          :no-action)))

(defn make-requests
  "goroutine that keeps listening from request-ch and performs http requests
   sending their result to result-ch"
  [do-request encoders {:keys [request-ch result-ch exception-ch]} {:keys [debugging] :as query-opts}]
  (go-loop [next-req (<! request-ch)
            timeout-ch (timeout (:global-timeout query-opts))
            uid  (generate-uuid!) ]
    (let [from (:from (second (second (first next-req))))]
      (go
        (alt!
          timeout-ch
            ([]
              (warn {:session uid
                     :resource from}
                    "Request timed out")
              (>! result-ch [(first (second (first next-req))) {:status 408 :body {:message "timeout"}}]))

          (do-request next-req encoders exception-ch query-opts)
            ([result]
              (log-status uid from result)
              (>! result-ch result)))))
    (recur (<! request-ch) (timeout (:global-timeout query-opts)) (generate-uuid!) )))

(defn do-run
  "it separates all queries in three states, :done :requested and :to-do
   then sends all to-dos to resolve, changing their statuses to :requested.
   As the results get ready, update the query status to :done and send all to-dos again.
   When all queries are :done, the process is complete, and the :done part of the state is returned."
  [query {:keys [request-ch result-ch output-ch]} ]
  (go-loop [state (initialize-state query)]
    (doseq [to-do (all-that-can-request state)]
      (go
        (>! request-ch {:to-do to-do :state state})))
    (let [new-state (update-state state (<! result-ch))]
      (go (>! output-ch new-state))
      (recur new-state))))

(defn run [do-request query encoders {:keys [debugging] :as query-opts}]
  (let [chans {:output-ch    (chan)
               :request-ch   (chan)
               :result-ch    (chan)
               :exception-ch (chan)}]
    (make-requests do-request encoders chans query-opts)
    (do-run query chans)
    [(:output-ch chans)
     (:exception-ch chans)]))
