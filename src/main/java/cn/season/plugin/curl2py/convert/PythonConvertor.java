package cn.season.plugin.curl2py.convert;

import cn.season.plugin.curl2py.curl.CurlObject;
import cn.season.plugin.curl2py.curl.CurlParser;
import cn.season.plugin.curl2py.curl.UrlConstant;
import cn.season.plugin.curl2py.utils.CmdTokensUtil;
import cn.season.plugin.curl2py.utils.Strings;
import com.intellij.openapi.diagnostic.Logger;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * the PythonConvertor
 *
 * @author PanLongfei
 * @date 2024-11-26
 */
public class PythonConvertor implements ICode {

    private static final Logger LOGGER = Logger.getInstance(PythonConvertor.class);

    @Override
    public String to(String text) {
        return toPythonCode(text);
    }

    public static String toPythonCode(final String cmd) {
        List<String> tokenizes = CmdTokensUtil.tokenize(cmd);
        List<String> newTokenizes = tokenizes.stream().filter(Strings::isNotBlank).collect(Collectors.toList());
        CurlObject curlObject = CurlParser.parse(newTokenizes);
        String code = toPythonCode(curlObject);
        return code;
    }

    /**
     * url = "https://exam.cfachina.org:8080/siteservice/GradesQueryService/GetUserGradesQuery"
     * headers = {
     * "apikey": "116ce9c7-186e-43cd-87fb-1263ad263262",
     * "accept": "application/json, text/plain, *",
     * "accept-language": "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7",
     * "cache-control": "no-cache",
     * "certsite-passport-token": "gKk1TcpDK99wmyXoJ9UEhyAA28wal783LNFv_COYPlz8Q8xWLgN0p1OoqHBbcEhvuBUcHH5Bo9fkUmVLphVtOA",
     * "connection": "keep-alive",
     * "content-type": "application/json;charset=UTF-8",
     * "cookie": "HMACCOUNT=9B2B6F710B36A033; Hm_lvt_2f0770487c6a21bfd1b5ebefd42f19e7=1731592923; .AspNetCore.Session=CfDJ8AbQA%2Bwfg%2FdBlQGgMjQdGpyZ20cJlnKHeTy8YXwEKPRtuZ%2BHQ0UZ%2BU6Jc68L9S7PUY6w865m0VncujBtZnl4en%2FeqJWQrM2kBeBtnVDSWvr0R5oBV8HyxMVajsyERYI1Nfwmj%2FjiWQsY2hgg1zsP4DZg11UmYT8J7ZvQ9VFRXmis; Hm_lvt_caecf5079fb62c832f3100bf931e1e84=1731592151; Hm_lpvt_caecf5079fb62c832f3100bf931e1e84=1732544388; Hm_lpvt_2f0770487c6a21bfd1b5ebefd42f19e7=1732544391",
     * "origin": "https://exam.cfachina.org",
     * "pragma": "no-cache",
     * "referer": "https://exam.cfachina.org/",
     * "sec-fetch-dest": "empty",
     * "sec-fetch-mode": "cors",
     * "sec-fetch-site": "same-origin",
     * "user-agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36",
     * "sec-ch-ua": "\"Google Chrome\";v=\"123\", \"Not:A-Brand\";v=\"8\", \"Chromium\";v=\"123\"",
     * "sec-ch-ua-mobile": "?0",
     * "sec-ch-ua-platform": "\"Windows\""
     * }
     * json = {
     * "fExamUserName": "xxx",
     * "fExamUserNum": "xxxx",
     * "txtValidCode": ""
     * }
     * proxies = {
     * 'http': 'http://username:password@proxy_ip:proxy_port',
     * 'https': 'https://username:password@proxy_ip:proxy_port'
     * }
     * <p>
     * res = requests.post(url=url,headers=headers,json=json, proxies=proxies)
     * res.raise_for_status()
     * print(res.text)
     *
     * @param curlObject CurlObject
     * @return python code
     */
    public static String toPythonCode(final CurlObject curlObject) {
        if (Strings.isBlank(curlObject.getUrl())) {
            LOGGER.warn("miss url.");
            return "";
        }

        List<String> varCodes = new ArrayList<>(16);
        List<String> options = new ArrayList<>(8);
        varCodes.add("url = '" + curlObject.getUrl() + "'");
        options.add("url=url");
        createHeaders(curlObject, varCodes, options);
        createParams(curlObject, varCodes, options);
        createData(curlObject, varCodes, options);
        createForms(curlObject, varCodes, options);
        createProxies(curlObject, varCodes, options);
        String variableCode = String.join("\n", varCodes) + "\n\n";

        StringBuilder sb = new StringBuilder();
        sb.append(variableCode);
        String method = getMethod(curlObject);
        String optionsCode = String.join(", ", options);
        //requests.post(url=url, headers=headers, json=json)
        sb.append("res = requests.").append(method).append("(").append(optionsCode).append(")\n");
        sb.append("res.raise_for_status()\n");
        sb.append("print(res.text)\n");
        return sb.toString();
    }

    static void createData(CurlObject curlObject, List<String> varCodes, List<String> options) {
        String data = curlObject.getData();
        if (Strings.isNotEmpty(data)) {
            String contentType = curlObject.getHeaderParam(UrlConstant.Header.contentType);
            if (Strings.isNotEmpty(contentType)) {
                contentType = contentType.toLowerCase(Locale.US);
                if (contentType.contains("json")) {
                    String json_data = Strings.formatJson(data);
                    varCodes.add("json_data = " + json_data);
                    options.add("json=json_data");
                    return;
                } else if (contentType.contains("x-www-form-urlencoded")) {
                    varCodes.add("data = " + formatFormData(data));
                    options.add("data=data");
                    return;
                }
            }

            varCodes.add("data = '" + data + "'");
            options.add("data=data");
        }
    }

    static void createHeaders(CurlObject curlObject, List<String> varCodes, List<String> options) {
        Map<String, String> headers = curlObject.getHs();
        if (!headers.isEmpty()) {
            // "apikey": "116ce9c7-186e-43cd-87fb-1263ad263262",
            String sb = "headers = " + Strings.toMyJson(headers);
            varCodes.add(sb);
            options.add("headers=headers");
        }
    }

    static void createParams(CurlObject curlObject, List<String> varCodes, List<String> options) {
        Map<String, String> params = curlObject.getPs();
        if (!params.isEmpty()) {
            // String sb = "params = {\n" + createKvJson(params) + "}";
            String sb = "params = " + Strings.toMyJson(params);
            varCodes.add(sb);
            options.add("params=params");
        }
    }

    /**
     * -F "page=@/tmp/a;filename=a.txt;type=text/html" 多个
     *
     * @param curlObject
     * @param varCodes
     * @param options
     */
    static void createForms(CurlObject curlObject, List<String> varCodes, List<String> options) {
        Map<String, CurlObject.Form> forms = curlObject.getForms();
        if (!forms.isEmpty()) {
            Map<String, String> formMap = new HashMap<>();
            forms.forEach((k, vform) -> {
                String fileValue = vform.getValue();
                // @文件路径和名
                if (fileValue.startsWith("@")) {
                    // file=@/app/a.txt ==> file: open('/app/a.txt', 'rb')
                    String filePath = fileValue.substring(1); // remove @
                    if (Strings.isEmpty(vform.getFilename())) {
                        String fp = "open('%s', 'rb')".formatted(filePath);
                        formMap.put(k, fp);
                        return;
                    }

                    String filename = vform.getFilename();
                    String type = vform.getType();
                    String fnt;
                    if (Strings.isEmpty(type)) {
                        fnt = "('%s', open('%s', 'rb'))".formatted(filename, filePath);
                    } else {
                        fnt = "('%s', open('%s', 'rb'), '%s')".formatted(filename, filePath, type);
                    }
                    formMap.put(k, fnt);
                } else {
                    formMap.put(k, "'" + fileValue + "'");
                }
            });

            StringBuilder sb = new StringBuilder("files_data = {\n");
            sb.append(formMap.entrySet().stream()
                    .map(e -> "    '%s': %s".formatted(e.getKey(), e.getValue()))
                    .collect(Collectors.joining(",\n")));
            sb.append("\n}");

            varCodes.add(sb.toString());
            options.add("files=files_data");
        }
    }

    private static void createProxies(CurlObject curlObject, List<String> varCodes, List<String> options) {
        Map<String, String> proxies = curlObject.getPxs();
        if (!proxies.isEmpty()) {
            String sb = "proxies = " + Strings.toMyJson(proxies);
            varCodes.add(sb);
            options.add("proxies=proxies");
        }
    }

    private static String getMethod(CurlObject curlObject) {
        if (curlObject.isForceGet()) {
            return "get";
        }
        return curlObject.getMethod().toLowerCase(Locale.US);
    }


    private static String formatFormData(String data) {
        try {
            String[] datakv = data.split("&");
            Map<String, String> kvMap = new LinkedHashMap<>(data.length());
            for (String kvStr : datakv) {
                String[] kvs = kvStr.split("=", 2);
                if (kvs.length > 1) {
                    String v = kvs[1];
                    v = URLDecoder.decode(v, StandardCharsets.UTF_8);
                    kvMap.put(kvs[0], v);
                }
            }
            return Strings.toMyJson(kvMap);
        } catch (Exception e) {
            LOGGER.warn("failed to format " + data, e);
        }
        return data;
    }


}
