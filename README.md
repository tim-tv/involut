# How to run

```
./gradlew run
```

# TODO
* Add DI framework
* Add framework for database migrations
* Optimize some SQL queries by using batches
* Make methods for creating transactions as idempotent by using Idempotency-Key

------------------------------------------------------------
# REST API

### Create a new account
Request:
```
curl -X POST \
  http://localhost:7000/api/v1/accounts \
  -H 'Content-Type: application/json' \
  -H 'cache-control: no-cache' \
  -d '{
	"currency": "RUR"
}'
```

Response:
```json
{
    "id": 1,
    "balance": 0,
    "currency": "RUR",
    "createdAt": "2019-12-09T04:33:36.048+03:00",
    "closedAt": null
}
```

------------------------------------------------------------
### Get account by id
Request:
```
curl -X GET http://localhost:7000/api/v1/accounts/1
```

Response:
```json
{
    "id": 1,
    "balance": 0,
    "currency": "RUR",
    "createdAt": "2019-12-09T04:33:36.048+03:00",
    "closedAt": null
}
```
------------------------------------------------------------
### Deposit money to an account
Request:
```
curl -X POST \
  http://localhost:7000/api/v1/accounts/1/operations/deposits \
  -H 'Content-Type: application/json' \
  -H 'cache-control: no-cache' \
  -d '{
	"amount": 2000
}'
```

Response:
```json
{
    "id": 1,
    "status": "COMPLETED",
    "createdAt": "2019-12-09T04:37:20.661+03:00",
    "updatedAt": "2019-12-09T04:37:20.661+03:00",
    "changeList": [
        {
            "id": 1,
            "accountId": 1,
            "transactionId": 1,
            "amount": 2000
        }
    ],
    "errorReason": null
}
```
------------------------------------------------------------
### Withdraw money from account
Request:
```
curl -X POST \
  http://localhost:7000/api/v1/accounts/1/operations/withdrawals \
  -H 'Content-Type: application/json' \
  -H 'cache-control: no-cache' \
  -d '{
	"amount": 4000
}'
```

Response:
```json
{
    "id": 2,
    "status": "FAILED",
    "createdAt": "2019-12-09T04:39:41.77+03:00",
    "updatedAt": "2019-12-09T04:39:41.77+03:00",
    "changeList": [
        {
            "id": 2,
            "accountId": 1,
            "transactionId": 2,
            "amount": -4000
        }
    ],
    "errorReason": "Account[id=1] has insufficient funds."
}
```
------------------------------------------------------------
### Close account forever
Request:
```
curl -X PATCH http://localhost:7000/api/v1/accounts/1 
```

Response:  
```json
{
    "id": 1,
    "balance": 784,
    "currency": "RUR",
    "createdAt": "2019-12-09T05:03:45.885+03:00",
    "closedAt": "2019-12-09T05:10:41.3+03:00"
}
```

------------------------------------------------------------
### Get list of completed transaction changes by datetime range (max limit = 100)
Request:
```
curl -X GET 'http://localhost:7000/api/v1/accounts/1/operations?from=2019-12-03T10:15:30Z&to=2019-12-29T10:15:30Z' 
```

Response:
```json
[
    {
        "completedAt": "2019-12-09T05:04:17.014+03:00",
        "change": {
            "id": 5,
            "accountId": 1,
            "transactionId": 5,
            "amount": -635
        }
    },
    {
        "completedAt": "2019-12-09T05:04:05.129+03:00",
        "change": {
            "id": 4,
            "accountId": 1,
            "transactionId": 4,
            "amount": 500
        }
    },
    {
        "completedAt": "2019-12-09T05:03:59.279+03:00",
        "change": {
            "id": 3,
            "accountId": 1,
            "transactionId": 3,
            "amount": -31
        }
    },
    {
        "completedAt": "2019-12-09T05:03:52.504+03:00",
        "change": {
            "id": 2,
            "accountId": 1,
            "transactionId": 2,
            "amount": 50
        }
    },
    {
        "completedAt": "2019-12-09T05:03:49.116+03:00",
        "change": {
            "id": 1,
            "accountId": 1,
            "transactionId": 1,
            "amount": 1000
        }
    }
]
```
------------------------------------------------------------
### Transfer money from account A to account B
Request:
```
curl -X POST \
  http://localhost:7000/api/v1/transfers \
  -H 'Content-Type: application/json' \
  -H 'cache-control: no-cache' \
  -d '{
	"sourceAccountId": 1,
	"targetAccountId": 2,
	"amount": 100
}'
```

Response:
```json
{
    "id": 6,
    "status": "COMPLETED",
    "createdAt": "2019-12-09T05:06:37.301+03:00",
    "updatedAt": "2019-12-09T05:06:37.301+03:00",
    "changeList": [
        {
            "id": 6,
            "accountId": 1,
            "transactionId": 6,
            "amount": -100
        },
        {
            "id": 7,
            "accountId": 2,
            "transactionId": 6,
            "amount": 100
        }
    ],
    "errorReason": null
}
```
------------------------------------------------------------
### Get transaction by id
Request:
```
curl -X GET http://localhost:7000/api/v1/transactions/7
```

Response:
```json
{
    "id": 7,
    "status": "FAILED",
    "createdAt": "2019-12-09T05:08:59.301+03:00",
    "updatedAt": "2019-12-09T05:08:59.301+03:00",
    "changeList": [
        {
            "id": 8,
            "accountId": 1,
            "transactionId": 7,
            "amount": -10000
        },
        {
            "id": 9,
            "accountId": 2,
            "transactionId": 7,
            "amount": 10000
        }
    ],
    "errorReason": "Account[id=1] has insufficient funds."
}
```


