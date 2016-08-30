package AsyncTasks;

import java.util.HashMap;

import CommonUtilPackage.ContactItem;

/**
 * Created by Asher on 28.05.2016.
 */
public interface IFetchContactsParent {
    public void OnContactsFetched(HashMap<Integer, ContactItem> contacts);
}
