package info.kgeorgiy.ja.pulnikova.bank;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class RemoteBank implements Bank {
    private final int port;

    private final ConcurrentMap<String, Person> persons = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Account> accounts = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Set<Account>> accountsForPersons = new ConcurrentHashMap<>();

    public RemoteBank(final int port) {
        this.port = port;
    }

    @Override
    public Account createAccountForRemotePerson(final String subId, final Person person) throws RemoteException {
        return createAccount(subId, person, false);
    }

    @Override
    public Account createAccountForLocalPerson(String subId, Person person) throws RemoteException {
        return createAccount(subId, person, true);
    }

    @Override
    public Account getAccountForLocalPerson(final String subId, final Person person) throws RemoteException {
        return getAccount(subId, person, true);
    }

    @Override
    public Account getAccountForRemotePerson(final String subId, final Person person) throws RemoteException {
        return getAccount(subId, person, false);
    }

    @Override
    public Person createPerson(String name, String surname, String passport) throws RemoteException {
        if (name == null || surname == null || passport == null) {
            return null;
        }
        System.out.printf("Creating person %s %s%n", name, surname);
        final RemotePerson person = new RemotePerson(name, surname, passport);
        if (persons.putIfAbsent(passport, person) == null) {
            UnicastRemoteObject.exportObject(person, port);
            accountsForPersons.put(passport, ConcurrentHashMap.newKeySet());
            return person;
        } else {
            return persons.get(passport);
        }

    }

    @Override
    public LocalPerson getLocalPerson(String passport) throws RemoteException {
        if (passport == null || persons.get(passport) == null) {
            return null;
        }
        Person person = persons.get(passport);
        Map<String, LocalAccount> accountsForLocalPerson = new ConcurrentHashMap<>();
        if (person instanceof LocalPerson) {
            for (LocalAccount localAccount : getAccountsForLocalPerson(person)) {
                accountsForLocalPerson.put(localAccount.getId(), localAccount);
            }
        } else {
            for (Account Account : getAccountsForRemotePerson(person)) {
                accountsForLocalPerson.put(Account.getId(), new LocalAccount(Account.getId()));
            }
        }
        return new LocalPerson(person.getName(), person.getSurname(), person.getPassport(), accountsForLocalPerson);
    }

    @Override
    public Person getRemotePerson(String passport) throws RemoteException {
        if (passport == null) {
            return null;
        }
        return persons.get(passport);
    }

    @Override
    public Set<LocalAccount> getAccountsForLocalPerson(Person person) throws RemoteException {
        return (((LocalPerson) person).getAccounts());
    }

    @Override
    public Set<Account> getAccountsForRemotePerson(Person person) throws RemoteException {
        return accountsForPersons.get(person.getPassport());
    }


    private String createId(String subId, Person person) throws RemoteException {
        return person.getPassport() + ":" + subId;
    }

    private Account createAccount(final String subId, final Person person, boolean local) throws RemoteException {
        if (subId == null || person == null) {
            return null;
        }
        String id = createId(subId, person);
        System.out.println("Creating account " + id + "for person " + person.getName() + " " + person.getSurname());
        if (local) {
            return ((LocalPerson) person).createAccount(id);
        } else {
            final Account account = new RemoteAccount(id);
            if (accounts.putIfAbsent(id, account) == null) {
                UnicastRemoteObject.exportObject(account, port);
                accountsForPersons.putIfAbsent(person.getPassport(), ConcurrentHashMap.newKeySet());
                accountsForPersons.get(person.getPassport()).add(account);
                return account;
            } else {
                return accounts.get(id);
            }
        }

    }

    private Account getAccount(final String subId, final Person person, boolean local) throws RemoteException {
        if (subId == null || person == null) {
            return null;
        }
        String id = createId(subId, person);
        System.out.println("Retrieving account " + id);
        if (local) {
            return ((LocalPerson) person).getAccountForId(id);
        } else {
            return accounts.get(id);
        }
    }
}
