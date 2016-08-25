package com.crowsofwar.avatar.common.bending;

import java.util.Random;

import com.crowsofwar.avatar.AvatarLog;
import com.crowsofwar.avatar.common.AvatarAbility;
import com.crowsofwar.avatar.common.controls.AvatarControl;
import com.crowsofwar.avatar.common.data.AvatarPlayerData;
import com.crowsofwar.avatar.common.gui.BendingMenuInfo;
import com.crowsofwar.gorecore.util.GoreCoreNBTInterfaces.CreateFromNBT;
import com.crowsofwar.gorecore.util.GoreCoreNBTInterfaces.ReadableWritable;
import com.crowsofwar.gorecore.util.GoreCoreNBTInterfaces.WriteToNBT;

import net.minecraft.nbt.NBTTagCompound;

/**
 * Base class for bending abilities. All bending classes extend this one. They can save data to NBT
 * if necessary. Functionality for bending should be in subclasses. Bending controllers are
 * singletons, but must be accessed through {@link BendingManager}.
 * <p>
 * For the sake of abstraction, you won't need to refer to bending controllers by their concrete
 * names.
 * <p>
 * Subclasses have access to client input via optionally* implementable hook methods.
 * <p>
 * *Optionally = the subclass must declare the method, but does not need to put any code inside of
 * it.
 *
 */
public interface IBendingController extends ReadableWritable {
	
	public static final CreateFromNBT<IBendingController> creator = new CreateFromNBT<IBendingController>() {
		@Override
		public IBendingController create(NBTTagCompound nbt, Object[] methodsExtraData, Object[] extraData) {
			int id = nbt.getInteger("ControllerID");
			try {
				IBendingController bc = BendingManager.getBending(id);
				return bc;
			} catch (Exception e) {
				AvatarLog.error(
						"Could not find bending controller from ID '" + id + "' - please check NBT data");
				e.printStackTrace();
				return null;
			}
		}
	};
	
	public static final WriteToNBT<IBendingController> writer = new WriteToNBT<IBendingController>() {
		@Override
		public void write(NBTTagCompound nbt, IBendingController object, Object[] methodsExtraData,
				Object[] extraData) {
			nbt.setInteger("ControllerID", object.getID());
		}
	};
	
	/**
	 * RNG available for convenient use.
	 */
	public static final Random random = new Random();
	
	/**
	 * Get an identifier for this bending ability. Should be unique per-class. (not per-instance)
	 */
	int getID();
	
	/**
	 * Hook method that gets called when the player activates the specified ability.
	 */
	default void onAbility(AvatarAbility ability, AvatarPlayerData data) {}
	
	/**
	 * Called to create an IBendingState for the player. This allows the IBendingController to store
	 * specific metadata for each player, making things much easier. <br />
	 * <br />
	 * Keep in mind - when loading a saved state, it will be read from NBT. However, when creating a
	 * new bending state when an ability is activated, it will NOT read from NBT. So ensure that all
	 * values are initialized.
	 * 
	 * @return
	 */
	IBendingState createState(AvatarPlayerData data);
	
	/**
	 * Called when this bending controller is activated on each tick.
	 * 
	 * @param data
	 *            The data for the player. Target block has not been calculated.
	 */
	void onUpdate(AvatarPlayerData data);
	
	/**
	 * Get the ability to be executed for the given client input.
	 * 
	 * @param data
	 *            Player data containing necessary information
	 * @param input
	 *            Input received from client
	 * @return The ability to execute, or null for none.
	 */
	IBendingAbility getAbility(AvatarPlayerData data, AvatarControl input);
	
	/**
	 * Get information about this bending controller's radial menu.
	 */
	BendingMenuInfo getRadialMenu();
	
	/**
	 * Get the name of this bending controller in lowercase. e.g. "earthbending"
	 */
	String getControllerName();
	
}
