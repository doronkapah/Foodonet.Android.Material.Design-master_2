package FooDoNetSQLClasses;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import CommonUtilPackage.CommonUtil;
import DataModel.FCPublication;
import DataModel.Group;
import DataModel.GroupMember;
import DataModel.PublicationReport;
import DataModel.RegisteredUserForPublication;
import CommonUtilPackage.InternalRequest;
import upp.foodonet.material.FooDoNetSQLProvider;
import upp.foodonet.material.R;

/**
 * Created by Asher on 24-Jul-15.
 */
public class FooDoNetSQLExecuterAsync extends AsyncTask<InternalRequest, Void, Void> {

    private static final String MY_TAG = "food_sqlAsyncTask";

    ArrayList<FCPublication> publicationsFromServer;
    ArrayList<FCPublication> publicationsFromDB;
    ArrayList<FCPublication> resultPublications;
    ArrayList<Group> resultGroups;
    ArrayList<FCPublication> publicationsForList;
    ArrayList<RegisteredUserForPublication> regUsersFromServer;
    ArrayList<RegisteredUserForPublication> regUsersFromDB;
    ArrayList<RegisteredUserForPublication> resultRegUsers;
    FCPublication newPublicationForSaving;
    FCPublication publicationAfterUpdate;
    FCPublication publicationDetailsByID;
    IFooDoNetSQLCallback callbackHandler;
    ContentResolver contentResolver;
    InternalRequest incomingRequest;
    int newNegativeID;
    ArrayList<Group> groupsFromDB;
    //    Map<Integer, Integer> needToLoadPicturesFor;
    Context context;

    public FooDoNetSQLExecuterAsync(IFooDoNetSQLCallback callback, Context context) {
        //ArrayList<FCPublication> fromServer removed from parameters
        //publicationsFromServer = fromServer;
        callbackHandler = callback;
        this.context = context;
        contentResolver = context.getContentResolver();
    }

    @Override
    protected Void doInBackground(InternalRequest... params) {
        if (params.length == 0) return null;
        incomingRequest = params[0];
        switch (incomingRequest.ActionCommand) {
            //region Update db from server
            case InternalRequest.ACTION_SQL_UPDATE_DB_PUBLICATIONS_FROM_SERVER:
                publicationsFromServer = params[0].publications;
                regUsersFromServer = params[0].registeredUsers;
//                needToLoadPicturesFor = new HashMap<>();
                if (contentResolver == null) {
                    Log.e(MY_TAG, "error: contentresolver null");
                    return null;
                }
                if (callbackHandler == null) {
                    Log.e(MY_TAG, "error: callbackHandler null");
                    return null;
                }
                if (publicationsFromServer != null && publicationsFromServer.size() != 0) {
                    Cursor cursor = contentResolver.query(FooDoNetSQLProvider.CONTENT_URI,
                            FCPublication.GetColumnNamesArray(), null, null, null);
                    publicationsFromDB = FCPublication.GetArrayListOfPublicationsFromCursor(cursor, false);
                    cursor.close();
                    cursor = null;
                    cursor = contentResolver.query(FooDoNetSQLProvider.URI_GET_ALL_REGS,
                            RegisteredUserForPublication.GetColumnNamesArray(), null, null, null);
                    ArrayList<RegisteredUserForPublication> regs
                            = RegisteredUserForPublication.GetArrayListOfRegisteredForPublicationsFromCursor(cursor);
                    cursor.close();
                    resultPublications = new ArrayList<FCPublication>();
                    for (FCPublication publicationFromServer : publicationsFromServer) {
                        resultPublications.add(publicationFromServer);
                        FCPublication pubFromDB
                                = FCPublication.GetPublicationFromArrayListByID(publicationsFromDB, publicationFromServer.getUniqueId());
                        if (pubFromDB == null) {
                            //todo: logics of pub.setIfTriedToGetPictureBefore can be improved
                            //todo: by updating this field only after call to amazon
//                        publicationFromServer.setIfTriedToGetPictureBefore(true);
                            DeletePublicationFromDB(contentResolver, publicationFromServer);
                            InsertPublicationToDB(contentResolver, publicationFromServer);
//                        needToLoadPicturesFor.put(publicationFromServer.getUniqueId(), publicationFromServer.getVersion());
                        } else {
                            if (pubFromDB.getVersion() < publicationFromServer.getVersion()) {
                                DeletePublicationFromDB(contentResolver, pubFromDB);
//                            publicationFromServer.setIfTriedToGetPictureBefore(true);
                                InsertPublicationToDB(contentResolver, publicationFromServer);
//                            needToLoadPicturesFor.put(publicationFromServer.getUniqueId(), publicationFromServer.getVersion());
                            } else {
                                UpdateRegsAndReports(contentResolver, publicationFromServer);
                                //if(pubFromDB.getImageByteArray() == null || pubFromDB.getImageByteArray().length == 0)
                            /*File f = new File(Environment.getExternalStorageDirectory(),
                                    pubFromDB.getUniqueId() + "." + pubFromDB.getVersion() + ".jpg");
                            if(!f.exists())
                                needToLoadPicturesFor.put(pubFromDB.getUniqueId(), pubFromDB.getVersion());*/
//                            if(!pubFromDB.getIfTriedToGetPictureBefore()){
//                                needToLoadPicturesFor.put(pubFromDB.getUniqueId(), pubFromDB.getVersion());
//                                DeletePublicationFromDB(contentResolver, pubFromDB);
//                                pubFromDB.setIfTriedToGetPictureBefore(true);
//                                InsertPublicationToDB(contentResolver, pubFromDB);
//                            }
                            }
                            publicationsFromDB.remove(pubFromDB);
                        }
                    }
                    ArrayList<Integer> toRemoveFromDB = new ArrayList<Integer>();
                    if (context != null) {
                        String androidID = CommonUtil.GetIMEI(context);
                        for (FCPublication publicationFromDB : publicationsFromDB) {
                            if (publicationFromDB.getPublisherUID().compareTo(androidID) != 0) {
                                toRemoveFromDB.add(publicationFromDB.getUniqueId());
                                DeletePublicationFromDB(contentResolver, publicationFromDB);
                            }
                        }
                    }
                    for (Integer i : toRemoveFromDB) {
                        FCPublication.DeletePublicationFromCollectionByID(publicationsFromDB, i);
                    }
                }
                //region groups
                contentResolver.delete(FooDoNetSQLProvider.URI_GROUP, null, null);
                contentResolver.delete(FooDoNetSQLProvider.URI_GROUP_MEMBERS, null, null);

                if (incomingRequest.groups != null && incomingRequest.groups.size() > 0) {
                    resultGroups = new ArrayList<>();

                    for (Group groupFromServer : incomingRequest.groups) {
                        contentResolver.insert(FooDoNetSQLProvider.URI_GROUP, groupFromServer.GetContentValuesRow());
                        for (GroupMember gm : groupFromServer.get_group_members()) {
                            if (gm.get_user_id() != CommonUtil.GetMyUserID(context))
                                contentResolver.insert(FooDoNetSQLProvider.URI_GROUP_MEMBERS, gm.GetContentValuesRow());
                        }
                        resultGroups.add(groupFromServer);
                    }
                }
/*
                Map<Integer, Group> groupsFromServer = new HashMap<>();
                for(Group group : incomingRequest.groups)
                    groupsFromServer.put(group.Get_id(), group);

                ArrayList<Group> myGroupsFromSQL
                        = Group.GetGroupsFromCursor(contentResolver
                            .query(FooDoNetSQLProvider.URI_GROUP, Group.GetColumnNamesArray(),
                            null, null, null));
                for(Group gSql : myGroupsFromSQL){
                    ArrayList<GroupMember> groupMembersFromSQL
                            = GroupMember.GetGroupMembersFromCursor(
                                contentResolver.query(FooDoNetSQLProvider.URI_GROUP_MEMBERS,
                                        GroupMember.GetColumnNamesArray(),
                                        GroupMember.GROUP_MEMBER_GROUP_ID_KEY + "=" + String.valueOf(gSql.Get_id()), null, null));
                    gSql.set_group_members(groupMembersFromSQL);
                }

                Map<Integer, Group> groupsFromSql = new HashMap<>();
                for (Group group : myGroupsFromSQL)
                    groupsFromSql.put(group.Get_id(), group);

                ArrayList<Group> groupsToAdd = new ArrayList<>();
                //ArrayList<Group> groupsToDelete = new ArrayList<>();
                ArrayList<String> groupsToDeleteIDS = new ArrayList<>();
                ArrayList<Group> groupsToCheck = new ArrayList<>();

                for(int keyFromServer : groupsFromServer.keySet())
                    if(!groupsFromSql.containsKey(keyFromServer))
                        groupsToAdd.add(groupsFromServer.get(keyFromServer));


                for(int keyFromSQL : groupsFromSql.keySet()){
                    if(!groupsFromServer.containsKey(keyFromSQL))
                        groupsToDeleteIDS.add(String.valueOf(keyFromSQL));
                    else groupsToCheck.add(groupsFromSql.get(keyFromSQL));
                }

                for (Group gToCheck : groupsToCheck){
                    Group gFromServer = groupsFromServer.get(gToCheck.Get_id());
                    if(gFromServer.Get_name().compareToIgnoreCase(gToCheck.Get_name()) != 0)
                        contentResolver.update(FooDoNetSQLProvider.URI_GROUP,
                                gFromServer.GetContentValuesRow(),
                                Group.GROUP_ID_KEY + "=" + String.valueOf(gToCheck.Get_id()), null);
                }

                String[] groupsToDeleteIDSArray = new String[groupsToDeleteIDS.size()];
                groupsToDeleteIDSArray = groupsToDeleteIDS.toArray(groupsToDeleteIDSArray);

                contentResolver.delete(FooDoNetSQLProvider.URI_GROUP_MEMBERS, GroupMember.GROUP_MEMBER_GROUP_ID_KEY + "=?", groupsToDeleteIDSArray);
                contentResolver.delete(FooDoNetSQLProvider.URI_GROUP, Group.GROUP_ID_KEY + "=?", groupsToDeleteIDSArray);

                for(Group g : groupsToAdd){
                    contentResolver.insert(FooDoNetSQLProvider.URI_GROUP, g.GetContentValuesRow());
                    for(GroupMember gm : g.get_group_members())
                        contentResolver.insert(FooDoNetSQLProvider.URI_GROUP_MEMBERS, gm.GetContentValuesRow());
                }





                ArrayList<GroupMember> myGroupMembersFromSQL
                        = GroupMember.GetGroupMembersFromCursor(contentResolver
                            .query(FooDoNetSQLProvider.URI_GROUP_MEMBERS, GroupMember.GetColumnNamesArray(),
                                    null, null, null));
*/

                return null;
            //endregion
            //region get publications for list
            case InternalRequest.ACTION_SQL_GET_ALL_PUBS_FOR_LIST_BY_ID_DESC:
                Cursor publicationsForListCursor
                        = contentResolver.query(FooDoNetSQLProvider.URI_GET_ALL_PUBS_FOR_LIST_ID_DESC,
                        FCPublication.GetColumnNamesForListArray(), null, null, null);
                publicationsForList = FCPublication.GetArrayListOfPublicationsFromCursor(publicationsForListCursor, true);
                publicationsForListCursor.close();
                break;
            //endregion
            //region Save new/edited publication
            case InternalRequest.ACTION_SQL_SAVE_NEW_PUBLICATION:
                if (params[0].publicationForSaving == null) {
                    Log.e(MY_TAG, "got null publication for saving");
                    return null;
                }
                newPublicationForSaving = params[0].publicationForSaving;
                if (params[0].publicationForSaving.getUniqueId() == 0) {
                    Cursor newNegativeIDCursor = contentResolver.query(FooDoNetSQLProvider.URI_GET_NEW_NEGATIVE_ID,
                            new String[]{FCPublication.PUBLICATION_NEW_NEGATIVE_ID}, null, null, null);
                    if (newNegativeIDCursor != null && newNegativeIDCursor.moveToFirst()) {
                        newNegativeID
                                = newNegativeIDCursor.getInt(
                                newNegativeIDCursor.getColumnIndex(FCPublication.PUBLICATION_NEW_NEGATIVE_ID));
                        if (newNegativeID > 0)
                            newNegativeID = 0;
                        newPublicationForSaving.setUniqueId(newNegativeID);
                    } else {
                        Log.e(MY_TAG, "cant get data from cursor, moveToFirst() == false");
                    }
                    newNegativeIDCursor.close();
                }
                Uri newRowURLForLog
                        = contentResolver.insert(FooDoNetSQLProvider.CONTENT_URI, newPublicationForSaving.GetContentValuesRow());
                newPublicationForSaving.setUniqueId(Integer.parseInt(newRowURLForLog.getLastPathSegment()));
                Log.i(MY_TAG, "insert succeeded! id: " + newPublicationForSaving.getUniqueId()
                        + "; new row url: " + newRowURLForLog);
                break;
            case InternalRequest.ACTION_SQL_UPDATE_ID_OF_PUB_AFTER_SAVING_ON_SERVER:
                Uri getUpdateUri = params[0].publicationForSaving.getUniqueId() < 0
                        ? FooDoNetSQLProvider.URI_PUBLICATION_ID_NEGATIVE
                        : FooDoNetSQLProvider.CONTENT_URI;
                int tmpIdToDelete = params[0].publicationForSaving.getUniqueId();
                long pubIdToUpdate = params[0].publicationForSaving.getUniqueId() < 0
                        ? params[0].publicationForSaving.getUniqueId() * -1
                        : params[0].publicationForSaving.getUniqueId();
                FCPublication publication = params[0].publicationForSaving;
                publication.setUniqueId(publication.getNewIdFromServer());
                publication.setVersion(publication.getVersionFromServer());
                int rowsUpdated = contentResolver.update(Uri.parse(getUpdateUri + "/" + pubIdToUpdate), publication.GetContentValuesRow(), null, null);
                if (rowsUpdated > 0)
                    Log.i(MY_TAG, "successfully updated new id and version");
                rowsUpdated = 0;
                publicationAfterUpdate = publication;
                break;
            case InternalRequest.ACTION_SQL_SAVE_EDITED_PUBLICATION:
                Uri updateUri = params[0].publicationForSaving.getUniqueId() < 0
                        ? FooDoNetSQLProvider.URI_PUBLICATION_ID_NEGATIVE
                        : FooDoNetSQLProvider.CONTENT_URI;
                int tmpIdToUpdate = params[0].publicationForSaving.getUniqueId();
                long pub_id_toUpdate = params[0].publicationForSaving.getUniqueId() < 0
                        ? params[0].publicationForSaving.getUniqueId() * -1
                        : params[0].publicationForSaving.getUniqueId();
                FCPublication pubToUpdate = params[0].publicationForSaving;
                int rows = contentResolver.update(Uri.parse(updateUri + "/" + pub_id_toUpdate),
                        pubToUpdate.GetContentValuesRow(), null, null);
                if (rows > 0)
                    Log.i(MY_TAG, "successfully updated publication in db after editing");
                break;
            //endregion
            //region get single publication
            case InternalRequest.ACTION_SQL_GET_SINGLE_PUBLICATION_BY_ID:
                Uri getPubUri = params[0].PublicationID < 0
                        ? FooDoNetSQLProvider.URI_PUBLICATION_ID_NEGATIVE
                        : FooDoNetSQLProvider.CONTENT_URI;
                long pubID = params[0].PublicationID < 0 ? params[0].PublicationID * -1 : params[0].PublicationID;
                Cursor cPub = contentResolver.query(Uri.parse(getPubUri + "/" + pubID),
                        FCPublication.GetColumnNamesArray(), null, null, null);
                ArrayList<FCPublication> pubs = FCPublication.GetArrayListOfPublicationsFromCursor(cPub, false);
                cPub.close();
                if (pubs == null || pubs.size() == 0) {
                    Log.e(MY_TAG, "can't get publication from sql by id: " + params[0].PublicationID);
                    return null;
                }
                FCPublication resultPublication = pubs.get(0);

                Uri getRegUri = params[0].PublicationID < 0
                        ? FooDoNetSQLProvider.URI_GET_REGISTERED_BY_PUBLICATION_NEG_ID
                        : FooDoNetSQLProvider.URI_GET_REGISTERED_BY_PUBLICATION_ID;
                Cursor cRegs = contentResolver.query(Uri.parse(getRegUri + "/" + pubID),
                        RegisteredUserForPublication.GetColumnNamesArray(), null, null, null);
                ArrayList<RegisteredUserForPublication> regsById
                        = RegisteredUserForPublication.GetArrayListOfRegisteredForPublicationsFromCursor(cRegs);
                cRegs.close();
                if (regsById != null && regsById.size() > 0)
                    resultPublication.setRegisteredForThisPublication(regsById);

                Uri getRepUri = params[0].PublicationID < 0
                        ? FooDoNetSQLProvider.URI_GET_ALL_REPORTS_BY_PUB_NEG_ID
                        : FooDoNetSQLProvider.URI_GET_ALL_REPORTS_BY_PUB_ID;
                Cursor cReports = contentResolver.query(Uri.parse(getRepUri + "/" + pubID),
                        PublicationReport.GetColumnNamesArray(), null, null, null);
                ArrayList<PublicationReport> reports
                        = PublicationReport.GetArrayListOfPublicationReportsFromCursor(cReports);
                cReports.close();
                if (reports != null && reports.size() > 0)
                    resultPublication.setPublicationReports(reports);

                if(resultPublication.getAudience() != 0){
                    Uri groupUri = Uri.parse(FooDoNetSQLProvider.URI_GROUP + "/" + resultPublication.getAudience());
                    Cursor groupCursor = contentResolver.query(groupUri, Group.GetColumnNamesArray(), null, null, null);
                    ArrayList<Group> groupArray = Group.GetGroupsFromCursor(groupCursor);
                    Group group = groupArray.get(0);
                    resultPublication.set_group_name(group.Get_name());
                    groupCursor.close();
                }

                publicationDetailsByID = resultPublication;
                break;
            //endregion
            //region register/unregister to publication
            case InternalRequest.ACTION_SQL_ADD_MYSELF_TO_REGISTERED_TO_PUB:
                if (params[0].myRegisterToPublication == null) {
                    Log.e(MY_TAG, "no data in myRegisterToPublication");
                    break;
                }
                RegisteredUserForPublication myRegistrationToPublication = params[0].myRegisterToPublication;
                int newNegativeID = 0;
                Cursor negIdCursor = contentResolver
                        .query(FooDoNetSQLProvider.URI_GET_REG_FOR_PUB_NEW_NEG_ID,
                                new String[]{RegisteredUserForPublication.REGISTERED_FOR_PUBLICATION_KEY_NEW_NEGATIVE_ID},
                                null, null, null);
                if (negIdCursor != null && negIdCursor.moveToFirst()) {
                    newNegativeID = negIdCursor.getInt(
                            negIdCursor.getColumnIndex(RegisteredUserForPublication
                                    .REGISTERED_FOR_PUBLICATION_KEY_NEW_NEGATIVE_ID));
                    newNegativeID = newNegativeID >= 0 ? -1 : newNegativeID;
                    myRegistrationToPublication.setId(newNegativeID);
                } else {
                    Log.e(MY_TAG, "got no cursor for new pub reg new neg id");
                    return null;
                }
                contentResolver.insert(
                        FooDoNetSQLProvider.URI_INSERT_REGISTERED_FOR_PUBLICATION,
                        myRegistrationToPublication.GetContentValuesRow());
                break;
            case InternalRequest.ACTION_SQL_REMOVE_MYSELF_FROM_REGISTERED_TO_PUB:
                if (params[0].myRegisterToPublication == null) {
                    Log.e(MY_TAG, "no data in myRegisterToPublication");
                    break;
                }
                RegisteredUserForPublication myRegistrationToCancel = params[0].myRegisterToPublication;
                String removeWhereString =
                        " " + RegisteredUserForPublication.REGISTERED_FOR_PUBLICATION_KEY_PUBLICATION_ID + " = "
                                + String.valueOf(myRegistrationToCancel.getPublication_id()) + " AND "
                                + RegisteredUserForPublication.REGISTERED_FOR_PUBLICATION_KEY_USER_ID + " = '"
                                + myRegistrationToCancel.getUserID() + "'";
                int rowsDeleted = contentResolver.delete(FooDoNetSQLProvider.URI_REMOVE_MYSELF_FROM_REGS_FOR_PUBLICATION,
                        removeWhereString, null);
                Log.i(MY_TAG, "removed " + rowsDeleted + " rows - unregister");
                break;
            //endregion
            case InternalRequest.ACTION_SQL_UPDATE_IMAGES_FOR_PUBLICATIONS:
                Map<Integer, byte[]> imgToPub = params[0].publicationImageMap;
                ContentValues cv = new ContentValues();
                for (int pubId : imgToPub.keySet()) {
                    cv.put(String.valueOf(pubId), imgToPub.get(pubId));
                }
                int rowsAffected = contentResolver.update(FooDoNetSQLProvider.URI_UPDATE_IMAGES, cv, null, null);
                break;
            case InternalRequest.ACTION_DELETE_PUBLICATION:
                FCPublication publicationToDelete = params[0].publicationForSaving;
                DeletePublicationFromDB(contentResolver, publicationToDelete);
                break;
            case InternalRequest.ACTION_PUSH_NEW_PUB:
                Cursor cPubSame = contentResolver.query(
                        Uri.parse(FooDoNetSQLProvider.CONTENT_URI + "/" + String.valueOf(params[0].PublicationID)),
                        FCPublication.GetColumnNamesArray(), null, null, null);
                if (cPubSame.moveToFirst()) {
                    FCPublication pubWithSameID
                            = FCPublication.GetArrayListOfPublicationsFromCursor(cPubSame, false).get(0);
                    if (pubWithSameID.getVersion() == params[0].publicationForSaving.getVersion())
                        return null;
                }
                FCPublication newPubToSave = params[0].publicationForSaving;
                InsertPublicationToDB(contentResolver, newPubToSave);
                break;
            case InternalRequest.ACTION_PUSH_PUB_DELETED:
                Cursor c = contentResolver.query(
                        Uri.parse(FooDoNetSQLProvider.CONTENT_URI + "/" + String.valueOf(params[0].PublicationID)),
                        FCPublication.GetColumnNamesArray(), null, null, null);
                if (c.moveToFirst())
                    publicationsFromDB = FCPublication.GetArrayListOfPublicationsFromCursor(c, false);
                else {
                    Log.e(MY_TAG, "publication to delete not found in db, nothing to delete");
                    return null;
                }
                int rowsDeletedFromPush = contentResolver.delete(
                        Uri.parse(FooDoNetSQLProvider.URI_REMOVE_PUBLICATION_COMPLETELY
                                + "/" + params[0].PublicationID), null, null);
                break;
            case InternalRequest.ACTION_PUSH_REPORT_FOR_PUB:
                Cursor c1 = contentResolver.query(
                        Uri.parse(FooDoNetSQLProvider.CONTENT_URI + "/" + String.valueOf(params[0].PublicationID)),
                        FCPublication.GetColumnNamesArray(), null, null, null);
                if (c1.moveToFirst())
                    publicationsFromDB = FCPublication.GetArrayListOfPublicationsFromCursor(c1, false);
                else {
                    Log.e(MY_TAG, "publication to add report not found in db, nothing to delete");
                    return null;
                }
                contentResolver.insert(FooDoNetSQLProvider.URI_GET_ALL_REPORTS, params[0].publicationReport.GetContentValuesRow());
                break;
            case InternalRequest.ACTION_PUSH_REG:
                UpdateRegs(contentResolver, incomingRequest.PublicationID, incomingRequest.registeredUsers);
                break;
            case InternalRequest.ACTION_GET_GROUPS_FROM_SQL:
                Cursor cursorGroups = contentResolver.query(FooDoNetSQLProvider.URI_GROUP, Group.GetColumnNamesArray(), null, null, null);
                groupsFromDB = Group.GetGroupsFromCursor(cursorGroups);
                break;
        }
        return null;
    }

    private void InsertPublicationToDB(ContentResolver contentResolver, FCPublication publication) {
        contentResolver.insert(FooDoNetSQLProvider.CONTENT_URI, publication.GetContentValuesRow());
        for (RegisteredUserForPublication reg : publication.getRegisteredForThisPublication())
            contentResolver.insert(FooDoNetSQLProvider.URI_INSERT_REGISTERED_FOR_PUBLICATION, reg.GetContentValuesRow());
        for (PublicationReport pr : publication.getPublicationReports()) {
            contentResolver.insert(FooDoNetSQLProvider.URI_GET_ALL_REPORTS, pr.GetContentValuesRow());
        }
    }

    private void DeletePublicationFromDB(ContentResolver contentResolver, FCPublication publication) {
        for (RegisteredUserForPublication reg : publication.getRegisteredForThisPublication())
            contentResolver.delete(
                    Uri.parse(FooDoNetSQLProvider.URI_DELETE_REGISTERED_FOR_PUBLICATION + "/" + reg.getId()), null, null);
        for (PublicationReport pr : publication.getPublicationReports())
            contentResolver.delete(
                    Uri.parse(FooDoNetSQLProvider.URI_GET_ALL_REPORTS + "/" + pr.getId()), null, null);
        Uri deleteUri = publication.getUniqueId() < 0 ? FooDoNetSQLProvider.URI_PUBLICATION_ID_NEGATIVE : FooDoNetSQLProvider.CONTENT_URI;
        int idToDelete = publication.getUniqueId() < 0 ? publication.getUniqueId() * -1 : publication.getUniqueId();
        contentResolver.delete(Uri.parse(deleteUri + "/" + idToDelete), null, null);
        File pubPic = new File(Environment.getExternalStorageDirectory() + context.getString(R.string.image_folder_path),
                context.getString(R.string.publication_picture_file_name_format)
                        .replace("{0}", String.valueOf(publication.getUniqueId()))
                        .replace("{1}", String.valueOf(publication.getVersion())));
        if(pubPic.exists())
            pubPic.delete();
    }

    private void UpdateRegsAndReports(ContentResolver contentResolver, FCPublication publication) {
//        for (RegisteredUserForPublication reg : publication.getRegisteredForThisPublication())
//            contentResolver.delete(
//                    Uri.parse(FooDoNetSQLProvider.URI_DELETE_REGISTERED_FOR_PUBLICATION + "/" + reg.getId()), null, null);
        contentResolver.delete(Uri.parse(FooDoNetSQLProvider.URI_DELETE_REGISTERED_USER_BY_PUBLICATION_ID + "/" + publication.getUniqueId()), null, null);
//        for (PublicationReport pr : publication.getPublicationReports())
//            contentResolver.delete(
//                    Uri.parse(FooDoNetSQLProvider.URI_GET_ALL_REPORTS + "/" + pr.getId()), null, null);
        contentResolver.delete(Uri.parse(FooDoNetSQLProvider.URI_GET_ALL_REPORTS_BY_PUB_ID + "/" + publication.getUniqueId()), null, null);
        for (RegisteredUserForPublication reg : publication.getRegisteredForThisPublication())
            contentResolver.insert(FooDoNetSQLProvider.URI_INSERT_REGISTERED_FOR_PUBLICATION, reg.GetContentValuesRow());
        for (PublicationReport pr : publication.getPublicationReports()) {
            contentResolver.insert(FooDoNetSQLProvider.URI_GET_ALL_REPORTS, pr.GetContentValuesRow());
        }
    }

    private void UpdateRegs(ContentResolver contentResolver, long publicationId, ArrayList<RegisteredUserForPublication> registeredUsers) {
        contentResolver.delete(Uri.parse(FooDoNetSQLProvider.URI_DELETE_REGISTERED_USER_BY_PUBLICATION_ID + "/" + publicationId), null, null);
        for (RegisteredUserForPublication reg : registeredUsers)
            contentResolver.insert(FooDoNetSQLProvider.URI_INSERT_REGISTERED_FOR_PUBLICATION, reg.GetContentValuesRow());
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        InternalRequest ir;
        switch (incomingRequest.ActionCommand) {
            case InternalRequest.ACTION_SQL_UPDATE_DB_PUBLICATIONS_FROM_SERVER:
                InternalRequest respond = new InternalRequest(incomingRequest.ActionCommand, resultPublications);
                respond.groups = resultGroups;
//                respond.listOfPubsToFetchImageFor = needToLoadPicturesFor;
                callbackHandler.OnSQLTaskComplete(respond);
                break;
            case InternalRequest.ACTION_SQL_GET_ALL_PUBS_FOR_LIST_BY_ID_DESC:
                callbackHandler.OnSQLTaskComplete(new InternalRequest(incomingRequest.ActionCommand, resultPublications));
                break;
            case InternalRequest.ACTION_SQL_SAVE_NEW_PUBLICATION:
                ir = new InternalRequest(incomingRequest.ActionCommand, newPublicationForSaving);
                ir.Status = InternalRequest.STATUS_OK;
                callbackHandler.OnSQLTaskComplete(ir);
                break;
            case InternalRequest.ACTION_SQL_GET_SINGLE_PUBLICATION_BY_ID:
                ir = new InternalRequest(incomingRequest.ActionCommand);
                ir.publicationForDetails = publicationDetailsByID;
                callbackHandler.OnSQLTaskComplete(ir);
                break;
            case InternalRequest.ACTION_SQL_UPDATE_ID_OF_PUB_AFTER_SAVING_ON_SERVER:
                InternalRequest response = new InternalRequest(incomingRequest.ActionCommand);
                response.Status = InternalRequest.STATUS_OK;
                response.publicationForSaving = publicationAfterUpdate;
                callbackHandler.OnSQLTaskComplete(response);
                break;
            case InternalRequest.ACTION_SQL_UPDATE_IMAGES_FOR_PUBLICATIONS:
                InternalRequest statusResponse = new InternalRequest(incomingRequest.ActionCommand);
                statusResponse.Status = InternalRequest.STATUS_OK;
                callbackHandler.OnSQLTaskComplete(statusResponse);
                break;
            case InternalRequest.ACTION_SQL_ADD_MYSELF_TO_REGISTERED_TO_PUB:
                callbackHandler.OnSQLTaskComplete(new InternalRequest(incomingRequest.ActionCommand, true));
                break;
            case InternalRequest.ACTION_SQL_REMOVE_MYSELF_FROM_REGISTERED_TO_PUB:
                callbackHandler.OnSQLTaskComplete(new InternalRequest(incomingRequest.ActionCommand, true));
                break;
            case InternalRequest.ACTION_SQL_SAVE_EDITED_PUBLICATION:
                InternalRequest res = new InternalRequest(incomingRequest.ActionCommand, true);
                res.publicationForSaving = incomingRequest.publicationForSaving;
                callbackHandler.OnSQLTaskComplete(res);
                break;
            case InternalRequest.ACTION_DELETE_PUBLICATION:
                InternalRequest irDelete = new InternalRequest(incomingRequest.ActionCommand, true);
                callbackHandler.OnSQLTaskComplete(irDelete);
                break;
            case InternalRequest.ACTION_PUSH_NEW_PUB:
                InternalRequest irNewPubFromPush = new InternalRequest(incomingRequest.ActionCommand, true);
                irNewPubFromPush.publicationForSaving = incomingRequest.publicationForSaving;
                callbackHandler.OnSQLTaskComplete(irNewPubFromPush);
                break;
            case InternalRequest.ACTION_PUSH_PUB_DELETED:
                InternalRequest irDeletedPub = new InternalRequest(incomingRequest.ActionCommand, publicationsFromDB.get(0));
                irDeletedPub.Status = InternalRequest.STATUS_OK;
                callbackHandler.OnSQLTaskComplete(irDeletedPub);
                break;
            case InternalRequest.ACTION_PUSH_REPORT_FOR_PUB:
                InternalRequest irReportFromPush = new InternalRequest(incomingRequest.ActionCommand, publicationsFromDB.get(0));
                irReportFromPush.Status = InternalRequest.STATUS_OK;
                callbackHandler.OnSQLTaskComplete(irReportFromPush);
                break;
            case InternalRequest.ACTION_PUSH_REG:
                InternalRequest irNewRegisteredUser = new InternalRequest(incomingRequest.ActionCommand, true);
                irNewRegisteredUser.publicationForSaving = incomingRequest.publicationForSaving;
                callbackHandler.OnSQLTaskComplete(irNewRegisteredUser);
                break;
            case InternalRequest.ACTION_GET_GROUPS_FROM_SQL:
                InternalRequest irGroupsFromDB = new InternalRequest(incomingRequest.ActionCommand, true);
                irGroupsFromDB.groups = groupsFromDB;
                callbackHandler.OnSQLTaskComplete(irGroupsFromDB);
                break;
/*  not needed
            case InternalRequest.ACTION_SQL_GET_NEW_NEGATIVE_ID:
                callbackHandler.OnSQLTaskComplete(new InternalRequest(incomingRequest.ActionCommand, newNegativeID));
                break;
*/
        }
    }
}
