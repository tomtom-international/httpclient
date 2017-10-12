# GOJI HTTP Client

**GOJI** stands for: **Groovy-oriented** and **JSON-implying.**

## License

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.


## Table of contents

* [Intro](#intro)
* [Changelog](#changelog)
* [Usage](#usage)
   * [Maven](#maven)
* [Request examples](#requests)
   * [Supported HTTP methods](#http-methods)
   * [A request to an arbitrary url](#url)
   * [Base url](#base-url)
   * [Request headers](#request-headers)
   * [Request body](#request-body)
* [Handling responses](#responses)
   * [Response status code](#status)
   * [Response headers](#response-headers)
   * [Response body](#response-body)
   * [Deserializing JSONs to Java object](#jsons)
   * [Deserializing JSONs to Java generics](#generics)
    
<a id='intro'></a>
## Intro
This client wraps around [Apache HttpClient](https://hc.apache.org/httpcomponents-client-ga/) and [Jackson Databind](https://github.com/FasterXML/jackson-databind) libraries providing lean Groovy syntax:
```groovy
given:
def http = new HttpClient(
    baseUrl: 'http://ice-cream-service.be')
    
when:
def response = http.post(
    path: '/ice-cream?banana=true',
    headers: [
        'Content-Type': 'application/json'],
    body: [
        sprinkles: true],
    expecting: BananaIceCream)
    
then:
response.statusCode == ResponseCode.OK
response.body == new BananaIceCream(
    sprinkles: true)
```

The library is initially intended for writing easily readable unit-tests but can also but used in other Groovy scripts. 

<a id='changelog'></a>
## Changelog
* [1.0.0](http://mvnrepository.com/artifact/com.tomtom.http/goji-http-client/1.0.0>)
  * initial release
* [1.1.0](http://mvnrepository.com/artifact/com.tomtom.http/goji-http-client/1.1.0>)
  * (doc) maven usage and javadocs

<a id='usage'></a>
## Usage

<a id='maven'></a>
### Maven

```xml
<dependency>
    <groupId>com.tomtom.http</groupId>
    <artifactId>goji-http-client</artifactId>
    <version>1.0.0</version>
</dependency>
```

<a id='requests'></a>
## Request examples

<a id='http-methods'></a>
### Supported HTTP methods:

`GET`, `POST`, `PUT` and `DELETE` are supported:
```groovy
http.get()
http.post()
http.put()
http.delete()
```

<a id='url'></a>
### A request to an arbitrary url:

```groovy
http.get(
    url: 'http://pizza-delivery.org/margheritas')
```

<a id='base-url'></a>
### Base url:

If you want to make a number of requests to a given service, you can specify the `baseUrl` constructor parameter:
```groovy
def http = new HttpClient(
    baseUrl: 'http://water-melon.com')
    
http.post(
    path: '/cut')
```

<a id='request-headers'></a>
### Request headers:

```groovy
http.put(
    path: '/put',
    headers: [
        Accept: 'application/json',
        'Content-Type': 'application/json'])
```

<a id='request-body'></a>
### Request body:

Any non-string body is serialized as JSON.

`String`:
```groovy
http.delete(
    path: '/delete',
    body: '<xml></xml>')
```
`Map`:
```groovy
http.get(
    path: '/get',
    body: [
        key: 'value']) 
```

<a id='responses'></a>
## Handling responses

<a id='status'></a>
### Response status code

```groovy
def response = http.get(
    path: '/get')
    
assert response.statusCode == ResponseCode.OK
```

<a id='response-headers'></a>
### Response headers

```groovy
def response = http.get(
    path: '/get')
    
assert response.headers == [
    'Content-Type': [
        'application/json',
        'application/vnd.tomtom+json'],
    Connection: 'keep-alive'] 
```

<a id='response-body'></a>
### Response body

By default, the response body is a String:
```groovy
Response<String> response = http.get(
    path: '/get')
    
assert response.body == 'A string'
```

<a id='jsons'></a>
### Deserializing JSON responses to Java object

A valid JSON response body can be deserialized into a Java object.
```groovy
Response<Map> response = http.get(
    path: '/get',
    expecting: Map)
    
assert response.body == [key: 'value']
```

<a id='generics'></a>
### Deserializing JSON responses to Java generics

```groovy
Response<List<Map>> response = http.get(
    path: '/get',
    expecting: List, of: Map)
    
assert response.body == [
    [key: 'value'],
    ['another-key': 'another value']]
```

See more use-cases in [integration tests](src/integration-test/groovy)

_**Disclaimer:**_ Our primary use-case of this http client is testing our REST services. The client has not been tested for any production use. The client has no verification of https certificates et al.