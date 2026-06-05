Develop a module that manages internal fund transfers between bank accounts,
including one-time transfers and recurring standing orders. 

After cloning and running with spring boot yoou can access it via http://localhost:8080/swagger-ui/index.html
The api key is required.
Run with: mvn spring-boot:run -Dspring-boot.run.arguments="--fbi.auth.api-key=your_real_key"
If you access via swagger - set authurisation there.

1) clone progect
2) navigate to project dir
account-transfer> docker compose up -d && docker compose logs -f
3) success  :  will see in logs:
    account-transfer-1  | [INFO] Results:
   account-transfer-1  | [INFO]
   account-transfer-1  | [INFO] Tests run: 14, Failures: 0, Errors: 0, Skipped: 0
   account-transfer-1  | [INFO]
   account-transfer-1  | [INFO]
   account-transfer-1  | [INFO] --- failsafe:3.5.2:verify (default) @ account-transfer

4) you can access and test it via: http://localhost:8080/swagger-ui/index.html
5) !!! authorize !!!

API Specification:
------------------------------------------------------------------------------------------------
| Method | Endpoint                       | Description                                        |
|--------|--------------------------------|----------------------------------------------------|
| POST   | `/api/v1/transfers`            | Execute a one-time transfer                        |
| GET    | `/api/v1/transfers/{id}`       | Get transfer details by ID                         |
| GET    | `/api/v1/ledger`               | Query ledger entries (paginated, filtered)         |
| GET    | `/api/v1/accounts`             | List all accounts with current balances            |
| GET    | `/api/v1/accounts/{iban}`      | Get single account details                         |
| POST   | `/api/v1/standing-orders`      | Create a standing order                            |
| GET    | `/api/v1/standing-orders`      | List all active standing orders                    |
| GET    | `/api/v1/standing-orders/{id}` | Get standing order by ID                           |
| DELETE | `/api/v1/standing-orders/{id}` | Cancel (soft-delete) a standing order              |