package com.kbalabala.tools;

import jodd.http.HttpRequest;
import jodd.http.HttpResponse;
import jodd.http.HttpUtil;
import jodd.util.StringPool;

import java.util.HashMap;
import java.util.Map;

/**
 * 积木盒子字符串公共类
 * <p>
 *  <ol>
 *   <li>http get请求并返回相应内容（默认返回内容编码UTF－8）{@link com.kbalabala.tools.JmbHttpUtils#get(String url, Map params)}</li>
 *   <li>http get请求并返回相应内容{@link com.kbalabala.tools.JmbHttpUtils#get(String url, String charset, Map params)}</li>
 *   <li>http post请求并返回相应内容（默认返回内容编码UTF－8）{@link com.kbalabala.tools.JmbHttpUtils#post(String url, Map params)}</li>
 *   <li>http post请求并返回相应内容{@link com.kbalabala.tools.JmbHttpUtils#post(String url, String charset, Map params)}</li>
 *   <li>http post使用Raw方式对body编码{@link com.kbalabala.tools.JmbHttpUtils#post(String url, String body)}</li>
 * </p>
 * @author kevin
 * @since  2015-4-21
 */
public class JmbHttpUtils extends HttpUtil {

    /**
     * http get请求并返回相应内容（默认返回内容编码UTF－8）
     * @param url
     * @param params
     * @return
     */
    public static String get(String url, Map<String, Object> params) {

        return get(url, Constants.CHARSET_UTF8, params);
    }

    /**
     * http get请求并返回相应内容
     * @param url
     * @param charset 返回内容编码
     * @param params
     * @return
     */
    public static String get(String url, String charset, Map<String, Object> params) {

        String realCharset = JmbStringUtils.isBlank(charset) ? Constants.CHARSET_UTF8 : charset;

        return HttpRequest.get(url)
                          .queryString(JmbStringUtils.joinPairs(params, JmbDateUtils.FORMAT_YYYY_MM_DD, StringPool.EQUALS, StringPool.AMPERSAND))
                          .charset(realCharset).send()
                          .charset(realCharset).bodyText();

    }

    /**
     * http get请求并返回相应内容
     * @param url
     * @param charset 返回内容编码
     * @param params
     * @return
     */
    public static HttpResponse getHttpResponseViaGet(String url, String charset, Map<String, Object> params, Map<String, String> headers) {

        String realCharset = JmbStringUtils.isBlank(charset) ? Constants.CHARSET_UTF8 : charset;

        HttpRequest request = HttpRequest.get(url);
        if(headers != null){
            for(Map.Entry<String, String> header : headers.entrySet()) {
                request.header(header.getKey(), header.getValue());
            }
        }

        return request
                .queryString(JmbStringUtils.joinPairs(params, JmbDateUtils.FORMAT_YYYY_MM_DD, StringPool.EQUALS, StringPool.AMPERSAND))
                .charset(realCharset).send();

    }


    /**
     * http post请求并返回相应内容（默认返回内容编码UTF－8）
     * @param url
     * @param params
     * @return
     */
    public static String post(String url, Map<String, Object> params){
        return post(url, Constants.CHARSET_UTF8, params);
    }

    /**
     * http post请求并返回相应内容
     * @param url
     * @param charset 返回内容编码
     * @param params
     * @return
     */
    public static String post(String url, String charset, Map<String, Object> params) {
        return postForm(url, charset, params, null);
    }

    /**
     * http post使用Raw方式对body编码
     * @param url
     * @param body
     * @return
     */
    public static String post(String url, String body){
        return postRaw(url, Constants.CHARSET_UTF8, body, null);
    }

    /**
     * Http post使用form方式编码body
     * @param url
     * @param charset
     * @param params
     * @param headers
     * @return
     */
    public static String postForm(String url,
                                  String charset,
                                  Map<String, Object> params,
                                  Map<String, String> headers) {

        String realCharset = JmbStringUtils.isBlank(charset) ? Constants.CHARSET_UTF8 : charset;

        HttpRequest request = HttpRequest.post(url);
        if(headers != null){
            for(Map.Entry<String, String> header : headers.entrySet()) {
                request.header(header.getKey(), header.getValue());
            }
        }

        if(params != null){
            request.form(params);
        }
        return request.send().charset(realCharset).bodyText();

    }

    /**
     * Http post使用form方式编码body
     * @param url
     * @param params
     * @param headers
     * @return
     */
    public static HttpResponse getHttpResponseViaPostForm(String url,
                                  Map<String, Object> params,
                                  Map<String, String> headers) {

        HttpRequest request = HttpRequest.post(url);
        if(headers != null){
            for(Map.Entry<String, String> header : headers.entrySet()) {
                request.header(header.getKey(), header.getValue());
            }
        }

        if(params != null){
            request.form(params);
        }
        return request.send();

    }

    /**
     * Http post使用raw方式编码body
     * @param url
     * @param charset
     * @param body
     * @param headers
     * @return
     */
    public static String postRaw(String url,
                                 String charset,
                                 String body,
                                 Map<String, String> headers) {

        String realCharset = JmbStringUtils.isBlank(charset) ? Constants.CHARSET_UTF8 : charset;

        HttpRequest request = HttpRequest.post(url);
        if(headers != null){
            for(Map.Entry<String, String> header : headers.entrySet()) {
                request.header(header.getKey(), header.getValue());
            }
        }

        if(JmbStringUtils.isBlank(body)) {
            body = Constants.STRING_BLANK;
        }
        return request.body(body).send().charset(realCharset).bodyText();
    }

    /**
     * Http post使用raw方式编码body
     * @param url
     * @param body
     * @param headers
     * @return
     */
    public static HttpResponse getHttpResponseViaPostRaw(String url,
                                 String body,
                                 Map<String, String> headers) {

        HttpRequest request = HttpRequest.post(url);
        if(headers != null){
            for(Map.Entry<String, String> header : headers.entrySet()) {
                request.header(header.getKey(), header.getValue());
            }
        }

        if(JmbStringUtils.isBlank(body)) {
            body = Constants.STRING_BLANK;
        }
        return request.body(body).send();
    }

    /**
     * Http put使用raw方式编码body
     * @param url
     * @param charset
     * @param body
     * @param headers
     * @return
     */
    public static String putRaw(String url,
                                 String charset,
                                 String body,
                                 Map<String, String> headers) {

        String realCharset = JmbStringUtils.isBlank(charset) ? Constants.CHARSET_UTF8 : charset;

        HttpRequest request = HttpRequest.put(url);
        if(headers != null){
            for(Map.Entry<String, String> header : headers.entrySet()) {
                request.header(header.getKey(), header.getValue());
            }
        }

        if(JmbStringUtils.isBlank(body)) {
            body = Constants.STRING_BLANK;
        }
        return request.body(body).send().charset(realCharset).bodyText();
    }

    /**
     * Http put使用raw方式编码body
     * @param url
     * @param body
     * @param headers
     * @return
     */
    public static HttpResponse getHttpResponseViaPutRaw(String url,
                                String body,
                                Map<String, String> headers) {

        HttpRequest request = HttpRequest.put(url);
        if(headers != null){
            for(Map.Entry<String, String> header : headers.entrySet()) {
                request.header(header.getKey(), header.getValue());
            }
        }

        if(JmbStringUtils.isBlank(body)) {
            body = Constants.STRING_BLANK;
        }
        return request.body(body).send();
    }

    /**
     * Http delete使用raw方式编码body
     * @param url
     * @param charset
     * @param body
     * @param headers
     * @return
     */
    public static String deleteRaw(String url,
                                String charset,
                                String body,
                                Map<String, String> headers) {

        String realCharset = JmbStringUtils.isBlank(charset) ? Constants.CHARSET_UTF8 : charset;

        HttpRequest request = HttpRequest.delete(url);
        if(headers != null){
            for(Map.Entry<String, String> header : headers.entrySet()) {
                request.header(header.getKey(), header.getValue());
            }
        }

        if(JmbStringUtils.isBlank(body)) {
            body = Constants.STRING_BLANK;
        }
        return request.body(body).send().charset(realCharset).bodyText();
    }

    /**
     * Http delete使用raw方式编码body
     * @param url
     * @param body
     * @param headers
     * @return
     */
    public static HttpResponse getHttpResponseViaDeleteRaw(String url,
                                   String body,
                                   Map<String, String> headers) {

        HttpRequest request = HttpRequest.delete(url);
        if(headers != null){
            for(Map.Entry<String, String> header : headers.entrySet()) {
                request.header(header.getKey(), header.getValue());
            }
        }

        if(JmbStringUtils.isBlank(body)) {
            body = Constants.STRING_BLANK;
        }
        return request.body(body).send();
    }

    public static void main(String[] args){


        Map<String, Object> params = new HashMap<>();
        params.put("type", "ranking");
        params.put("openid", 10230);
        params.put("zcm", 12312312);
        System.out.println(JmbHttpUtils.get("https://www.jimubox.com", params));

    }
}
