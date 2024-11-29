(ns datomic-01.db 
  (:require [datomic.api :as d]))

(def ^:private schemas
  [;; Tx metadata
   {:db/ident       :tx-data/ip
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one}

   ;; Product
   {:db/ident       :product/name
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc         "Product name"}

   {:db/ident       :product/slug
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc         "Path to access this product"}

   {:db/ident       :product/price
    :db/index       true
    :db/valueType   :db.type/bigdec
    :db/cardinality :db.cardinality/one
    :db/doc         "Product price with decimal precision"}

   {:db/ident       :product/tags
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/many
    :db/doc         "Tags associated to the product"}

   {:db/ident       :product/id
    :db/valueType   :db.type/uuid
    :db/cardinality :db.cardinality/one
    :db/doc         "UUID of the product entity"
    :db/unique      :db.unique/identity}

   {:db/ident       :product/category
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/one
    :db/doc         "Reference to the category entity"}

   {:db/ident       :product/stock
    :db/valueType   :db.type/long
    :db/cardinality :db.cardinality/one
    :db/doc         "Number of items in stock"}

   {:db/ident       :product/digital
    :db/valueType   :db.type/boolean
    :db/cardinality :db.cardinality/one
    :db/doc         "Is the product digital?"}

   {:db/ident       :product/variations
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/many
    :db/isComponent true
    :db/doc         "Reference to the product variations"}

   {:db/ident       :product/visualizations
    :db/valueType   :db.type/long
    :db/cardinality :db.cardinality/one
    :db/noHistory   true}

   ;;  Variation
   {:db/ident       :variation/id
    :db/valueType   :db.type/uuid
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity}

   {:db/ident       :variation/name
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one}

   {:db/ident       :variation/price
    :db/valueType   :db.type/bigdec
    :db/cardinality :db.cardinality/one}

   ;; Category
   {:db/ident       :category/name
    :db/valueType   :db.type/string
    :db/cardinality :db.cardinality/one
    :db/doc         "Category name"}

   {:db/ident       :category/id
    :db/valueType   :db.type/uuid
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity
    :db/doc         "Category UUID"}

   ;; Order  
   {:db/ident       :order/id
    :db/valueType   :db.type/uuid
    :db/cardinality :db.cardinality/one
    :db/unique      :db.unique/identity
    :db/doc         "Order UUID"}

   {:db/ident       :order/product
    :db/valueType   :db.type/ref
    :db/cardinality :db.cardinality/one}

   {:db/ident       :order/quantity
    :db/valueType   :db.type/long
    :db/cardinality :db.cardinality/one}])

(def ^:private db-uri "datomic:dev://localhost:4334/hello")

(defn connect! []
  (d/create-database db-uri)
  (d/connect db-uri))

(defn delete! []
  (d/delete-database db-uri))

(defn register-schemas! [conn]
  (d/transact conn schemas))
