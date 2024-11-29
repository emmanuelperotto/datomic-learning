(ns datomic-01.filters-and-as-of 
  (:require [datomic-01.db :as db]
            [datomic.api :as d]))

;; Connecting to the DB
(def conn (db/connect!))

;; Querying all product entities in the past
(d/q '[:find (pull ?e [*])
       :where [?e :product/name]]
     (d/as-of (d/db conn) #inst "2024-04-29T18:37:13.960-00:00"))

;; Querying all product entities now
(d/q '[:find (pull ?e [*])
       :where [?e :product/name]]
     (d/db conn))

;; Querying order total price considering the product price at the instant of the order.
;; It prevents the order total price from being affected by the product price changes.
(let [order-tx (d/q '[:find ?tx .
                      :where [_ :order/id _ ?tx true]]
                    (d/history (d/db conn)))]
  (d/q '[:find ?order-total-price .
         :where
         [?e :order/product ?p]
         [?e :order/quantity ?quantity]
         [?p :product/price ?product-price]
         [(* ?product-price ?quantity) ?order-total-price]]
       (d/as-of (d/history (d/db conn)) order-tx)))

;; Querying history of price changes from product entities
(d/q '[:find ?id ?price ?instant
       :keys id price instant
       :where
       [?p :product/id ?id]
       [?p :product/price ?price ?tx true]
       [?tx :db/txInstant ?instant]]
     (d/history (d/db conn)))

;; Querying with since and joining dbs
(d/q '[:find ?id ?price
       :keys id price
       :in $ $since
       :where
       [$ ?p :product/id ?id]
       [$since ?p :product/price ?price ?tx true]]
     (d/db conn)
     (d/since (d/db conn) #inst "2024-05-06T01:45:11.657-00:00"))
