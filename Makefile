datomic-transactor:
	@echo "Starting Datomic..."
	@cd $(HOME)/datomic-pro-1.0.7075 && bin/transactor -Ddatomic.printConnectionInfo=true config/dev-transactor-template.properties

datomic-console:
	@echo "Starting Datomic Console..."
	@cd $(HOME)/datomic-pro-1.0.7075 && bin/console -p 8080 dev datomic:dev://localhost:4334/
