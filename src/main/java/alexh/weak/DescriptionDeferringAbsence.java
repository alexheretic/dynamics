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
import java.util.NoSuchElementException;

import static java.util.stream.Collectors.toList;

/** Must form a chain of DescriptionDeferringAbsentChild with a IssueDescribingDynamicChild parent */
class DescriptionDeferringAbsence extends AbstractAbsence<DynamicChild> {

    DescriptionDeferringAbsence(DescriptionDeferringAbsence parent, Object key) {
        super(parent, key);
    }

    DescriptionDeferringAbsence(IssueDescribingChild parent, Object key) {
        super(parent, key);
    }

    @Override
    public Dynamic get(Object key) {
        return new DescriptionDeferringAbsence(this, key);
    }

    @Override
    public Object asObject() {
        LinkedList<DynamicChild> chainFromDescriber = DynamicChildLogic.using(this).getAscendingChainAllWith(DescriptionDeferringAbsence.class::isInstance);

        throw new NoSuchElementException(((IssueDescribingChild) chainFromDescriber.getFirst().parent())
            .describeIssue(chainFromDescriber.stream().map(child -> child.key().asObject()).collect(toList())));
    }
}
