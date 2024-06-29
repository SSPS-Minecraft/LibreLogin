/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package xyz.kyngs.librelogin.common.command.commands.authorization;

import co.aikar.commands.annotation.*;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Bukkit;
import xyz.kyngs.librelogin.api.event.events.AuthenticatedEvent;
import xyz.kyngs.librelogin.common.AuthenticLibreLogin;
import xyz.kyngs.librelogin.common.command.InvalidCommandArgument;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.concurrent.CompletionStage;

@CommandAlias("register|reg")
public class RegisterCommand<P> extends AuthorizationCommand<P> {
    public RegisterCommand(AuthenticLibreLogin<P, ?> premium) {
        super(premium);
    }

    @Default
    @Syntax("{@@syntax.register}")
    @CommandCompletion("%autocomplete.register")
    public CompletionStage<Void> onRegister(Audience sender, P player, @Single String password, String passwordRepeat, String code) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(Bukkit.getPluginsFolder().getPath() + "/SSPSLogin/players.csv"));
            String line = reader.readLine();
            boolean found = false;
            while (line != null) {
                String[] items = line.split(",");
                if (items[0].equals(getUser(player).getLastNickname())) {
                    found = true;
                    if (!items[1].equals(code)) {
                        sender.sendMessage(getMessage("ssps-bad-code"));
                        return null;
                    }
                }
                line = reader.readLine();
            }
            if (!found) {
                sender.sendMessage(getMessage("ssps-no-code"));
                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return runAsync(() -> {
            checkUnauthorized(player);
            var user = getUser(player);

            if (user.isRegistered()) throw new InvalidCommandArgument(getMessage("error-already-registered"));
            if (!password.contentEquals(passwordRepeat))
                throw new InvalidCommandArgument(getMessage("error-password-not-match"));

            setPassword(sender, user, password, "info-registering");

            sender.sendMessage(getMessage("info-registered"));

            getAuthorizationProvider().authorize(user, player, AuthenticatedEvent.AuthenticationReason.REGISTER);
        });
    }

}
