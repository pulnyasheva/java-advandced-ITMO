package info.kgeorgiy.ja.pulnikova.bank;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public final class Client {

    private Client() {

    }

    public static void main(final String... args) throws RemoteException {
        final Bank bank;
        try {
            bank = (Bank) Naming.lookup("//localhost/bank");
        } catch (final NotBoundException e) {
            System.out.println("Bank is not bound");
            return;
        } catch (final MalformedURLException e) {
            System.out.println("Bank URL is invalid");
            return;
        }
        if (args.length >= 5) {
            String name = args[0];
            String surname = args[1];
            String passport = args[2];
            String subId = args[3];
            int amount;
            try {
                amount = Integer.parseInt(args[4]);
            } catch (NumberFormatException e) {
                System.err.println("Ammount is not number");
                return;
            }
            Person person = bank.getRemotePerson(passport);
            Account account;
            if (person == null) {
                System.out.println("Creating person");
                person = bank.createPerson(name, surname, passport);
                account = bank.createAccountForRemotePerson(subId, person);
            } else {
                if (!person.getName().equals(name)
                        || !person.getSurname().equals(surname)) {
                    System.err.println("The data is uncorrected");
                    return;
                }
                System.out.println("Person already exists");
                account = bank.getAccountForRemotePerson(subId, person);
                if (account == null) {
                    System.out.println("Creating account");
                    account = bank.createAccountForRemotePerson(subId, person);
                } else {
                    System.out.println("Account already exists");
                    account = bank.getAccountForRemotePerson(subId, person);
                }
            }
            System.out.println("Account id: " + account.getId());
            System.out.println("Money: " + account.getAmount());
            System.out.println("Adding money");
            account.setAmount(account.getAmount() + amount);
            System.out.println("Money: " + account.getAmount());
        } else {
            System.err.println("Incorrect number of arguments");
        }
    }
}
