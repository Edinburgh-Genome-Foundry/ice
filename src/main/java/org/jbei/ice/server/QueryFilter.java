package org.jbei.ice.server;

import java.util.List;

import org.jbei.ice.shared.FilterTrans;
import org.jbei.ice.shared.QueryOperator;
import org.jbei.ice.shared.SearchFilterType;

/**
 * Created from the transmitted filter info
 * from the client
 * 
 * @author Hector Plahar
 */

public class QueryFilter {

    private final SearchFilterType type;
    private final QueryOperator operator;
    private final String operand;
    private List<QueryFilterParams> params;

    public QueryFilter(FilterTrans trans) {

        if (trans == null)
            throw new IllegalArgumentException("Null parameter for QueryFilter");

        type = SearchFilterType.valueOf(trans.getType());
        operator = QueryOperator.valueOf(trans.getOperator());
        operand = trans.getOperand();

        params = SearchFilterCallbackFactory.getFilterParameters(trans);
    }

    public QueryFilter(SearchFilterType type, QueryOperator operator, String operand) {

        this.type = type;
        this.operator = operator;
        this.operand = operand;
    }

    public List<QueryFilterParams> getParams() {
        return this.params;
    }

    public String getOperand() {
        return this.operand;
    }

    public QueryOperator getOperator() {
        return this.operator;
    }

    public SearchFilterType getSearchType() {
        return this.type;
    }
}
