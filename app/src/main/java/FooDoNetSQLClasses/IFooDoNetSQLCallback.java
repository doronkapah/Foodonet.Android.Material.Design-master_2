package FooDoNetSQLClasses;

import CommonUtilPackage.InternalRequest;

/**
 * Created by Asher on 24-Jul-15.
 */
public interface IFooDoNetSQLCallback {
    //void OnUpdateLocalDBComplete(ArrayList<FCPublication> publications);
    //void OnGetPublicationForListCompleted(ArrayList<FCPublication> publicationsForList);
    void OnSQLTaskComplete(InternalRequest request);
}
