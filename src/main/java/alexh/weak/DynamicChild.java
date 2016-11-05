/*
 * Copyright 2015 Alex Butler
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package alexh.weak;

import java.util.Collection;
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
        else if (val instanceof Collection) return new DynamicCollection.Child(parent, key, (Collection) val);
        return new DynamicSomething.Child(parent, key, val);
    }

    static DynamicChild key(Dynamic parent, Object key) {
        return from(parent, key, key);
    }

    Dynamic parent();
}
