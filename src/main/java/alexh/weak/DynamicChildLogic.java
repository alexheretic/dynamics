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

import java.util.LinkedList;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toCollection;

class DynamicChildLogic {

    static DynamicChildLogic using(DynamicChild child) {
        return new DynamicChildLogic(child);
    }

    private final DynamicChild child;

    private DynamicChildLogic(DynamicChild child) {
        this.child = child;
    }

    public LinkedList<Object> getAscendingKeyChainWithRoot() {
        LinkedList<Object> ascendingKeys = getAscendingChainAllWith(dc -> true).stream()
            .map(child -> {
                return child.key().asObject();
            })
            .collect(toCollection(LinkedList::new));
        ascendingKeys.addFirst(Dynamic.ROOT_KEY);
        return ascendingKeys;
    }

    public LinkedList<DynamicChild> getAscendingChainAllWith(Predicate<DynamicChild> pd) {
        LinkedList<DynamicChild> chain = new LinkedList<>();
        if (!pd.test(child)) return chain;

        chain.add(child);

        Dynamic nextParent = child.parent();
        while (nextParent instanceof DynamicChild && pd.test((DynamicChild) nextParent)) {
            chain.addFirst((DynamicChild) nextParent);
            nextParent = ((DynamicChild) nextParent).parent();
        }
        return chain;
    }
}
