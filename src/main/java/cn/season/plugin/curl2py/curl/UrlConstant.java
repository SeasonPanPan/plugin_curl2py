package cn.season.plugin.curl2py.curl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * the UrlConstant
 *
 * @author PanLongfei
 * @date 2024-12-08
 */
public interface UrlConstant {

    Set<String> validMethods = new HashSet<>(Arrays.asList("get", "post", "put", "delete", "head"));

    interface Header {
        String cookie = "cookie";
        String contentType = "content-type";
        String acceptEncoding = "accept-encoding";
        String userAgent = "user-agent";
        String referer = "referer";
        String authorization = "authorization";

    }
}
