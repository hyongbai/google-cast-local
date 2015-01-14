package yourbay.me.castimages.cast;

/**
 * Created by ram on 15/1/7.
 */
public class CastUtil {

    /**
     * Print class name and its parent name of an object
     *
     * @param obj
     */
    public final static String getClassInheritence(Object obj) {
        if (obj == null) {
            return null;
        }
        StringBuilder stb = new StringBuilder();
        Class<?> cls = obj.getClass();
        String name = cls.getSimpleName();
        stb.append("[");
        stb.append(String.valueOf(obj));
        stb.append(":");
        while (!name.toLowerCase().equals("object")) {
            stb.append(name);
            stb.append("<-");
            cls = cls.getSuperclass();
            name = cls.getSimpleName();
        }
        stb.append("]");
        return stb.toString();
    }
}
