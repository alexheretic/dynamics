package alexh.weak;

import java.util.List;

interface IssueDescribingChild extends DynamicChild {

    /**
     * @param childKeys all child keys of this empty child in ascending order
     * @return the reason this child has an issue
     */
    String describeIssue(List<Object> childKeys);
}
