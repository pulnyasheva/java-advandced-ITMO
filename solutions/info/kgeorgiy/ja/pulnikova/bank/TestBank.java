package info.kgeorgiy.ja.pulnikova.bank;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestBank {

    private static Bank bank;
    private final static int PORT = 8888;
    private final static String NAME = "Ivan";
    private final static String SURNAME = "Ivanov";
    private final static String PASSPORT = "12345";
    private final static String ZERO = "0";
    private final static String SUBID1 = "54321";
    private final static String ID1 = "12345:54321";
    private final static String SUBID2 = "100";
    private final static String ID2 = "12345:100";
    private final static int AMOUNT = 10;


    @BeforeClass
    public static void beforeClass() throws Exception {
        Server.main();
        bank = (Bank) Naming.lookup("//localhost/bank");
    }

    @Test
    public void test01_getPersonNotExist() throws RemoteException {
        Assert.assertNull(bank.getRemotePerson(PASSPORT));
        Assert.assertNull(bank.getLocalPerson(PASSPORT));
    }

    @Test
    public void test02_createPerson() throws RemoteException {
        Person person = bank.createPerson(NAME, SURNAME, PASSPORT);
        equalsPerson(person, PASSPORT);
    }

    @Test
    public void test03_getRemotePerson() throws RemoteException {
        Person person = bank.getRemotePerson(PASSPORT);
        equalsPerson(person, PASSPORT);
    }

    @Test
    public void test04_createRemoteAccount() throws RemoteException {
        Person person = bank.getRemotePerson(PASSPORT);
        Account account = bank.createAccountForRemotePerson(SUBID1, person);
        equalsAccount(account, ID1, 0);
    }

    @Test
    public void test05_getRemoteAccount() throws RemoteException {
        Person person = bank.getRemotePerson(PASSPORT);
        Account account = bank.getAccountForRemotePerson(SUBID1, person);
        equalsAccount(account, ID1, 0);
    }

    @Test
    public void test06_getLocalPerson() throws RemoteException {
        LocalPerson person = bank.getLocalPerson(PASSPORT);
        LocalAccount ac = new LocalAccount(ID1);
        Set<LocalAccount> accounts = ConcurrentHashMap.newKeySet();
        accounts.add(ac);
        equalsPerson(person, PASSPORT);
        Assert.assertEquals(accounts, person.getAccounts());
    }

    @Test
    public void test07_createLocalAccount() throws RemoteException {
        LocalPerson person = bank.getLocalPerson(PASSPORT);
        Account account = bank.createAccountForLocalPerson(SUBID2, person);
        equalsAccount(account, ID2, 0);
        Assert.assertNull(bank.getAccountForRemotePerson(SUBID2, person));
    }

    @Test
    public void test08_createPersonIsExist() throws RemoteException {
        Person person = bank.createPerson(NAME, SURNAME, PASSPORT);
        equalsPerson(person, PASSPORT);
    }

    @Test
    public void test09_createPersons() throws RemoteException {
        for (int i = 0; i < 20; i++) {
            Person person = bank.createPerson(NAME, SURNAME, String.valueOf(i));
            equalsPerson(person, String.valueOf(i));
        }
    }

    @Test
    public void test10_createRemoteAccounts() throws RemoteException {
        Person person = bank.getRemotePerson(ZERO);
        for (int i = 0; i < 20; i++) {
            Account account = bank.createAccountForRemotePerson(String.valueOf(i), person);
            String id = person.getPassport() + ":" + i;
            equalsAccount(account, id, 0);
        }
    }

    @Test
    public void test11_createLocalAcounts() throws RemoteException {
        Person person = bank.getLocalPerson(PASSPORT);
        for (int i = 0; i < 20; i++) {
            Account localAccount = bank.createAccountForLocalPerson(String.valueOf(i), person);
            Account account = bank.getAccountForRemotePerson(String.valueOf(i), person);
            String id = person.getPassport() + ":" + i;
            equalsAccount(localAccount, id, 0);
            Assert.assertNull(account);
        }
    }

    @Test
    public void test12_setAmountRemotePerson() throws RemoteException {
        Person person = bank.getRemotePerson(ZERO);
        Account account = bank.getAccountForRemotePerson(ZERO, person);
        account.setAmount(AMOUNT);
        Assert.assertEquals(AMOUNT, account.getAmount());
    }

    @Test
    public void test13_setAmountLocalePerson() throws RemoteException {
        LocalPerson person = bank.getLocalPerson(PASSPORT);
        Account account = bank.getAccountForLocalPerson(SUBID1, person);
        Account remoteAccount = bank.getAccountForRemotePerson(SUBID1, person);
        account.setAmount(AMOUNT);
        Assert.assertEquals(AMOUNT, account.getAmount());
        Assert.assertEquals(0, remoteAccount.getAmount());
    }

    @Test
    public void test14_application() throws RemoteException {
        Client.main(NAME, SURNAME, "7890", "56", "200");
        Person person = bank.getRemotePerson("7890");
        Account account = bank.getAccountForRemotePerson("56", person);
        equalsPerson(person, "7890");
        equalsAccount(account, "7890:56", 200);
    }

    private void equalsPerson(Person person, String passport) throws RemoteException {
        Assert.assertEquals(NAME, person.getName());
        Assert.assertEquals(SURNAME, person.getSurname());
        Assert.assertEquals(passport, person.getPassport());
    }

    private void equalsAccount(Account account, String id, int amount) throws RemoteException {
        Assert.assertEquals(id, account.getId());
        Assert.assertEquals(amount, account.getAmount());
    }
}
