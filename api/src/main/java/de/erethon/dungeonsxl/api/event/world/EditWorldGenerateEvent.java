/*
 * Copyright (C) 2014-2021 Daniel Saukel
 *
 * This library is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNULesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package de.erethon.dungeonsxl.api.event.world;

import de.erethon.dungeonsxl.api.world.EditWorld;
import org.bukkit.event.HandlerList;

/**
 * Fired after a dungeon world is generated.
 *
 * @author Daniel Saukel
 */
public class EditWorldGenerateEvent extends EditWorldEvent {

    private static final HandlerList handlers = new HandlerList();

    public EditWorldGenerateEvent(EditWorld editWorld) {
        super(editWorld);
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

}
