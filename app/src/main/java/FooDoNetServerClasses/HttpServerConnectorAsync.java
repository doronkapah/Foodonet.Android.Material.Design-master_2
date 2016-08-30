package FooDoNetServerClasses;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.JsonWriter;
import android.util.Log;

//import org.apache.http.HttpEntity;
//import org.apache.http.HttpResponse;
//import org.apache.http.client.ClientProtocolException;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
//import org.apache.http.client.methods.HttpGet;
//import org.apache.http.entity.StringEntity;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URI;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import CommonUtilPackage.InternalRequest;
import DataModel.FCPublication;
import DataModel.Group;
import DataModel.GroupMember;
import DataModel.ICanWriteSelfToJSONWriter;
import DataModel.PublicationReport;
import DataModel.RegisteredUserForPublication;
import DataModel.UserRegisterData;
import FooDoNetServiceUtil.ServicesBroadcastReceiver;

/**
 * Created by Asher on 21-Jul-15.
 */
public class HttpServerConnectorAsync extends AsyncTask<InternalRequest, Void, String> {

    private static final String MY_TAG = "food_httpConnecterAsync";

    private static final String REQUEST_METHOD_POST = "POST";
    private static final String REQUEST_METHOD_GET = "GET";
    private static final String REQUEST_METHOD_DELETE = "DELETE";
    private static final String REQUEST_METHOD_PUT = "PUT";

    private String baseUrl;
    private IFooDoNetServerCallback callbackListener;
    private Context context;

    private String responseString;
    private JSONArray responseJSONArray;
    private JSONObject responseJSONObject;
    private InternalRequest internalResponse;
    private ArrayList<FCPublication> resultPublications;
    private ArrayList<Group> resultGroups;
    private FCPublication publicationForSaving;
    private RegisteredUserForPublication registrationToPublicationToPost;
    private PublicationReport publicationReport;
    private FCPublication publication;
    public ArrayList<RegisteredUserForPublication> registeredUsers;
    private int newUserID;
    private Group group;
    private int groupMemberToDeleteID;

    private int Action_Command_ID;
    private long publicationID;

    private boolean isSuccess = false;

    private int doAfterRegistrationActionID;

    ArrayList<RegisteredUserForPublication> regedUpdateByPush;

    public HttpServerConnectorAsync(String baseUrl, IFooDoNetServerCallback callbackListener) {
        this.baseUrl = baseUrl;
        this.callbackListener = callbackListener;
    }

    public HttpServerConnectorAsync(String baseUrl, Context context) {
        this.baseUrl = baseUrl;
        this.context = context;
    }

    public void setContextForBroadcasting(Context context) {
        this.context = context;
    }

    public void executeSync(InternalRequest... params) {
        switch (params[0].ActionCommand) {
            case InternalRequest.ACTION_POST_REGISTER:
                break;
            case InternalRequest.ACTION_POST_REGISTER_TO_PUBLICATION:
                Intent intent = new Intent(ServicesBroadcastReceiver.BROADCAST_REC_INTENT_FILTER);
                doInBackground(params);
                intent.putExtra(ServicesBroadcastReceiver.BROADCAST_REC_EXTRA_ACTION_KEY,
                        isSuccess ? ServicesBroadcastReceiver.ACTION_CODE_REGISTER_TO_PUBLICATION_SUCCESS
                                : ServicesBroadcastReceiver.ACTION_CODE_REGISTER_TO_PUBLICATION_FAIL);
                context.sendBroadcast(intent);
                break;
            case InternalRequest.ACTION_POST_UNREGISTER_FROM_PUBLICATION:
                break;
        }
    }

    @Override
    protected String doInBackground(InternalRequest... params) {
        responseString = "";
        if (params.length == 0 || params[0] == null)
            return "";
        Action_Command_ID = params[0].ActionCommand;
        String server_sub_path = params[0].ServerSubPath;
        switch (Action_Command_ID) {
            //region case get all pubs
            case InternalRequest.ACTION_GET_ALL_PUBLICATIONS:
                //region publications
                MakeServerRequest(REQUEST_METHOD_GET, server_sub_path, null, true);
                if (!isSuccess)
                    return "";
                isSuccess = false;
                ArrayList<FCPublication> publications = new ArrayList<>();
                try {
                    publications = FCPublication.GetArrayListOfPublicationsFromJSON(new JSONArray(responseString));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                responseString = "";
                //endregion publications
                ArrayList<RegisteredUserForPublication> regedUsers = new ArrayList<>();
                ArrayList<PublicationReport> pubReports = new ArrayList<>();
                //region registeredForPubs
                for (FCPublication pub : publications) {
                    MakeServerRequest(REQUEST_METHOD_GET,
                            params[1].ServerSubPath.replace("{0}",
                                    String.valueOf(pub.getUniqueId())), null, true);
                    if (!isSuccess)
                        return "";
                    isSuccess = false;
                    try {
                        regedUsers = RegisteredUserForPublication.
                                GetArrayListOfRegisteredForPublicationsFromJSON(
                                        new JSONArray(responseString));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        responseString = "";
                    }

/*
                    MakeServerRequest(REQUEST_METHOD_GET,
                            params[2].ServerSubPath.replace("{0}",
                                    String.valueOf(pub.getUniqueId())), null, true);
                    try {
                        pubReports = PublicationReport.
                                GetArrayListOfPublicationReportsFromJSON(
                                        new JSONArray(responseString));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
*/

                    if (regedUsers.size() > 0) {
                        pub.setRegisteredForThisPublication(regedUsers);
                    }
/*
                    if (pubReports.size() > 0) {
                        pub.setPublicationReports(pubReports);
                    }
*/
                    regedUsers.clear();
                    //pubReports.clear();
                    responseString = "";
                }
                //endregion registeredForPubs
                //region reports
                MakeServerRequest(REQUEST_METHOD_GET,
                        params[2].ServerSubPath.replace("{0}",
                                String.valueOf(0)), null, true);
                if (!isSuccess)
                    return "";
                try {
                    pubReports = PublicationReport.
                            GetArrayListOfPublicationReportsFromJSON(
                                    new JSONArray(responseString));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (pubReports.size() > 0) {
                    publications = tmpConnectReportsToPublications(publications, pubReports);
                }
                responseString = "";
                //endregion reports
                //region groups
                if(!params[3].IsSkipGroup) {
                    isSuccess = false;
                    MakeServerRequest(REQUEST_METHOD_GET,
                            params[3].ServerSubPath.replace("{0}",
                                    String.valueOf(params[3].newUserID)), null, true);//"96"
                    if (!isSuccess) {
                        Log.e(MY_TAG, "cant get groups by userID");
                    } else {
                        if (!TextUtils.isEmpty(responseString)) {
                            try {
                                JSONArray jsonArray = new JSONArray(responseString);
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    Group group = Group.ParseSingleGroupFromJSON(jsonObject);
                                    if (resultGroups == null)
                                        resultGroups = new ArrayList<>();
                                    resultGroups.add(group);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }
                //endregion groups

                if (publications.size() > 0) {
                    if (resultPublications == null)
                        resultPublications = new ArrayList<>();
                    resultPublications.addAll(publications);
                }
                return "";
            //endregion
            //region case post new publication
            case InternalRequest.ACTION_POST_NEW_PUBLICATION:
                //MakeServerRequest(REQUEST_METHOD_GET, server_sub_path, params[0].canWriteSelfToJSONWriterObject, true);
                //Get(server_sub_path, (FCPublication) params[0].canWriteSelfToJSONWriterObject);
                if (params[0].publicationForSaving == null) {
                    Log.e(MY_TAG, "got null pubForSaving!");
                    return "";
                }
                publicationForSaving = params[0].publicationForSaving;
                for (int i = 0; i < 1; i++) {
                    MakeServerRequest(REQUEST_METHOD_POST, server_sub_path, params[0].canWriteSelfToJSONWriterObject, true);
                    if (!TextUtils.isEmpty(responseString))
                        i = 1;
                }
                if (TextUtils.isEmpty(responseString))
                    return "";
                try {
                    AbstractMap.SimpleEntry<Integer, Integer> responsePair = FCPublication.ParseServerResponseToNewPublication(new JSONObject(responseString));
                    if (responsePair != null) {
                        publicationForSaving.setNewIdFromServer(responsePair.getKey());
                        publicationForSaving.setVersionFromServer(responsePair.getValue());
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return "";
            //endregion
            //region edit publication
            case InternalRequest.ACTION_PUT_EDIT_PUBLICATION:
                if (params[0].publicationForSaving == null) {
                    Log.e(MY_TAG, "got null pubForSaving!");
                    return "";
                }
                publicationForSaving = params[0].publicationForSaving;
                MakeServerRequest(REQUEST_METHOD_PUT, server_sub_path, params[0].canWriteSelfToJSONWriterObject, true);
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(responseString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                responseString = "";
                if (jsonObject == null) {
                    Log.e(MY_TAG, "cant transform edit pub response to json");
                    isSuccess = false;
                    return "";
                }
                try {
                    publicationForSaving.setVersion(jsonObject.getInt(FCPublication.PUBLICATION_VERSION_KEY));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return "";
            //endregion
            //region case register user to system
            case InternalRequest.ACTION_POST_REGISTER:
                publicationForSaving = params[0].publicationForSaving;
                MakeServerRequest(REQUEST_METHOD_POST, server_sub_path, params[0].canWriteSelfToJSONWriterObject, false);
                return "";
            //endregion
            //region case register to publication
            case InternalRequest.ACTION_POST_REGISTER_TO_PUBLICATION:
                registrationToPublicationToPost = params[0].myRegisterToPublication;
                MakeServerRequest(REQUEST_METHOD_POST, server_sub_path, registrationToPublicationToPost, false);
                return "";
            //endregion
            //region case unregister from publication
            case InternalRequest.ACTION_POST_UNREGISTER_FROM_PUBLICATION:
                registrationToPublicationToPost = params[0].myRegisterToPublication;
                MakeServerRequest(REQUEST_METHOD_DELETE, server_sub_path, registrationToPublicationToPost, false);
//                TestDeleteWithBody(registrationToPublicationToPost, baseUrl + server_sub_path);

                return "";
            //endregion
            //region case report for publication
            case InternalRequest.ACTION_POST_REPORT_FOR_PUBLICATION:
                publicationReport = params[0].publicationReport;
                MakeServerRequest(REQUEST_METHOD_POST, server_sub_path, publicationReport, true);
                return "";
            //endregion
            //region take pub off air
            case InternalRequest.ACTION_PUT_TAKE_PUBLICATION_OFF_AIR:
                //publicationForSaving = params[0].publicationForSaving;
                MakeServerRequest(REQUEST_METHOD_PUT, server_sub_path, ForDisablingOrEnabling, false);
                return "";
            //endregion
            //region reactivate pub
            case InternalRequest.ACTION_PUT_REACTIVATE_PUBLICATION:
                MakeServerRequest(REQUEST_METHOD_PUT, server_sub_path, ForDisablingOrEnabling, false);
                return "";
            //endregion
            //region delete publication
            case InternalRequest.ACTION_DELETE_PUBLICATION:
                MakeServerRequest(REQUEST_METHOD_DELETE, server_sub_path, null, false);
                return "";
            //endregion
            //region REPORT_LOCATION
            case InternalRequest.ACTION_REPORT_LOCATION:
                MyLocationForReport locationForReport
                        = new MyLocationForReport(params[0].imei, params[0].location);
                MakeServerRequest(REQUEST_METHOD_PUT, server_sub_path, locationForReport, false);
                return "";
            //endregion
            //region FROM PUSH
            case InternalRequest.ACTION_PUSH_NEW_PUB:
                MakeServerRequest(REQUEST_METHOD_GET, server_sub_path, null, true);
                if (!isSuccess) {
                    Log.e(MY_TAG, "cant get publication by id (from push)");
                    return "";
                }
                isSuccess = false;
                publication = null;
                try {
                    publication = FCPublication.ParseSinglePublicationFromJSON(new JSONObject(responseString));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (publication == null) {
                    Log.e(MY_TAG, "publication null");
                    return "";
                }
                responseString = "";
                MakeServerRequest(REQUEST_METHOD_GET,
                        params[1].ServerSubPath.replace("{0}",
                                String.valueOf(publication.getUniqueId())), null, true);
                if (!isSuccess) {
                    Log.e(MY_TAG, "can't get reged users (from push)");
                    return "";
                }
                isSuccess = false;
                ArrayList<RegisteredUserForPublication> regedUsers1 = new ArrayList<>();
                try {
                    regedUsers1 = RegisteredUserForPublication.
                            GetArrayListOfRegisteredForPublicationsFromJSON(
                                    new JSONArray(responseString));
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    responseString = "";
                }
                publication.setRegisteredForThisPublication(regedUsers1);
                MakeServerRequest(REQUEST_METHOD_GET,
                        params[2].ServerSubPath.replace("{0}", String.valueOf(publication.getUniqueId())), null, true);
                if (!isSuccess) {
                    Log.e(MY_TAG, "cant get reports! (from push)");
                    return "";
                }
                ArrayList<PublicationReport> reports = new ArrayList<>();
                try {
                    reports = PublicationReport.GetArrayListOfPublicationReportsFromJSON(new JSONArray(responseString));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                ArrayList<PublicationReport> matchingReports = new ArrayList<>();
                for (PublicationReport pr : reports)
                    if (pr.getPublication_id() == publication.getUniqueId())
                        matchingReports.add(pr);
                if (matchingReports.size() > 0)
                    publication.setPublicationReports(matchingReports);
                return "";
            case InternalRequest.ACTION_PUSH_REG:
                MakeServerRequest(REQUEST_METHOD_GET, server_sub_path, null, true);
                if (!isSuccess) {
                    Log.e(MY_TAG, "cant get publication by id (from push)");
                    return "";
                }
                isSuccess = false;
                publication = null;
                try {
                    publication = FCPublication.ParseSinglePublicationFromJSON(new JSONObject(responseString));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (publication == null) {
                    Log.e(MY_TAG, "publication null");
                    return "";
                }
                publicationForSaving = publication;

                responseString = "";
                publicationID = params[2].PublicationID;
                MakeServerRequest(REQUEST_METHOD_GET,
                        params[1].ServerSubPath.replace("{0}",
                                String.valueOf(publicationID)), null, true);
                if (!isSuccess) {
                    Log.e(MY_TAG, "can't get reged users (from push)");
                    return "";
                }
                try {
                    registeredUsers = RegisteredUserForPublication.
                            GetArrayListOfRegisteredForPublicationsFromJSON(
                                    new JSONArray(responseString));
                } catch (JSONException e) {
                    e.printStackTrace();
                } finally {
                    responseString = "";
                }
                return "";
            //endregion
            //region POST FEEDBACK
            case InternalRequest.ACTION_POST_FEEDBACK:
                FeedbackReport fr
                        = new FeedbackReport(
                        params[0].publicationReport.getReportUserName(),
                        params[0].publicationReport.getReportContactInfo(),
                        params[0].publicationReport.getDevice_uuid());
                MakeServerRequest(REQUEST_METHOD_POST, server_sub_path, fr, false);
                return "";
            //endregion
            //region POST NEW USER
            case InternalRequest.ACTION_POST_NEW_USER:
                doAfterRegistrationActionID = params[0].DoAfterRegistrationActionID;
                UserRegistrationAddEdit registration = new UserRegistrationAddEdit();
                registration.IsLoggedIn = true;
                registration.SocialNetworkType = params[0].SocialNetworkType;
                registration.SocialNetworkID = params[0].SocialNetworkID;
                registration.Token = params[0].SocialNetworkToken;
                registration.PhoneNumber = params[0].PhoneNumber;
                registration.Email = params[0].Email;
                registration.UserName = params[0].UserName;
                registration.DeviceUUID = params[0].DeviceUUID;
                MakeServerRequest(REQUEST_METHOD_POST, server_sub_path, registration, true);
                if (!isSuccess || TextUtils.isEmpty(responseString)) {
                    Log.e(MY_TAG, "failed to register user and get id");
                } else {
                    try {
                        JSONObject obj = new JSONObject(responseString);
                        newUserID = obj.getInt("id");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                isSuccess = newUserID > 0;
                return "";
            //endregion
            //region PUT EDIT USER
            case InternalRequest.ACTION_PUT_EDIT_USER:
                UserRegistrationAddEdit updateUser = new UserRegistrationAddEdit();
                updateUser.IsLoggedIn = true;
                updateUser.SocialNetworkType = params[0].SocialNetworkType;
                updateUser.SocialNetworkID = params[0].SocialNetworkID;
                updateUser.Token = params[0].SocialNetworkToken;
                updateUser.PhoneNumber = params[0].PhoneNumber;
                updateUser.Email = params[0].Email;
                updateUser.UserName = params[0].UserName;
                updateUser.DeviceUUID = params[0].DeviceUUID;
                MakeServerRequest(REQUEST_METHOD_PUT, server_sub_path, updateUser, true);
                return "";
            //endregion
            //region POST GROUP
            case InternalRequest.ACTION_POST_NEW_GROUP:
                group = (Group) params[0].canWriteSelfToJSONWriterObject;
                MakeServerRequest(REQUEST_METHOD_POST, server_sub_path, params[0].canWriteSelfToJSONWriterObject, true);
                if (!isSuccess || TextUtils.isEmpty(responseString)) {
                    Log.e(MY_TAG, "failed to create group");
                } else {
                    try {
                        JSONObject groupObj = new JSONObject(responseString);
                        group.Set_id(groupObj.getInt("id"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                isSuccess = false;
                responseString = "";
                if (group.Get_id() > 0) {
                    params[0].groupOwner.set_group_id(group.Get_id());
                    ArrayList<GroupMember> members = new ArrayList<>();
                    members.add(params[0].groupOwner);
                    members.addAll(params[0].groupMembersToAdd);
                        isSuccess = false;
                        responseString = "";
                        GroupMembersForPost memberOwner = new GroupMembersForPost();
                        for(GroupMember member : members){
                            member.set_group_id(group.Get_id());
                        memberOwner.AddGroupMembers(member);
                        }
                        MakeServerRequest(REQUEST_METHOD_POST, params[0].MembersServerSubPath, memberOwner, true);
                        if(!isSuccess || TextUtils.isEmpty(responseString)){
                            //Log.e(MY_TAG, "failed to save user: " + member.get_name() + " " + member.get_phone_number() + (member.get_is_admin() ? "(admin)":""));
                            Log.e(MY_TAG, "failed to save users");
                        }else{
                            try {
                                JSONArray memberObjArray = new JSONArray(responseString);
                                for(int i = 0; i < memberOwner.groupMembers.size(); i++) {
                                    members.get(i).set_id(memberObjArray.getJSONObject(i).getInt("id"));
                                    group.add_group_member(members.get(i));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
//                                try {
//                                    JSONArray memberObjArray = new JSONArray(responseString);
//                                    member.set_id(memberObjArray.getInt(0));
//                                    group.add_group_member(member);
//                                } catch (JSONException e1){
//                                    e.printStackTrace();
//                                }
                            }
                        }
                } else
                    isSuccess = false;
                return "";
            //endregion
            //region GET GROUPS BY USER
            case InternalRequest.ACTION_GET_GROUPS_BY_USER:
                MakeServerRequest(REQUEST_METHOD_GET, server_sub_path, null, true);
                return "";
            //endregion
            //region Delete group member
            case InternalRequest.ACTION_DELETE_GROUP_MEMBER:
                groupMemberToDeleteID = params[0].GroupMemberToDeleteID;
                MakeServerRequest(REQUEST_METHOD_DELETE, server_sub_path  + "/" + String.valueOf(params[0].GroupMemberToDeleteID), null, false);
                return "";
            //endregion
            //region reload registered for publication
            case InternalRequest.ACTION_GET_ALL_REGISTERED_FOR_PUBLICATION:
                publicationID = params[0].PublicationID;
                MakeServerRequest(REQUEST_METHOD_GET, server_sub_path, null, true);
                if(!isSuccess || TextUtils.isEmpty(responseString))
                    Log.e(MY_TAG, "failed to get registered");
                else {
                    try {
                        regedUpdateByPush = RegisteredUserForPublication.
                                GetArrayListOfRegisteredForPublicationsFromJSON(
                                        new JSONArray(responseString));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                return "";
            //endregion
            default:
                return "";
        }
    }

    private void MakeServerRequest(String requestMethod, String server_sub_path, ICanWriteSelfToJSONWriter writableObject, boolean isForResult) {
        String post_url = baseUrl + server_sub_path;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(post_url);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(requestMethod);
            connection.setReadTimeout(30000);
            connection.setConnectTimeout(15000);
            connection.addRequestProperty("Content-Type", "application/json");
            connection.addRequestProperty("Accept", "application/json");
            connection.setUseCaches(false);
            //connection.setRequestMethod("GET");
            byte[] bytes = null;
            switch (requestMethod) {
                case REQUEST_METHOD_GET:
                    connection.setRequestProperty("Content-length", "0");
                    connection.setAllowUserInteraction(false);
                    break;
                case REQUEST_METHOD_POST:
                case REQUEST_METHOD_PUT:
                    //connection.setDoInput(true);
                    connection.setDoOutput(true);
                    break;
                case REQUEST_METHOD_DELETE:
                    break;
            }
            //connection.connect();
            if (writableObject != null && requestMethod != REQUEST_METHOD_DELETE) {

                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                String s = writableObject.GetJsonObjectForPost().toString();
                String out = null;
                try {
                    out = new String(writableObject.GetJsonObjectForPost().toString().getBytes("UTF-8"), "ISO-8859-1");
                } catch (java.io.UnsupportedEncodingException e) {

                }

                wr.writeBytes(out);
                wr.flush();
                wr.close();
            }
            int connectionResponse = connection.getResponseCode();
            Log.i(MY_TAG, "http action " + String.valueOf(Action_Command_ID) + " response: " + String.valueOf(connectionResponse));
            switch (connectionResponse) {
                case HttpURLConnection.HTTP_OK:
                case HttpURLConnection.HTTP_CREATED:
                    isSuccess = true;
                    if (isForResult) {
                        BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line + "\n");
                        }
                        br.close();
                        //Log.i(MY_TAG, this.hashCode() + sb.toString());
                        responseString = sb.toString();
                    }
                    break;
                default:
                    isSuccess = false;
                    break;
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.disconnect();
                    connection = null;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }


    @Override
    protected void onPostExecute(String s) {
        //super.onPostExecute(s);
        switch (Action_Command_ID) {
            case InternalRequest.ACTION_GET_ALL_PUBLICATIONS:
                InternalRequest irLoadData;
                if (isSuccess) {
                    Log.i(MY_TAG, "data loaded from http, calling callback");
                    irLoadData = new InternalRequest(InternalRequest.ACTION_GET_ALL_PUBLICATIONS, resultPublications, null, resultGroups);
                    irLoadData.Status = InternalRequest.STATUS_OK;
                } else {
                    Log.i(MY_TAG, "failed to load data from server");
                    irLoadData = new InternalRequest(InternalRequest.ACTION_GET_ALL_PUBLICATIONS, false);
                }
                callbackListener.OnServerRespondedCallback(irLoadData);
                break;
            case InternalRequest.ACTION_POST_REGISTER:
                Log.i(MY_TAG, "successfully registered user on server");
                callbackListener.OnServerRespondedCallback(new InternalRequest(Action_Command_ID, isSuccess));
                Intent intent = new Intent(ServicesBroadcastReceiver.BROADCAST_REC_INTENT_FILTER);
                intent.putExtra(ServicesBroadcastReceiver.BROADCAST_REC_EXTRA_ACTION_KEY,
                        isSuccess ? ServicesBroadcastReceiver.ACTION_CODE_REGISTRATION_SUCCESS
                                : ServicesBroadcastReceiver.ACTION_CODE_REGISTRATION_FAIL);
                context.getApplicationContext().sendBroadcast(intent);
                break;
            case InternalRequest.ACTION_POST_NEW_PUBLICATION:
                Log.i(MY_TAG, "post new publication to server: " + (isSuccess ? "ok" : "fail"));
                InternalRequest response = new InternalRequest(Action_Command_ID, publicationForSaving);
                response.Status = isSuccess ? InternalRequest.STATUS_OK : InternalRequest.STATUS_FAIL;
                callbackListener.OnServerRespondedCallback(response);

                break;
            case InternalRequest.ACTION_POST_REGISTER_TO_PUBLICATION:
                Log.i(MY_TAG, "register to pub: " + (isSuccess ? "ok" : "fail"));
                callbackListener.OnServerRespondedCallback(new InternalRequest(Action_Command_ID, isSuccess));
                break;
            case InternalRequest.ACTION_POST_UNREGISTER_FROM_PUBLICATION:
                Log.i(MY_TAG, "unregister from pub: " + (isSuccess ? "ok" : "fail"));
                callbackListener.OnServerRespondedCallback(new InternalRequest(Action_Command_ID, isSuccess));
                break;
            case InternalRequest.ACTION_POST_REPORT_FOR_PUBLICATION:
                Log.i(MY_TAG, "report for publication: " + (isSuccess ? "ok" : "fail"));
                callbackListener.OnServerRespondedCallback(new InternalRequest(Action_Command_ID, isSuccess));
                break;
            case InternalRequest.ACTION_PUT_EDIT_PUBLICATION:
                Log.i(MY_TAG, "save edited publication on server: " + (isSuccess ? "ok" : "fail"));
                InternalRequest irEdit = new InternalRequest(Action_Command_ID, isSuccess);
                irEdit.publicationForSaving = publicationForSaving;
                callbackListener.OnServerRespondedCallback(irEdit);
                break;
            case InternalRequest.ACTION_PUT_TAKE_PUBLICATION_OFF_AIR:
                Log.i(MY_TAG, "take publication off air: " + (isSuccess ? "ok" : "fail"));
                InternalRequest irOffAir = new InternalRequest(Action_Command_ID, isSuccess);
                callbackListener.OnServerRespondedCallback(irOffAir);
                break;
            case InternalRequest.ACTION_PUT_REACTIVATE_PUBLICATION:
                Log.i(MY_TAG, "reactivate publication: " + (isSuccess ? "ok" : "fail"));
                InternalRequest irReact = new InternalRequest(Action_Command_ID, isSuccess);
                callbackListener.OnServerRespondedCallback(irReact);
                break;
            case InternalRequest.ACTION_DELETE_PUBLICATION:
                Log.i(MY_TAG, "delete publication: " + (isSuccess ? "ok" : "fail"));
                InternalRequest irDelete = new InternalRequest(Action_Command_ID, isSuccess);
                callbackListener.OnServerRespondedCallback(irDelete);
                break;
            case InternalRequest.ACTION_REPORT_LOCATION:
                Log.i(MY_TAG, "report location: " + (isSuccess ? "ok" : "fail"));
                break;
            case InternalRequest.ACTION_PUSH_NEW_PUB:
                Log.i(MY_TAG, "get new pub (from push): " + (isSuccess ? "ok" : "fail"));
                InternalRequest irNewPubFromPush = new InternalRequest(Action_Command_ID, isSuccess);
                irNewPubFromPush.publicationForSaving = publication;
                callbackListener.OnServerRespondedCallback(irNewPubFromPush);
                break;
            case InternalRequest.ACTION_PUSH_REG:
                Log.i(MY_TAG, "update list of registered users for pub (from push): " + (isSuccess ? "ok" : "fail"));
                InternalRequest irUpdateListRegUsersFromPush = new InternalRequest(Action_Command_ID, isSuccess);
                irUpdateListRegUsersFromPush.registeredUsers = registeredUsers;
                irUpdateListRegUsersFromPush.PublicationID = publicationID;
                irUpdateListRegUsersFromPush.publicationForSaving = publicationForSaving;
                callbackListener.OnServerRespondedCallback(irUpdateListRegUsersFromPush);
                break;
            case InternalRequest.ACTION_POST_FEEDBACK:
                Log.i(MY_TAG, "send feedback complete: " + (isSuccess ? "ok" : "fail"));
                InternalRequest irFeedback = new InternalRequest(Action_Command_ID, isSuccess);
                callbackListener.OnServerRespondedCallback(irFeedback);
                break;
            case InternalRequest.ACTION_POST_NEW_USER:
                Log.i(MY_TAG, "post new user complete: " + (isSuccess ? "ok" : "fail"));
                InternalRequest irNewUser = new InternalRequest(Action_Command_ID, isSuccess);
                irNewUser.newUserID = newUserID;
                irNewUser.DoAfterRegistrationActionID = doAfterRegistrationActionID;
                callbackListener.OnServerRespondedCallback(irNewUser);
                break;
            case InternalRequest.ACTION_PUT_EDIT_USER:
                Log.i(MY_TAG, "put edit user complete: " + (isSuccess ? "ok" : "fail"));
                InternalRequest irEditedUser = new InternalRequest(Action_Command_ID, isSuccess);
                callbackListener.OnServerRespondedCallback(irEditedUser);
                break;
            case InternalRequest.ACTION_POST_NEW_GROUP:
                Log.i(MY_TAG, "post new group complete: " + (isSuccess ? "ok" : "fail"));
                InternalRequest irGroupResponse = new InternalRequest(Action_Command_ID, isSuccess);
                irGroupResponse.group = group;
                callbackListener.OnServerRespondedCallback(irGroupResponse);
                break;
            case InternalRequest.ACTION_GET_GROUPS_BY_USER:
                Log.i(MY_TAG, "get groups by user complete: " + (isSuccess ? "ok" : "fail"));
                InternalRequest irGroupsByUser = new InternalRequest(Action_Command_ID, isSuccess);
                irGroupsByUser.resultString = responseString;
                callbackListener.OnServerRespondedCallback(irGroupsByUser);
                break;
            case InternalRequest.ACTION_DELETE_GROUP_MEMBER:
                Log.i(MY_TAG, "delete group member: " + (isSuccess ? "ok" : "fail"));
                InternalRequest irDeleteMember = new InternalRequest(Action_Command_ID, isSuccess);
                irDeleteMember.GroupMemberToDeleteID = groupMemberToDeleteID;
                callbackListener.OnServerRespondedCallback(irDeleteMember);
                break;
            case InternalRequest.ACTION_GET_ALL_REGISTERED_FOR_PUBLICATION:
                Log.i(MY_TAG, "getting updated reged users after push: " + (isSuccess ? "ok" : "fail"));
                InternalRequest irRegedByPush = new InternalRequest(Action_Command_ID, isSuccess);
                irRegedByPush.registeredUsers = regedUpdateByPush;
                irRegedByPush.PublicationID = publicationID;
                callbackListener.OnServerRespondedCallback(irRegedByPush);
                break;
        }
    }

    private ArrayList<FCPublication> tmpConnectReportsToPublications(ArrayList<FCPublication> publications, ArrayList<PublicationReport> reports) {
        //ArrayList<Integer> validReports = new ArrayList<>();
        for (PublicationReport report : reports) {
            int pubId = report.getPublication_id();
            int pubVersion = report.getPublication_version();
            FCPublication pub = FCPublication.GetPublicationFromArrayListByID(publications, pubId);
            if (pub == null) continue;
            if (pub.getVersion() == pubVersion) {
                if (pub.getPublicationReports() == null)
                    pub.setPublicationReports(new ArrayList<PublicationReport>());
                pub.getPublicationReports().add(report);
            }
        }
        return publications;
    }

    private FCPublicationForDisabling ForDisablingOrEnabling = new FCPublicationForDisabling();

    class FCPublicationForDisabling implements ICanWriteSelfToJSONWriter {

        @Override
        public org.json.simple.JSONObject GetJsonObjectForPost() {
            Map<String, Object> publicationData = new HashMap<String, Object>();
            publicationData.put(FCPublication.PUBLICATION_IS_ON_AIR_KEY, false);//getIsOnAir());
            Map<String, Object> dataToSend = new HashMap<String, Object>();
            dataToSend.put(FCPublication.PUBLICATION_JSON_ITEM_KEY, publicationData);
            org.json.simple.JSONObject json = new org.json.simple.JSONObject();
            json.putAll(dataToSend);
            return json;
        }
    }

    class MyLocationForReport implements ICanWriteSelfToJSONWriter {

        private String imei;
        private Location location;

        public MyLocationForReport(String imei, Location location) {
            this.imei = imei;
            this.location = location;
        }

        @Override
        public org.json.simple.JSONObject GetJsonObjectForPost() {
            Map<String, Object> publicationData = new HashMap<String, Object>();
            publicationData.put(UserRegisterData.USER_DATA_DEV_UUID_FIELD_NAME, imei);
            publicationData.put("last_location_latitude", location.getLatitude());
            publicationData.put("last_location_longitude", location.getLongitude());
            Map<String, Object> dataToSend = new HashMap<String, Object>();
            dataToSend.put("active_device", publicationData);
            org.json.simple.JSONObject json = new org.json.simple.JSONObject();
            json.putAll(dataToSend);
            return json;
        }
    }

    class FeedbackReport implements ICanWriteSelfToJSONWriter {

        String ReporterName, Feedback, Imei;

        public FeedbackReport(String reporterName, String feedback, String imei) {
            ReporterName = reporterName;
            Feedback = feedback;
            Imei = imei;
        }

        @Override
        public org.json.simple.JSONObject GetJsonObjectForPost() {
            Map<String, Object> publicationData = new HashMap<String, Object>();
            publicationData.put(FCPublication.PUBLICATION_PUBLISHER_UUID_KEY, Imei);
            publicationData.put("reporter_name", ReporterName);
            publicationData.put(PublicationReport.PUBLICATION_REPORT_FIELD_KEY_REPORT, Feedback);
            Map<String, Object> dataToSend = new HashMap<String, Object>();
            dataToSend.put("feedback", publicationData);
            org.json.simple.JSONObject json = new org.json.simple.JSONObject();
            json.putAll(dataToSend);
            return json;
        }
    }

    class UserRegistrationAddEdit implements ICanWriteSelfToJSONWriter {

        private final String USER_JSON_KEY_USER = "user";
        private final String USER_JSON_KEY_IDENTITY_PROVIDER = "identity_provider";
        private final String USER_JSON_KEY_IDENTITY_PROVIDER_USER_ID = "identity_provider_user_id";
        private final String USER_JSON_KEY_IDENTITY_PROVIDER_TOKEN = "identity_provider_token";
        private final String USER_JSON_KEY_PHONE_NUMBER = "phone_number";
        private final String USER_JSON_KEY_IDENTITY_PROVIDER_EMAIL = "identity_provider_email";
        private final String USER_JSON_KEY_USER_NAME = "identity_provider_user_name";
        private final String USER_JSON_KEY_IS_LOGGED_IN = "is_logged_in";
        private final String USER_JSON_KEY_ACTIVE_DEVICE_DEV_UUID = "active_device_dev_uuid";

        public String SocialNetworkID;
        public String SocialNetworkType;
        public String Token;
        public String PhoneNumber;
        public String Email;
        public String UserName;
        public String DeviceUUID;
        public boolean IsLoggedIn;

        @Override
        public org.json.simple.JSONObject GetJsonObjectForPost() {
            Map<String, Object> userData = new HashMap<String, Object>();
            userData.put(USER_JSON_KEY_IDENTITY_PROVIDER, SocialNetworkType);
            userData.put(USER_JSON_KEY_IDENTITY_PROVIDER_USER_ID, SocialNetworkID);
            userData.put(USER_JSON_KEY_IDENTITY_PROVIDER_TOKEN, Token);
            userData.put(USER_JSON_KEY_PHONE_NUMBER, PhoneNumber);
            userData.put(USER_JSON_KEY_IDENTITY_PROVIDER_EMAIL, Email);
            userData.put(USER_JSON_KEY_USER_NAME, UserName);
            userData.put(USER_JSON_KEY_IS_LOGGED_IN, IsLoggedIn);
            userData.put(USER_JSON_KEY_ACTIVE_DEVICE_DEV_UUID, DeviceUUID);
            Map<String, Object> dataToSend = new HashMap<String, Object>();
            dataToSend.put(USER_JSON_KEY_USER, userData);
            org.json.simple.JSONObject json = new org.json.simple.JSONObject();
            json.putAll(dataToSend);
            return json;
        }
    }

    class GroupMembersForPost implements ICanWriteSelfToJSONWriter {

        private ArrayList<GroupMember> groupMembers;

        public ArrayList<GroupMember> Get_GroupMembers() {
            return groupMembers;
        }

        public void Set_GroupMembers(ArrayList<GroupMember> members) {
            groupMembers = members;
        }

        public void AddGroupMembers(GroupMember... members) {
            if (groupMembers == null)
                groupMembers = new ArrayList<>();
            for (GroupMember gm : members)
                groupMembers.add(gm);
        }

        public void AddGroupMembers(ArrayList<GroupMember> members){
            if (groupMembers == null)
                groupMembers = new ArrayList<>();
            groupMembers.addAll(members);
        }

        GroupMembersForPost() {
            Set_GroupMembers(new ArrayList<GroupMember>());
        }

        GroupMembersForPost(ArrayList<GroupMember> members) {
            Set_GroupMembers(members);
        }

        @Override
        public org.json.simple.JSONObject GetJsonObjectForPost() {
            if (groupMembers == null || groupMembers.size() == 0)
                return null;
            org.json.simple.JSONArray array = new org.json.simple.JSONArray();
            for (GroupMember gm : groupMembers) {
                array.add(gm.GetJsonObjectForPost());
            }
            Map<String, Object> outerMap = new HashMap<>();
            outerMap.put("group_members", array);
            return new org.json.simple.JSONObject(outerMap);
        }
    }
}
