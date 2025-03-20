package org.obsidian.client.screen.account;

import lombok.extern.log4j.Log4j2;
import org.obsidian.client.Obsidian;
import org.obsidian.client.api.client.Constants;
import org.obsidian.client.utils.file.FileManager;
import org.obsidian.client.utils.file.FileType;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Log4j2
public class AccountManager extends CopyOnWriteArrayList<Account> {
    public static File ACCOUNT_DIRECTORY;

    public AccountManager() {
        init();
    }

    public void init() {
        ACCOUNT_DIRECTORY = new File(FileManager.DIRECTORY, FileType.ACCOUNT.getName());
        if (!ACCOUNT_DIRECTORY.exists()) {
            if (!ACCOUNT_DIRECTORY.mkdir()) {
                log.error("Не удалось создать папку {}", FileType.ACCOUNT.getName());
                System.exit(0);
            }
        }

        Obsidian.eventHandler().subscribe(this);
    }

    public AccountFile get() {
        final File file = new File(ACCOUNT_DIRECTORY, FileType.ACCOUNT.getName() + Constants.FILE_FORMAT);
        return new AccountFile(file);
    }

    public void set() {
        final File file = new File(ACCOUNT_DIRECTORY, FileType.ACCOUNT.getName() + Constants.FILE_FORMAT);
        AccountFile accountFile = get();
        if (accountFile == null) {
            accountFile = new AccountFile(file);
        }
        accountFile.write();
    }

    public void addAccount(Account account) {
        if (isAccount(account.name())) return;
        this.add(account);
        set();
    }

    public Optional<Account> getAccount(String name) {
        return this.stream().filter(account -> account.name().equalsIgnoreCase(name)).findFirst();
    }

    public boolean isAccount(String name) {
        return this.stream().anyMatch(account -> account.name().equalsIgnoreCase(name));
    }

    public void removeAccount(String name) {
        this.removeIf(account -> account.name().equalsIgnoreCase(name));
        set();
    }

    public void clearAccounts() {
        this.clear();
        set();
    }

    public List<Account> getFavoriteAccountsSorted() {
        return this.stream()
                .filter(Account::favorite)
                .collect(Collectors.toList());
    }
}
