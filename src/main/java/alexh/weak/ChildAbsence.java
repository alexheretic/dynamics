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

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import alexh.LiteJoiner;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

public abstract class ChildAbsence<Parent extends Dynamic> extends AbstractAbsence<Parent> implements IssueDescribingChild {

    protected ChildAbsence(Parent parent, Object key) {
        super(parent, key);
    }

    abstract protected String describeIssue(LinkedList<Object> ascendingMarkedKeyChain, LinkedList<Object> ascendingKeyChainBeforeSelf);

    @Override
    public final String describeIssue(List<Object> childKeys) {
        LinkedList<Object> keyChainUntilSelf = DynamicChildLogic.using(this).getAscendingKeyChainWithRoot();
        keyChainUntilSelf.removeLast();

        LinkedList<Object> fullKeyChain = new LinkedList<>(keyChainUntilSelf);
        fullKeyChain.addLast("*"+ key +"*");
        fullKeyChain.addAll(childKeys);
        return describeIssue(fullKeyChain, keyChainUntilSelf);
    }

    @Override
    public Dynamic get(Object key) {
        return new DescriptionDeferringAbsence(this, key);
    }

    @Override
    public Object asObject() {
        throw new NoSuchElementException(describeIssue(emptyList()));
    }

    public static class Null extends ChildAbsence<Dynamic> {

        public Null(Dynamic parent, Object key) {
            super(parent, key);
        }

        @Override
        protected String describeIssue(LinkedList<Object> ascendingMarkedKeyChain, LinkedList<Object> ascendingKeyChainBeforeSelf) {
            return format("null '%s' premature end of path %s", key, LiteJoiner.on(ARROW).join(ascendingMarkedKeyChain));
        }
    }

    public static class Missing<P extends Dynamic & Describer> extends ChildAbsence<P> {

        public Missing(P parent, Object key) {
            super(parent, key);
        }

        @Override
        protected String describeIssue(LinkedList<Object> ascendingMarkedKeyChain, LinkedList<Object> ascendingKeyChainBeforeSelf) {
            return format("'%s' key is missing in path %s, from %s: %s", key,
                LiteJoiner.on(ARROW).join(ascendingMarkedKeyChain), LiteJoiner.on(ARROW).join(ascendingKeyChainBeforeSelf),
                parent.describe());
        }
    }
}