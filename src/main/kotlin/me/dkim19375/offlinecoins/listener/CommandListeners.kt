/*
 *     OfflineCoins, a plugin that adds offline player support to CoinsAPI
 *     Copyright (C) 2021  dkim19375
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.dkim19375.offlinecoins.listener

import de.NeonnBukkit.CoinsAPI.API.CoinsAPI
import me.dkim19375.dkimbukkitcore.function.playSound
import me.dkim19375.dkimcore.extension.containsIgnoreCase
import me.dkim19375.offlinecoins.OfflineCoins
import me.dkim19375.offlinecoins.config.ConfigPaths
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.Sound
import org.bukkit.command.BlockCommandSender
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.server.ServerCommandEvent

class CommandListeners(private val plugin: OfflineCoins) : Listener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private fun ServerCommandEvent.onCommand() {
        onPreCmdEvent(command, sender, this)
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    private fun PlayerCommandPreprocessEvent.onCommand() {
        onPreCmdEvent(message.removePrefix("/"), player, this)
    }

    private fun onPreCmdEvent(command: String, sender: CommandSender, event: Cancellable) {
        val args = command.trim().split(" ")
        if (args.size < 2) {
            return
        }
        if (!setOf("coins", "pay").containsIgnoreCase(args[0])) {
            return
        }
        if (Bukkit.getPlayer(args[1])?.isOnline == true) {
            return
        }
        event.isCancelled = true
        Bukkit.getScheduler().runTaskAsynchronously(plugin) {
            @Suppress("DEPRECATION")
            onCommandEvent(command, sender, Bukkit.getOfflinePlayer(args[1]))
        }
    }

    @Suppress("SpellCheckingInspection")
    private fun onCommandEvent(command: String, sender: CommandSender, player: OfflinePlayer) {
        val args = command.trim().split(" ")
        val uuid = player.uniqueId.toString()
        val prefix = ConfigPaths.PREFIX.getString(plugin)
        val name = args[1]
        if (args[0].equals("pay", true) && ConfigPaths.COMMAND_PAY.getBoolean(plugin)) {
            if (args.size < 3) {
                Bukkit.dispatchCommand(sender, command)
                return
            }
            val amount = (args[2].toIntOrNull() ?: run {
                Bukkit.dispatchCommand(sender, command)
                return
            })
            if (amount <= 0) {
                Bukkit.dispatchCommand(sender, command)
                return
            }
            if (sender !is Player) {
                Bukkit.dispatchCommand(sender, command)
                return
            }
            if (!sender.hasPermission("coinsapi.pay")) {
                Bukkit.dispatchCommand(sender, command)
                return
            }
            if (CoinsAPI.getCoins(sender.uniqueId.toString()) < amount) {
                Bukkit.dispatchCommand(sender, command)
                return
            }
            CoinsAPI.createPlayer(uuid)
            CoinsAPI.removeCoins(sender.uniqueId.toString(), amount)
            CoinsAPI.addCoins(uuid, amount)
            sender.sendMessage("$prefix ${ConfigPaths.GIVE_COINS_PAY.getString(
                plugin = plugin,
                player = player,
                name = name,
                coins = amount
            )}")
            if (ConfigPaths.SOUNDS.getBoolean(plugin)) {
                sender.playSound(Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f)
            }
            return
        }
        if (!args[0].equals("coins", true)) {
            Bukkit.dispatchCommand(sender, command)
            return
        }
        if (args.size == 2) {
            if (!sender.hasPermission("coinsapi.showcoins")) {
                Bukkit.dispatchCommand(sender, command)
                return
            }
            CoinsAPI.createPlayer(uuid)
            val amount = CoinsAPI.getCoins(uuid)
            sender.sendMessage("$prefix ${ConfigPaths.PLAYER_COINS.getString(
                plugin = plugin,
                player = player,
                name = name,
                coins = amount
            )}")
            return
        }
        if (args.size < 3) {
            Bukkit.dispatchCommand(sender, command)
            return
        }
        val amount = args.getOrNull(3)?.toIntOrNull()
        if (amount != null && amount <= 0) {
            Bukkit.dispatchCommand(sender, command)
            return
        }
        when (args[2].lowercase()) {
            "add" -> {
                if (!sender.hasPermission("coinsapi.editcoins")) {
                    Bukkit.dispatchCommand(sender, command)
                    return
                }
                amount ?: run {
                    Bukkit.dispatchCommand(sender, command)
                    return
                }
                CoinsAPI.createPlayer(uuid)
                CoinsAPI.addCoins(uuid, amount)
                sender.sendMessage("$prefix ${ConfigPaths.ADD_COINS.getString(
                    plugin = plugin,
                    player = player,
                    name = name,
                    coins = amount
                )}")
            }
            "remove" -> {
                if (!sender.hasPermission("coinsapi.editcoins")) {
                    Bukkit.dispatchCommand(sender, command)
                    return
                }
                amount ?: run {
                    Bukkit.dispatchCommand(sender, command)
                    return
                }
                CoinsAPI.createPlayer(uuid)
                CoinsAPI.removeCoins(uuid, amount)
                sender.sendMessage("$prefix ${ConfigPaths.REMOVE_COINS.getString(
                    plugin = plugin,
                    player = player,
                    name = name,
                    coins = amount
                )}")
            }
            "set" -> {
                if (!sender.hasPermission("coinsapi.editcoins")) {
                    Bukkit.dispatchCommand(sender, command)
                    return
                }
                amount ?: run {
                    Bukkit.dispatchCommand(sender, command)
                    return
                }
                CoinsAPI.createPlayer(uuid)
                CoinsAPI.setCoins(uuid, amount)
                sender.sendMessage("$prefix ${ConfigPaths.SET_COINS.getString(
                    plugin = plugin,
                    player = player,
                    name = name,
                    coins = amount
                )}")
            }
            "reset" -> {
                if (!sender.hasPermission("coinsapi.reset")) {
                    Bukkit.dispatchCommand(sender, command)
                }
                CoinsAPI.createPlayer(uuid)
                CoinsAPI.setCoins(uuid, 0)
                sender.sendMessage("$prefix ${ConfigPaths.RESET_COINS.getString(
                    plugin = plugin,
                    player = player,
                    name = name,
                    coins = amount
                )}")
            }
        }
    }
}