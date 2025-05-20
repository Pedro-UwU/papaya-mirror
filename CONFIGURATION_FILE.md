# YAML Configuration file

This project is configured using a YAML file that follows the structure defined in this document.

## Options
Execution options are defined in the top level section `options`.

- `requestsPerEndpoint`: Defines how many requests should be made to each defined endpoint. Defaults to 1.
- `numberOfWorkers`: How many internal Workers should run concurrently. Defaults to 1.

## Example

```yaml
options:
  requestsPerEndpoint: 100
  numberOfWorkers: 4
```

## Global Parameters

These are parameters (variables) that may be shared between all endpoints.

Global parameters live in top level section `globalParameters`.

There are two mutually exclusive options for defining global parameters:

1. Give them a constant value.
    ```yaml
    parameterName:
      value: "hardcoded value"
    ```
2. Let Papaya generate a random value in a specific [Category](#categories).
    ```yaml
    parameterName:
      category: "text" # Specify a Category and generate a random value for it
    ```
   
Notice that `value` and `category` are mutually exclusive and trying to define both will throw an error.

To use a global parameter, you should follow the syntax `${parameterName}` when defining your strings in endpoints.

### Example

```yaml
globalParameters:
  baseUrl:
    value: "http://pf.zahnd.me:8000"

# Use as: "${baseUrl}"
```

## Endpoints

In this section, called `endpoints`, you should define all the endpoints you want to test.

Endpoints follow a simple structure:
```yaml
endpointName:
  url: Endpoint URL
  dependsOn: "another endpoint name"  # Optional
  method: HTTP Method
  headers: required header  # Optional
  body: required body parameters  # Optional
  query: required query parameters  # Optional
  response:  #  Expected response - Optional
    responseCode: 200  # expected code
    responseHeader:  # same as headers - Optional
    responseBody: # same as body - Optional
  parameters: parameter definition
```

- `url` is the absolute endpoint URL. For example `"localhost:8080/users"`
- `dependsOn` is a string to establish a parent-child relationship. 
  Endpoints that depend on another one will only be run after its parent.
- `method` represents the HTTP method for the request. 
  All standard methods are available (GET, POST, PUT, DELETE, HEAD, and PATCH).
- `parameters` are like global parameters, they may have a predefined value 
  or a [category](#categories) to which the value should belong and Papaya will automatically generate a value for them. 
  With the addition that they could come from a previous request, in which case we say they are `inherited`
  ```yaml
  name:  # "${name}" now becomes accessible from within the endpoint and contains a randomly generated Name
    category: "name"
  ```
  ```yaml
  age:  # "${age}" now becomes accessible from within the endpoint and contains a randomly generated integer in [13; 90). 
    category: "integer"
    range:  # Categories float, integer, timestamp and currency may define a range of desired values.
      from: 13
      to: 90
  ```
 
  ```yaml
   userId:
     inherited: true  # This indicates that `userId` came in the response of a previous request defined in `depensOn`
  ```

Once `parameters` are set, `headers`, `body` and `query` follow the same structure:
 
- `headers` useful when the request requires specific headers.
- `body` useful when the request requires a body.
- `query` useful when the request requires query parameters.

```yaml
body:  # May be body, headers, query
   name: "${name}"  # This is the parameter name defined in `parameters`
```

Finally, `responseHeader` and `responseBody` have a slightly different syntax:

- `responseHeader` expected headers in the response.
  Those not specified here get ignored.
- `responseBody` expected body in the response.

```yaml
responseBody:  # Identical for `responseHeader`
   userId:  # This is the parameter name as it will be used in the configuration
      fieldName: "user_id"  # This is the key received in the JSON response

## In another endpoint that has the previous one as parent, userId may be retrieved as `${userId}`
```


## Full example

```yaml
endpoints:
  create-user:  # This is a POST request that sends a JSON body and receives an answer.
    url: "${baseUrl}/user"
    method: "POST"
    body:
      name: "${name}"
      username: "${username}"
      password: "${password}"
      email: "${email}"
      age: "${age}"
      surname: "${surname}"
    response:
      responseCode: 200
      responseBody:
        userId:
          fieldName: "user_id"
    parameters:
      name:
        category: "name"
      username:
        category: "username"
      password:
        category: "password"
      email:
        category: "email"
      age:
        category: "integer"
        range:
          from: 13
          to: 90
      surname:
        category: "surname"
        optional: true

  get-user: # This GET request uses `userId` from `create-user`
     dependsOn: "create-user" # This request will be executed after `create-user`
     url: "${baseUrl}/user"
     method: "GET"
     query:
        user_id: "${userId}"
     response:
        responseCode: 200
     parameters:
        userId:
           inherited: true
```

## Categories

Categories currently implemented that will provide valid results are:

- `USERNAME`: generates a Username
- `NAME`: generates a Name
- `SURNAME`: generates a Surname
- `PASSWORD`: generates a Password
- `EMAIL`: generates a seemingly valid email
- `CURRENCY`: generates a numeric value with two decimal places.
- `INTEGER`: generates an integer (uses Long under the hood).
- `FLOAT`: generates a floating point value (uses Double under the hood).
- `TIMESTAMP`: generates an integer (uses Long under the hood).


In a following release, these categories are also planed to be fully functional:

- `TEXT`: Will return a random string.
- `FILE`: Will return a random path file.
- `PICTURE`: Will generate an image.
- `AVATAR`: Will generate an avatar image (much smaller than a `PICTURE`).
