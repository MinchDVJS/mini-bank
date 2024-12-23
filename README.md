# mini-bank
Mini Bank Test App

## Test

To run the tests, type the following:

```bash
$> cd mini-bank-app 
$> ./mvnw clean test
```

```
[INFO] Results:
[INFO] 
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
```

## Build and Run

One-liner to build and run the project:

```bash
$> ./mvnw spring-boot:run
```

## Behavior

### What is not done

1. Everything is commited right in repo, so no real DB-transaction control
2. In case, 2 transaction (money transfer events) is not succeeded there should be logic how to rollback them, but it is not done
3. Error Handling is more on PoC stage, then properly implemented
4. Common sense business logic is omitted except from check of insufficient balance
5. Project is done in a module without parent pom - wrong copypaste
6. Maybe something else, but I already forgot about it

### API

By default, application is persisting data into file-system storage, you can change behaviour by changing parameter in application config.
Database with some test data is attached to repo.

Create User And Account

```bash
curl --location 'localhost:8081/api/v1/user' \
--header 'Content-Type: application/json' \
--data '{
    "login": "foo"
}'
```

Get User (1 is userId)

```bash
curl --location 'localhost:8081/api/v1/user/1'
```

Get All User Accounts (that allows to see balances also)

```bash
curl --location 'localhost:8081/api/v1/account?userId=1'
```

Deactivate User (deactivation does not lock any ATM, it is just change of status)

```bash
curl --location --request POST 'localhost:8081/api/v1/user/1/deactivate'
```

Deposit Money (2 is account ID)

```bash
curl --location --request POST 'localhost:8081/api/v1/account/2/deposit?amount=50'
```

Withdraw Money

```bash
curl --location --request POST 'localhost:8081/api/v1/account/2/withdraw?amount=50'
```

Transfer Money (transfer to self is possible and generates 2 transactions)

```bash
curl --location --request POST 'localhost:8081/api/v1/account/2/transfer?amount=50'
```

List all Transactions

```bash
curl --location 'localhost:8081/api/v1/account/2/transactions'
```

Bit of user flows can be found in tests
