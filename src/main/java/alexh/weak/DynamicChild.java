package alexh.weak;

import java.util.List;
import java.util.Map;
import java.util.Objects;

interface DynamicChild extends Dynamic {

    String ARROW = "->";

    static DynamicChild from(Dynamic parent, Object key, Object val) {
        Objects.requireNonNull(parent);
        Objects.requireNonNull(key);
        Objects.requireNonNull(val);

        if (val instanceof Map) return new DynamicMap.Child(parent, key, (Map) val);
        else if (val instanceof List) return new DynamicList.Child(parent, key, (List) val);
        else return new DynamicSomething.Child(parent, key, val);
    }

    Dynamic parent();

    Object key();
}
