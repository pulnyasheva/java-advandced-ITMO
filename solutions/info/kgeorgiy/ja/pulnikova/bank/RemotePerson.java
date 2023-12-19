package info.kgeorgiy.ja.pulnikova.bank;

import java.rmi.RemoteException;

public class RemotePerson implements Person {

    private final String name;
    private final String surname;
    private final String passport;

    public RemotePerson(String name, String surname, String passport){
        this.name = name;
        this.surname = surname;
        this.passport = passport;
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
}
