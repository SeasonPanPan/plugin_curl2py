
/**
 * the Curl2py
 *
 * @author PanLongfei
 * @date 2024-11-24
 */
package cn.season.plugin.curl2py;

import com.intellij.AbstractBundle;
import com.intellij.DynamicBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.PropertyKey;

import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

public final class Curl2pyBundle extends AbstractBundle {

    public static final String BUNDLE = "messages.Curl2pyBundle";

    public static final Curl2pyBundle INSTANCE = new Curl2pyBundle();

    public Curl2pyBundle() {
        super(BUNDLE);
    }

    public static String message(@NotNull @PropertyKey(resourceBundle = BUNDLE) String key, Object... params) {
        return INSTANCE.getMessage(key, params);
    }

    @Override
    protected ResourceBundle findBundle(@NotNull @NonNls String pathToBundle, @NotNull ClassLoader loader, @NotNull Control control) {
        return ResourceBundle.getBundle(pathToBundle, DynamicBundle.getLocale(), loader, control);
    }
}
