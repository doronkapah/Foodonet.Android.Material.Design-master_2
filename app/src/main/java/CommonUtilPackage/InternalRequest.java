package CommonUtilPackage;

import android.location.Location;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Map;

import DataModel.FCPublication;
import DataModel.FNotification;
import DataModel.Group;
import DataModel.GroupMember;
import DataModel.ICanWriteSelfToJSONWriter;
import DataModel.PublicationReport;
import DataModel.RegisteredUserForPublication;
import DataModel.UserRegisterData;

/**
 * Created by Asher on 23-Jul-15.
 */
public class InternalRequest implements Serializable {

    private static final String MY_TAG = "food_internalRequest";

    public static final String INTERNAL_REQUEST_EXTRA_KEY = "internal_request";

    public static final int ACTION_GET_ALL_PUBLICATIONS = 0;
    public static final int ACTION_POST_NEW_PUBLICATION = 1;
    public static final int ACTION_POST_REGISTER = 2;
    public static final int ACTION_GET_ALL_REGISTERED_FOR_PUBLICATION = 3;
    public static final int ACTION_SQL_GET_ALL_PUBS_FOR_LIST_BY_ID_DESC = 4;
    public static final int ACTION_SQL_UPDATE_DB_PUBLICATIONS_FROM_SERVER = 5;
    public static final int ACTION_SQL_SAVE_NEW_PUBLICATION = 6;
    public static final int ACTION_SQL_GET_NEW_NEGATIVE_ID = 7;
    public static final int ACTION_GET_PUBLICATION_REPORTS = 8;
    public static final int ACTION_SQL_GET_SINGLE_PUBLICATION_BY_ID = 9;
    public static final int ACTION_SQL_UPDATE_ID_OF_PUB_AFTER_SAVING_ON_SERVER = 10;
    public static final int ACTION_SQL_UPDATE_IMAGES_FOR_PUBLICATIONS = 11;
    public static final int ACTION_POST_REGISTER_TO_PUBLICATION = 12;
    public static final int ACTION_POST_UNREGISTER_FROM_PUBLICATION = 13;
    public static final int ACTION_POST_DELETE_PUBLICATION = 14;
    public static final int ACTION_REPORT_FOR_PUBLICATION = 15;
    public static final int ACTION_SQL_ADD_MYSELF_TO_REGISTERED_TO_PUB = 16;
    public static final int ACTION_SQL_REMOVE_MYSELF_FROM_REGISTERED_TO_PUB = 17;
    public static final int ACTION_POST_REPORT_FOR_PUBLICATION = 18;
    public static final int ACTION_PUT_EDIT_PUBLICATION = 19;
    public static final int ACTION_SQL_SAVE_EDITED_PUBLICATION = 20;
    public static final int ACTION_PUT_TAKE_PUBLICATION_OFF_AIR = 21;
    public static final int ACTION_PUT_REACTIVATE_PUBLICATION = -21;
    public static final int ACTION_DELETE_PUBLICATION = 22;
    public static final int ACTION_NO_ACTION = 23;
    public static final int ACTION_REPORT_LOCATION = 24;
    public static final int ACTION_PUSH_NEW_PUB = 25;
    public static final int ACTION_PUSH_PUB_DELETED = 26;
    public static final int ACTION_PUSH_REPORT_FOR_PUB = 27;
    public static final int ACTION_PUSH_REG = 28;
    public static final int ACTION_POST_NEW_USER = 29;
    public static final int ACTION_PUT_EDIT_USER = 30;
    public static final int ACTION_POST_NEW_GROUP = 31;
    public static final int ACTION_GET_GROUPS_BY_USER = 32;
    public static final int ACTION_GET_GROUPS_FROM_SQL = 33;
    public static final int ACTION_DELETE_GROUP_MEMBER = 34;
    public static final int ACTION_DELETE_LEAVE_GROUP = 35;
    public static final int ACTION_POST_FEEDBACK = 99;


    public static final int STATUS_OK = 1;
    public static final int STATUS_FAIL = 0;

    public int ActionCommand;
    public String ServerSubPath;
    public JSONObject jsonObject;
    public JSONArray jsonArray;
    public FCPublication publicationForSaving;
    public FCPublication publicationForDetails;
    public ArrayList<FCPublication> publications;
    public ArrayList<Group> groups;
    public ArrayList<RegisteredUserForPublication> registeredUsers;
    public ICanWriteSelfToJSONWriter canWriteSelfToJSONWriterObject;
    public int Status;
    public int newNegativeID;
    public long PublicationID;
    public Map<Integer, Integer> listOfPubsToFetchImageFor;
    public Map<Integer, byte[]> publicationImageMap;
    public RegisteredUserForPublication myRegisterToPublication;
    public PublicationReport publicationReport;
    public Location location;
    public String imei;
    public String resultString;

    public boolean IsSkipGroup;

    // for registering/editing user
    public String SocialNetworkID;
    public String SocialNetworkType;
    public String SocialNetworkToken;
    public String PhoneNumber;
    public String Email;
    public String UserName;
    public String DeviceUUID;
    public boolean IsLoggedIn;
    public String PhotoURL;
    public int newUserID;

    public Group group;
    public GroupMember groupOwner;
    public ArrayList<GroupMember> groupMembersToAdd;
    public String MembersServerSubPath;

    public int GroupMemberToDeleteID;

    public int DoAfterRegistrationActionID;

    public InternalRequest(int actionCommand, JSONObject obj, String sub_path) {
        ActionCommand = actionCommand;
        jsonObject = obj;
        ServerSubPath = sub_path;
    }

    public InternalRequest(int com, JSONArray arr, String sub_path) {
        ActionCommand = com;
        jsonArray = arr;
        ServerSubPath = sub_path;
    }

    public InternalRequest(int com, ArrayList<FCPublication> publications){
        ActionCommand = com;
        this.publications = publications;
    }

    public InternalRequest(int com, FCPublication newPublication){
        ActionCommand = com;
        switch (ActionCommand){
            case ACTION_SQL_SAVE_NEW_PUBLICATION:
            case ACTION_SQL_SAVE_EDITED_PUBLICATION:
            case ACTION_SQL_UPDATE_ID_OF_PUB_AFTER_SAVING_ON_SERVER:
            case ACTION_DELETE_PUBLICATION:
            case ACTION_PUSH_PUB_DELETED:
            case ACTION_PUSH_REPORT_FOR_PUB:
                publicationForSaving = newPublication;
                break;
            case ACTION_POST_NEW_PUBLICATION:
                publicationForSaving = newPublication;
                canWriteSelfToJSONWriterObject = newPublication;
                break;
            default:
                Log.e(MY_TAG, "ctor (int,fcpub): unexpected action: " + ActionCommand);
                break;
        }
    }

/*
    public InternalRequest(int com, ArrayList<FCPublication> pubs, String sub_path) {
        ActionCommand = com;
        publications = pubs;
        ServerSubPath = sub_path;
    }
*/

    public InternalRequest(int com, int newNegativeIDFromSQL){
        ActionCommand = com;
        newNegativeID = newNegativeIDFromSQL;
    }

    public InternalRequest(int com, String sub_path) {
        ActionCommand = com;
        ServerSubPath = sub_path;
    }

    public InternalRequest(int com, String sub_path, int userId){
        ActionCommand = com;
        ServerSubPath = sub_path;
        newUserID = userId;
    }

    public InternalRequest(int com, boolean status) {
        ActionCommand = com;
        Status = status ? STATUS_OK : STATUS_FAIL;
    }

    public InternalRequest(int com, String subPath, ICanWriteSelfToJSONWriter data) {
        ActionCommand = com;
        ServerSubPath = subPath;
        canWriteSelfToJSONWriterObject = data;
    }

    public InternalRequest(int com, ArrayList<FCPublication> publications, ArrayList<RegisteredUserForPublication> regUsers){
        ActionCommand = com;
        this.publications =  publications;
        registeredUsers = regUsers;
    }

    public InternalRequest(int com, ArrayList<FCPublication> publications, ArrayList<RegisteredUserForPublication> regUsers, ArrayList<Group> groups){
        ActionCommand = com;
        this.publications =  publications;
        registeredUsers = regUsers;
        this.groups = groups;
    }

    public InternalRequest(int com){
        ActionCommand = com;
    }

    public InternalRequest(int com, PublicationReport report){
        ActionCommand = com;
        publicationReport = report;
    }

    public InternalRequest(int com, long pubId){
        ActionCommand = com;
        PublicationID = pubId;
    }

    public InternalRequest(int com, Group group){
        ActionCommand = com;
        this.group = group;
    }

    public InternalRequest SetIsSkipGroup(boolean isSkipGroup){
        this.IsSkipGroup = isSkipGroup;
        return this;
    }
}
