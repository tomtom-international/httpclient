/*
 * Copyright (C) 2017. TomTom International BV (http://tomtom.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tomtom.http

import com.fasterxml.jackson.databind.ObjectMapper
import com.tomtom.http.response.Response
import groovy.transform.PackageScope
import org.apache.http.HttpEntity
import org.apache.http.client.methods.*
import org.apache.http.entity.StringEntity
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.message.BasicHeader

import java.util.function.Function

@PackageScope
class RequestBuilder {

    private ObjectMapper mapper = new ObjectMapper()
    private String baseUrl

    HttpRequestBase request(Map properties) {
        def method = properties['method']
        def url = urlFrom properties
        def request = requestFor method, url

        def headers = properties['headers'] as Map
        if (headers) addHeaders request, headers

        def body = properties['body']
        if (body)
            if (body instanceof File)
                addFile request, body
            else {
                def serialized = serialize body
                addBody request, serialized
            }

        request
    }

    private urlFrom(Map properties) {
        def url = properties['url'] as String
        if (url) return url
        def path = properties['path']
        if (baseUrl && path) return "$baseUrl$path"
        throw new NoUrl()
    }

    private static addHeaders(request, Map headers) {
        headers.collect { new BasicHeader(it.key as String, it.value as String) }
                .forEach { request.addHeader it }
    }

    private String serialize(body) {
        (body instanceof String) ? body : mapper.writeValueAsString(body)
    }

    private static addBody(request, String body) {
        addBody request, new StringEntity(body)
    }

    private static addFile(request, File file) {
        def body = MultipartEntityBuilder
                .create()
                .addBinaryBody("file", file)
                .build()
        addBody request, body
    }

    private static addBody(request, HttpEntity body) {
        (request as HttpEntityEnclosingRequestBase).setEntity body
    }

    private static HttpRequestBase requestFor(method, String url) {
        switch (method) {
            case 'head':
                return new HttpHead(url)
            case 'get':
                return new HttpGet(url)
            case 'post':
                return new HttpPost(url)
            case 'put':
                return new HttpPut(url)
            case 'delete':
                return new HttpDelete(url)
            case 'options':
                return new HttpOptions(url)
            case 'patch':
                return new HttpPatch(url)
            case 'trace':
                return new HttpTrace(url)
            default:
                throw new UnsupportedOperationException("$method not supported")
        }
    }

    static class UrlBuilder<T> {
        private Function<Map, Response<T>> method

        ParametersBuilder<T> url(String url) {
            new ParametersBuilder<>(method: method, parameters: [url: url])
        }

        ParametersBuilder<T> url(URI url) {
            this.url(url.toString())
        }

        ParametersBuilder<T> url(URL url) {
            this.url(url.toString())
        }

        ParametersBuilder<T> path(String path) {
            new ParametersBuilder<>(method: method, parameters: [path: path])
        }
    }

    static class ParametersBuilder<T> {
        private Function<Map, Response<T>> method
        private Map parameters

        ParametersBuilder<T> header(String name, String value) {
            parameters.headers = (parameters.headers ?: [:]) + [(name): value]
            this
        }

        ParametersBuilder<T> body(body) {
            parameters.body = body
            this
        }

        ParametersBuilder<T> expecting(Class<T> type) {
            parameters.expecting = type
            this
        }

        ParametersBuilder<T> of(Class type) {
            parameters.of = type
            this
        }

        Response<T> execute() {
            method.apply(parameters)
        }
    }

}
