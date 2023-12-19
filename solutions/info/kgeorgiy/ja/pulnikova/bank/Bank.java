package info.kgeorgiy.ja.pulnikova.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Set;

public interface Bank extends Remote {
    /**
     * Creates a new account with specified identifier if it does not already exist.
     * @param subId account id
     * @return created or existing account.
     */
    Account createAccountForRemotePerson(String subId, Person person) throws RemoteException;

    Account createAccountForLocalPerson(String subId, Person person) throws RemoteException;
    /**
     * Returns account by identifier.
     * @param subId account id
     * @return account with specified identifier or {@code null} if such account does not exist.
     */
    Account getAccountForLocalPerson(String subId, Person person) throws RemoteException;

    Account getAccountForRemotePerson(String subId, Person person) throws RemoteException;

    Person createPerson(String name, String surname, String passport) throws RemoteException;

    LocalPerson getLocalPerson(String passport) throws RemoteException;

    Person getRemotePerson(String passport) throws RemoteException;

    Set<LocalAccount> getAccountsForLocalPerson(Person person) throws RemoteException;

    Set<Account> getAccountsForRemotePerson(Person person) throws RemoteException;
}
