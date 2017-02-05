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

import static alexh.weak.DynamicChildLogic.using;
import static java.lang.String.format;
import alexh.LiteJoiner;
import java.util.*;

interface DynamicChild extends Dynamic {

    String ARROW = "->";

    static DynamicChild from(Dynamic parent, Object key, Object val) {
        Objects.requireNonNull(parent);
        Objects.requireNonNull(key);
        Objects.requireNonNull(val);

        if (val instanceof Map) return new DynamicMap.Child(parent, key, (Map) val);
        if (val instanceof List) return new DynamicList.Child(parent, key, (List) val);
        if (val instanceof Collection) return new DynamicCollection.Child(parent, key, (Collection) val);
        return new DynamicSomething.Child(parent, key, val);
    }

    static DynamicChild key(Dynamic parent, Object key) {
        return from(parent, key, key);
    }

    Dynamic parent();

    /* provides a better ClassCastMessage */
    @Override
    default <T> T as(Class<T> type) {
        try { return type.cast(asObject()); }
        catch (ClassCastException ex) {
            LinkedList<Object> ascendingKeyChain = using(this).getAscendingKeyChainWithRoot();
            Object thisKey = ascendingKeyChain.pollLast();
            ascendingKeyChain.add(format("*%s*", thisKey));
            throw new ClassCastException(format("'%s' miscast in path %s: %s. Avoid by checking " +
                    "`if (aDynamic.is(%s.class)) ...` or using `aDynamic.maybe().as(%<s.class)`",
                thisKey, LiteJoiner.on(ARROW).join(ascendingKeyChain), ex.getMessage(), type.getSimpleName()));
        }
    }
}
