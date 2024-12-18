package cn.season.plugin.curl2py.curl;

import cn.season.plugin.curl2py.utils.Strings;
import com.intellij.openapi.diagnostic.Logger;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import static cn.season.plugin.curl2py.curl.UrlConstant.Header;
import static cn.season.plugin.curl2py.curl.UrlConstant.validMethods;

/**
 * Curl解析器
 *
 * @author PanLongfei
 * @date 2024-11-26
 */
public class CurlParser {

    private static final Logger LOGGER = Logger.getInstance(CurlParser.class);

    static final Pattern urlPattern = Pattern.compile("^[A-Za-z]+:/{2,3}.*");

    static final Map<String, BiConsumer<CurlObject, String>> tokenDealMap = new LinkedHashMap<>();

    static {
        //--------- get, post, put, delete ...
        tokenDealMap.put("-X", CurlParser::dealMethod);
        tokenDealMap.put("--request", CurlParser::dealMethod);

        // ------ header params -----
        tokenDealMap.put("-H", CurlParser::dealHeader);
        tokenDealMap.put("--header", CurlParser::dealHeader);

        tokenDealMap.put("-c", CurlParser::dealCookies);
        tokenDealMap.put("--cookie", CurlParser::dealCookies);

        tokenDealMap.put("-e", CurlParser::dealReferer);
        tokenDealMap.put("--referer", CurlParser::dealReferer);

        tokenDealMap.put("-u", CurlParser::dealAuthUser);
        tokenDealMap.put("--user", CurlParser::dealAuthUser);

        tokenDealMap.put("-A", CurlParser::dealUserAgent);
        tokenDealMap.put("--user-agent", CurlParser::dealUserAgent);

        // ------- body -----------
        tokenDealMap.put("-d", CurlParser::dealData);
        tokenDealMap.put("--data", CurlParser::dealData);

        tokenDealMap.put("--data-ascii", CurlParser::dealRawData);
        tokenDealMap.put("--data-raw", CurlParser::dealRawData);
        tokenDealMap.put("--data-binary", CurlParser::dealRawData);

        tokenDealMap.put("--data-urlencode", CurlParser::dealDataUrlEncode);

        tokenDealMap.put("-F", CurlParser::dealFormData);
        tokenDealMap.put("--form", CurlParser::dealFormData);
        tokenDealMap.put("--form-file", CurlParser::dealFormData);
        tokenDealMap.put("--form-string", CurlParser::dealFormData);

        // -------- proxy ---------------
        tokenDealMap.put("-x", CurlParser::dealProxies);
        tokenDealMap.put("--proxy", CurlParser::dealProxies);
    }

    private CurlParser() {}

    public static CurlObject parse(List<String> urlTokens) {
        CurlObject curlObject = new CurlObject();
        for (int i = 0; i < urlTokens.size(); i++) {
            String token = urlTokens.get(i);
            if (Strings.isEmpty(token) || "curl".equalsIgnoreCase(token)) {
                continue;
            }
            // url
            if (urlPattern.matcher(token).matches()) {
                dealUrl(curlObject, token);
                continue;
            }

            if ("-G".equals(token) || "--get".equals(token)) {
                dealForceGet(curlObject);
                continue;
            }

            if ("-I".equals(token) || "--head".equals(token)) {
                dealHeadMethod(curlObject);
                continue;
            }

            if ("--compressed".equals(token)) {
                dealCompressed(curlObject);
                continue;
            }

            // method, headers, params, cookies
            BiConsumer<CurlObject, String> biConsumer = tokenDealMap.get(token);
            if (biConsumer != null && (i + 1) < urlTokens.size()) {
                try {
                    biConsumer.accept(curlObject, urlTokens.get(i + 1));
                    i++;
                } catch (Exception e) {
                    LOGGER.warn("failed to deal:" + token, e);
                }
            }
        }
        return curlObject;
    }


    public static void dealUrl(CurlObject curlObject, String url) {
        try {
            URI uri = new URI(url);
            String query = uri.getQuery();
            if (Strings.isNotEmpty(query)) {
                parameterHandler(curlObject, query);
            }
            StringBuilder builder = new StringBuilder();
            builder.append(uri.getScheme()).append("://").append(uri.getHost());
            if (uri.getPort() != -1) {
                builder.append(":").append(uri.getPort());
            }
            builder.append(uri.getRawPath());

            LOGGER.info("formatted url=" + builder);
            curlObject.setUrl(builder.toString());
        } catch (URISyntaxException e) {
            LOGGER.warn("invalid url:" + url, e);
            curlObject.setUrl(url);
        }
    }

    public static void dealForceGet(CurlObject curlObject) {
        curlObject.setMethod("get");
        curlObject.setForceGet(true);
    }

    private static void dealHeadMethod(CurlObject curlObject) {
        dealMethod(curlObject, "head");
    }

    private static void dealCompressed(CurlObject curlObject) {
        if (Strings.isEmpty(curlObject.getHeaderParam(Header.acceptEncoding))) {
            curlObject.addHeader(Header.acceptEncoding, "gzip, deflate, br");
        }
    }

    public static void dealMethod(CurlObject curlObject, String method) {
        if (!curlObject.isForceGet()) {
            String lowerMethod = method.toLowerCase(Locale.US);
            if (!validMethods.contains(lowerMethod)) {
                LOGGER.warn("Unsupported method: " + method + ", use GET as default.");
                lowerMethod = "get";
            }
            curlObject.setMethod(lowerMethod);
        }
    }


    public static void dealHeader(CurlObject curlObject, String hkv) {
        String[] kv = hkv.split(":", 2);
        if (kv.length < 2) {
            return;
        }
        // 小写key
        String hk = kv[0].trim().toLowerCase(Locale.US);
        // 特殊处理 -H $'cookie:aa'
        if (hk.startsWith("$")) {
            hk = hk.substring(1);
        }
        String hv = kv[1].trim();

        if (Header.cookie.equals(hk)) {
            dealCookies(curlObject, hv);
        } else {
            curlObject.addHeader(hk, hv);
        }
    }

    public static void dealData(CurlObject curlObject, String data) {
        if (curlObject.isForceGet()) {
            parameterHandler(curlObject, data);
            return;
        }
        if (Strings.isEmpty(curlObject.getHeaderParam(Header.contentType))) {
            curlObject.addHeader(Header.contentType, "application/x-www-form-urlencoded");
        }

        String oldData = curlObject.getData();
        if (Strings.isEmpty(oldData)) {
            dealRawData(curlObject, data);
        } else {
            curlObject.setData(oldData + "&" + data);
        }
    }

    public static void dealRawData(CurlObject curlObject, String rawData) {
        String method = curlObject.getMethod();
        if ("GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method)) {
            dealMethod(curlObject, "POST");
        }
        curlObject.setData(rawData);
    }

    public static void dealDataUrlEncode(CurlObject curlObject, String data) {
        data = URLEncoder.encode(data, StandardCharsets.UTF_8);
        dealData(curlObject, data);
    }


    public static void dealUserAgent(CurlObject curlObject, String userAgent) {
        curlObject.addHeader(Header.userAgent, userAgent);
    }

    public static void dealCookies(CurlObject curlObject, String cookie) {
        String headCookies = curlObject.getHeaderParam(Header.cookie);
        if (Strings.isEmpty(headCookies)) {
            headCookies = cookie;
        } else {
            headCookies = headCookies + "; " + cookie;
        }
        curlObject.addHeader(Header.cookie, headCookies);
    }

    public static void dealReferer(CurlObject curlObject, String referer) {
        curlObject.addHeader(Header.referer, referer);
    }


    public static void dealFormData(CurlObject curlObject, String data) {
        String method = curlObject.getMethod();
        if ("GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method)) {
            dealMethod(curlObject, "POST");
        }
        // form格式的如果有文件，在 multipart/form-data 后面还有文件信息，py会自动生成
        curlObject.getHs().remove(Header.contentType);
        // curlObject.addHeader("Content-Type", "application/x-www-form-urlencoded");

        // -F "page=@/tmp/a;filename=a.txt;type=text/html"，-F可以多次出现
        String[] dataForms = data.split(";");
        // LOGGER.info("dataForms="+ Arrays.toString(dataForms));
        CurlObject.Form form = new CurlObject.Form();
        String fileKey = null;
        for (String dataForm : dataForms) {
            try {
                String[] kv = dataForm.trim().split("=", 2);
                if (kv.length > 1) {
                    String ktrim = kv[0].trim().toLowerCase(Locale.US);
                    String vtrim = kv[1].trim();
                    switch (ktrim) {
                        case "type":
                            form.setType(vtrim);
                            break;
                        case "filename":
                            form.setFilename(vtrim);
                            break;
                        default:
                            if (fileKey == null) {
                                fileKey = ktrim;
                            }
                            form.setKeyValue(ktrim, vtrim);
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("invalid form data:"+ dataForm, e);
            }
        }
        if (Strings.isNotEmpty(fileKey)) {
            curlObject.addForm(fileKey, form);
        }
    }

    public static void dealAuthUser(CurlObject curlObject, String authUser) {
        curlObject.addHeader(Header.authorization, authUser);
    }

    public static void dealProxies(CurlObject curlObject, String proxy) {
        if (proxy.startsWith("https")) {
            curlObject.addProxy("https", proxy);
        } else {
            curlObject.addProxy("http", proxy);
        }
    }


    private static void parameterHandler(CurlObject curlObject, String parameter) {
        String[] params = parameter.split("&");
        for (String param : params) {
            String[] kv = param.trim().split("=", 2);
            if (kv.length > 1) {
                curlObject.addParam(kv[0].trim(), kv[1].trim());
            }
        }

    }
}
