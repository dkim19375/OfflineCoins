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

package me.dkim19375.offlinecoins.config

import me.dkim19375.dkimbukkitcore.function.formatAll
import me.dkim19375.offlinecoins.OfflineCoins
import org.bukkit.OfflinePlayer

enum class ConfigPaths(private val path: String) {
    PREFIX("Prefix"),
    PLAYER_COINS("PlayerCoins"),
    ADD_COINS("Editor.AddCoins"),
    REMOVE_COINS("Editor.RemoveCoins"),
    SET_COINS("Editor.SetCoins"),
    RESET_COINS("Editor.ResetCoins"),
    GIVE_COINS_PAY("Pay.CoinsGiven"),
    COMMAND_PAY("Settings.CommandPay"),
    SOUNDS("Settings.Sounds");

    fun getBoolean(plugin: OfflineCoins): Boolean = plugin.messages.config.getBoolean(path)

    fun getString(
        plugin: OfflineCoins,
        player: OfflinePlayer? = null,
        name: String? = null,
        coins: Int? = null,
    ): String {
        return plugin.messages.config.getString(path).formatAll(player).let {
            if (coins != null) {
                it.replace("%coins%", coins.toString())
            } else {
                it
            }
        }.let {
            if (name != null) {
                it.replace("%player%", name)
            } else {
                it
            }
        }
    }
}