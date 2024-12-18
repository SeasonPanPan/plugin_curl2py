package cn.season.plugin.curl2py.convert;

/**
 * the ICodeConvertor
 *
 * @author PanLongfei
 * @date 2024-12-04
 */
@FunctionalInterface
public interface ICode {

    /**
     * 代码转换器。
     *
     * @param text
     * @return
     */
    String to(String text);

}
