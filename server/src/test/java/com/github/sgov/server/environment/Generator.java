package com.github.sgov.server.environment;

import com.github.sgov.server.model.User;
import com.github.sgov.server.model.UserAccount;
import com.github.sgov.server.model.Workspace;
import java.net.URI;
import java.util.Random;

public class Generator {

    private static Random random = new Random();

    private Generator() {
        throw new AssertionError();
    }

    /**
     * Generates a (pseudo) random URI, usable for test individuals.
     *
     * @return Random URI
     */
    public static URI generateUri() {
        return URI.create(Environment.BASE_URI + "randomInstance" + randomInt());
    }

    /**
     * Generates a (pseudo-)random integer between the specified lower and upper bounds.
     *
     * @param lowerBound Lower bound, inclusive
     * @param upperBound Upper bound, exclusive
     * @return Randomly generated integer
     */
    public static int randomInt(int lowerBound, int upperBound) {
        int rand;
        do {
            rand = random.nextInt(upperBound);
        } while (rand < lowerBound);
        return rand;
    }

    /**
     * Generates a (pseudo) random integer.
     *
     * <p>This version has no bounds (aside from the integer range), so the returned number may be
     * negative or zero.
     *
     * @return Randomly generated integer
     * @see #randomInt(int, int)
     */
    public static int randomInt() {
        return random.nextInt();
    }

    /**
     * Creates a random instance of {@link User}.
     *
     * <p>The instance has no identifier set.
     *
     * @return New {@code User} instance
     * @see #generateUserWithId()
     */
    public static User generateUser() {
        final User user = new User();
        user.setFirstName("Firstname" + randomInt());
        user.setLastName("Lastname" + randomInt());
        return user;
    }

    /**
     * Creates a random instance of {@link User} with a generated identifier.
     *
     * <p>The presence of identifier is the only difference between this method and
     * {@link #generateUser()}.
     *
     * @return New {@code User} instance
     */
    public static User generateUserWithId() {
        final User user = generateUser();
        user.setUri(Generator.generateUri());
        return user;
    }

    /**
     * Generates a random {@link UserAccount} instance, initialized with first name, last name,
     * username and identifier.
     *
     * @return A new {@code UserAccount} instance
     */
    public static UserAccount generateUserAccount() {
        final UserAccount account = new UserAccount();
        account.setFirstName("FirstName" + randomInt());
        account.setLastName("LastName" + randomInt());
        account.setUsername("user" + randomInt() + "@example.cz");
        account.setUri(Generator.generateUri());
        return account;
    }

    /**
     * Generates a random {@link UserAccount} instance, initialized with first name, last name,
     * username, password and identifier.
     *
     * @return A new {@code UserAccount} instance
     * @see #generateUserAccount()
     */
    public static UserAccount generateUserAccountWithPassword() {
        final UserAccount account = generateUserAccount();
        account.setPassword("Pass" + randomInt(0, 10000));
        return account;
    }

    /**
     * Generates a random {@link Workspace} instance, initialized with
     * label and identifier.
     *
     * @return A new {@code Workspace} instance
     */
    public static Workspace generateWorkspace() {
        final Workspace workspace = new Workspace();
        workspace.setUri(Generator.generateUri());
        workspace.setLabel("Label" + randomInt());
        return workspace;
    }
}
