package info.kgeorgiy.ja.pulnikova.bank;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LocalPerson implements Person, Serializable {

    private final String name;
    private final String surname;
    private final String passport;
    private final Map<String,LocalAccount> accounts;

    public LocalPerson(String name, String surname, String passport, Map<String, LocalAccount> accounts){
        this.name = name;
        this.surname = surname;
        this.passport = passport;
        this.accounts = accounts;
    }

    @Override
    public String getName() throws RemoteException {
        return name;
    }

    @Override
    public String getSurname() throws RemoteException {
        return surname;
    }

    @Override
    public String getPassport() throws RemoteException {
        return passport;
    }

    public Set<LocalAccount> getAccounts(){
        return new HashSet<>(accounts.values());
    }

    public Account getAccountForId(String id){
        return accounts.get(id);
    }

    public Account createAccount(String id){
        LocalAccount account = new LocalAccount(id);
        if (accounts.putIfAbsent(id, account) == null){
            return account;
        } else {
            return accounts.get(id);
        }
    }
}
