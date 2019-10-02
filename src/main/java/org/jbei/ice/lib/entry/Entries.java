package org.jbei.ice.lib.entry;

import com.opencsv.CSVReader;
import org.apache.commons.lang3.StringUtils;
import org.jbei.ice.lib.access.PermissionsController;
import org.jbei.ice.lib.account.AccountType;
import org.jbei.ice.lib.dto.access.AccessPermission;
import org.jbei.ice.lib.dto.entry.*;
import org.jbei.ice.lib.dto.folder.FolderAuthorization;
import org.jbei.ice.lib.dto.folder.FolderType;
import org.jbei.ice.lib.dto.search.SearchQuery;
import org.jbei.ice.lib.dto.search.SearchResult;
import org.jbei.ice.lib.dto.search.SearchResults;
import org.jbei.ice.lib.group.GroupController;
import org.jbei.ice.lib.search.SearchController;
import org.jbei.ice.storage.DAOFactory;
import org.jbei.ice.storage.InfoToModelFactory;
import org.jbei.ice.storage.hibernate.dao.AccountDAO;
import org.jbei.ice.storage.hibernate.dao.EntryDAO;
import org.jbei.ice.storage.model.Account;
import org.jbei.ice.storage.model.Entry;
import org.jbei.ice.storage.model.Folder;
import org.jbei.ice.storage.model.Group;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Hector Plahar
 */
public class Entries extends HasEntry {

    private final EntryDAO dao;
    private final AccountDAO accountDAO;
    private final String userId;
    private final EntryAuthorization authorization;

    /**
     * @param userId unique identifier for user creating permissions. Must have write privileges on the entry
     *               if one exists
     */
    public Entries(String userId) {
        this.dao = DAOFactory.getEntryDAO();
        this.accountDAO = DAOFactory.getAccountDAO();
        this.authorization = new EntryAuthorization();
        this.userId = userId;
    }

    /**
     * Update the information associated with the specified part.<br>
     * <b>Note</b> that any missing information will be deleted from the original entry.
     * In other words, if the part referenced by id <code>partId</code> has an alias value
     * of <code>alias</code> and the part object passed in the parameter does not contain this value,
     * when this method returns, the original entry's alias field will be removed.
     *
     * @param partId   unique identifier for part being updated. This overrides the id in the partData object
     * @param partData information to update part with
     * @return unique identifier for part that was updated
     * @throws IllegalArgumentException if the entry associated with the partId cannot be located
     */
    public long update(long partId, PartData partData) {
        Entry existing = dao.get(partId);
        if (existing == null)
            throw new IllegalArgumentException();

        authorization.expectWrite(userId, existing);

        Entry entry = InfoToModelFactory.updateEntryField(partData, existing);
        entry.setModificationTime(Calendar.getInstance().getTime());
        if (entry.getVisibility() == Visibility.DRAFT.getValue()) {
            List<EntryField> invalidFields = EntryUtil.validates(partData);
            if (invalidFields.isEmpty())
                entry.setVisibility(Visibility.OK.getValue());
        }
        entry = dao.update(entry);

        EntryHistory history = new EntryHistory(userId, partId);
        history.addEdit();

        // check pi email
        String piEmail = entry.getPrincipalInvestigatorEmail();
        if (StringUtils.isNotEmpty(piEmail)) {
            Account pi = DAOFactory.getAccountDAO().getByEmail(piEmail);
            if (pi != null) {
                // add write permission for the PI (method also checks to see if permission already exists)
                AccessPermission accessPermission = new AccessPermission();
                accessPermission.setArticle(AccessPermission.Article.ACCOUNT);
                accessPermission.setArticleId(pi.getId());
                accessPermission.setType(AccessPermission.Type.WRITE_ENTRY);
                accessPermission.setTypeId(entry.getId());
                new PermissionsController().addPermission(userId, accessPermission);
            }
        }

        return entry.getId();
    }

    public List<Long> updateVisibility(List<Long> entryIds, Visibility visibility) {
        List<Long> updated = new ArrayList<>();
        for (long entryId : entryIds) {
            Entry entry = dao.get(entryId);
            if (entry.getVisibility() == visibility.getValue())
                continue;

            if (!authorization.canWrite(userId, entry))
                continue;

            entry.setVisibility(visibility.getValue());
            dao.update(entry);
            updated.add(entryId);
        }
        return updated;
    }

    public List<Long> getEntriesFromSelectionContext(EntrySelection context) {
        boolean all = context.isAll();
        EntryType entryType = context.getEntryType();

        if (context.getSelectionType() == null)
            return context.getEntries();

        switch (context.getSelectionType()) {
            default:
            case FOLDER:
                if (!context.getEntries().isEmpty()) {
                    return context.getEntries();
                } else {
                    long folderId = Long.decode(context.getFolderId());
                    return getFolderEntries(folderId, all, entryType);
                }

            case SEARCH:
                return getSearchResults(context.getSearchQuery());

            case COLLECTION:
                if (!context.getEntries().isEmpty()) {
                    return context.getEntries();
                } else {
                    return getCollectionEntries(context.getFolderId(), all, entryType);
                }
        }
    }

    /**
     * Validate list of entries in a csv file either via names or partnumbers
     *
     * @param stream    csv file input stream
     * @param checkName whether to check names or part numbers
     */
    public List<ParsedEntryId> validateEntries(InputStream stream, boolean checkName) throws IOException {
        List<ParsedEntryId> accepted = new ArrayList<>();
        EntryAuthorization authorization = new EntryAuthorization();

        try (CSVReader reader = new CSVReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            List<String[]> results = reader.readAll();

            for (String[] result : results) {
                if (result[0].isEmpty())
                    continue;

                List<Entry> entries;
                if (checkName) {
                    entries = dao.getByName(result[0].trim());
                } else {
                    Entry entry = dao.getByPartNumber(result[0].trim());
                    entries = new ArrayList<>(1);
                    if (entry != null)
                        entries.add(entry);
                }

                if (entries.isEmpty())
                    accepted.add(new ParsedEntryId(result[0], null));
                else {
                    for (Entry e : entries) {
                        if (!authorization.canRead(this.userId, e)) {
                            accepted.add(new ParsedEntryId(result[0], null));
                            continue;
                        }
                        PartData partData = new PartData(EntryType.nameToType(e.getRecordType()));
                        partData.setPartId(e.getPartNumber());
                        partData.setId(e.getId());
                        accepted.add(new ParsedEntryId(result[0], partData));
                    }
                }
            }
        }
        return accepted;
    }

    private List<Long> getCollectionEntries(String collection, boolean all, EntryType type) {
        if (collection == null || collection.isEmpty())
            return null;

        Account account = accountDAO.getByEmail(userId);
        List<Long> entries;

        switch (collection.toLowerCase()) {
            case "personal":
                if (all)
                    type = null;
                entries = dao.getOwnerEntryIds(userId, type);
                break;
            case "shared":
                entries = dao.sharedWithUserEntryIds(account, account.getGroups());
                break;
            case "available":
            case "featured":
                Group publicGroup = new GroupController().createOrRetrievePublicGroup();
                entries = dao.getVisibleEntryIds(account.getType() == AccountType.ADMIN, publicGroup);
                break;
            default:
                return null;
        }

        return entries;
    }

    // todo : folder controller
    private List<Long> getFolderEntries(long folderId, boolean all, EntryType type) {
        Folder folder = DAOFactory.getFolderDAO().get(folderId);
        FolderAuthorization folderAuthorization = new FolderAuthorization();
        folderAuthorization.expectRead(userId, folder);

        if (all)
            type = null;

        boolean visibleOnly = folder.getType() != FolderType.TRANSFERRED;
        return DAOFactory.getFolderDAO().getFolderContentIds(folderId, type, visibleOnly);
    }

    private List<Long> getSearchResults(SearchQuery searchQuery) {
        SearchController searchController = new SearchController();
        SearchResults searchResults = searchController.runSearch(userId, searchQuery);
        // todo : inefficient: have search return ids only
        List<Long> results = new LinkedList<>();
        for (SearchResult result : searchResults.getResults()) {
            results.add(result.getEntryInfo().getId());
        }
        return results;
    }
}
