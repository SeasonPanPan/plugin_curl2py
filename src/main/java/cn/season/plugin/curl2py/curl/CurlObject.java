package cn.season.plugin.curl2py.curl;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * the CurlObject
 *
 * @author PanLongfei
 * @date 2024-11-26
 */
public class CurlObject {

    private String url = "";
    private String method = "get";
    private boolean forceGet = false;
    private String data;

    private final Map<String, String> hs = new LinkedHashMap<>();
    private final Map<String, String> ps = new LinkedHashMap<>();
    private final Map<String, String> cs = new LinkedHashMap<>();
    private final Map<String, Form> fs = new LinkedHashMap<>();
    private final Map<String, String> pxs = new LinkedHashMap<>();


    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public boolean isForceGet() {
        return forceGet;
    }

    public void setForceGet(boolean forceGet) {
        this.forceGet = forceGet;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Map<String, String> getHs() {
        return hs;
    }

    public String getHeaderParam(String key) {
        return hs.get(key);
    }

    public void addHeader(String k, String v) {
        this.hs.put(k, v);
    }

    public Map<String, String> getPs() {
        return ps;
    }

    public void addParam(String k, String v) {
        this.ps.put(k, v);
    }

    public Map<String, String> getCookies() {
        return cs;
    }

    public void addCookie(String k, String v) {
        this.cs.put(k, v);
    }

    public Map<String, Form> getForms() {
        return fs;
    }

    public void addForm(String k, Form v) {
        this.fs.put(k, v);
    }

    public Map<String, String> getPxs() {
        return pxs;
    }

    public void addProxy(String k, String v) {
        this.pxs.put(k, v);
    }

    @Override
    public String toString() {
        return "CurlObject{" +
                "url='" + url + '\'' +
                ", method='" + method + '\'' +
                ", data=" + data +
                ", headers=" + hs +
                ", ps=" + ps +
                ", cookies=" + cs +
                ", forms=" + fs +
                ", proxies=" + pxs +
                '}';
    }

    /**
     * -F "page=@/tmp/a;filename=a.txt;type=text/html"
     */
    public static class Form {

        private String key;
        private String value;
        private String filename;
        private String type;

        public Form() {
        }

        public Form(String key, String value) {
            this.key = key;
            this.value = value;
        }

        public void setKeyValue(String fileKey, String fileValue) {
            this.key = fileKey;
            this.value = fileValue;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return "Form{" +
                    "key='" + key + '\'' +
                    ", value='" + value + '\'' +
                    ", filename='" + filename + '\'' +
                    ", type='" + type + '\'' +
                    '}';
        }
    }
}
