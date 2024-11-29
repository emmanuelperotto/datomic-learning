(ns datomic-01.seed
  (:require [datomic-01.db :as db]
            [datomic.api :as d]))



;; Deleting the DB
(db/delete!)

;; Connecting to the DB
(def conn (db/connect!))

;; Registering schemas
(db/register-schemas! conn)

;; Registering tx functions
(def increment-visualizations
  #db/fn {:lang   :clojure
          :params [db product-id]
          :code   (let [visualizations (or (d/q '[:find ?visualizations .
                                                  :in $ ?product-id
                                                  :where
                                                  [?e :product/id ?product-id]
                                                  [?e :product/visualizations ?visualizations]]
                                                db product-id)
                                           0)
                        new-visualizations (inc visualizations)]
                    [{:product/id product-id
                      :product/visualizations new-visualizations}])})

(d/transact conn [{:db/ident :inc-visualizations
                   :db/fn    increment-visualizations
                   :db/doc   "fn to increment :product/visualizations by 1"}])

;; Inserting categories and products (nested maps + lookup ref)
(let [electronics #:category{:id (random-uuid)
                             :name "electronics"}
      sports      #:category{:id (random-uuid)
                             :name "sports"}
      computer    #:product{:id (random-uuid)
                            :stock 10
                            :name "Computer"
                            :slug "computer"
                            :price 1000M
                            :category electronics ;; nested map
                            :tags ["work" "gaming"]}
      t-shirt     #:product{:id (random-uuid)
                            :stock 5
                            :name "T-shirt"
                            :slug "t-shirt"
                            :price 100M
                            :category sports ;; nested map
                            :tags ["workout" "gym" "casual"]}
      tablet      #:product{:id (random-uuid)
                            :stock 7
                            :name "Tablet"
                            :slug "tablet"
                            :price 1000M
                            :category [:category/id (-> electronics :category/id)] ;; lookup ref
                            :tags ["work" "gaming" "communication"]}
      smartwatch  #:product{:id (random-uuid)
                            :stock 8
                            :name "Smartwatch"
                            :slug "smartwatch"
                            :price 2700M
                            :category [:category/id (-> sports :category/id)] ;; lookup ref
                            :tags ["gym" "workout" "health"]}
      xiaomi      #:product{:id (random-uuid)
                            :stock 1
                            :name "Xiaomi cellphone"
                            :slug "cellphone"
                            :price 1M
                            :category [:category/id (-> electronics :category/id)] ;; lookup ref
                            :tags ["cheap" "but" "won't" "last"]
                            :variations [{:variation/id (random-uuid)
                                          :variation/name "The camera is better than the iPhone"
                                          :variation/price 1M}
                                         {:variation/id (random-uuid)
                                          :variation/name "More expensive and less durable"
                                          :variation/price 2M}]}
      order       #:order{:id (random-uuid)
                          :product [:product/id (-> computer :product/id)] ;; lookup ref
                          :quantity 3}]
  ;; Transacting data with tx metadata
  (d/transact conn [[:db/add "datomic.tx" :tx-data/ip "127.0.0.1"] computer t-shirt])
  (d/transact conn [[:db/add "datomic.tx" :tx-data/ip "127.0.0.2"] tablet smartwatch order])
  
  ;; Compare and swap (CAS) for optimistic concurrency control
  (d/transact conn [[:db/cas [:product/id (:product/id smartwatch)] :product/price 2700M 2800M]])

  ;; Add variation with temp id
  (d/transact conn [{:db/id "temp-id"
                     :variation/id (random-uuid)
                     :variation/name "Plus"
                     :variation/price 1500M}

                    #:product{:id (random-uuid)
                              :stock 8
                              :name "iPhone"
                              :slug "iphone"
                              :price 600M
                              :category [:category/id (-> electronics :category/id)]
                              :tags ["gym" "workout" "health" "work" "gaming" "communication"]
                              :variations [{:db/id "temp-id"}]}])

  ;; Retract entity with variations component (cascade retracting)
  (d/transact conn [[:db/retractEntity [:product/id (:product/id xiaomi)]]])
  (d/transact conn [[:db/retractEntity [:order/id (:order/id order)]]])

  ;; Increment visualizations using tx functions
  (d/transact conn [[:inc-visualizations (:product/id computer)]
                    [:inc-visualizations (:product/id t-shirt)]
                    [:inc-visualizations (:product/id tablet)]
                    [:inc-visualizations (:product/id smartwatch)]]))
