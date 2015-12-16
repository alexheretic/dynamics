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
import static java.util.Collections.emptyList;
import alexh.LiteJoiner;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

public abstract class ParentAbsence<Parent extends Dynamic> extends AbstractAbsence<Parent> implements IssueDescribingChild {

    public ParentAbsence(Parent parent, Object key) {
        super(parent, key);
    }

    protected abstract String describeIssue(LinkedList<Object> ascendingMarkedKeyChain, Object parentKey);

    @Override
    public final String describeIssue(List<Object> childKeys) {
        LinkedList<Object> keyChain = using(this).getAscendingKeyChainWithRoot();
        keyChain.set(keyChain.size() - 2, "*" + keyChain.get(keyChain.size() - 2).toString() + "*");
        keyChain.addAll(childKeys);
        return describeIssue(keyChain, parent.key().asObject());
    }

    @Override
    public Dynamic get(Object key) {
        return new DescriptionDeferringAbsence(this, key);
    }

    @Override
    public Object asObject() {
        throw new NoSuchElementException(describeIssue(emptyList()));
    }

    public static class Empty<P extends Dynamic & Describer> extends ParentAbsence<P> {

        public Empty(P parent, Object key) {
            super(parent, key);
        }

        @Override
        protected String describeIssue(LinkedList<Object> ascendingMarkedKeyChain, Object parentKey) {
            return format("%s '%s' premature end of path %s", parent.describe(), parentKey,
                LiteJoiner.on(ARROW).join(ascendingMarkedKeyChain));
        }
    }

    public static class Barren<P extends Dynamic & Describer> extends ParentAbsence<P> {

        public Barren(P parent, Object key) {
            super(parent, key);
        }

        @Override
        protected String describeIssue(LinkedList<Object> ascendingMarkedKeyChain, Object parentKey) {
            return format("%s '%s' premature end of path %s", parent.describe(),
                parentKey, LiteJoiner.on(ARROW).join(ascendingMarkedKeyChain));
        }
    }
}