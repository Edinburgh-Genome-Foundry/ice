package org.jbei.ice.lib.managers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jbei.ice.lib.dao.DAO;
import org.jbei.ice.lib.logging.Logger;
import org.jbei.ice.server.QueryFilter;
import org.jbei.ice.server.QueryFilterParams;
import org.jbei.ice.shared.QueryOperator;

public class QueryManager {

    @SuppressWarnings("unchecked")
    private static Set<Long> runHibernateQuery(String queryString) throws ManagerException {
        Set<Long> results = null;

        Session session = DAO.newSession();
        session.beginTransaction();
        Query query = session.createQuery(queryString);

        try {
            results = new HashSet<Long>(query.list());
            return results;
        } catch (HibernateException e) {
            Logger.error("Could not query ", e);
            throw new ManagerException(e);
        } finally {
            session.getTransaction().rollback();
            if (session.isOpen()) {
                session.close();
            }
        }
    }

    public static Set<Long> runFilters(ArrayList<QueryFilter> filters) throws ManagerException {

        Set<Long> results = null;

        for (QueryFilter filter : filters) {

            Set<Long> intermediate = fetchIntermediateResults(filter.getParams(),
                filter.getOperator(), filter.getOperand());

            if (results == null) {
                results = new HashSet<Long>();
                results.addAll(intermediate);
            } else
                results.retainAll(intermediate);

            // if current results set is empty no need to run other filters
            if (results.isEmpty())
                return results;
        }

        return results;
    }

    protected static Set<Long> fetchIntermediateResults(List<QueryFilterParams> filterParams,
            QueryOperator operator, String operand) throws ManagerException {

        switch (operator) {
        case BOOLEAN:
            return getIntermediateResultsHasX(filterParams.get(0), Boolean.valueOf(operand));

        default:
            return getIntermediateResultsUnion(filterParams);
        }
    }

    /**
     * Runs query using the specified filter params. If the user is interested in seeing the
     * complement
     * of that (specified using yes/no radio box), then the complement of that query results is
     * returned.
     * The universe is all the entry records
     * 
     * @param filterParams
     *            expects only a singleton filterParameter. The criterion in this case is also not
     *            needed
     * @param yes
     *            true if results from param should be returned, false if complement is desired
     * @return set of computed entry ids
     * @throws ManagerException
     */
    protected static Set<Long> getIntermediateResultsHasX(QueryFilterParams param, boolean yes)
            throws ManagerException {

        String queryString = "SELECT DISTINCT " + param.getSelection() + " FROM " + param.getFrom();
        if (yes)
            return runHibernateQuery(queryString);

        Set<Long> allEntries = runHibernateQuery("SELECT entry.id FROM Entry entry");
        Set<Long> hasXEntries = runHibernateQuery(queryString);

        allEntries.removeAll(hasXEntries);
        return allEntries;
    }

    protected static Set<Long> getIntermediateResultsUnion(List<QueryFilterParams> filterParams)
            throws ManagerException {
        Set<Long> intermediate = null;
        for (QueryFilterParams params : filterParams) {

            String queryString = "SELECT DISTINCT " + params.getSelection() + " FROM "
                    + params.getFrom();
            if (params.getCriterion() != null && !params.getCriterion().isEmpty())
                queryString += " WHERE " + params.getCriterion();

            if (intermediate == null) {
                intermediate = runHibernateQuery(queryString);
            } else {

                intermediate.addAll(runHibernateQuery(queryString));
            }
        }
        return intermediate;
    }
}
