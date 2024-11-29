(ns datomic-01.query-and-pull
  (:require [datomic-01.db :as db]
            [datomic.api :as d]))

;; Connecting to the DB
(def conn (db/connect!))

;; Querying all product entities
(d/q '[:find  [?e ...]
       :where [?e :product/name]] 
     (d/db conn))

;; Querying all entities by slug
(d/q '[:find  ?e
       :in    $ ?slug
       :where [?e :product/slug ?slug]]
     (d/db conn)
     "computer")

;; Querying all slugs
(d/q '[:find  ?slug
       :where [_ :product/slug ?slug]]
     (d/db conn))

;; Querying all names by specific price
(d/q '[:find  ?name ?price
       :keys  product/name product/price
       :in    $ ?price
       :where
       [?e :product/price ?price]
       [?e :product/name ?name]]
     (d/db conn)
     1000M)

;; Querying all names by min price (predicate)
(d/q '[:find  ?name ?price
       :keys  product/name product/price
       :in    $ ?min-price
       :where
       [?e :product/price ?price]
       [(>= ?price ?min-price)]
       [?e :product/name ?name]]
     (d/db conn)
     500M)

;; Querying all categories and returning all data
(d/q '[:find  [(pull ?e [*]) ...]
       :where [?e :category/id]] 
     (d/db conn))

;; Querying all products and returning all data
(d/q '[:find  (pull ?e [* {:product/category [*]}])
       :where [?e :product/name]] 
     (d/db conn))

;; Querying all products and returning specific data
(d/q '[:find  (pull ?e [:db/id :product/name :product/price])
       :where [?e :product/name]]
     (d/db conn))

;; Querying all products by tag
(d/q '[:find  (pull ?e [*])
       :in    $ ?tag
       :where [?e :product/tags ?tag]]
     (d/db conn)
     "work")

;; Querying all products joining data with category (forward navigation)
(d/q '[:find (pull ?p [* {:product/category [*]}])
       :where
       [?p :product/name ?name]
       [?p :product/category ?c]]
     (d/db conn))

;; Querying all products from a category (backward navigation)
(d/q '[:find (pull ?c [* {:product/_category [*]}])
       :in $ ?category
       :where
       [?c :category/name ?category]]
     (d/db conn)
     "electronics")

;; Querying all products with aggregates
(d/q '[:find (max ?price) (min ?price) (avg ?price) (median ?price) (count ?price)
       :keys max-price min-price avg-price median-price total-products
       :with ?p
       :where [?p :product/price ?price]]
     (d/db conn))

;; Querying all products with aggregates and group by
(d/q '[:find ?category (max ?price) (min ?price) (avg ?price) (median ?price) (sum ?price) (count ?price)
       :keys category max-price min-price avg-price median-price sum-price total-products
       :with ?p
       :where
       [?p :product/price ?price]
       [?p :product/category ?c]
       [?c :category/name ?category]]
     (d/db conn))

;; Querying all most expensive products with nested queries
(d/q '[:find (pull ?p [*])
       :where
       [(q '[:find  (max ?price)
             :where [_ :product/price ?price]]
           $)
        [[?max-price]]]
       [?p :product/price ?max-price]]
     (d/db conn))

;; Querying all products from a specific tx, filtering by IP
(d/q '[:find ?ip (pull ?p [*])
       :where
       [?tx :tx-data/ip ?ip]
       [?p :product/name _ ?tx]]
     (d/db conn))

;; Querying all products from a collection of categories (Collection binding)
(d/q '[:find (pull ?p [* {:product/category [*]}])
       :in $ [?category ...]
       :where
       [?c :category/name ?category]
       [?p :product/category ?c]]
     (d/db conn)
     ["electronics" "sports"])

;; Pulling one product by entity id
(d/pull (d/db conn) '[*] 17592186045421)

;; Pulling one product by custom id (uuid)
(d/pull (d/db conn) '[*] [:product/id #uuid "4a796c60-7ab9-4422-b34d-a0abd8688768"])
